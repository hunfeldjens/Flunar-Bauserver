package eu.hunfeld.flunarbauserver.commands.player;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

public final class ShowEntityCommand extends BaseCommand {
  public ShowEntityCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    Player p = player(s);
    if (p == null) return true;
    RayTraceResult result =
        p.getWorld()
            .rayTraceEntities(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                50,
                1,
                entity -> entity != p);
    Entity entity = result == null ? null : result.getHitEntity();
    if (entity == null) {
      context.messages().send(p, "<gray>Du schaust <red>keine <gray>Entity an.");
      return true;
    }
    switch (entity) {
      case Player _ -> {
        context.messages().send(p, "<gray>Spieler können <red>nicht <gray>verändert werden.");
        return true;
      }
      case Animals _ -> {
        context.messages().send(p, "<gray>Tiere können <red>nicht <gray>verändert werden.");
        return true;
      }
      default -> {
        entity.setVisibleByDefault(true);
        context.messages().action(p, "<gray>Entity → <green>sichtbar");
      }
    }
    return true;
  }
}
