package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.gui.ProjectMenu;
import org.bukkit.entity.Player;

final class ProjectOverviewSubcommand extends AbstractProjectSubcommand {
  private final ProjectMenu menu;

  ProjectOverviewSubcommand(BauserverContext c, ProjectMenu m) {
    super(c);
    menu = m;
  }

  public void execute(Player p, String[] a) {
    if (require(p, "bauserver.builder") && database(p)) menu.open(p, 0, false);
  }
}
