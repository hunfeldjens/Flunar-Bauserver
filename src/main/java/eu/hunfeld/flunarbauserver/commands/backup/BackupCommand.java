package eu.hunfeld.flunarbauserver.commands.backup;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import eu.hunfeld.flunarbauserver.gui.BackupMenu;
import eu.hunfeld.flunarbauserver.service.BackupCoordinator;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BackupCommand extends BaseCommand {
  private final BackupCoordinator coordinator;
  private final BackupMenu menu;

  public BackupCommand(BauserverContext c, BackupCoordinator b, BackupMenu m) {
    super(c);
    coordinator = b;
    menu = m;
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    if (a.length == 0) {
      Player p = player(s);
      if (p != null) menu.open(p);
      return true;
    }
    switch (a[0].toLowerCase()) {
      case "safe", "-safe" -> coordinator.start(s, true);
      case "unsafe", "-unsafe", "-force" -> coordinator.start(s, false);
      case "cancel", "-cancel", "-stop" -> coordinator.cancel(s);
      default -> context.messages().send(s, "<gray>/backup [safe|unsafe|cancel]");
    }
    return true;
  }
}
