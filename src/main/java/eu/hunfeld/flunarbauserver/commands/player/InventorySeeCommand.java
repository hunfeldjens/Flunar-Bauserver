package eu.hunfeld.flunarbauserver.commands.player;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class InventorySeeCommand extends BaseCommand {
  public InventorySeeCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    Player p = player(s);
    if (p == null) return true;
    if (a.length == 0) {
      context.messages().send(p, "<red>Bitte gib einen Spieler an.");
      return true;
    }
    Player t = Bukkit.getPlayerExact(a[0]);
    if (t == null) context.messages().send(p, "<red>Spieler nicht online.");
    else if (t == p)
      context.messages().send(p, "<red>Du kannst dein eigenes Inventar nicht öffnen.");
    else {
      p.openInventory(t.getInventory());
      context.messages().action(p, "<gray>Inventar von <yellow>" + t.getName());
    }
    return true;
  }

  @Override
  public java.util.List<String> onTabComplete(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    return a.length == 1
        ? Bukkit.getOnlinePlayers().stream().map(Player::getName).toList()
        : java.util.List.of();
  }
}
