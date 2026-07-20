package eu.hunfeld.flunarbauserver.commands;

import eu.hunfeld.flunarbauserver.FlunarBauserver;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

/** Registers the existing command handlers through Paper's native command API. */
public final class CommandRegistry {
  private final FlunarBauserver plugin;
  private final YamlConfiguration definitions;

  public CommandRegistry(FlunarBauserver plugin) {
    this.plugin = plugin;
    this.definitions = loadDefinitions();
    registerPermissions();
  }

  public void register(String name, BaseCommand handler) {
    ConfigurationSection definition = definitions.getConfigurationSection("commands." + name);
    if (definition == null)
      throw new IllegalStateException("Befehl fehlt in commands.yml: " + name);

    List<String> aliases = definition.getStringList("aliases");
    String description = definition.getString("description", "Flunar-Bauserver-Befehl");
    String permission = definition.getString("permission");
    Command bridge =
        new Command(name, description, "/" + name, aliases) {
          @Override
          public boolean execute(
              @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            return handler.onCommand(sender, this, commandLabel, args);
          }
        };

    BasicCommand paperCommand =
        new BasicCommand() {
          @Override
          public void execute(CommandSourceStack source, String[] args) {
            handler.onCommand(source.getSender(), bridge, name, args);
          }

          @Override
          public Collection<String> suggest(CommandSourceStack source, String[] args) {
            // Paper liefert bei genau einem abschließenden Leerzeichen ein leeres Array.
            // Bukkit-TabCompleter erwarten an dieser Stelle dagegen ein leeres erstes Argument.
            String[] normalizedArgs = args.length == 0 ? new String[] {""} : args;
            List<String> suggestions =
                handler.onTabComplete(source.getSender(), bridge, name, normalizedArgs);
            return suggestions == null ? List.of() : suggestions;
          }

          @Override
          public String permission() {
            return permission == null || permission.isBlank() ? null : permission;
          }
        };
    plugin.registerCommand(name, description, aliases, paperCommand);
  }

  private YamlConfiguration loadDefinitions() {
    InputStream resource = plugin.getResource("commands.yml");
    if (resource == null) throw new IllegalStateException("commands.yml fehlt in der Plugin-JAR");
    try (InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
      return YamlConfiguration.loadConfiguration(reader);
    } catch (Exception exception) {
      throw new IllegalStateException("commands.yml konnte nicht geladen werden", exception);
    }
  }

  private void registerPermissions() {
    ConfigurationSection permissions = definitions.getConfigurationSection("permissions");
    if (permissions == null) return;
    for (String name : permissions.getKeys(false)) {
      if (plugin.getServer().getPluginManager().getPermission(name) != null) continue;
      ConfigurationSection definition = permissions.getConfigurationSection(name);
      String defaultName = definition == null ? "op" : definition.getString("default", "op");
      PermissionDefault defaultValue = PermissionDefault.getByName(defaultName);
      if (defaultValue == null) defaultValue = PermissionDefault.OP;
      String description =
          definition == null ? "" : definition.getString("description", "Flunar-Bauserver");
      plugin
          .getServer()
          .getPluginManager()
          .addPermission(new Permission(name, description, defaultValue));
    }
  }
}
