package eu.hunfeld.flunarbauserver.commands.player;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class AnvilCommand extends BaseCommand {
  public AnvilCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player p = player(s);
    if (p != null) {
      p.openAnvil(null, true);
      context.messages().action(p, "<gray>Amboss geöffnet.");
    }
    return true;
  }
}
