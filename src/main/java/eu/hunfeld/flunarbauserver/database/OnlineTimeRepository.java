package eu.hunfeld.flunarbauserver.database;

import eu.hunfeld.flunarbauserver.manager.database.DatabaseManager;
import eu.hunfeld.flunarbauserver.model.OnlineTimeRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("SqlNoDataSourceInspection")
public final class OnlineTimeRepository implements CacheRepository {
  private final DatabaseManager database;
  private final Map<UUID, OnlineTimeRecord> times = new ConcurrentHashMap<>();
  private Map<UUID, TimeUpdate> pending = new LinkedHashMap<>();
  private CompletableFuture<Boolean> flushChain = CompletableFuture.completedFuture(true);

  public OnlineTimeRepository(DatabaseManager database) {
    this.database = database;
    database.register(this);
  }

  @Override
  public void load(Connection connection) throws Exception {
    Map<UUID, OnlineTimeRecord> loaded = new ConcurrentHashMap<>();
    String sql =
        "SELECT o.uuid,COALESCE(p.name,'Unbekannt') name,p.first_seen,p.last_seen,"
            + "COALESCE(o.onlinetime_aktiv,0) active_seconds,"
            + "COALESCE(o.onlinetime_afk,0) afk_seconds,COALESCE(o.joins,0) joins "
            + "FROM onlinetime o LEFT JOIN player_data p ON o.uuid=p.uuid";
    try (Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(sql)) {
      while (result.next()) {
        UUID uuid = Sql.uuid(result.getString("uuid"));
        if (uuid != null)
          loaded.put(
              uuid,
              new OnlineTimeRecord(
                  uuid,
                  Sql.text(result, "name"),
                  result.getInt("active_seconds"),
                  result.getInt("afk_seconds"),
                  result.getInt("joins"),
                  Sql.localDateTime(result.getTimestamp("first_seen")),
                  Sql.localDateTime(result.getTimestamp("last_seen"))));
      }
    }
    times.clear();
    times.putAll(loaded);
  }

  public Optional<OnlineTimeRecord> get(UUID uuid) {
    return Optional.ofNullable(times.get(uuid));
  }

  public List<OnlineTimeRecord> ranking() {
    return times.values().stream()
        .sorted(Comparator.comparingInt(OnlineTimeRecord::totalSeconds).reversed())
        .toList();
  }

  public List<OnlineTimeRecord> all() {
    return ranking();
  }

  public void recordJoin(UUID uuid, String name, String ipAddress) {
    database.submit(
        connection -> {
          try (PreparedStatement player =
                  connection.prepareStatement(
                      "INSERT INTO player_data (uuid,name,ip_adresse) VALUES (?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name),ip_adresse=VALUES(ip_adresse),last_seen=CURRENT_TIMESTAMP");
              PreparedStatement online =
                  connection.prepareStatement(
                      "INSERT INTO onlinetime (uuid,joins) VALUES (?,1) ON DUPLICATE KEY UPDATE joins=joins+1")) {
            player.setString(1, uuid.toString());
            player.setString(2, name);
            player.setString(3, ipAddress);
            player.executeUpdate();
            online.setString(1, uuid.toString());
            online.executeUpdate();
          }
          times.compute(
              uuid,
              (_, old) ->
                  new OnlineTimeRecord(
                      uuid,
                      name,
                      old == null ? 0 : old.activeSeconds(),
                      old == null ? 0 : old.afkSeconds(),
                      old == null ? 1 : old.joins() + 1,
                      old == null ? LocalDateTime.now() : old.firstSeen(),
                      LocalDateTime.now()));
          return true;
        },
        false);
  }

  public CompletableFuture<Boolean> add(UUID uuid, String name, int activeSeconds, int afkSeconds) {
    queue(uuid, name, activeSeconds, afkSeconds);
    return flush();
  }


  public synchronized void queue(UUID uuid, String name, int activeSeconds, int afkSeconds) {
    int active = Math.max(0, activeSeconds);
    int afk = Math.max(0, afkSeconds);
    if (active == 0 && afk == 0) return;
    pending.merge(
        uuid,
        new TimeUpdate(uuid, name, active, afk),
        (old, added) ->
            new TimeUpdate(
                uuid,
                added.name(),
                old.activeSeconds() + added.activeSeconds(),
                old.afkSeconds() + added.afkSeconds()));
    times.compute(
        uuid,
        (_, old) ->
            new OnlineTimeRecord(
                uuid,
                name,
                (old == null ? 0 : old.activeSeconds()) + active,
                (old == null ? 0 : old.afkSeconds()) + afk,
                old == null ? 0 : old.joins(),
                old == null ? LocalDateTime.now() : old.firstSeen(),
                LocalDateTime.now()));
  }


