package eu.hunfeld.flunarbauserver.settings;

import org.bukkit.configuration.file.FileConfiguration;

public record DatabaseSettings(
    String jdbcUrl, String username, String password, int poolSize, long connectionTimeoutMs) {
  public static DatabaseSettings from(FileConfiguration config) {
    return new DatabaseSettings(
        normaliseJdbcUrl(
            config.getString(
                "database.jdbc-url", "jdbc:mariadb://127.0.0.1:3306/flunar_bauserver")),
        config.getString("database.username", "fl-bauserver"),
        config.getString("database.password", ""),
        Math.max(2, config.getInt("database.pool-size", 4)),
        Math.max(2_500L, config.getLong("database.connection-timeout-ms", 5_000L)));
  }

  public boolean configured() {
    return !password.isBlank() && !password.equals("CHANGE_ME");
  }

  public String safeTarget() {
    int parameters = jdbcUrl.indexOf('?');
    return parameters < 0 ? jdbcUrl : jdbcUrl.substring(0, parameters);
  }

  private static String normaliseJdbcUrl(String jdbcUrl) {
    String value = jdbcUrl == null ? "" : jdbcUrl.strip();
    if (value.startsWith("jdbc:mysql://"))
      return "jdbc:mariadb://" + value.substring("jdbc:mysql://".length());
    return value;
  }
}
