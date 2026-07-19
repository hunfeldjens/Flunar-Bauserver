package eu.hunfeld.flunarBauserver.commands.admin;

import eu.hunfeld.flunarBauserver.*;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

public final class BroadcastCommand extends BaseCommand {
  public BroadcastCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    if (a.length == 0) {
      context.messages().send(s, "<gray>Benutzung: <green>/broadcast <Nachricht>");
      return true;
    }
    Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(Component.empty()));
    Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(Component.empty()));
    context.messages().broadcast("<gray>" + String.join(" ", a));
    Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(Component.empty()));
    Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(Component.empty()));
    return true;
  }
}
