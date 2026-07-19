package eu.hunfeld.flunarBauserver.commands.admin;

import eu.hunfeld.flunarBauserver.*;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class VanishCommand extends BaseCommand {
  public VanishCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player p = player(s);
    if (p != null) {
      boolean v = context.vanish().toggle(p);
      context
          .messages()
          .send(p, "<gray>Vanish ist " + (v ? "<green>aktiv" : "<red>inaktiv") + "<gray>.");
    }
    return true;
  }
}
