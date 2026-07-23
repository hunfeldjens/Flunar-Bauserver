package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.database.Sql;
import eu.hunfeld.flunarbauserver.utils.Players;
import java.util.*;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

final class ProjectExportSubcommand extends AbstractProjectSubcommand {
  private static final Pattern SAFE_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9._-]+");
  ProjectExportSubcommand(BauserverContext c) {
    super(c);
  }

  @SuppressWarnings("resource")
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
    if (!SAFE_NAME_PATTERN.matcher(world).matches()
        || !SAFE_NAME_PATTERN.matcher(export).matches()) {
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
