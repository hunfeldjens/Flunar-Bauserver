package eu.hunfeld.flunarbauserver.commands.admin;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

public final class ResetCommand extends BaseCommand {
  public ResetCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    if (!requireDatabase(s)) return true;
    if (a.length == 0 || !a[0].equalsIgnoreCase("confirm")) {
      context
          .messages()
          .send(s, "<yellow>Dies lädt alle DB-Caches neu. Bestätigen: <red>/reset confirm");
      return true;
    }
    complete(
        context.database().reloadCaches(),
        s,
        "<green>Alle persistenten Bauserver-Caches wurden neu geladen.");
    return true;
  }
}
