package eu.hunfeld.flunarbauserver.commands.player;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public final class WorkbenchCommand extends BaseCommand {
  public WorkbenchCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    Player p = player(s);
    if (p != null) {
      p.openInventory(MenuType.CRAFTING.create(p));
      context.messages().action(p, "<gray>Werkbank geöffnet.");
    }
    return true;
  }
}
