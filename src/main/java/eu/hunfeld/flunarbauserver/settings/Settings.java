package eu.hunfeld.flunarbauserver.settings;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public record Settings(
    String title,
    String prefix,
    String mainWorld,
    String accessPermission,
    TabList tabList,
    Spawn spawn,
    Paths paths,
    OnlineTime onlineTime,
    Messages messages,
    Map<String, String> templates,
    Map<Integer, String> banReasons) {
  public static Settings from(FileConfiguration config) {
    Spawn spawn =
        new Spawn(
            config.getDouble("spawn.x", -49.5),
            config.getDouble("spawn.y", 84),
            config.getDouble("spawn.z", -49.5),
            (float) config.getDouble("spawn.yaw", 0),
            (float) config.getDouble("spawn.pitch", 0));
    Paths paths =
        new Paths(
            Path.of(config.getString("paths.server-root", "/home/netzwerk/Bauserver")),
            Path.of(config.getString("paths.project-worlds", "world/dimensions/projekt")),
            Path.of(config.getString("paths.private-worlds", "world/dimensions/privat")),
            Path.of(
                config.getString(
                    "paths.backup-script", "/home/netzwerk/Bauserver/Shell/backup.sh")),
            Path.of(
                config.getString(
                    "paths.export-script", "/home/netzwerk/Bauserver/Shell/worldexport.sh")));
    OnlineTime online =
        new OnlineTime(
            config.getInt("online-time.afk-warning-seconds", 30),
            config.getInt("online-time.afk-kick-seconds", 7_200),
            config.getInt("online-time.active-reset-seconds", 30),
            config.getInt("online-time.tick-seconds", 10),
            config.getInt("online-time.database-save-seconds", 60),
            config.getDouble("online-time.movement-threshold", 3.5));
    Messages messages =
        new Messages(
            config.getString(
                "messages.player-not-online", "<red>Dieser Spieler ist nicht online."));
    TabList tabList =
        new TabList(
            config.getString(
                "server.tab-header",
                "\n<dark_gray><strikethrough>        </strikethrough> <bold><gradient:#1FADFF:#ABE0FF>Flunar.de</gradient></bold> <dark_gray><strikethrough>        </strikethrough>\n<white>Bauserver <dark_gray>• <gray>Online: <aqua>{online}<dark_gray>/<aqua>{max_players}\n"),
            config.getString(
                "server.tab-footer",
                "\n<dark_gray>Projektbau <white>• <aqua>Kreativität <white>• <gray>Teamwork\n<gray>Discord: <aqua>dc.flunar.de\n"));
    Map<String, String> templates = new LinkedHashMap<>();
    ConfigurationSection templateSection = config.getConfigurationSection("templates");
    if (templateSection != null) {
      for (String key : templateSection.getKeys(false)) {
        String value = templateSection.getString(key);
        if (value != null && !value.isBlank()) templates.put(key.toLowerCase(), value);
      }
    }
    Map<Integer, String> reasons = new LinkedHashMap<>();
    ConfigurationSection reasonSection = config.getConfigurationSection("ban-reasons");
    if (reasonSection != null) {
      for (String key : reasonSection.getKeys(false)) {
        try {
          reasons.put(Integer.parseInt(key), reasonSection.getString(key, ""));
        } catch (NumberFormatException ignored) {
        }
      }
    }
    return new Settings(
        config.getString("server.title", "<bold>Flunar</bold>"),
        config.getString("server.prefix", "<bold>Flunar</bold> <white>»</white>"),
        config.getString("server.main-world", "world"),
        config.getString("server.access-permission", "bauserver.access"),
        tabList,
        spawn,
        paths,
        online,
        messages,
        Map.copyOf(templates),
        Map.copyOf(reasons));
  }

  public record Spawn(double x, double y, double z, float yaw, float pitch) {
    public Location location(World world) {
      return new Location(world, x, y, z, yaw, pitch);
    }
  }

  public record Paths(
      Path serverRoot,
      Path projectWorlds,
      Path privateWorlds,
      Path backupScript,
      Path exportScript) {}

  public record OnlineTime(
      int afkWarningSeconds,
      int afkKickSeconds,
      int activeResetSeconds,
      int tickSeconds,
      int databaseSaveSeconds,
      double movementThreshold) {}

  public record Messages(String playerNotOnline) {}

  public record TabList(String header, String footer) {}
}
