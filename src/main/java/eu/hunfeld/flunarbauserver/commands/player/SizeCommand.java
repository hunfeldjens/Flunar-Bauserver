package eu.hunfeld.flunarbauserver.commands.player;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.attribute.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SizeCommand extends BaseCommand {
  public SizeCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player p = player(s);
    if (p == null) return true;
    if (a.length < 1) {
      context.messages().send(p, "<gray>/size <0.1-10> <dark_gray>- Standard = 1");
      return true;
    }
    try {
      double n = Double.parseDouble(a[0]);
      AttributeInstance at = p.getAttribute(Attribute.SCALE);
      if (at == null || n < 0.1 || n > 10) throw new NumberFormatException();
      at.setBaseValue(n);
      context.messages().action(p, "<gray>Größe → <green>" + a[0]);
    } catch (NumberFormatException e) {
      context.messages().send(p, "<red>Wert muss zwischen 0.1 und 10 liegen.");
    }
    return true;
  }

  @Override
  public java.util.List<String> onTabComplete(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    return a.length == 1
        ? java.util.List.of("0.2", "0.4", "0.6", "0.8", "1", "1.2", "1.4", "1.6", "1.8", "2")
        : java.util.List.of();
  }
}
