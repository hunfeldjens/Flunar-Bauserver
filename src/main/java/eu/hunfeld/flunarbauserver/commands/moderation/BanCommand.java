package eu.hunfeld.flunarbauserver.commands.moderation;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import eu.hunfeld.flunarbauserver.utils.Players;
import java.util.*;
import org.bukkit.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;

public final class BanCommand extends BaseCommand {
  public BanCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player by = player(s);
    if (by == null || !requireDatabase(by)) return true;
    if (a.length < 2) {
      context.messages().send(by, "<gray>Benutzung: <green>/ban <Spieler> <Grund-ID>");
      return true;
    }
    OfflinePlayer t = Players.known(a[0]).orElse(null);
    String reason = reason(a[1]);
    if (t == null) {
      context.messages().send(by, "<red>Spieler nicht gefunden.");
      return true;
    }
    if (t.getUniqueId().equals(by.getUniqueId())) {
      context.messages().send(by, "<red>Du kannst dich nicht selbst bannen.");
      return true;
    }
    if (context.moderation().activeBan(t.getUniqueId()).isPresent()) {
      context
          .messages()
          .send(
              by,
              "<yellow>"
                  + (t.getName() == null ? a[0] : t.getName())
                  + " <gray>ist bereits gebannt.");
      return true;
    }
    if (reason == null) {
      context.messages().send(by, "<red>Ungültige Grund-ID. Nutze /reasons.");
      return true;
    }
    if (t.isOnline() && t.getPlayer().hasPermission("bauserver.ban.bypass")) {
      context.messages().send(by, "<red>Dieser Spieler besitzt einen Bypass.");
      return true;
    }
    String full = "[" + a[1] + "] " + reason;
    context
        .moderation()
        .ban(
            t.getUniqueId(),
            t.getName() == null ? a[0] : t.getName(),
            by.getUniqueId(),
            by.getName(),
            full)
        .whenComplete(
            (ok, e) ->
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> {
                          if (!Boolean.TRUE.equals(ok)) {
                            if (context.moderation().activeBan(t.getUniqueId()).isPresent())
                              context
                                  .messages()
                                  .send(
                                      by,
                                      "<yellow>"
                                          + (t.getName() == null ? a[0] : t.getName())
                                          + " <gray>ist bereits gebannt.");
                            else
                              context
                                  .messages()
                                  .send(by, "<red>Der Ban konnte nicht gespeichert werden.");
                            return;
                          }
                          Bukkit.getOnlinePlayers().stream()
                              .filter(player -> player.hasPermission("bauserver.team"))
                              .forEach(
                                  player -> {
                                    context.messages().raw(player, "");
                                    context
                                        .messages()
                                        .send(player, "<green>" + a[0] + " <gray>wurde gebannt.");
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
                          if (t.isOnline()) {
                            Player target = t.getPlayer();
                            target.getWorld().strikeLightningEffect(target.getLocation());
                            target.kick(
                                context
                                    .messages()
                                    .parse(
                                        "<aqua><bold>Flunar.de</bold> <dark_gray>» <white>Bauserver\n\n<red>Du wurdest permanent ausgeschlossen.\n\n<gray>Grund: <white>"
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
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    if (a.length == 1) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    if (a.length == 2)
      return context.settings().banReasons().keySet().stream().map(String::valueOf).toList();
    return List.of();
  }
}
