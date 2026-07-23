package eu.hunfeld.flunarbauserver.commands.teleport;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpaDenyCommand extends BaseCommand {
  public TpaDenyCommand(BauserverContext context) {
    super(context);
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String @NotNull [] args) {
    Player target = player(sender);
    if (target == null) return true;
    var requester = context.tpa().consume(target.getUniqueId());
    if (requester.isEmpty()) {
      context.messages().send(target, "<red>Du hast keine offene Teleportanfrage.");
      return true;
    }
    Player player = Bukkit.getPlayer(requester.get());
    if (player != null)
      context
          .messages()
          .send(player, "<gray>Anfrage an <green>" + target.getName() + " <red>abgelehnt<gray>.");
    context
        .messages()
        .send(
            target,
            "<gray>Anfrage von <green>"
                + (player == null ? "Unbekannt" : player.getName())
                + " <red>abgelehnt<gray>.");
    return true;
  }
}
