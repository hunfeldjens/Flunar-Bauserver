package eu.hunfeld.flunarBauserver.database;

import eu.hunfeld.flunarBauserver.manager.database.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class PrivateWorldRepository implements CacheRepository {
  private final DatabaseManager database;
  private final Map<UUID, String> worlds = new ConcurrentHashMap<>();

  public PrivateWorldRepository(DatabaseManager database) {
    this.database = database;
    database.register(this);
  }

  @Override
  public void load(Connection connection) throws Exception {
    Map<UUID, String> loaded = new ConcurrentHashMap<>();
    try (Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT uuid,weltname FROM privat_worlds")) {
      while (result.next()) {
        UUID uuid = Sql.uuid(result.getString("uuid"));
        if (uuid != null) loaded.put(uuid, Sql.cleanWorld(result.getString("weltname")));
      }
    }
    worlds.clear();
    worlds.putAll(loaded);
  }

  public Optional<String> get(UUID uuid) {
    return Optional.ofNullable(worlds.get(uuid));
  }

  public CompletableFuture<Boolean> set(UUID uuid, String worldName) {
    String world = Sql.cleanWorld(worldName);
    return database.submit(
        connection -> {
          try (PreparedStatement statement =
              connection.prepareStatement(
                  "INSERT INTO privat_worlds (uuid,weltname) VALUES (?,?) ON DUPLICATE KEY UPDATE weltname=VALUES(weltname)")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, world);
            statement.executeUpdate();
          }
          worlds.put(uuid, world);
          return true;
        },
        false);
  }

  @Override
  public void clear() {
    worlds.clear();
  }
}
