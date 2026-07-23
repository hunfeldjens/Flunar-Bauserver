package eu.hunfeld.flunarbauserver.database;

import eu.hunfeld.flunarbauserver.manager.database.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("SqlNoDataSourceInspection")
public final class AutoloadRepository implements CacheRepository {
  private final DatabaseManager database;
  private final Set<String> worlds = ConcurrentHashMap.newKeySet();

  public AutoloadRepository(DatabaseManager database) {
    this.database = database;
    database.register(this);
  }

  @Override
  public void load(Connection connection) throws Exception {
    Set<String> loaded = ConcurrentHashMap.newKeySet();
    try (Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT world FROM projekt_autoload")) {
      while (result.next()) loaded.add(Sql.cleanWorld(result.getString(1)));
    }
    worlds.clear();
    worlds.addAll(loaded);
  }

  public Set<String> all() {
    return Set.copyOf(worlds);
  }

  public boolean contains(String world) {
    return worlds.contains(Sql.cleanWorld(world));
  }

  public CompletableFuture<Boolean> set(String worldName, boolean active) {
    String world = Sql.cleanWorld(worldName);
    boolean changed = active ? worlds.add(world) : worlds.remove(world);
    if (!changed) return CompletableFuture.completedFuture(false);
    String sql =
        active
            ? "INSERT IGNORE INTO projekt_autoload (world) VALUES (?)"
            : "DELETE FROM projekt_autoload WHERE LOWER(world)=LOWER(?)";
    return database
        .submit(
            connection -> {
              try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, world);
                statement.executeUpdate();
              }
              return true;
            },
            false)
        .thenApply(
            saved -> {
              if (!saved) {
                if (active) worlds.remove(world);
                else worlds.add(world);
              }
              return saved;
            });
  }

  @Override
  public void clear() {
    worlds.clear();
  }
}
