package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.database.Sql;
import eu.hunfeld.flunarbauserver.model.Project;
import java.util.*;
import org.bukkit.entity.Player;

final class ProjectAutoloadSubcommand extends AbstractProjectSubcommand {
  ProjectAutoloadSubcommand(BauserverContext c) {
    super(c);
  }

  public void execute(Player p, String[] a) {
    if (!require(p, "bauserver.srbuilder") || !database(p)) return;
    if (a.length < 2) {
      context.messages().send(p, "<gray>/projekt autoload <Welt> [on|off]");
      return;
    }
    String w = Sql.cleanWorld(a[1]);
    if (a.length >= 3 && !a[2].equalsIgnoreCase("on") && !a[2].equalsIgnoreCase("off")) {
      context.messages().send(p, "<gray>/projekt autoload <Welt> [on|off]");
      return;
    }
    boolean active = a.length < 3 ? !context.autoload().contains(w) : a[2].equalsIgnoreCase("on");
    if (a.length >= 3 && context.autoload().contains(w) == active) {
      context
          .messages()
          .send(
              p,
              active
                  ? "<yellow>Autoload ist für diese Welt bereits aktiviert."
                  : "<yellow>Autoload ist für diese Welt bereits deaktiviert.");
      return;
    }
    complete(context.autoload().set(w, active), p, "<green>Autoload wurde aktualisiert.");
  }

  public List<String> tabComplete(Player p, String[] a) {
    return a.length == 2
        ? filter(context.projects().all().stream().map(Project::worldName).toList(), a[1])
        : a.length == 3 ? filter(List.of("on", "off"), a[2]) : List.of();
  }
}
