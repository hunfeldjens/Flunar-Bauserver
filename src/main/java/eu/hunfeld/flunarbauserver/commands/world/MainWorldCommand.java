package eu.hunfeld.flunarbauserver.commands.world;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class MainWorldCommand extends BaseCommand {
  public MainWorldCommand(BauserverContext context) {
    super(context);
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String @NotNull [] args) {
    Player player = player(sender);
    if (player == null) return true;
    context.teleports().remember(player);
    context.worlds().teleport(player, context.worlds().mainWorld().getSpawnLocation());
    context.messages().action(player, "<green>Teleportiert zur Plotwelt");
    return true;
  }
}
