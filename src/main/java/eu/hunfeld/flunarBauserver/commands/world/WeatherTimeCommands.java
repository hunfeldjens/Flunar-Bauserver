package eu.hunfeld.flunarBauserver.commands.world;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Groups the five tiny world-state commands without mixing them into larger command routers. */
public final class WeatherTimeCommands extends BaseCommand {
  public WeatherTimeCommands(BauserverContext context) {
    super(context);
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    Player player = player(sender);
    if (player == null) return true;
    String message;
    switch (command.getName()) {
      case "sun" -> {
        player.getWorld().setStorm(false);
        player.getWorld().setThundering(false);
        message = "<gray>Wetter → <green>Sonnig";
      }
      case "rain" -> {
        player.getWorld().setStorm(true);
        player.getWorld().setThundering(false);
        message = "<gray>Wetter → <green>Regnerisch";
      }
      case "storm" -> {
        player.getWorld().setStorm(true);
        player.getWorld().setThundering(true);
        message = "<gray>Wetter → <green>Stürmisch";
      }
      case "tag" -> {
        player.getWorld().setTime(1_000L);
        message = "<gray>Zeit → <green>Morgen";
      }
      case "nacht" -> {
        player.getWorld().setTime(13_000L);
        message = "<gray>Zeit → <green>Nacht";
      }
      default ->
          throw new IllegalStateException("Nicht unterstützter Weltbefehl: " + command.getName());
    }
    // Wetter- und Zeitänderungen gehören ausschließlich in die Actionbar.
    context.messages().actionAll(message);
    return true;
  }
}
