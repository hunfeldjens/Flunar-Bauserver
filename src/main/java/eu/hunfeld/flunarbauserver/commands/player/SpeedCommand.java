package eu.hunfeld.flunarbauserver.commands.player;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import java.util.List;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SpeedCommand extends BaseCommand {
  public SpeedCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    Player p = player(s);
    if (p == null) return true;
    if (a.length < 1) {
      context.messages().send(p, "<gray>/speed <0-10|off>");
      return true;
    }
    if (a[0].equalsIgnoreCase("off")) {
      p.setWalkSpeed(.2f);
      p.setFlySpeed(.1f);
      context.messages().action(p, "<gray>Speed → <green>Zurückgesetzt");
      return true;
    }
    try {
      float n = Float.parseFloat(a[0]);
      if (n < 0 || n > 10) throw new NumberFormatException();
      float value = n / 10f;
      p.setFlySpeed(value);
      p.setWalkSpeed(value);
      context.messages().action(p, "<gray>Speed → <green>" + n + "<gray>/10");
    } catch (NumberFormatException e) {
      context
          .messages()
          .send(
              p, "<gray>Zahl zwischen <green>0 <gray>und <green>10 <gray>oder '<green>off<gray>'.");
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    return a.length == 1
        ? List.of("off", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        : List.of();
  }
}
