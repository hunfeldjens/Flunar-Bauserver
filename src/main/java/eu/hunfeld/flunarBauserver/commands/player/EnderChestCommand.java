package eu.hunfeld.flunarBauserver.commands.player;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class EnderChestCommand extends BaseCommand {
  public EnderChestCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player p = player(s);
    if (p != null) {
      p.openInventory(p.getEnderChest());
      context.messages().action(p, "<dark_purple>Enderkiste geöffnet.");
    }
    return true;
  }
}
