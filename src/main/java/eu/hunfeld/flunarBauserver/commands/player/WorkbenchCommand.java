package eu.hunfeld.flunarBauserver.commands.player;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class WorkbenchCommand extends BaseCommand {
  public WorkbenchCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player p = player(s);
    if (p != null) {
      p.openWorkbench(null, true);
      context.messages().action(p, "<gray>Werkbank geöffnet.");
    }
    return true;
  }
}
