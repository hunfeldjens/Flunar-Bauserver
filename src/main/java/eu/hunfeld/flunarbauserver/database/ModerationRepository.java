package eu.hunfeld.flunarbauserver.database;

import eu.hunfeld.flunarbauserver.manager.database.DatabaseManager;
import eu.hunfeld.flunarbauserver.model.ActiveBan;
import eu.hunfeld.flunarbauserver.model.ModerationHistoryPage;
import eu.hunfeld.flunarbauserver.model.ModerationRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("SqlNoDataSourceInspection")
public final class ModerationRepository implements CacheRepository {
  private final DatabaseManager database;
  private final Map<UUID, ActiveBan> activeBans = new ConcurrentHashMap<>();

  public ModerationRepository(DatabaseManager database) {
    this.database = database;
    database.register(this);
  }

  @Override
  public void load(Connection connection) throws Exception {
    Map<UUID, ActiveBan> loaded = new ConcurrentHashMap<>();
    try (Statement statement = connection.createStatement();
        ResultSet result =
            statement.executeQuery(
                "SELECT uuid,reason,by_name FROM server_bans WHERE active=1 ORDER BY id")) {
      while (result.next()) {
        UUID uuid = Sql.uuid(result.getString("uuid"));
        if (uuid != null)
          loaded.put(
              uuid, new ActiveBan(uuid, Sql.text(result, "reason"), Sql.text(result, "by_name")));
      }
    }
    activeBans.clear();
    activeBans.putAll(loaded);
  }

  public Optional<ActiveBan> activeBan(UUID uuid) {
    return Optional.ofNullable(activeBans.get(uuid));
  }

  public CompletableFuture<ModerationHistoryPage> banHistory(int page, int pageSize) {
    return history(true, page, pageSize);
  }

  public CompletableFuture<ModerationHistoryPage> kickHistory(int page, int pageSize) {
    return history(false, page, pageSize);
  }


  private CompletableFuture<ModerationHistoryPage> history(
      boolean bans, int requestedPage, int requestedPageSize) {
    int pageSize = Math.clamp(requestedPageSize, 1, 45);
    String table = bans ? "server_bans" : "server_kicks";
    String activeColumn = bans ? "active" : "0 AS active";
    return database.submit(
        connection -> {
          long total;
          try (Statement statement = connection.createStatement();
              ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            result.next();
            total = result.getLong(1);
          }
          int pages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
          int page = Math.clamp(requestedPage, 1, pages);
          int offset = (page - 1) * pageSize;
          List<ModerationRecord> entries = new ArrayList<>();
          try (PreparedStatement statement =
              connection.prepareStatement(
                  "SELECT id,uuid,name,by_name,reason,created_at,"
                      + activeColumn
                      + " FROM "
                      + table
                      + " ORDER BY id DESC LIMIT ? OFFSET ?")) {
            statement.setInt(1, pageSize);
            statement.setInt(2, offset);
            try (ResultSet result = statement.executeQuery()) {
              while (result.next()) {
                Timestamp created = result.getTimestamp("created_at");
                LocalDateTime createdAt = created == null ? null : created.toLocalDateTime();
                entries.add(
                    new ModerationRecord(
                        result.getLong("id"),
                        Sql.uuid(result.getString("uuid")),
                        value(result.getString("name"), "Unbekannt"),
                        value(result.getString("by_name"), "Konsole"),
                        value(result.getString("reason"), "Kein Grund angegeben"),
                        createdAt,
                        result.getBoolean("active")));
              }
            }
          }
          return new ModerationHistoryPage(page, pages, total, entries);
        },
        null);
  }

  public CompletableFuture<Boolean> recordKick(
      UUID uuid, String name, UUID byUuid, String byName, String reason) {
    return database.submit(
        connection -> {
          try (PreparedStatement statement =
              connection.prepareStatement(
                  "INSERT INTO server_kicks (uuid,name,by_uuid,by_name,reason) VALUES (?,?,?,?,?)")) {
            bindPunishment(statement, uuid, name, byUuid, byName, reason);
            statement.executeUpdate();
          }
          return true;
        },
        false);
  }

  public CompletableFuture<Boolean> ban(
      UUID uuid, String name, UUID byUuid, String byName, String reason) {
    ActiveBan ban = new ActiveBan(uuid, reason, byName);
    if (activeBans.putIfAbsent(uuid, ban) != null) return CompletableFuture.completedFuture(false);
    return database
        .submit(
            connection -> {
              try (PreparedStatement statement =
                  connection.prepareStatement(
                      "INSERT INTO server_bans (uuid,name,by_uuid,by_name,reason,active) VALUES (?,?,?,?,?,1)")) {
                bindPunishment(statement, uuid, name, byUuid, byName, reason);
                statement.executeUpdate();
              }
              return true;
            },
            false)
        .thenApply(
            saved -> {
              if (!saved) activeBans.remove(uuid, ban);
              return saved;
            });
  }

  public CompletableFuture<Boolean> unban(UUID uuid) {
    ActiveBan removed = activeBans.remove(uuid);
    if (removed == null) return CompletableFuture.completedFuture(false);
    return database
        .submit(
            connection -> {
              try (PreparedStatement statement =
                  connection.prepareStatement(
                      "UPDATE server_bans SET active=0 WHERE uuid=? AND active=1")) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
              }
              return true;
            },
            false)
        .thenApply(
            saved -> {
              if (!saved) activeBans.putIfAbsent(uuid, removed);
              return saved;
            });
  }

  private static void bindPunishment(
      PreparedStatement statement,
      UUID uuid,
      String name,
      UUID byUuid,
      String byName,
      String reason)
      throws Exception {
    statement.setString(1, uuid.toString());
    statement.setString(2, name);
    statement.setString(3, byUuid == null ? null : byUuid.toString());
    statement.setString(4, byName);
    statement.setString(5, reason);
  }

  private static String value(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }

  @Override
  public void clear() {
    activeBans.clear();
  }
}
