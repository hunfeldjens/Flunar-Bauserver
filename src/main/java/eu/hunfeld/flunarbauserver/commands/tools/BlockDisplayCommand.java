package eu.hunfeld.flunarbauserver.commands.tools;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import eu.hunfeld.flunarbauserver.service.BlockDisplayService;
import java.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

public final class BlockDisplayCommand extends BaseCommand {
  private final BlockDisplayService service;

  public BlockDisplayCommand(BauserverContext c, BlockDisplayService s) {
    super(c);
    service = s;
  }

  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String @NotNull [] args) {
    Player p = player(sender);
    if (p == null) return true;
    if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equals("?")) {
      help(p);
      return true;
    }
    String action = args[0].toLowerCase();
    if (action.equals("create") || action.equals("set")) {
      if (args.length < 2) {
        error(p, "Bitte einen Block angeben.");
        return true;
      }
      Material m = BlockDisplayService.blockMaterial(args[1]);
      Block target = p.getTargetBlockExact(64, FluidCollisionMode.NEVER);
      if (m == null || target == null) {
        error(p, "Ungültiger Block oder kein Zielblock.");
        return true;
      }
      BlockDisplay d = target.getWorld().spawn(target.getLocation(), BlockDisplay.class);
      d.setBlock(m.createBlockData());
      d.addScoreboardTag("flunar_blockdisplay");
      service.select(p, d);
      context
          .messages()
          .send(p, "<gray>BlockDisplay mit <green>" + m + " <gray>erstellt und ausgewählt.");
      return true;
    }
    if (action.equals("select")) {
      BlockDisplay d = service.lookedAt(p, 64).orElse(null);
      if (d == null) error(p, "Kein BlockDisplay anvisiert.");
      else {
        service.select(p, d);
        context.messages().send(p, "<green>BlockDisplay ausgewählt.");
      }
      return true;
    }
    BlockDisplay d = service.selected(p).orElse(null);
    if (d == null) {
      error(p, "Kein BlockDisplay ausgewählt.");
      return true;
    }
    if (d.getWorld() != p.getWorld() || d.getLocation().distance(p.getLocation()) > 64) {
      error(p, "Display ist in einer anderen Welt oder zu weit entfernt.");
      return true;
    }
    try {
      switch (action) {
        case "block" -> {
          Material m = args.length > 1 ? BlockDisplayService.blockMaterial(args[1]) : null;
          if (m == null) throw new IllegalArgumentException();
          d.setBlock(m.createBlockData());
        }
        case "move" -> {
          if (args.length < 4) throw new IllegalArgumentException();
          d.teleport(
              d.getLocation()
                  .add(
                      Double.parseDouble(args[1]),
                      Double.parseDouble(args[2]),
                      Double.parseDouble(args[3])));
        }
        case "scale" -> {
          if (args.length < 2) throw new IllegalArgumentException();
          float x = Float.parseFloat(args[1]),
              y = args.length > 2 ? Float.parseFloat(args[2]) : x,
              z = args.length > 3 ? Float.parseFloat(args[3]) : x;
          if (x <= 0 || y <= 0 || z <= 0 || x > 64 || y > 64 || z > 64)
            throw new IllegalArgumentException();
          BlockDisplayService.scale(d, x, y, z);
        }
        case "rotate" -> {
          if (args.length < 2) throw new IllegalArgumentException();
          BlockDisplayService.rotate(d, Float.parseFloat(args[1]));
        }
        case "glow" -> d.setGlowing(!d.isGlowing());
        case "info" -> {
          var scale = d.getTransformation().getScale();
          context
              .messages()
              .send(
                  p,
                  "<gray>Block: <white>"
                      + d.getBlock().getMaterial()
                      + " <gray>Position: <white>"
                      + format(d.getLocation().x())
                      + ", "
                      + format(d.getLocation().y())
                      + ", "
                      + format(d.getLocation().z())
                      + " <gray>Skalierung: <white>"
                      + scale.x
                      + ", "
                      + scale.y
                      + ", "
                      + scale.z);
          return true;
        }
        case "remove", "delete" -> {
          d.remove();
          service.clear(p);
          context.messages().send(p, "<gray>BlockDisplay <red>gelöscht<gray>.");
          return true;
        }
        default -> {
          help(p);
          return true;
        }
      }
      context.messages().send(p, "<green>BlockDisplay aktualisiert.");
    } catch (IllegalArgumentException e) {
      error(p, "Ungültige Argumente. Nutze /bd help.");
    }
    return true;
  }

  private void help(Player p) {
    context
        .messages()
        .send(
            p,
            "<green>/bd create <Block>, select, block <Block>, move <X> <Y> <Z>, scale <X> [Y Z], rotate <Grad>, glow, info, remove");
  }

  private void error(Player p, String text) {
    context.messages().send(p, "<red>" + text);
  }

  private static String format(double v) {
    return String.format(Locale.ROOT, "%.2f", v);
  }

  public List<String> onTabComplete(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    if (a.length == 1)
      return List.of(
          "create", "select", "block", "move", "scale", "rotate", "glow", "info", "remove", "help");
    if (a.length == 2 && (a[0].equalsIgnoreCase("create") || a[0].equalsIgnoreCase("block")))
      return List.of(
          "stone",
          "grass_block",
          "oak_planks",
          "oak_log",
          "glass",
          "white_concrete",
          "barrier",
          "light");
    return List.of();
  }
}
