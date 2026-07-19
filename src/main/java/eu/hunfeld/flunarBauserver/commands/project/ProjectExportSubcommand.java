package eu.hunfeld.flunarBauserver.commands.project;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.database.Sql;
import eu.hunfeld.flunarBauserver.utils.Players;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

final class ProjectExportSubcommand extends AbstractProjectSubcommand {
  ProjectExportSubcommand(BauserverContext c) {
    super(c);
  }

  public void execute(Player p, String[] a) {
    if (!require(p, "bauserver.admin")) return;
    if (a.length < 3) {
      context.messages().send(p, "<gray>/projekt export <projekt|privat> <Name>");
      return;
    }
    String namespace = a[1].toLowerCase(), world, export = a[2];
    if (namespace.equals("projekt")) world = Sql.cleanWorld(a[2]);
    else if (namespace.equals("privat")) {
      OfflinePlayer target = Players.known(a[2]).orElse(null);
      if (target == null) {
        context.messages().send(p, "<red>Spieler nicht gefunden.");
        return;
      }
      world = target.getUniqueId().toString();
    } else {
      context.messages().send(p, "<red>Namespace muss projekt oder privat sein.");
      return;
    }
    if (!world.matches("[a-zA-Z0-9._-]+") || !export.matches("[a-zA-Z0-9._-]+")) {
      context.messages().send(p, "<red>Ungültiger Name.");
      return;
    }
    context.messages().send(p, "<yellow>Export gestartet.");
    context
        .worldTransfers()
        .export(namespace, world, export)
        .whenComplete(
            (code, error) ->
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () ->
                            context
                                .messages()
                                .send(
                                    p,
                                    error == null && code == 0
                                        ? "<green>Export abgeschlossen."
                                        : "<red>Export fehlgeschlagen (Exit " + code + ").")));
  }

  public List<String> tabComplete(Player p, String[] a) {
    return a.length == 2 ? filter(List.of("projekt", "privat"), a[1]) : List.of();
  }
}
