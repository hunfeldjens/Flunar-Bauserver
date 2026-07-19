package eu.hunfeld.flunarBauserver.gui;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.utils.Messages;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/** Originale Operator-Item-Menüs aus 05_gui.sk. */
public final class ToolsMenu implements Listener {
  private final BauserverContext context;
  private final NamespacedKey pistonToolKey;
  private static final String FIXED_MINECART_TAG = "flunar_fixed_minecart";

  public ToolsMenu(BauserverContext context) {
    this.context = context;
    this.pistonToolKey = new NamespacedKey(context.plugin(), "extended_piston_tool");
  }

  public void open(Player player) {
    MenuHolder holder = new MenuHolder(MenuType.MAIN);
    Inventory inventory =
        create(holder, 45, "<gold><bold>Operator Items", Material.GRAY_STAINED_GLASS_PANE);
    inventory.setItem(10, named(Material.BARRIER, "<red><bold>Barriere"));
    inventory.setItem(
        11,
        named(
            Material.LIGHT,
            "<yellow><bold>Lichtblöcke",
            List.of("<gray>Klicke für alle Helligkeitsstufen")));
    inventory.setItem(12, named(Material.STRUCTURE_VOID, "<light_purple><bold>Konstruktionsleere"));
    inventory.setItem(13, named(Material.DEBUG_STICK, "<aqua><bold>Debug-Stick"));
    inventory.setItem(14, named(Material.STRUCTURE_BLOCK, "<dark_purple><bold>Konstruktionsblock"));
    inventory.setItem(
        15,
        named(
            Material.COMMAND_BLOCK,
            "<gold><bold>Commandblöcke",
            List.of("<gray>Öffnet die Commandblock-Auswahl")));
    inventory.setItem(16, named(Material.JIGSAW, "<dark_green><bold>Jigsaw-Block"));
    inventory.setItem(19, named(Material.SPAWNER, "<dark_red><bold>Spawner"));
    inventory.setItem(20, named(Material.TRIAL_SPAWNER, "<red><bold>Trial-Spawner"));
    inventory.setItem(21, named(Material.VAULT, "<yellow><bold>Vault"));
    // PISTON_HEAD ist in Paper 26.1.2 ein Block, aber kein gültiger ItemStack.
    ItemStack pistonTool =
        named(
            Material.PISTON,
            "<gold><bold>Ausgefahrener Piston-Kopf",
            List.of("<gray>Platziert einen vollständig", "<gray>ausgefahrenen Piston samt Kopf"));
    ItemMeta pistonMeta = pistonTool.getItemMeta();
    pistonMeta.getPersistentDataContainer().set(pistonToolKey, PersistentDataType.BYTE, (byte) 1);
    pistonTool.setItemMeta(pistonMeta);
    inventory.setItem(22, pistonTool);
    inventory.setItem(23, named(Material.PLAYER_HEAD, "<aqua><bold>Spieler-Kopf"));
    inventory.setItem(
        24,
        named(
            Material.ARMOR_STAND,
            "<white><bold>Custom Armorstand",
            List.of("<gray>- Mit Armen", "<gray>- Ohne Bodenplatte", "<gray>- Keine Gravitation")));
    inventory.setItem(
        25,
        named(
            Material.MINECART,
            "<gray><bold>Minecarts",
            List.of("<gray>Öffnet die Minecart-Auswahl")));
    inventory.setItem(28, named(Material.ITEM_FRAME, "<yellow><bold>Unsichtbarer Itemframe"));
    inventory.setItem(29, named(Material.LIGHT_GRAY_CONCRETE, "<gray><bold>Builder-Block"));
    inventory.setItem(30, named(Material.BEDROCK, "<dark_gray><bold>Bedrock"));
    inventory.setItem(
        31, named(Material.REINFORCED_DEEPSLATE, "<dark_gray><bold>Verstärkter Tiefenschiefer"));
    inventory.setItem(32, named(Material.END_PORTAL_FRAME, "<dark_purple><bold>Endportalrahmen"));
    inventory.setItem(33, named(Material.SCULK_SENSOR, "<aqua><bold>Sculk-Sensor"));
    inventory.setItem(
        34, named(Material.CALIBRATED_SCULK_SENSOR, "<aqua><bold>Kalibrierter Sculk-Sensor"));
    show(player, inventory);
  }

  private void openLights(Player player) {
    MenuHolder holder = new MenuHolder(MenuType.LIGHTS);
    Inventory inventory =
        create(holder, 18, "<yellow><bold>Licht-Stufen", Material.GRAY_STAINED_GLASS_PANE);
    for (int level = 0; level <= 15; level++) {
      inventory.setItem(
          level,
          named(
              Material.LIGHT,
              "<yellow><bold>Licht-Level <white>" + level,
              List.of("<dark_gray>Level: " + level)));
    }
    inventory.setItem(17, named(Material.ARROW, "<gray>Zurück"));
    show(player, inventory);
  }

