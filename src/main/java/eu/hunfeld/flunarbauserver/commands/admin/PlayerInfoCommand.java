package eu.hunfeld.flunarbauserver.commands.admin;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PlayerInfoCommand extends BaseCommand {
  public PlayerInfoCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player t = a.length > 0 ? Bukkit.getPlayerExact(a[0]) : s instanceof Player p ? p : null;
    if (t == null) {
      context.messages().send(s, "<red>Spieler nicht online.");
      return true;
    }
    context
        .messages()
        .raw(
            s,
            "<dark_gray><strikethrough>                                        </strikethrough>");
    context.messages().send(s, "<gray>Name: <aqua>" + t.getName());
    context.messages().send(s, "<gray>UUID: <yellow>" + t.getUniqueId());
    String ip = t.getAddress() == null ? "-" : t.getAddress().getAddress().getHostAddress();
    context.messages().send(s, "<gray>IP: <yellow>" + ip);
    context.messages().send(s, "<gray>Ping: <yellow>" + t.getPing() + "<gray>ms");
    String brand = t.getClientBrandName();
    context
        .messages()
        .send(
            s, "<gray>Client: <yellow>" + (brand == null || brand.isBlank() ? "Unbekannt" : brand));
    context.messages().send(s, "<gray>Protocol: <yellow>" + t.getProtocolVersion());
    context.messages().send(s, "<gray>Welt: <yellow>" + t.getWorld().getName());
    context
        .messages()
        .raw(
            s,
            "<dark_gray><strikethrough>                                        </strikethrough>");
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
