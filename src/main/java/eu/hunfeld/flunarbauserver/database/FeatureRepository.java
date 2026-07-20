package eu.hunfeld.flunarbauserver.database;

import eu.hunfeld.flunarbauserver.manager.database.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class FeatureRepository implements CacheRepository {
  public static final List<String> NAMES =
      List.of(
          "damage",
          "break",
          "place",
          "inventory",
          "pickup",
          "hunger",
          "weather",
          "craft",
          "drop",
          "farmland",
          "explosion",
          "blockbreak",
          "blockplace",
          "blockdamage",
          "mobspawn",
          "summon",
          "operator",
          "firespread",
          "leafdecay",
          "liquidflow",
          "itemdamage",
          "portal",
          "gravity",
          "falldamage",
          "daynight",
          "commandblocks",
          "mobgriefing",
          "keepinventory",
          "locatorbar",
          "advancements");
  private static final Set<String> DEFAULT_ENABLED =
      Set.of(
          "blockbreak",
          "blockdamage",
          "blockplace",
          "break",
          "craft",
          "drop",
          "inventory",
          "liquidflow",
          "mobspawn",
          "pickup",
          "place",
          "keepinventory");

  private final DatabaseManager database;
  private final Map<String, Boolean> features = new ConcurrentHashMap<>();

  public FeatureRepository(DatabaseManager database) {
    this.database = database;
    defaults();
    database.register(this);
  }

  @Override
  public void load(Connection connection) throws Exception {
    Map<String, Boolean> loaded = new ConcurrentHashMap<>();
    NAMES.forEach(name -> loaded.put(name, DEFAULT_ENABLED.contains(name)));
    try (Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT feature,aktiv FROM server_features")) {
      while (result.next())
        loaded.put(result.getString(1).toLowerCase(Locale.ROOT), result.getBoolean(2));
    }
    features.clear();
    features.putAll(loaded);
  }

  public boolean enabled(String name) {
    return features.getOrDefault(name.toLowerCase(Locale.ROOT), false);
  }

  public static boolean defaultEnabled(String name) {
    return DEFAULT_ENABLED.contains(name.toLowerCase(Locale.ROOT));
  }

  public Map<String, Boolean> all() {
    return Map.copyOf(features);
  }

  public CompletableFuture<Boolean> set(String featureName, boolean active) {
    String feature = featureName.toLowerCase(Locale.ROOT);
    if (!NAMES.contains(feature)) return CompletableFuture.completedFuture(false);
    return database.submit(
        connection -> {
          try (PreparedStatement statement =
              connection.prepareStatement(
                  "INSERT INTO server_features (feature,aktiv) VALUES (?,?) ON DUPLICATE KEY UPDATE aktiv=VALUES(aktiv)")) {
            statement.setString(1, feature);
            statement.setBoolean(2, active);
            statement.executeUpdate();
          }
          features.put(feature, active);
          return true;
        },
        false);
  }

  public CompletableFuture<Boolean> setAll(Map<String, Boolean> values) {
    Map<String, Boolean> requested = new java.util.LinkedHashMap<>();
    NAMES.forEach(name -> requested.put(name, Boolean.TRUE.equals(values.get(name))));
    return database.submit(
        connection -> {
          boolean previousAutoCommit = connection.getAutoCommit();
          connection.setAutoCommit(false);
          try (PreparedStatement statement =
              connection.prepareStatement(
                  "INSERT INTO server_features (feature,aktiv) VALUES (?,?) ON DUPLICATE KEY UPDATE aktiv=VALUES(aktiv)")) {
            for (var entry : requested.entrySet()) {
              statement.setString(1, entry.getKey());
              statement.setBoolean(2, entry.getValue());
              statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
            features.clear();
            features.putAll(requested);
            return true;
          } catch (Exception exception) {
            connection.rollback();
            throw exception;
          } finally {
            connection.setAutoCommit(previousAutoCommit);
          }
        },
        false);
  }

  @Override
  public void clear() {
    features.clear();
    defaults();
  }

  private void defaults() {
    NAMES.forEach(name -> features.put(name, DEFAULT_ENABLED.contains(name)));
  }
}
