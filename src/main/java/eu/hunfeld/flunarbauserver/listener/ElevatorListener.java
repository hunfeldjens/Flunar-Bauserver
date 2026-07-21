package eu.hunfeld.flunarbauserver.listener;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;

public final class ElevatorListener implements Listener {
  private final Plugin plugin;

  public ElevatorListener(Plugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void down(PlayerToggleSneakEvent event) {
    Player player = event.getPlayer();
    if (!event.isSneaking()
        || !player.hasPermission("bauserver.builder")
        || blockBelow(player).getType() != Material.NETHERITE_BLOCK) return;
    Bukkit.getScheduler()
        .runTaskLater(
            plugin,
            () -> {
              if (player.isOnline() && player.isSneaking()) move(player, -1);
            },
            1L);
  }

  @EventHandler
  public void up(PlayerJumpEvent event) {
    Player player = event.getPlayer();
    if (!player.hasPermission("bauserver.builder")
        || blockBelow(player).getType() != Material.NETHERITE_BLOCK) return;
    move(player, 1);
  }

  private void move(Player player, int direction) {
    Location origin = player.getLocation();
    int x = player.getLocation().getBlockX();
    int z = player.getLocation().getBlockZ();
    int platformY = blockBelow(player).getY();
    for (int distance = 1; distance <= 100; distance++) {
      int y = platformY + (distance * direction);
      Block platform = player.getWorld().getBlockAt(x, y, z);
      if (platform.getType() != Material.NETHERITE_BLOCK) continue;
      Block feet = platform.getRelative(0, 1, 0);
      if (!feet.isPassable()) continue;
      Location target = origin.clone();
      target.setY(platform.getY() + 1.0);
      player.teleport(target);
      player.playSound(
          player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, .5f, direction > 0 ? 1.5f : 1.0f);
      player.showTitle(
          Title.title(
              Component.text(direction > 0 ? "↑" : "↓")
                  .color(net.kyori.adventure.text.format.NamedTextColor.GREEN),
              Component.text(direction > 0 ? "Hoch" : "Runter")
                  .color(net.kyori.adventure.text.format.NamedTextColor.GRAY),
              Title.Times.times(
                  java.time.Duration.ZERO,
                  java.time.Duration.ofMillis(500),
                  java.time.Duration.ZERO)));
      return;
    }
  }

  private static Block blockBelow(Player player) {
    return player.getLocation().clone().subtract(0, 0.01, 0).getBlock();
  }
}
