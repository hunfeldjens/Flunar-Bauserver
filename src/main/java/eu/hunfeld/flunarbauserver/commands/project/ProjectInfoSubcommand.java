package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.gui.ProjectInfoMenu;
import eu.hunfeld.flunarbauserver.model.ProjectInfo;
import java.util.*;
import org.bukkit.entity.Player;

final class ProjectInfoSubcommand extends AbstractProjectSubcommand {
  private final ProjectInfoMenu menu;

  ProjectInfoSubcommand(BauserverContext c, ProjectInfoMenu menu) {
    super(c);
    this.menu = menu;
  }

  public void execute(Player p, String[] a) {
    if (!require(p, "bauserver.builder") || !database(p)) return;
    String world = currentWorld(p);
    if (a.length > 1 && a[1].equalsIgnoreCase("add")) {
      if (!context.projects().exists(world) || a.length < 4) {
        context
            .messages()
            .send(p, "<gray>/projekt info add <Titel> <Beschreibung> (auf einer Projektwelt)");
        return;
      }
      var l = p.getLocation();
      String description = String.join(" ", Arrays.copyOfRange(a, 3, a.length));
      ProjectInfo info =
          new ProjectInfo(
              0,
              world,
              a[2],
              description,
              p.getUniqueId(),
              l.x(),
              l.y(),
              l.z(),
              l.getYaw(),
              l.getPitch());
      complete(context.projectInfos().add(info), p, "<green>Projekt-Info gespeichert.");
      return;
    }
    if (a.length > 1 && !a[1].equalsIgnoreCase("-all")) {
      context.messages().send(p, "<gray>/projekt info [add <Titel> <Beschreibung> | -all]");
      return;
    }
    if (a.length == 1 && !context.projects().exists(world)) {
      context.messages().send(p, "<red>Du befindest dich auf keiner Projektwelt.");
      return;
    }
    menu.open(p, a.length > 1 ? "" : world);
  }

  public List<String> tabComplete(Player p, String[] a) {
    return a.length == 2 ? filter(List.of("add", "-all"), a[1]) : List.of();
  }
}
