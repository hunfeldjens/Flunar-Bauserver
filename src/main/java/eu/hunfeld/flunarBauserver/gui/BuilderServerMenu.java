package eu.hunfeld.flunarBauserver.gui;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.utils.Messages;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/** Zentrale, permissionsabhängige Navigation zu allen Bauserver-GUIs. */
public final class BuilderServerMenu implements Listener {
  private final BauserverContext context;
  private final ProjectMenu projects;
  private final ProjectInfoMenu projectInfos;
  private final ToolsMenu tools;
  private final FeatureMenu features;
  private final BackupMenu backups;
  private final OnlineTimeMenu onlineTime;
  private final ModerationHistoryMenu moderationHistory;
  private final NamespacedKey menuItemKey;

  public BuilderServerMenu(
      BauserverContext context,
      ProjectMenu projects,
      ProjectInfoMenu projectInfos,
      ToolsMenu tools,
      FeatureMenu features,
      BackupMenu backups,
      OnlineTimeMenu onlineTime,
      ModerationHistoryMenu moderationHistory) {
    this.context = context;
    this.projects = projects;
    this.projectInfos = projectInfos;
    this.tools = tools;
    this.features = features;
    this.backups = backups;
    this.onlineTime = onlineTime;
    this.moderationHistory = moderationHistory;
    this.menuItemKey = new NamespacedKey(context.plugin(), "bauserver_menu");
  }

  public void give(Player player) {
    ItemStack item =
        named(
            Material.NETHER_STAR,
            "<aqua><bold>Bauserver-Menü",
            List.of("<gray>Rechtsklick öffnet alle", "<gray>für dich verfügbaren Menüs."));
    ItemMeta meta = item.getItemMeta();
    meta.getPersistentDataContainer().set(menuItemKey, PersistentDataType.BYTE, (byte) 1);
    item.setItemMeta(meta);
    Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
    if (overflow.isEmpty()) {
      context.messages().action(player, "<green>Bauserver-Menü erhalten.");
      context.messages().sound(player, Messages.UiSound.CONFIRM);
    } else context.messages().send(player, "<red>Dein Inventar ist voll.");
  }

  public void open(Player player) {
    Map<Integer, Destination> destinations = new LinkedHashMap<>();
    Holder holder = new Holder(destinations);
    Inventory inventory =
        Bukkit.createInventory(holder, 27, context.messages().parse("<aqua><bold>Bauserver-Menüs"));
    holder.inventory = inventory;
    for (int slot = 0; slot < inventory.getSize(); slot++) inventory.setItem(slot, filler());
    add(player, inventory, destinations, 10, Destination.PROJECTS);
    add(player, inventory, destinations, 11, Destination.PROJECT_INFOS);
    add(player, inventory, destinations, 12, Destination.TOOLS);
    add(player, inventory, destinations, 14, Destination.PROJECT_ADMIN);
    add(player, inventory, destinations, 15, Destination.FEATURES);
    add(player, inventory, destinations, 16, Destination.BACKUP);
    add(player, inventory, destinations, 19, Destination.BAN_HISTORY);
    add(player, inventory, destinations, 20, Destination.KICK_HISTORY);
    add(player, inventory, destinations, 22, Destination.ONLINE_TIME);
    inventory.setItem(26, named(Material.BARRIER, "<red>Schließen", List.of()));
    player.openInventory(inventory);
    context.messages().sound(player, Messages.UiSound.OPEN);
  }

  public boolean open(Player player, String requested) {
    Destination destination = Destination.byName(requested);
    if (destination == null) return false;
    open(player, destination);
    return true;
  }

  public List<String> available(Player player) {
    return java.util.Arrays.stream(Destination.values())
        .filter(destination -> player.hasPermission(destination.permission))
        .map(destination -> destination.commandName)
        .toList();
  }

  @EventHandler
  public void interact(PlayerInteractEvent event) {
    if (event.getHand() != EquipmentSlot.HAND
        || (event.getAction() != Action.RIGHT_CLICK_AIR
            && event.getAction() != Action.RIGHT_CLICK_BLOCK)
        || !isMenuItem(event.getItem())) return;
    event.setCancelled(true);
    open(event.getPlayer());
  }

