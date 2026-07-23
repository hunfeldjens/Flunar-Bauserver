package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@SuppressWarnings("SpellCheckingInspection")
final class ProjectBanSubcommand extends AbstractProjectSubcommand {
  ProjectBanSubcommand(BauserverContext c) {
    super(c);
  }

  public void execute(Player p, String[] a) {
    if (!require(p, "bauserver.builder") || !database(p)) return;
    if (a.length < 2) {
      context.messages().send(p, "<gray>/projekt ban <Spieler>");
      return;
    }
    Player target = Bukkit.getPlayerExact(a[1]);
    if (target == null || target == p || target.hasPermission("bauserver.admin.bypass")) {
      context.messages().send(p, "<red>Spieler kann nicht projektgebannt werden.");
      return;
    }
    String world = currentWorld(p);
    if (context.projectAccess().isBanned(target.getUniqueId(), world)) {
      context
          .messages()
          .send(
              p, "<yellow>" + target.getName() + " <gray>ist auf diesem Projekt bereits gebannt.");
      return;
    }
    context
        .projectAccess()
        .setBan(target.getUniqueId(), world, true)
        .whenComplete(
            (saved, _) ->
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> {
                          if (!Boolean.TRUE.equals(saved)) {
                            if (context.projectAccess().isBanned(target.getUniqueId(), world))
                              context
                                  .messages()
                                  .send(
                                      p,
                                      "<yellow>"
                                          + target.getName()
                                          + " <gray>ist auf diesem Projekt bereits gebannt.");
                            else
                              context
                                  .messages()
                                  .send(p, "<red>Der Projekt-Ban konnte nicht gespeichert werden.");
                            return;
                          }
                          context
                              .messages()
                              .send(
                                  p,
                                  "<green>"
                                      + target.getName()
                                      + " <gray>wurde auf diesem Projekt gebannt.");
                          if (target.getWorld() == p.getWorld()) {
                            context.worlds().clearPlayer(target);
                            context
                                .worlds()
                                .moveToMain(
                                    target,
                                    "<gray>Du wurdest von <green>"
                                        + world
                                        + " <gray>entfernt, weil du dort gebannt wurdest.");
                          }
                        }));
  }

  public List<String> tabComplete(Player p, String[] a) {
    return a.length == 2
        ? filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), a[1])
        : List.of();
  }
}
