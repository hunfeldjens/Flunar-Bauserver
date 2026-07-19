package eu.hunfeld.flunarBauserver.commands.project;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.model.Project;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

final class ProjectRemoveSubcommand extends AbstractProjectSubcommand {
  ProjectRemoveSubcommand(BauserverContext c) {
    super(c);
  }

  public void execute(Player p, String[] a) {
    if (!require(p, "bauserver.srbuilder") || !database(p)) return;
    if (a.length < 2) {
      context.messages().send(p, "<gray>/projekt remove <Projektname>");
      return;
    }
    Project project =
        context.projects().all().stream()
            .filter(candidate -> candidate.name().equalsIgnoreCase(a[1]))
            .findFirst()
            .orElse(null);
    if (project == null) {
      context.messages().send(p, "<red>Projekt nicht gefunden.");
      return;
    }
    context
        .projects()
        .delete(project.name())
        .whenComplete(
            (ok, error) ->
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> {
                          if (!Boolean.TRUE.equals(ok)) {
                            context
                                .messages()
                                .send(p, "<red>Projekt konnte nicht entfernt werden.");
                            return;
                          }
                          context.autoload().set(project.worldName(), false);
                          context
                              .worlds()
                              .evacuateAndUnload(
                                  project.worldName(),
                                  "<gray>Das Projekt <green>"
                                      + project.name()
                                      + " <gray>wurde aus der Datenbank entfernt und die Welt entladen.");
                          context
                              .messages()
                              .send(
                                  p,
                                  "<green>Projekt aus der Datenbank entfernt und Welt entladen; Weltdaten bleiben erhalten.");
                        }));
  }

  public List<String> tabComplete(Player p, String[] a) {
    return a.length == 2
        ? filter(context.projects().all().stream().map(Project::name).toList(), a[1])
        : List.of();
  }
}
