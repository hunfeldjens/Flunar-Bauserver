package eu.hunfeld.flunarbauserver.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

public final class BlockDisplayService {
  private final Map<UUID, UUID> selections = new ConcurrentHashMap<>();

  public Optional<BlockDisplay> selected(Player player) {
    UUID uuid = selections.get(player.getUniqueId());
    Entity entity = uuid == null ? null : Bukkit.getEntity(uuid);
    if (entity instanceof BlockDisplay display && display.isValid()) return Optional.of(display);
    selections.remove(player.getUniqueId());
    return Optional.empty();
  }

  public void select(Player player, BlockDisplay display) {
    selections.put(player.getUniqueId(), display.getUniqueId());
  }

  public void clear(Player player) {
    selections.remove(player.getUniqueId());
  }

  public Optional<BlockDisplay> lookedAt(Player player, double range) {
    Vector start = player.getEyeLocation().toVector(),
        direction = player.getEyeLocation().getDirection().normalize();
    BlockDisplay best = null;
    double distance = Double.MAX_VALUE;
    for (Entity entity : player.getWorld().getEntitiesByClass(BlockDisplay.class)) {
      Vector center = entity.getLocation().add(.5, .5, .5).toVector(),
          to = center.clone().subtract(start);
      double forward = to.dot(direction);
      if (forward < 0 || forward > range) continue;
      double side = to.lengthSquared() - forward * forward;
      if (side <= .75 && forward < distance) {
        distance = forward;
        best = (BlockDisplay) entity;
      }
    }
    return Optional.ofNullable(best);
  }

  public static Material blockMaterial(String value) {
    Material material = Material.matchMaterial(value);
    return material != null && material.isBlock() && !material.isAir() ? material : null;
  }

  public static void scale(BlockDisplay display, float x, float y, float z) {
    Transformation t = display.getTransformation();
    t.getScale().set(x, y, z);
    display.setTransformation(t);
  }

  public static void rotate(BlockDisplay display, float degrees) {
    Transformation t = display.getTransformation();
    t.getLeftRotation().rotationY((float) Math.toRadians(degrees));
    display.setTransformation(t);
  }
}