  public synchronized CompletableFuture<Boolean> flush() {
    if (pending.isEmpty()) return flushChain;
    Map<UUID, TimeUpdate> snapshot = pending;
    pending = new LinkedHashMap<>();
    flushChain =
        flushChain
            .handle((_, _) -> true)
            .thenCompose(_ -> writeBatch(snapshot))
            .thenApply(
                success -> {
                  if (!success) restore(snapshot);
                  return success;
                });
    return flushChain;
  }

  private CompletableFuture<Boolean> writeBatch(Map<UUID, TimeUpdate> updates) {
    return database.submit(
        connection -> {
          try (PreparedStatement statement =
              connection.prepareStatement(
                  "INSERT INTO onlinetime (uuid,onlinetime_aktiv,onlinetime_afk) VALUES (?,?,?) ON DUPLICATE KEY UPDATE onlinetime_aktiv=onlinetime_aktiv+VALUES(onlinetime_aktiv),onlinetime_afk=onlinetime_afk+VALUES(onlinetime_afk)")) {
            for (TimeUpdate update : updates.values()) {
              statement.setString(1, update.uuid().toString());
              statement.setInt(2, update.activeSeconds());
              statement.setInt(3, update.afkSeconds());
              statement.addBatch();
            }
            statement.executeBatch();
          }
          return true;
        },
        false);
  }

  private synchronized void restore(Map<UUID, TimeUpdate> updates) {
    for (TimeUpdate update : updates.values())
      pending.merge(
          update.uuid(),
          update,
          (current, failed) ->
              new TimeUpdate(
                  current.uuid(),
                  current.name(),
                  current.activeSeconds() + failed.activeSeconds(),
                  current.afkSeconds() + failed.afkSeconds()));
  }

  public synchronized CompletableFuture<Boolean> reset(UUID uuid) {
    TimeUpdate removed = pending.remove(uuid);
    flushChain =
        flushChain.thenCompose(
            previousSuccess -> {
              if (!previousSuccess) return CompletableFuture.completedFuture(false);
              return database.submit(
                  connection -> {
                    try (PreparedStatement statement =
                        connection.prepareStatement(
                            "UPDATE onlinetime SET onlinetime_aktiv=0,onlinetime_afk=0,joins=0 WHERE uuid=?")) {
                      statement.setString(1, uuid.toString());
                      statement.executeUpdate();
                    }
                    times.computeIfPresent(
                        uuid,
                        (_, old) ->
                            new OnlineTimeRecord(
                                uuid, old.name(), 0, 0, 0, old.firstSeen(), old.lastSeen()));
                    return true;
                  },
                  false);
            });
    return preserveRemovedOnFailure(flushChain, removed);
  }

  public synchronized CompletableFuture<Boolean> delete(UUID uuid) {
    TimeUpdate removed = pending.remove(uuid);
    flushChain =
        flushChain.thenCompose(
            previousSuccess -> {
              if (!previousSuccess) return CompletableFuture.completedFuture(false);
              return database.submit(
                  connection -> {
                    try (PreparedStatement online =
                            connection.prepareStatement("DELETE FROM onlinetime WHERE uuid=?");
                        PreparedStatement player =
                            connection.prepareStatement("DELETE FROM player_data WHERE uuid=?")) {
                      online.setString(1, uuid.toString());
                      online.executeUpdate();
                      player.setString(1, uuid.toString());
                      player.executeUpdate();
                    }
                    times.remove(uuid);
                    return true;
                  },
                  false);
            });
    return preserveRemovedOnFailure(flushChain, removed);
  }

  private CompletableFuture<Boolean> preserveRemovedOnFailure(
      CompletableFuture<Boolean> operation, TimeUpdate removed) {
    return operation.thenApply(
        success -> {
          if (!success && removed != null) restore(Map.of(removed.uuid(), removed));
          return success;
        });
  }

  @Override
  public void clear() {
    times.clear();
    synchronized (this) {
      pending.clear();
    }
  }

  private record TimeUpdate(UUID uuid, String name, int activeSeconds, int afkSeconds) {}
}
