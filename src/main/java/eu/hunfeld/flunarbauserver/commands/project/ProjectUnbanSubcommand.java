package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.utils.Players;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

final class ProjectUnbanSubcommand extends AbstractProjectSubcommand {
  ProjectUnbanSubcommand(BauserverContext c) {
    super(c);
  }

  public void execute(Player p, String[] a) {
    if (!require(p, "bauserver.srbuilder") || !database(p)) return;
    if (a.length < 2) {
      context.messages().send(p, "<gray>/projekt unban <Spieler>");
      return;
    }
    OfflinePlayer target = Players.known(a[1]).orElse(null);
    if (target == null) {
      context.messages().send(p, "<red>Spieler nicht gefunden.");
      return;
    }
    String world = currentWorld(p);
    String targetName = target.getName() == null ? a[1] : target.getName();
    if (!context.projectAccess().isBanned(target.getUniqueId(), world)) {
      context
          .messages()
          .send(p, "<yellow>" + targetName + " <gray>ist auf diesem Projekt nicht gebannt.");
      return;
    }
    complete(
        context.projectAccess().setBan(target.getUniqueId(), world, false),
        p,
        "<green>Projekt-Ban von " + targetName + " <gray>wurde entfernt.");
  }

  @Override
  public List<String> tabComplete(Player player, String[] args) {
    return args.length == 2
        ? filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1])
        : List.of();
  }
}
