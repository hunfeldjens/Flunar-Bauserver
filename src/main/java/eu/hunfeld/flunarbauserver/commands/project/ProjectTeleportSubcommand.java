package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.database.Sql;
import eu.hunfeld.flunarbauserver.model.Project;
import eu.hunfeld.flunarbauserver.utils.UiSound;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

final class ProjectTeleportSubcommand extends AbstractProjectSubcommand {
  ProjectTeleportSubcommand(BauserverContext c) {
    super(c);
  }

  public void execute(Player p, String[] a) {
    if (!require(p, "bauserver.admin")) return;
    if (a.length < 2) {
      context.messages().send(p, "<gray>/projekt tp <Welt>");
      return;
    }
    String w = Sql.cleanWorld(a[1]);
    if (context.backups().safeWorldLocked(w)) {
      context.messages().send(p, "<red>Welt ist während des Backups gesperrt.");
      return;
    }
    try {
      World world =
          context.worlds().loaded(w).orElseGet(() -> context.worlds().createProject(w, "normal"));
      context.teleports().remember(p);
      context.worlds().clearPlayer(p);
      context
          .worlds()
          .teleport(p, world.getSpawnLocation())
          .thenAccept(
              success ->
                  Bukkit.getScheduler()
                      .runTask(
                          context.plugin(),
                          () -> {
                            if (!success) {
                              UiSound.ERROR.play(p);
                              return;
                            }
                            context.messages().action(p, "<green>Teleportiert nach <yellow>" + w);
                            UiSound.TELEPORT.play(p);
                          }));
    } catch (RuntimeException e) {
      context.messages().send(p, "<red>Welt konnte nicht geladen werden.");
      UiSound.ERROR.play(p);
    }
  }

  public List<String> tabComplete(Player p, String[] a) {
    return a.length == 2
        ? filter(context.projects().all().stream().map(Project::worldName).toList(), a[1])
        : List.of();
  }
}
