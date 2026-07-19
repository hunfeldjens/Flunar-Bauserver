package eu.hunfeld.flunarBauserver.database;

import eu.hunfeld.flunarBauserver.manager.database.DatabaseManager;
import eu.hunfeld.flunarBauserver.model.ProjectInfo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ProjectInfoRepository implements CacheRepository {
  private final DatabaseManager database;
  private final Map<Integer, ProjectInfo> infos = new ConcurrentHashMap<>();

  public ProjectInfoRepository(DatabaseManager database) {
    this.database = database;
    database.register(this);
  }

  @Override
  public void load(Connection connection) throws Exception {
    Map<Integer, ProjectInfo> loaded = new ConcurrentHashMap<>();
    try (Statement statement = connection.createStatement();
        ResultSet result =
            statement.executeQuery(
                "SELECT id,world,name,beschreibung,created_by,x,y,z,yaw,pitch FROM projekt_infos")) {
      while (result.next()) {
        int id = result.getInt("id");
        loaded.put(
            id,
            new ProjectInfo(
                id,
                Sql.cleanWorld(result.getString("world")),
                result.getString("name"),
                Sql.text(result, "beschreibung"),
                Sql.uuid(result.getString("created_by")),
                Sql.nullableDouble(result, "x"),
                Sql.nullableDouble(result, "y"),
                Sql.nullableDouble(result, "z"),
                Sql.nullableFloat(result, "yaw"),
                Sql.nullableFloat(result, "pitch")));
      }
    }
    infos.clear();
    infos.putAll(loaded);
  }

  public List<ProjectInfo> forWorld(String worldName) {
    String world = Sql.cleanWorld(worldName);
    return infos.values().stream()
        .filter(info -> world.isBlank() || info.worldName().equals(world))
        .sorted(Comparator.comparing(ProjectInfo::worldName).thenComparingInt(ProjectInfo::id))
        .toList();
  }

  public CompletableFuture<Boolean> add(ProjectInfo info) {
    String sql =
        "INSERT INTO projekt_infos (world,name,beschreibung,created_by,x,y,z,yaw,pitch) VALUES (?,?,?,?,?,?,?,?,?)";
    return database.submit(
        connection -> {
          try (PreparedStatement statement =
              connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, Sql.cleanWorld(info.worldName()));
            statement.setString(2, info.name());
            statement.setString(3, info.description());
            statement.setString(4, info.createdBy().toString());
            Sql.nullableNumber(statement, 5, info.x());
            Sql.nullableNumber(statement, 6, info.y());
            Sql.nullableNumber(statement, 7, info.z());
            Sql.nullableNumber(statement, 8, info.yaw());
            Sql.nullableNumber(statement, 9, info.pitch());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
              if (keys.next()) {
                int id = keys.getInt(1);
                infos.put(
                    id,
                    new ProjectInfo(
                        id,
                        Sql.cleanWorld(info.worldName()),
                        info.name(),
                        info.description(),
                        info.createdBy(),
                        info.x(),
                        info.y(),
                        info.z(),
                        info.yaw(),
                        info.pitch()));
              }
            }
          }
          return true;
        },
        false);
  }

  public CompletableFuture<Boolean> delete(int id) {
    return database.submit(
        connection -> {
          try (PreparedStatement statement =
              connection.prepareStatement("DELETE FROM projekt_infos WHERE id=?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
          }
          infos.remove(id);
          return true;
        },
        false);
  }

  @Override
  public void clear() {
    infos.clear();
  }
}
