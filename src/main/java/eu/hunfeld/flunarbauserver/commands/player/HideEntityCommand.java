package eu.hunfeld.flunarbauserver.commands.player;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

public final class HideEntityCommand extends BaseCommand {
  public HideEntityCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    Player p = player(s);
    if (p == null) return true;
    RayTraceResult r =
        p.getWorld()
            .rayTraceEntities(
                p.getEyeLocation(), p.getEyeLocation().getDirection(), 50, 1, e -> e != p);
    Entity e = r == null ? null : r.getHitEntity();
    if (e == null) {
      context.messages().send(p, "<gray>Du schaust <red>keine <gray>Entity an.");
      return true;
    }
    switch (e) {
      case Player _ -> {
        context.messages().send(p, "<gray>Spieler können <red>nicht <gray>verändert werden.");
        return true;
      }
      case Animals _ -> {
        context.messages().send(p, "<gray>Tiere können <red>nicht <gray>verändert werden.");
        return true;
      }
      default -> {
        e.setVisibleByDefault(false);
        context.messages().action(p, "<gray>Entity → <red>unsichtbar");
      }
    }
    return true;
  }
}
