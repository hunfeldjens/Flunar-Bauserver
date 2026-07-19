package eu.hunfeld.flunarBauserver.listener;

import eu.hunfeld.flunarBauserver.service.FeatureService;
import eu.hunfeld.flunarBauserver.utils.Messages;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;

public final class FeatureListener implements Listener {
  private final FeatureService features;
  private final Messages messages;

  public FeatureListener(FeatureService features, Messages messages) {
    this.features = features;
    this.messages = messages;
  }

  @EventHandler
  public void worldLoad(WorldLoadEvent event) {
    features.apply(event.getWorld());
  }

  @EventHandler(ignoreCancelled = true)
  public void damage(EntityDamageEvent e) {
    if (!features.enabled("damage")) e.setCancelled(true);
    else if (e.getCause() == EntityDamageEvent.DamageCause.FALL && !features.enabled("falldamage"))
      e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void breakBlock(BlockBreakEvent e) {
    if (!features.enabled("break") || !features.enabled("blockbreak")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void place(BlockPlaceEvent e) {
    boolean commandBlockDenied =
        isCommandBlock(e.getBlock().getType()) && !features.enabled("commandblocks");
    if (!features.enabled("place") || !features.enabled("blockplace") || commandBlockDenied)
      e.setCancelled(true);
    if (commandBlockDenied)
      messages.action(
          e.getPlayer(), "<gray>Command Blocks sind auf diesem Server <red>deaktiviert<gray>.");
  }

  @EventHandler(ignoreCancelled = true)
  public void blockDamage(BlockDamageEvent e) {
    if (!features.enabled("blockdamage")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void inventory(InventoryClickEvent e) {
    if (!features.enabled("inventory")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void pickup(EntityPickupItemEvent e) {
    if (e.getEntity() instanceof Player && !features.enabled("pickup")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void hunger(FoodLevelChangeEvent e) {
    if (!features.enabled("hunger")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void weather(WeatherChangeEvent e) {
    if (!features.enabled("weather")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void craft(CraftItemEvent e) {
    if (!features.enabled("craft")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void drop(PlayerDropItemEvent e) {
    if (!features.enabled("drop")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void explode(EntityExplodeEvent e) {
    if (!features.enabled("explosion")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void spawn(CreatureSpawnEvent e) {
    if (!features.enabled("mobspawn")
        && e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) e.setCancelled(true);
    if (!features.enabled("summon") && e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.COMMAND)
      e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void falling(EntitySpawnEvent e) {
    if (e.getEntity() instanceof FallingBlock && !features.enabled("gravity")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void spread(BlockSpreadEvent e) {
    if (!features.enabled("firespread")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void burn(BlockBurnEvent e) {
    if (!features.enabled("firespread")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void decay(LeavesDecayEvent e) {
    if (!features.enabled("leafdecay")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void flow(BlockFromToEvent e) {
    if (!features.enabled("liquidflow")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void itemDamage(PlayerItemDamageEvent e) {
    if (!features.enabled("itemdamage")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void droppedItemDamage(EntityDamageEvent e) {
    if (e.getEntity() instanceof Item && !features.enabled("itemdamage")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void portal(PlayerPortalEvent e) {
    if (!features.enabled("portal")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void farmland(EntityChangeBlockEvent e) {
    if (e.getBlock().getType() == Material.FARMLAND && !features.enabled("farmland"))
      e.setCancelled(true);
    if (e.getEntity() instanceof FallingBlock && !features.enabled("gravity")) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void commandBlock(PlayerInteractEvent e) {
    if (e.getClickedBlock() != null
        && isCommandBlock(e.getClickedBlock().getType())
        && !features.enabled("commandblocks")) {
      e.setCancelled(true);
      messages.action(
          e.getPlayer(), "<gray>Command Blocks sind auf diesem Server <red>deaktiviert<gray>.");
    }
  }

  private static boolean isCommandBlock(Material m) {
    return m == Material.COMMAND_BLOCK
        || m == Material.CHAIN_COMMAND_BLOCK
        || m == Material.REPEATING_COMMAND_BLOCK;
  }
}
