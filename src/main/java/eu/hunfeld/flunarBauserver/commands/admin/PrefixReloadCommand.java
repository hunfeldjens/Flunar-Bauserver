package eu.hunfeld.flunarBauserver.commands.admin;

import eu.hunfeld.flunarBauserver.*;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

public final class PrefixReloadCommand extends BaseCommand {
  private final Runnable reload;

  public PrefixReloadCommand(BauserverContext c, Runnable r) {
    super(c);
    reload = r;
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    if (a.length == 1 && a[0].equalsIgnoreCase("reload")) {
      reload.run();
      context.messages().send(s, "<green>Prefixe und Tablist wurden aktualisiert.");
    } else context.messages().send(s, "<gray>Benutzung: <green>/prefix reload");
    return true;
  }
}
