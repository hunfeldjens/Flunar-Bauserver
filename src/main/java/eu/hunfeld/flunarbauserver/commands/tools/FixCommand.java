package eu.hunfeld.flunarbauserver.commands.tools;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import eu.hunfeld.flunarbauserver.service.FixService;
import eu.hunfeld.flunarbauserver.utils.UiSound;
import java.util.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class FixCommand extends BaseCommand {
  private final FixService fix;

  public FixCommand(BauserverContext c, FixService f) {
    super(c);
    fix = f;
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player p = player(s);
    if (p == null) return true;
    if (a.length == 0) fix.execute(p);
    else
      switch (a[0].toLowerCase()) {
        case "1" -> fix.set(p, 1);
        case "2" -> fix.set(p, 2);
        case "undo" -> fix.undo(p);
        case "clear" -> {
          fix.clear(p);
          UiSound.CLICK.play(p);
          context.messages().send(p, "<gray>Markierung <red>entfernt<gray>.");
        }
        case "?" -> help(p);
        default -> context.messages().send(p, "<gray>/fix 1, /fix 2, /fix, /fix undo, /fix clear");
      }
    return true;
  }

  public List<String> onTabComplete(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    return a.length == 1 ? List.of("1", "2", "undo", "clear") : List.of();
  }

  private void help(Player player) {
    context.messages().send(player, "<gray>Benutzung:");
    context.messages().send(player, "<green>/fix 1 <gray>– Ecke 1 markieren (Block unter dir)");
    context.messages().send(player, "<green>/fix 2 <gray>– Ecke 2 markieren (Block unter dir)");
    context
        .messages()
        .send(player, "<green>/fix <gray>– zugedecktes Gras / Trampelpfade zu Dirt fixen");
    context.messages().send(player, "<green>/fix undo <gray>– letzten Fix rückgängig machen");
    context.messages().send(player, "<green>/fix clear <gray>– Markierung entfernen");
    context
        .messages()
        .send(
            player,
            "<red>Warnung: <gray>Nach Rejoin oder Verbindungsabbruch geht <white>/fix undo <gray>nicht mehr.");
  }
}