  private void openCommandBlocks(Player player) {
    MenuHolder holder = new MenuHolder(MenuType.COMMAND_BLOCKS);
    Inventory inventory =
        create(holder, 27, "<gold><bold>Commandblock-Items", Material.GRAY_STAINED_GLASS_PANE);
    inventory.setItem(10, named(Material.COMMAND_BLOCK, "<gold><bold>Command-Block"));
    inventory.setItem(12, named(Material.CHAIN_COMMAND_BLOCK, "<green><bold>Ketten-CMD-Block"));
    inventory.setItem(14, named(Material.REPEATING_COMMAND_BLOCK, "<blue><bold>Repeat-CMD-Block"));
    inventory.setItem(16, named(Material.COMMAND_BLOCK_MINECART, "<gold><bold>Minecart CMD-Block"));
    inventory.setItem(22, named(Material.ARROW, "<gray>Zurück"));
    show(player, inventory);
  }

  private void openMinecarts(Player player) {
    MenuHolder holder = new MenuHolder(MenuType.MINECARTS);
    Inventory inventory =
        create(holder, 27, "<gray><bold>Minecart-Items", Material.GRAY_STAINED_GLASS_PANE);
    inventory.setItem(
        10,
        named(
            Material.MINECART,
            "<gray><bold>Lore",
            List.of(
                "<gray>Minecart ohne Gravitation",
                "<gray>Bestmöglich gegen Anschubsen gesichert")));
    inventory.setItem(11, named(Material.CHEST_MINECART, "<yellow><bold>Truhen-Lore"));
    inventory.setItem(12, named(Material.FURNACE_MINECART, "<dark_gray><bold>Ofen-Lore"));
    inventory.setItem(13, named(Material.HOPPER_MINECART, "<green><bold>Trichter-Lore"));
    inventory.setItem(14, named(Material.TNT_MINECART, "<red><bold>TNT-Lore"));
    inventory.setItem(15, named(Material.COMMAND_BLOCK_MINECART, "<gold><bold>Commandblock-Lore"));
    inventory.setItem(22, named(Material.ARROW, "<gray>Zurück"));
    show(player, inventory);
  }

