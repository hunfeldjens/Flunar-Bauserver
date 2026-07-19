package eu.hunfeld.flunarBauserver.commands.teleport;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpaCommand extends BaseCommand {
  public TpaCommand(BauserverContext context) {
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
    if (args.length < 1) {
      context.messages().send(player, "<gray>Benutzung: <green>/tpa <Spieler>");
      return true;
    }
    Player target = Bukkit.getPlayerExact(args[0]);
    if (target == null) {
      context.messages().send(player, "<red>Dieser Spieler ist nicht online.");
      return true;
    }
    if (target == player) {
      context.messages().send(player, "<red>Du kannst dich nicht zu dir teleportieren.");
      return true;
    }
    if (context.backups().safeWorldLocked(target.getWorld().getName())) {
      context
          .messages()
          .send(player, "<red>Safe Backup läuft: TPA in Projekt-/Privatwelten ist gesperrt.");
      return true;
    }
    if (!context.tpa().request(player.getUniqueId(), target.getUniqueId())) {
      context.messages().send(player, "<gray>Dieser Spieler hat bereits eine aktive Anfrage.");
      return true;
    }
    context
        .messages()
        .send(target, "<green>" + player.getName() + " <gray>möchte sich zu dir teleporten.");
    context
        .messages()
        .raw(
            target,
            " <click:run_command:/tpaccept><green>[ANNEHMEN]</green></click> <click:run_command:/tpadeny><red>[ABLEHNEN]</red></click>");
    context
        .messages()
        .send(
            player,
            "<gray>Anfrage an <green>" + target.getName() + " <gray>gesendet. <dark_gray>(30s)");
    Bukkit.getScheduler()
        .runTaskLater(
            context.plugin(),
            () -> {
              if (!context.tpa().expire(player.getUniqueId(), target.getUniqueId())) return;
              if (player.isOnline())
                context
                    .messages()
                    .send(
                        player,
                        "<gray>Anfrage an <green>" + target.getName() + " <gray>abgelaufen.");
              if (target.isOnline())
                context
                    .messages()
                    .send(
                        target,
                        "<gray>Anfrage von <green>" + player.getName() + " <gray>abgelaufen.");
            },
            600L);
    return true;
  }

  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {
    if (args.length != 1) return List.of();
    String input = args[0].toLowerCase(Locale.ROOT);
    return Bukkit.getOnlinePlayers().stream()
        .map(Player::getName)
        .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input))
        .toList();
  }
}
