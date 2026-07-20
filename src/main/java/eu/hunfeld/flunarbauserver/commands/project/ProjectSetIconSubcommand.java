package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;

final class ProjectSetIconSubcommand extends AbstractProjectSubcommand {
  ProjectSetIconSubcommand(BauserverContext c) {
    super(c);
  }

  public void execute(Player p, String[] a) {
    if (!require(p, "bauserver.srbuilder") || !database(p)) return;
    String w = currentWorld(p);
    if (a.length < 2 || Material.matchMaterial(a[1]) == null) {
      context.messages().send(p, "<gray>/projekt seticon <Item>");
      return;
    }
    if (!context.projects().exists(w)) {
      context.messages().send(p, "<red>Du stehst auf keiner Projektwelt.");
      return;
    }
    complete(context.projects().setIcon(w, a[1]), p, "<green>Projekt-Icon aktualisiert.");
  }

  @Override
  public List<String> tabComplete(Player player, String[] args) {
    return args.length == 2
        ? filter(
            List.of(
                "map",
                "grass_block",
                "diamond_boots",
                "iron_pickaxe",
                "oak_sign",
                "chest",
                "beacon"),
            args[1])
        : List.of();
  }
}
