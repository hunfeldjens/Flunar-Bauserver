package eu.hunfeld.flunarBauserver.commands.project;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.gui.ProjectMenu;
import org.bukkit.entity.Player;

final class ProjectAdminSubcommand extends AbstractProjectSubcommand {
  private final ProjectMenu menu;

  ProjectAdminSubcommand(BauserverContext c, ProjectMenu m) {
    super(c);
    menu = m;
  }

  public void execute(Player p, String[] a) {
    if (require(p, "bauserver.srbuilder") && database(p)) menu.open(p, 0, true);
  }
}
