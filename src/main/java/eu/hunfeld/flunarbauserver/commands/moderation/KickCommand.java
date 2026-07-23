package eu.hunfeld.flunarbauserver.commands.moderation;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import java.util.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;

public final class KickCommand extends BaseCommand {
  public KickCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    Player by = player(s);
    if (by == null || !requireDatabase(by)) return true;
    if (a.length < 2) {
      context.messages().send(by, "<gray>Benutzung: <green>/kick <Spieler> <Grund-ID> [-force]");
      return true;
    }
    Player t = Bukkit.getPlayerExact(a[0]);
    String reason = reason(a[1]);
    if (t == null) {
      context.messages().send(by, "<red>Spieler nicht online.");
      return true;
    }
    if (t.getUniqueId().equals(by.getUniqueId())) {
      context.messages().send(by, "<red>Du kannst dich nicht selbst kicken.");
      return true;
    }
    if (reason == null) {
      context.messages().send(by, "<red>Ungültige Grund-ID. Nutze /reasons.");
      return true;
    }
    if (t.hasPermission("bauserver.admin.bypass")
        && !(a.length > 2
            && a[2].equalsIgnoreCase("-force")
            && by.hasPermission("bauserver.admin"))) {
      context.messages().send(by, "<red>Dieser Spieler besitzt einen Bypass.");
      return true;
    }
    String full = "[" + a[1] + "] " + reason;
    context
        .moderation()
        .recordKick(t.getUniqueId(), t.getName(), by.getUniqueId(), by.getName(), full)
        .whenComplete(
            (ok, _) ->
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> {
                          if (Boolean.TRUE.equals(ok)) {
                            t.getWorld().strikeLightningEffect(t.getLocation());
                            Bukkit.getOnlinePlayers().stream()
                                .filter(player -> player.hasPermission("bauserver.team"))
                                .forEach(
                                    player -> {
                                      context.messages().raw(player, "");
                                      context
                                          .messages()
                                          .send(
                                              player,
                                              "<green>" + t.getName() + " <gray>wurde gekickt.");
                                      context
                                          .messages()
                                          .send(
                                              player,
                                              "<gray>Grund: <white>ID " + a[1] + ": " + reason);
                                      context
                                          .messages()
                                          .send(player, "<gray>Von: <green>" + by.getName());
                                      context.messages().raw(player, "");
                                    });
                            t.kick(
                                context
                                    .messages()
                                    .parse(
                                        "<aqua><bold>Flunar.de</bold> <dark_gray>» <white>Bauserver\n\n<red>Du wurdest gekickt.\n\n<gray>Grund: <white>"
                                            + full
                                            + "\n<gray>Von: <white>"
                                            + by.getName()),
                                PlayerKickEvent.Cause.PLUGIN);
                          }
                        }));
    return true;
  }

  private String reason(String id) {
    try {
      return context.settings().banReasons().get(Integer.parseInt(id));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public List<String> onTabComplete(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    if (a.length == 1) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    if (a.length == 2)
      return context.settings().banReasons().keySet().stream().map(String::valueOf).toList();
    return List.of("-force");
  }
}
