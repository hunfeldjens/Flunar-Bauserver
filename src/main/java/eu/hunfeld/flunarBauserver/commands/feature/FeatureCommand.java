package eu.hunfeld.flunarBauserver.commands.feature;

import eu.hunfeld.flunarBauserver.*;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import eu.hunfeld.flunarBauserver.gui.FeatureMenu;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class FeatureCommand extends BaseCommand {
  private final FeatureMenu menu;

  public FeatureCommand(BauserverContext c, FeatureMenu m) {
    super(c);
    menu = m;
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player p = player(s);
    if (p != null && requireDatabase(p)) menu.open(p);
    return true;
  }
}