  @EventHandler
  public void click(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder(false) instanceof MenuHolder holder)) return;
    event.setCancelled(true);
    if (!(event.getWhoClicked() instanceof Player player)) return;
    int slot = event.getRawSlot();
    if (slot < 0 || slot >= event.getInventory().getSize()) return;
    ItemStack clicked = event.getCurrentItem();
    if (clicked == null
        || clicked.getType().isAir()
        || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

    switch (holder.type) {
      case MAIN -> clickMain(player, slot, clicked);
      case LIGHTS -> clickLights(player, slot);
      case COMMAND_BLOCKS -> clickSimpleSubmenu(player, slot, clicked);
      case MINECARTS -> clickMinecarts(player, slot, clicked);
    }
  }

  private void clickMain(Player player, int slot, ItemStack clicked) {
    if (slot == 11) {
      click(player);
      openLights(player);
      return;
    }
    if (slot == 15) {
      click(player);
      openCommandBlocks(player);
      return;
    }
    if (slot == 25) {
      click(player);
      openMinecarts(player);
      return;
    }
    if (slot == 22) {
      click(player);
      player.getInventory().addItem(clicked.clone());
      context
          .messages()
          .action(player, "<gray>Du hast einen <gold>ausgefahrenen Piston-Kopf <gray>erhalten.");
      return;
    }
    if (slot == 24) {
      giveCommand(
          player,
          "armor_stand[entity_data={id:\"minecraft:armor_stand\",ShowArms:1b,NoBasePlate:1b,NoGravity:1b}] 1");
      context
          .messages()
          .action(player, "<gray>Du hast einen <white>Armorstand ohne Gravitation <gray>erhalten.");
      return;
    }
    if (slot == 28) {
      giveCommand(player, "item_frame[entity_data={id:\"minecraft:item_frame\",Invisible:1b}] 1");
      context
          .messages()
          .action(player, "<gray>Du hast einen <yellow>unsichtbaren Itemframe <gray>erhalten.");
      return;
    }
    give(player, clicked);
  }

  private void clickLights(Player player, int slot) {
    if (slot == 17) {
      nav(player);
      open(player);
      return;
    }
    if (slot < 0 || slot > 15) return;
    giveCommand(player, "minecraft:light[block_state={level:\"" + slot + "\"}] 1");
    context
        .messages()
        .action(player, "<gray>Du hast Licht-Level <yellow>" + slot + " <gray>erhalten.");
  }

  private void clickSimpleSubmenu(Player player, int slot, ItemStack clicked) {
    if (slot == 22) {
      nav(player);
      open(player);
      return;
    }
    give(player, clicked);
  }

  private void clickMinecarts(Player player, int slot, ItemStack clicked) {
    if (slot == 22) {
      nav(player);
      open(player);
      return;
    }
    if (slot == 10) {
      giveCommand(
          player,
          "minecart[entity_data={id:\"minecraft:minecart\",Tags:[\""
              + FIXED_MINECART_TAG
              + "\"],NoGravity:1b,Invulnerable:1b,Motion:[0.0d,0.0d,0.0d]}] 1");
      context.messages().action(player, "<gray>Du hast eine <white>fixierte Lore <gray>erhalten.");
      return;
    }
    give(player, clicked);
  }

  private void give(Player player, ItemStack displayItem) {
    click(player);
    ItemStack item = displayItem.clone();
    ItemMeta meta = item.getItemMeta();
    meta.displayName(null);
    meta.lore(null);
    item.setItemMeta(meta);
    player.getInventory().addItem(item);
    context
        .messages()
        .action(player, "<gray>Du hast <green>" + readable(item.getType()) + " <gray>erhalten.");
  }

  private void giveCommand(Player player, String itemArgument) {
    click(player);
    Bukkit.dispatchCommand(
        Bukkit.getConsoleSender(), "minecraft:give " + player.getName() + " " + itemArgument);
  }

  @EventHandler(ignoreCancelled = true)
  public void placeExtendedPiston(BlockPlaceEvent event) {
    ItemMeta meta = event.getItemInHand().getItemMeta();
    if (meta == null
        || !meta.getPersistentDataContainer().has(pistonToolKey, PersistentDataType.BYTE)) return;
    Block base = event.getBlockPlaced();
    if (!(base.getBlockData() instanceof Piston piston)) return;
    BlockFace facing = piston.getFacing();
    Block headBlock = base.getRelative(facing);
    if (!headBlock.isPassable()) {
      event.setCancelled(true);
      context
          .messages()
          .send(event.getPlayer(), "<red>Vor dem Piston ist nicht genug Platz für den Kopf.");
      return;
    }
    piston.setExtended(true);
    base.setBlockData(piston, false);
    headBlock.setType(Material.PISTON_HEAD, false);
    PistonHead head = (PistonHead) headBlock.getBlockData();
    head.setFacing(facing);
    headBlock.setBlockData(head, false);
  }

  @EventHandler
  public void prepareFixedMinecart(VehicleCreateEvent event) {
    if (!(event.getVehicle() instanceof Minecart minecart)
        || !minecart.getScoreboardTags().contains(FIXED_MINECART_TAG)) return;
    minecart.setGravity(false);
    minecart.setInvulnerable(true);
    minecart.setMaxSpeed(0.0D);
    minecart.setVelocity(new org.bukkit.util.Vector());
  }

  @EventHandler
  public void preventFixedMinecartEntry(VehicleEnterEvent event) {
    if (event.getVehicle().getScoreboardTags().contains(FIXED_MINECART_TAG))
      event.setCancelled(true);
  }

  @EventHandler
  public void lockFixedMinecart(VehicleMoveEvent event) {
    if (!(event.getVehicle() instanceof Minecart minecart)
        || !minecart.getScoreboardTags().contains(FIXED_MINECART_TAG)) return;
    minecart.setVelocity(new org.bukkit.util.Vector());
    if (event.getFrom().distanceSquared(event.getTo()) > 0.000001D)
      minecart.teleport(event.getFrom());
  }

  private Inventory create(MenuHolder holder, int size, String title, Material backgroundMaterial) {
    Inventory inventory = Bukkit.createInventory(holder, size, context.messages().parse(title));
    holder.inventory = inventory;
    ItemStack background = named(backgroundMaterial, " ");
    for (int slot = 0; slot < size; slot++) inventory.setItem(slot, background);
    return inventory;
  }

  private void show(Player player, Inventory inventory) {
    player.openInventory(inventory);
    context.messages().sound(player, Messages.UiSound.OPEN);
  }

  private void click(Player player) {
    context.messages().sound(player, Messages.UiSound.CLICK);
  }

  private void nav(Player player) {
    context.messages().sound(player, Messages.UiSound.NAV);
  }

  private ItemStack named(Material material, String name) {
    return named(material, name, List.of());
  }

  private ItemStack named(Material material, String name, List<String> lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    meta.displayName(context.messages().parse(name));
    meta.lore(lore.stream().map(context.messages()::parse).toList());
    item.setItemMeta(meta);
    return item;
  }

  private static String readable(Material material) {
    return material.name().toLowerCase().replace('_', ' ');
  }

  private enum MenuType {
    MAIN,
    LIGHTS,
    COMMAND_BLOCKS,
    MINECARTS
  }

  private static final class MenuHolder implements InventoryHolder {
    private final MenuType type;
    private Inventory inventory;

    private MenuHolder(MenuType type) {
      this.type = type;
    }

    @Override
    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }
}
