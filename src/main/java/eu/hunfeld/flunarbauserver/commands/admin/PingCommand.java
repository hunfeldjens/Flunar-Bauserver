package eu.hunfeld.flunarbauserver.commands.admin;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PingCommand extends BaseCommand {
  public PingCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player t = a.length > 0 ? Bukkit.getPlayerExact(a[0]) : player(s);
    if (t == null) {
      context.messages().send(s, "<red>Spieler nicht online.");
      return true;
    }
    if (a.length == 0)
      context.messages().send(s, "<gray>Dein Ping: <green>" + t.getPing() + "<gray>ms");
    else
      context
          .messages()
          .send(
              s,
              "<gray>Ping von <yellow>"
                  + t.getName()
                  + "<gray>: <green>"
                  + t.getPing()
                  + "<gray>ms");
    return true;
  }

  @Override
  public java.util.List<String> onTabComplete(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    return a.length == 1
        ? Bukkit.getOnlinePlayers().stream().map(Player::getName).toList()
        : java.util.List.of();
  }
}
