package eu.hunfeld.flunarBauserver.listener;

import eu.hunfeld.flunarBauserver.service.WorldService;
import eu.hunfeld.flunarBauserver.utils.Messages;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

/** Auf der Hauptwelt dürfen ausschließlich Builder die Welt verändern. */
public final class MainWorldBuildProtectionListener implements Listener {
  private final WorldService worlds;
  private final Messages messages;

  public MainWorldBuildProtectionListener(WorldService worlds, Messages messages) {
    this.worlds = worlds;
    this.messages = messages;
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void place(BlockPlaceEvent event) {
    if (deny(event.getPlayer(), event.getBlock().getWorld())) event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void breakBlock(BlockBreakEvent event) {
    if (deny(event.getPlayer(), event.getBlock().getWorld())) event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void emptyBucket(PlayerBucketEmptyEvent event) {
    if (deny(event.getPlayer(), event.getBlock().getWorld())) event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void fillBucket(PlayerBucketFillEvent event) {
    if (deny(event.getPlayer(), event.getBlock().getWorld())) event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void placeHanging(HangingPlaceEvent event) {
    Player player = event.getPlayer();
    if (player != null && deny(player, event.getEntity().getWorld())) event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void breakHanging(HangingBreakByEntityEvent event) {
    if (event.getRemover() instanceof Player player && deny(player, event.getEntity().getWorld()))
      event.setCancelled(true);
  }

  private boolean deny(Player player, World world) {
    if (world != worlds.mainWorld() || player.hasPermission("bauserver.builder")) return false;
    messages.action(player, "<red>Auf der Hauptwelt dürfen nur Builder bauen.");
    return true;
  }
}
