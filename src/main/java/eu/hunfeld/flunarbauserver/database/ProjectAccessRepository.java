package eu.hunfeld.flunarbauserver.database;

import eu.hunfeld.flunarbauserver.manager.database.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ProjectAccessRepository implements CacheRepository {
  private final DatabaseManager database;
  private final ProjectRepository projects;
  private final Set<Member> bans = ConcurrentHashMap.newKeySet();
  private final Set<Member> whitelist = ConcurrentHashMap.newKeySet();

  public ProjectAccessRepository(DatabaseManager database, ProjectRepository projects) {
    this.database = database;
    this.projects = projects;
    database.register(this);
  }

  @Override
  public void load(Connection connection) throws Exception {
    Set<Member> loadedBans = ConcurrentHashMap.newKeySet();
    Set<Member> loadedWhitelist = ConcurrentHashMap.newKeySet();
    load(connection, "SELECT world,uuid FROM projekt_bans", loadedBans);
    load(connection, "SELECT world,uuid FROM projekt_whitelist", loadedWhitelist);
    bans.clear();
    bans.addAll(loadedBans);
    whitelist.clear();
    whitelist.addAll(loadedWhitelist);
  }

  private static void load(Connection connection, String sql, Set<Member> target) throws Exception {
    try (Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(sql)) {
      while (result.next()) {
        UUID uuid = Sql.uuid(result.getString("uuid"));
        if (uuid != null) target.add(new Member(result.getString("world"), uuid));
      }
    }
  }

  public boolean isBanned(UUID uuid, String world) {
    return bans.contains(new Member(world, uuid));
  }

  public boolean isWhitelisted(UUID uuid, String world) {
    return whitelist.contains(new Member(world, uuid));
  }

  public Set<UUID> whitelisted(String world) {
    String clean = Sql.cleanWorld(world);
    return whitelist.stream()
        .filter(member -> member.world().equals(clean))
        .map(Member::uuid)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
  }


  public boolean mayEnter(UUID uuid, String world, boolean admin) {
    if (!database.isReady()) return false;
    if (admin) return true;
    String clean = Sql.cleanWorld(world);
    if (isBanned(uuid, clean)) return false;
    return projects
        .byWorld(clean)
        .map(
            project ->
                !project.whitelistActive()
                    || uuid.equals(project.owner())
                    || isWhitelisted(uuid, clean))
        .orElse(true);
  }

  public CompletableFuture<Boolean> setBan(UUID uuid, String worldName, boolean active) {
    return set("projekt_bans", bans, uuid, worldName, active);
  }

  public CompletableFuture<Boolean> setWhitelist(UUID uuid, String worldName, boolean active) {
    return set("projekt_whitelist", whitelist, uuid, worldName, active);
  }

  private CompletableFuture<Boolean> set(
      String table, Set<Member> cache, UUID uuid, String worldName, boolean active) {
    String world = Sql.cleanWorld(worldName);
    Member member = new Member(world, uuid);
    boolean changed = active ? cache.add(member) : cache.remove(member);
    if (!changed) return CompletableFuture.completedFuture(false);
    String sql =
        active
            ? "INSERT IGNORE INTO " + table + " (world,uuid) VALUES (?,?)"
            : "DELETE FROM " + table + " WHERE world=? AND uuid=?";
    return database
        .submit(
            connection -> {
              try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, world);
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
              }
              return true;
            },
            false)
        .thenApply(
            saved -> {
              if (!saved) {
                if (active) cache.remove(member);
                else cache.add(member);
              }
              return saved;
            });
  }

  @Override
  public void clear() {
    bans.clear();
    whitelist.clear();
  }

  private record Member(String world, UUID uuid) {
    private Member {
      world = Sql.cleanWorld(world);
    }
  }
}
