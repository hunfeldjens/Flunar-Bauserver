package eu.hunfeld.flunarbauserver.commands.world;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


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
    context.messages().actionAll(message);
    return true;
  }
}