  @EventHandler
  public void click(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder(false) instanceof Holder holder)) return;
    event.setCancelled(true);
    if (!(event.getWhoClicked() instanceof Player player)) return;
    if (event.getRawSlot() == 26) {
      player.closeInventory();
      return;
    }
    Destination destination = holder.destinations.get(event.getRawSlot());
    if (destination != null) open(player, destination);
  }

  private void add(
      Player player,
      Inventory inventory,
      Map<Integer, Destination> destinations,
      int slot,
      Destination destination) {
    if (!player.hasPermission(destination.permission)) return;
    inventory.setItem(slot, named(destination.icon, destination.title, destination.lore));
    destinations.put(slot, destination);
  }

  private void open(Player player, Destination destination) {
    if (!player.hasPermission(destination.permission)) {
      context.messages().noPermission(player);
      return;
    }
    context.messages().sound(player, Messages.UiSound.CLICK);
    switch (destination) {
      case PROJECTS -> projects.open(player, 0, false);
      case PROJECT_INFOS -> projectInfos.open(player, "");
      case TOOLS -> tools.open(player);
      case PROJECT_ADMIN -> projects.open(player, 0, true);
      case FEATURES -> features.open(player);
      case BACKUP -> backups.open(player);
      case ONLINE_TIME -> onlineTime.open(player, 1);
      case BAN_HISTORY -> moderationHistory.open(player, ModerationHistoryMenu.Type.BAN, 1);
      case KICK_HISTORY -> moderationHistory.open(player, ModerationHistoryMenu.Type.KICK, 1);
    }
  }

  private boolean isMenuItem(ItemStack item) {
    if (item == null || !item.hasItemMeta()) return false;
    return item.getItemMeta()
        .getPersistentDataContainer()
        .has(menuItemKey, PersistentDataType.BYTE);
  }

  private ItemStack filler() {
    return named(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
  }

  private ItemStack named(Material material, String name, List<String> lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    meta.displayName(context.messages().parse(name));
    if (!lore.isEmpty()) meta.lore(lore.stream().map(context.messages()::parse).toList());
    item.setItemMeta(meta);
    return item;
  }

  private enum Destination {
    PROJECTS(
        "projekte",
        "bauserver.team",
        Material.GRASS_BLOCK,
        "<green><bold>Projekte",
        List.of("<gray>Öffnet die Projektübersicht.")),
    PROJECT_INFOS(
        "infos",
        "bauserver.builder",
        Material.OAK_SIGN,
        "<dark_green><bold>Projekt-Infos",
        List.of("<gray>Zeigt Infos aller Projekte.")),
    TOOLS(
        "tools",
        "bauserver.builder",
        Material.DEBUG_STICK,
        "<gold><bold>Operator-Items",
        List.of("<gray>Öffnet das Tools-Menü.")),
    PROJECT_ADMIN(
        "projektadmin",
        "bauserver.srbuilder",
        Material.COMMAND_BLOCK,
        "<red><bold>Projekt-Administration",
        List.of("<gray>Verwaltet Projekte und Welten.")),
    FEATURES(
        "features",
        "bauserver.srbuilder",
        Material.COMPARATOR,
        "<yellow><bold>Feature-Toggles",
        List.of("<gray>Öffnet die Feature-Einstellungen.")),
    BACKUP(
        "backup",
        "bauserver.builder",
        Material.ENDER_CHEST,
        "<aqua><bold>Backup",
        List.of("<gray>Öffnet das Backup-Menü.")),
    ONLINE_TIME(
        "onlinezeit",
        "bauserver.admin",
        Material.CLOCK,
        "<red><bold>Onlinezeiten",
        List.of("<gray>Öffnet die Onlinezeit-Verwaltung.")),
    BAN_HISTORY(
        "banhistory",
        "bauserver.team",
        Material.PLAYER_HEAD,
        "<red><bold>Ban-Historie",
        List.of("<gray>Zeigt alle gespeicherten Bans.")),
    KICK_HISTORY(
        "kickhistory",
        "bauserver.team",
        Material.LEATHER_BOOTS,
        "<gold><bold>Kick-Historie",
        List.of("<gray>Zeigt alle gespeicherten Kicks."));

    private final String commandName;
    private final String permission;
    private final Material icon;
    private final String title;
    private final List<String> lore;

    Destination(
        String commandName, String permission, Material icon, String title, List<String> lore) {
      this.commandName = commandName;
      this.permission = permission;
      this.icon = icon;
      this.title = title;
      this.lore = lore;
    }

    private static Destination byName(String value) {
      String normalized = value.toLowerCase(Locale.ROOT);
      return java.util.Arrays.stream(values())
          .filter(destination -> destination.commandName.equals(normalized))
          .findFirst()
          .orElse(null);
    }
  }

  private static final class Holder implements InventoryHolder {
    private final Map<Integer, Destination> destinations;
    private Inventory inventory;

    private Holder(Map<Integer, Destination> destinations) {
      this.destinations = destinations;
    }

    @Override
    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }
}
