package eu.hunfeld.flunarbauserver.commands.moderation;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import eu.hunfeld.flunarbauserver.utils.Players;
import org.bukkit.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

public final class UnbanCommand extends BaseCommand {
  public UnbanCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    if (!requireDatabase(s)) return true;
    if (a.length < 1) {
      context.messages().send(s, "<gray>Benutzung: <green>/unban <Spieler>");
      return true;
    }
    OfflinePlayer t = Players.known(a[0]).orElse(null);
    if (t == null) {
      context.messages().send(s, "<red>Spieler nicht gefunden.");
      return true;
    }
    String targetName = t.getName() == null ? a[0] : t.getName();
    if (context.moderation().activeBan(t.getUniqueId()).isEmpty()) {
      context.messages().send(s, "<yellow>" + targetName + " <gray>ist nicht gebannt.");
      return true;
    }
    complete(
        context.moderation().unban(t.getUniqueId()),
        s,
        "<green>" + targetName + " <gray>wurde entbannt.");
    return true;
  }
}
