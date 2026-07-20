package eu.hunfeld.flunarbauserver.database;

import eu.hunfeld.flunarbauserver.manager.database.DatabaseManager;
import eu.hunfeld.flunarbauserver.model.Project;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ProjectRepository implements CacheRepository {
  private final DatabaseManager database;
  private final Map<String, Project> projects = new ConcurrentHashMap<>();

  public ProjectRepository(DatabaseManager database) {
    this.database = database;
    database.register(this);
  }

  @Override
  public void load(Connection connection) throws Exception {
    Map<String, Project> loaded = new ConcurrentHashMap<>();
    try (Statement statement = connection.createStatement();
        ResultSet result =
            statement.executeQuery(
                "SELECT projekt_name,beschreibung,weltname,owner_uuid,whitelist_active,icon FROM bau_projekte")) {
      while (result.next()) {
        String world = Sql.cleanWorld(result.getString("weltname"));
        loaded.put(
            world,
            new Project(
                result.getString("projekt_name"),
                Sql.text(result, "beschreibung"),
                world,
                Sql.uuid(result.getString("owner_uuid")),
                result.getBoolean("whitelist_active"),
                icon(result.getString("icon"))));
      }
    }
    projects.clear();
    projects.putAll(loaded);
  }

  public List<Project> all() {
    return projects.values().stream()
        .sorted(Comparator.comparing(Project::name, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  public Optional<Project> byWorld(String world) {
    return Optional.ofNullable(projects.get(Sql.cleanWorld(world)));
  }

  public boolean exists(String world) {
    return projects.containsKey(Sql.cleanWorld(world));
  }

  public boolean isOwner(UUID uuid, String world) {
    return byWorld(world).map(Project::owner).map(uuid::equals).orElse(false);
  }

  public CompletableFuture<Boolean> save(Project project) {
    String world = Sql.cleanWorld(project.worldName());
    String sql =
        "INSERT INTO bau_projekte (projekt_name,beschreibung,weltname,owner_uuid,whitelist_active,icon) VALUES (?,?,?,?,?,?) "
            + "ON DUPLICATE KEY UPDATE beschreibung=VALUES(beschreibung),weltname=VALUES(weltname),icon=VALUES(icon)";
    return database.submit(
        connection -> {
          try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, project.name());
            statement.setString(2, project.description());
            statement.setString(3, world);
            statement.setString(4, project.owner() == null ? null : project.owner().toString());
            statement.setBoolean(5, project.whitelistActive());
            statement.setString(6, icon(project.icon()));
            statement.executeUpdate();
          }
          projects.put(
              world,
              new Project(
                  project.name(),
                  project.description(),
                  world,
                  project.owner(),
                  project.whitelistActive(),
                  icon(project.icon())));
          return true;
        },
        false);
  }

  public CompletableFuture<Boolean> delete(String projectName) {
    return database.submit(
        connection -> {
          try (PreparedStatement statement =
              connection.prepareStatement("DELETE FROM bau_projekte WHERE projekt_name=?")) {
            statement.setString(1, projectName);
            statement.executeUpdate();
          }
          projects
              .entrySet()
              .removeIf(entry -> entry.getValue().name().equalsIgnoreCase(projectName));
          return true;
        },
        false);
  }

  public CompletableFuture<Boolean> setIcon(String worldName, String value) {
    String world = Sql.cleanWorld(worldName);
    String icon = icon(value);
    return database.submit(
        connection -> {
          try (PreparedStatement statement =
              connection.prepareStatement(
                  "UPDATE bau_projekte SET icon=? WHERE LOWER(weltname)=LOWER(?)")) {
            statement.setString(1, icon);
            statement.setString(2, world);
            statement.executeUpdate();
          }
          projects.computeIfPresent(world, (key, project) -> project.withIcon(icon));
          return true;
        },
        false);
  }

  public CompletableFuture<Boolean> setWhitelistActive(String worldName, boolean active) {
    String world = Sql.cleanWorld(worldName);
    Project previous = projects.get(world);
    if (previous == null || previous.whitelistActive() == active)
      return CompletableFuture.completedFuture(false);
    Project updated = previous.withWhitelist(active);
    if (!projects.replace(world, previous, updated))
      return CompletableFuture.completedFuture(false);
    return database
        .submit(
            connection -> {
              try (PreparedStatement statement =
                  connection.prepareStatement(
                      "UPDATE bau_projekte SET whitelist_active=? WHERE LOWER(weltname)=LOWER(?)")) {
                statement.setBoolean(1, active);
                statement.setString(2, world);
                statement.executeUpdate();
              }
              return true;
            },
            false)
        .thenApply(
            saved -> {
              if (!saved) projects.replace(world, updated, previous);
              return saved;
            });
  }

  @Override
  public void clear() {
    projects.clear();
  }

  private static String icon(String value) {
    return value == null || value.isBlank() ? "map" : value.toLowerCase(Locale.ROOT);
  }
}
