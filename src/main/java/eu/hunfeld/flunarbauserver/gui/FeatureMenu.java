package eu.hunfeld.flunarbauserver.gui;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.database.FeatureRepository;
import eu.hunfeld.flunarbauserver.service.FeatureService;
import eu.hunfeld.flunarbauserver.utils.UiSound;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public final class FeatureMenu extends AbstractMenu implements Listener {
  private static final Map<String, Material> ICONS = icons();
  private final FeatureService service;

  public FeatureMenu(BauserverContext context, FeatureService service) {
    super(context);
    this.service = service;
  }

  public void open(Player player) {
    Holder holder = new Holder();
    Inventory inventory =
        Bukkit.createInventory(holder, 54, context.messages().parse("<gold><bold>Feature-Toggles"));
    holder.inventory = inventory;
    for (int slot = 0; slot < FeatureRepository.NAMES.size(); slot++) {
      String feature = FeatureRepository.NAMES.get(slot);
      boolean active = service.enabled(feature);
      List<String> lore = new ArrayList<>(description(feature));
      lore.add("");
      lore.add("<gray>Status: " + (active ? "<green>✔ Aktiviert" : "<red>✖ Deaktiviert"));
      lore.add("");
      lore.add("<gray>Klick zum Umschalten");
      inventory.setItem(
          slot,
          named(ICONS.getOrDefault(feature, Material.STONE), "<yellow><bold>" + feature, lore));
    }
    for (int slot = 45; slot <= 53; slot++)
      inventory.setItem(slot, named(Material.GRAY_STAINED_GLASS_PANE, " ", List.of()));
    inventory.setItem(
        47,
        named(
            Material.LIME_CONCRETE,
            "<green><bold>Alle aktivieren",
            List.of("<gray>Schaltet alle Features auf <green>allow<gray>.")));
    inventory.setItem(
        49,
        named(
            Material.BOOK,
            "<yellow><bold>Standard einstellen",
            List.of("<gray>Setzt alle Features auf die", "<gray>Bauserver-Standardwerte zurück.")));
    inventory.setItem(
        51,
        named(
            Material.RED_CONCRETE,
            "<red><bold>Alle deaktivieren",
            List.of("<gray>Schaltet alle Features auf <red>disallow<gray>.")));
    player.openInventory(inventory);
  }

  @EventHandler
  public void click(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder(false) instanceof Holder)) return;
    event.setCancelled(true);
    if (!(event.getWhoClicked() instanceof Player player)
        || !player.hasPermission("bauserver.srbuilder")) return;
    int slot = event.getRawSlot();
    if (slot == 47) {
      Map<String, Boolean> values = new LinkedHashMap<>();
      FeatureRepository.NAMES.forEach(name -> values.put(name, true));
      saveAll(
          player,
          values,
          UiSound.TOGGLE_ON,
          "<gray>Alle Features wurden <green>aktiviert<gray>.");
      return;
    }
    if (slot == 49) {
      Map<String, Boolean> values = new LinkedHashMap<>();
      FeatureRepository.NAMES.forEach(
          name -> values.put(name, FeatureRepository.defaultEnabled(name)));
      saveAll(
          player,
          values,
          UiSound.CONFIRM,
          "<gray>Alle Features wurden auf <yellow>Standard<gray> gesetzt.");
      return;
    }
    if (slot == 51) {
      Map<String, Boolean> values = new LinkedHashMap<>();
      FeatureRepository.NAMES.forEach(name -> values.put(name, false));
      saveAll(
          player,
          values,
          UiSound.TOGGLE_OFF,
          "<gray>Alle Features wurden <red>deaktiviert<gray>.");
      return;
    }
    if (slot < 0 || slot >= FeatureRepository.NAMES.size()) return;
    String feature = FeatureRepository.NAMES.get(slot);
    boolean active = !service.enabled(feature);
    context
        .features()
        .set(feature, active)
        .whenComplete(
            (ok, error) ->
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> {
                          if (!Boolean.TRUE.equals(ok)) {
                            context
                                .messages()
                                .send(player, "<red>Feature konnte nicht gespeichert werden.");
                            UiSound.ERROR.play(player);
                            return;
                          }
                          service.applyAll();
                          (active ? UiSound.TOGGLE_ON : UiSound.TOGGLE_OFF).play(player);
                          context
                              .messages()
                              .action(
                                  player,
                                  "<yellow>"
                                      + feature
                                      + (active ? " <green>aktiviert" : " <red>deaktiviert"));
                          open(player);
                        }));
  }

  private void saveAll(
      Player player, Map<String, Boolean> values, UiSound sound, String message) {
    context
        .features()
        .setAll(values)
        .whenComplete(
            (ok, error) ->
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> {
                          if (!Boolean.TRUE.equals(ok)) {
                            context
                                .messages()
                                .send(player, "<red>Features konnten nicht gespeichert werden.");
                            UiSound.ERROR.play(player);
                            return;
                          }
                          service.applyAll();
                          sound.play(player);
                          context.messages().action(player, message);
                          open(player);
                        }));
  }

  private static Map<String, Material> icons() {
    Map<String, Material> icons = new LinkedHashMap<>();
    icons.put("damage", Material.DIAMOND_SWORD);
    icons.put("break", Material.IRON_PICKAXE);
    icons.put("place", Material.GRASS_BLOCK);
    icons.put("inventory", Material.CHEST);
    icons.put("pickup", Material.HOPPER);
    icons.put("hunger", Material.COOKED_BEEF);
    icons.put("weather", Material.SUNFLOWER);
    icons.put("craft", Material.CRAFTING_TABLE);
    icons.put("drop", Material.DROPPER);
    icons.put("farmland", Material.FARMLAND);
    icons.put("explosion", Material.TNT);
    icons.put("blockbreak", Material.WOODEN_PICKAXE);
    icons.put("blockplace", Material.BRICKS);
    icons.put("blockdamage", Material.SHIELD);
    icons.put("mobspawn", Material.ZOMBIE_HEAD);
    icons.put("summon", Material.PIG_SPAWN_EGG);
    icons.put("operator", Material.COMMAND_BLOCK);
    icons.put("firespread", Material.FLINT_AND_STEEL);
    icons.put("leafdecay", Material.OAK_LEAVES);
    icons.put("liquidflow", Material.WATER_BUCKET);
    icons.put("itemdamage", Material.ANVIL);
    icons.put("portal", Material.OBSIDIAN);
    icons.put("gravity", Material.SAND);
    icons.put("falldamage", Material.FEATHER);
    icons.put("daynight", Material.CLOCK);
    icons.put("commandblocks", Material.REPEATING_COMMAND_BLOCK);
    icons.put("mobgriefing", Material.CREEPER_HEAD);
    icons.put("keepinventory", Material.TOTEM_OF_UNDYING);
    icons.put("locatorbar", Material.COMPASS);
    icons.put("advancements", Material.KNOWLEDGE_BOOK);
    return Map.copyOf(icons);
  }

  private static List<String> description(String feature) {
    return switch (feature) {
      case "damage" -> List.of("<gray>Erlaubt Schaden an Spielern", "<gray>und anderen Entities.");
      case "break" -> List.of("<gray>Erlaubt allgemeines Abbauen", "<gray>von Blöcken.");
      case "place" -> List.of("<gray>Erlaubt allgemeines Platzieren", "<gray>von Blöcken.");
      case "inventory" ->
          List.of("<gray>Erlaubt die Benutzung", "<gray>von Inventaren und Containern.");
      case "pickup" -> List.of("<gray>Erlaubt das Aufheben", "<gray>herumliegender Items.");
      case "hunger" -> List.of("<gray>Aktiviert Hunger und", "<gray>Sättigungsverlust.");
      case "weather" -> List.of("<gray>Aktiviert den natürlichen", "<gray>Wetterwechsel.");
      case "craft" -> List.of("<gray>Erlaubt das Herstellen", "<gray>von Gegenständen.");
      case "drop" -> List.of("<gray>Erlaubt Spielern, Items", "<gray>aus dem Inventar zu werfen.");
      case "farmland" -> List.of("<gray>Erlaubt das Zertrampeln", "<gray>von Ackerboden.");
      case "explosion" -> List.of("<gray>Erlaubt Explosionen", "<gray>und deren Blockschaden.");
      case "blockbreak" ->
          List.of("<gray>Erlaubt BlockBreakEvents", "<gray>für Builder-Werkzeuge.");
      case "blockplace" ->
          List.of("<gray>Erlaubt BlockPlaceEvents", "<gray>für Builder-Werkzeuge.");
      case "blockdamage" ->
          List.of("<gray>Erlaubt das beginnende", "<gray>Beschädigen von Blöcken.");
      case "mobspawn" -> List.of("<gray>Erlaubt das natürliche", "<gray>Spawnen von Mobs.");
      case "summon" -> List.of("<gray>Erlaubt das Beschwören", "<gray>von Entities.");
      case "operator" -> List.of("<gray>Erlaubt Operator-Aktionen", "<gray>und geschützte Blöcke.");
      case "firespread" ->
          List.of("<gray>Erlaubt Feuer, sich auf", "<gray>benachbarte Blöcke auszubreiten.");
      case "leafdecay" -> List.of("<gray>Aktiviert den natürlichen", "<gray>Zerfall von Blättern.");
      case "liquidflow" -> List.of("<gray>Erlaubt Wasser und Lava", "<gray>zu fließen.");
      case "itemdamage" ->
          List.of("<gray>Aktiviert Haltbarkeitsverlust", "<gray>bei benutzten Items.");
      case "portal" -> List.of("<gray>Erlaubt das Aktivieren und", "<gray>Benutzen von Portalen.");
      case "gravity" ->
          List.of("<gray>Aktiviert das Fallen von", "<gray>Sand, Kies und ähnlichen Blöcken.");
      case "falldamage" -> List.of("<gray>Aktiviert Fallschaden", "<gray>für Spieler.");
      case "daynight" -> List.of("<gray>Aktiviert den natürlichen", "<gray>Tag-Nacht-Wechsel.");
      case "commandblocks" ->
          List.of("<gray>Aktiviert Ausgaben und", "<gray>Funktionen von Commandblöcken.");
      case "mobgriefing" ->
          List.of("<gray>Erlaubt Mobs, Blöcke", "<gray>und die Welt zu verändern.");
      case "keepinventory" -> List.of("<gray>Behält Inventar und Erfahrung", "<gray>nach dem Tod.");
      case "locatorbar" -> List.of("<gray>Zeigt Spieler auf der", "<gray>Vanilla-Locator-Bar an.");
      case "advancements" -> List.of("<gray>Zeigt Fortschrittsmeldungen", "<gray>im Chat an.");
      default -> List.of("<gray>Bauserver-Feature.");
    };
  }

  private static final class Holder implements InventoryHolder {
    private Inventory inventory;

    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }
}
