package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

final class ProjectKickSubcommand extends AbstractProjectSubcommand {
  ProjectKickSubcommand(BauserverContext c) {
    super(c);
  }

  public void execute(Player p, String[] a) {
    if (!require(p, "bauserver.team")) return;
    if (a.length < 2) {
      context.messages().send(p, "<gray>/projekt kick <Spieler>");
      return;
    }
    Player target = Bukkit.getPlayerExact(a[1]);
    if (target == null || target == p || target.getWorld() != p.getWorld()) {
      context
          .messages()
          .send(p, "<red>Spieler ist nicht auf deiner Welt oder kann nicht gekickt werden.");
      return;
    }
    context.worlds().clearPlayer(target);
    context.worlds().teleport(target, context.worlds().mainWorld().getSpawnLocation());
    context.messages().send(target, "<gray>Du wurdest aus dem Projekt entfernt.");
  }

  public List<String> tabComplete(Player p, String[] a) {
    return a.length == 2
        ? filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), a[1])
        : List.of();
  }
}
