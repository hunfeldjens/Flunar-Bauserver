package eu.hunfeld.flunarbauserver.settings;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigFiles {
  private ConfigFiles() {}

  public static Loaded load(JavaPlugin plugin) {
    return new Loaded(
        Settings.from(loadAndMerge(plugin, "settings.yml")),
        DatabaseSettings.from(loadAndMerge(plugin, "database.yml")),
        LabyModSettings.from(loadAndMerge(plugin, "labymod.yml")));
  }

  private static void saveIfMissing(JavaPlugin plugin, String name) {
    File target = new File(plugin.getDataFolder(), name);
    if (!target.exists()) plugin.saveResource(name, false);
  }


  private static YamlConfiguration loadAndMerge(JavaPlugin plugin, String name) {
    saveIfMissing(plugin, name);
    File target = new File(plugin.getDataFolder(), name);
    YamlConfiguration loaded = YamlConfiguration.loadConfiguration(target);
    try (InputStream resource = plugin.getResource(name)) {
      if (resource == null) return loaded;
      YamlConfiguration defaults =
          YamlConfiguration.loadConfiguration(
              new InputStreamReader(resource, StandardCharsets.UTF_8));
      loaded.setDefaults(defaults);
      loaded.options().copyDefaults(true);
      migrateKnownDefaults(name, loaded);
      loaded.save(target);
    } catch (Exception exception) {
      plugin
          .getLogger()
          .warning(
              name
                  + " konnte nicht um neue Standardwerte ergänzt werden: "
                  + exception.getMessage());
    }
    return loaded;
  }

  private static void migrateKnownDefaults(String name, YamlConfiguration config) {
    if (!name.equals("labymod.yml")) return;
    String path = "features.playing-game-mode.text";
    if (config.getString(path, "").equals("Bauserver - {world}"))
      config.set(path, "Flunar.de Bauserver {project}");
  }

  public record Loaded(Settings settings, DatabaseSettings database, LabyModSettings labyMod) {}
}
