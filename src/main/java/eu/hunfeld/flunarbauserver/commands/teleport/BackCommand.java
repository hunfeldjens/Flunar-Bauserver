package eu.hunfeld.flunarbauserver.commands.teleport;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BackCommand extends BaseCommand {
  public BackCommand(BauserverContext context) {
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
    var target = context.teleports().backLocation(player);
    if (target.isEmpty()) {
      context.messages().send(player, "<red>Kein Ort gespeichert.");
      return true;
    }
    if (context.backups().safeWorldLocked(target.get().getWorld().getName())) {
      context.messages().send(player, "<red>Diese Welt ist während des Backups gesperrt.");
      return true;
    }
    var current = player.getLocation().clone();
    context
        .worlds()
        .teleport(player, target.get())
        .thenAccept(
            ok -> {
              if (ok) {
                context.teleports().remember(player, current);
                context.messages().action(player, "<gray>Zurück teleportiert <green>✔");
              }
            });
    return true;
  }
}
