package eu.hunfeld.flunarbauserver.gui;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.model.OnlineTimeRecord;
import eu.hunfeld.flunarbauserver.service.OnlineTimeService;
import eu.hunfeld.flunarbauserver.utils.UiSound;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

/** Admin-GUI aus 08_onlinetime.sk. */
public final class OnlineTimeMenu extends AbstractMenu implements Listener {
  private static final int PAGE_SIZE = 45;
  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final OnlineTimeService service;

  public OnlineTimeMenu(BauserverContext context, OnlineTimeService service) {
    super(context);
    this.service = service;
  }

  public void open(Player player, int requestedPage) {
    List<View> entries = new ArrayList<>();
    for (OnlineTimeRecord record : context.onlineTime().all()) entries.add(view(record));
    entries.sort(Comparator.comparingInt(View::total).reversed());
    int pages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    int page = Math.max(1, Math.min(requestedPage, pages));
    OverviewHolder holder = new OverviewHolder(page, pages, List.copyOf(entries));
    Inventory inventory =
        Bukkit.createInventory(
            holder,
            54,
            context.messages().parse("Onlinezeiten (Seite " + page + "/" + pages + ")"));
    holder.inventory = inventory;
    int start = (page - 1) * PAGE_SIZE;
    for (int slot = 0; slot < PAGE_SIZE && start + slot < entries.size(); slot++)
      inventory.setItem(slot, playerHead(entries.get(start + slot), true));
    for (int slot = 45; slot <= 53; slot++) inventory.setItem(slot, filler());
    if (page > 1)
      inventory.setItem(
          45,
          named(Material.ARROW, "<yellow>◀ Vorherige Seite", List.of("<gray>Seite " + (page - 1))));
    inventory.setItem(
        49,
        named(
            Material.PAPER,
            "<gold>Seite " + page + " / " + pages,
            List.of("<gray>" + entries.size() + " Spieler insgesamt")));
    if (page < pages)
      inventory.setItem(
          53,
          named(Material.ARROW, "<yellow>Nächste Seite ▶", List.of("<gray>Seite " + (page + 1))));
    show(player, inventory);
  }

  private void detail(Player player, int page, View entry) {
    DetailHolder holder = new DetailHolder(page, entry);
    Inventory inventory = filled(holder, "<red>Onlinezeit: " + entry.record.name());
    inventory.setItem(4, playerHead(entry, false));
    inventory.setItem(
        11,
        named(
            Material.ORANGE_CONCRETE,
            "<gold><bold>Zeiten zurücksetzen",
            List.of(
                "<gray>Setzt Aktiv-, AFK-Zeit und",
                "<gray>Verbindungen auf <white>0<gray>.",
                "<dark_gray>Der Spieler bleibt registriert.",
                "",
                "<red>⚠ Mit Sicherheitsabfrage")));
    inventory.setItem(
        15,
        named(
            Material.TNT,
            "<red><bold>Komplett löschen",
            List.of(
                "<gray>Entfernt den Spieler vollständig",
                "<gray>aus <white>onlinetime <gray>und <white>player_data<gray>.",
                "",
                "<red>⚠ Mit Sicherheitsabfrage")));
    inventory.setItem(22, named(Material.ARROW, "<yellow>◀ Zurück zur Übersicht", List.of()));
    show(player, inventory);
  }

  private void confirm(Player player, int page, View entry, Action action) {
    ConfirmHolder holder = new ConfirmHolder(page, entry, action);
    String title =
        action == Action.DELETE
            ? "<red>Spieler wirklich löschen?"
            : "<gold>Zeiten wirklich zurücksetzen?";
    Inventory inventory = filled(holder, title);
    List<String> question =
        action == Action.DELETE
            ? List.of(
                "<gray>Spieler <red>komplett aus der Datenbank",
                "<gray>löschen? <dark_gray>(onlinetime + player_data)",
                "",
                "<red>Das kann nicht rückgängig gemacht werden!")
            : List.of(
                "<gray>Aktiv-, AFK-Zeit und Verbindungen",
                "<gray>auf <white>0 <gray>zurücksetzen?",
                "",
                "<red>Das kann nicht rückgängig gemacht werden!");
    inventory.setItem(
        13, named(Material.OAK_SIGN, "<white><bold>" + entry.record.name(), question));
    inventory.setItem(
        11,
        named(
            Material.LIME_CONCRETE,
            action == Action.DELETE ? "<green><bold>Ja, löschen" : "<green><bold>Ja, zurücksetzen",
            List.of()));
    inventory.setItem(
        15,
        named(
            Material.RED_CONCRETE, "<red><bold>Abbrechen", List.of("<gray>Zurück zur Verwaltung")));
    show(player, inventory);
  }

  @EventHandler
  public void click(InventoryClickEvent event) {
    InventoryHolder holder = event.getInventory().getHolder(false);
    if (!(holder instanceof OverviewHolder)
        && !(holder instanceof DetailHolder)
        && !(holder instanceof ConfirmHolder)) return;
    event.setCancelled(true);
    if (!(event.getWhoClicked() instanceof Player player)
        || !player.hasPermission("bauserver.admin")) return;
    int slot = event.getRawSlot();
    if (slot < 0 || slot >= event.getInventory().getSize()) return;

    if (holder instanceof OverviewHolder overview) {
      if (slot == 45 && overview.page > 1) {
        UiSound.NAV.play(player);
        open(player, overview.page - 1);
      } else if (slot == 53 && overview.page < overview.pages) {
        UiSound.NAV.play(player);
        open(player, overview.page + 1);
      } else if (slot < PAGE_SIZE) {
        int index = (overview.page - 1) * PAGE_SIZE + slot;
        if (index < overview.entries.size()) {
          UiSound.CLICK.play(player);
          detail(player, overview.page, overview.entries.get(index));
        }
      }
      return;
    }
    if (holder instanceof DetailHolder detail) {
      if (slot == 11) {
        UiSound.CLICK.play(player);
        confirm(player, detail.page, detail.entry, Action.RESET);
      } else if (slot == 15) {
        UiSound.CLICK.play(player);
        confirm(player, detail.page, detail.entry, Action.DELETE);
      } else if (slot == 22) {
        UiSound.NAV.play(player);
        open(player, detail.page);
      }
      return;
    }
    ConfirmHolder confirm = (ConfirmHolder) holder;
    if (slot == 15) {
      UiSound.CANCEL.play(player);
      detail(player, confirm.page, confirm.entry);
      return;
    }
    if (slot != 11) return;
    UiSound.CONFIRM.play(player);
    var future =
        confirm.action == Action.DELETE
            ? context.onlineTime().delete(confirm.entry.record.uuid())
            : context.onlineTime().reset(confirm.entry.record.uuid());
    future.whenComplete(
        (success, error) ->
            Bukkit.getScheduler()
                .runTask(
                    context.plugin(),
                    () -> {
                      if (!player.isOnline()) return;
                      if (error != null || !Boolean.TRUE.equals(success)) {
                        context
                            .messages()
                            .send(player, "<red>Die Aktion konnte nicht gespeichert werden.");
                        return;
                      }
                      service.resetSession(confirm.entry.record.uuid());
                      if (confirm.action == Action.DELETE)
                        context
                            .messages()
                            .send(
                                player,
                                "<gray>Spieler <green>"
                                    + confirm.entry.record.name()
                                    + " <gray>wurde <red>komplett <gray>aus der Datenbank gelöscht.");
                      else
                        context
                            .messages()
                            .send(
                                player,
                                "<gray>Onlinezeit von <green>"
                                    + confirm.entry.record.name()
                                    + " <gray>wurde zurückgesetzt.");
                      open(player, confirm.page);
                    }));
  }

  private View view(OnlineTimeRecord record) {
    int active = record.activeSeconds() + service.sessionActive(record.uuid());
    int afk = record.afkSeconds() + service.sessionAfk(record.uuid());
    return new View(record, active, afk, active + afk);
  }

  private ItemStack playerHead(View entry, boolean clickable) {
    List<String> lore = new ArrayList<>();
    lore.add("<gray>Gesamt: <green>" + full(entry.total));
    lore.add("<gray>Aktiv: <green>" + dhm(entry.active) + " <gray>| AFK: <red>" + dhm(entry.afk));
    lore.add("<gray>Verbindungen: <yellow>" + entry.record.joins());
    lore.add("<gray>Letzter Login: <yellow>" + date(entry.record.lastSeen()));
    if (clickable) {
      lore.add("");
      lore.add("<aqua>Klick: Verwalten <dark_gray>(Reset / Löschen)");
    }
    ItemStack item = named(Material.PLAYER_HEAD, "<yellow>" + entry.record.name(), lore);
    SkullMeta meta = (SkullMeta) item.getItemMeta();
    OfflinePlayer owner = Bukkit.getOfflinePlayer(entry.record.uuid());
    meta.setOwningPlayer(owner);
    item.setItemMeta(meta);
    return item;
  }

  private Inventory filled(InventoryHolder holder, String title) {
    Inventory inventory = Bukkit.createInventory(holder, 27, context.messages().parse(title));
    if (holder instanceof DetailHolder detail) detail.inventory = inventory;
    if (holder instanceof ConfirmHolder confirm) confirm.inventory = inventory;
    for (int slot = 0; slot < inventory.getSize(); slot++) inventory.setItem(slot, filler());
    return inventory;
  }

  private ItemStack filler() {
    return named(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
  }

  private void show(Player player, Inventory inventory) {
    player.openInventory(inventory);
    UiSound.OPEN.play(player);
  }

  public static String dhm(int seconds) {
    int total = Math.max(0, seconds);
    int days = total / 86_400;
    int hours = total % 86_400 / 3_600;
    int minutes = total % 3_600 / 60;
    return days + "d " + hours + "h " + String.format(java.util.Locale.ROOT, "%02d", minutes) + "m";
  }

  public static String teamTime(int seconds) {
    int total = Math.max(0, seconds);
    return total / 86_400
        + " Tage, "
        + total % 86_400 / 3_600
        + " Stunden, "
        + total % 3_600 / 60
        + " Minuten";
  }

  public static String full(int seconds) {
    int total = Math.max(0, seconds);
    int months = total / 2_592_000;
    int rest = total % 2_592_000;
    int days = rest / 86_400;
    int hours = rest % 86_400 / 3_600;
    int minutes = rest % 3_600 / 60;
    if (months > 0)
      return months
          + " Monat(e) "
          + days
          + "d "
          + hours
          + "h "
          + String.format(java.util.Locale.ROOT, "%02d", minutes)
          + "m";
    if (days > 0)
      return days
          + "d "
          + hours
          + "h "
          + String.format(java.util.Locale.ROOT, "%02d", minutes)
          + "m";
    return hours + "h " + String.format(java.util.Locale.ROOT, "%02d", minutes) + "m";
  }

  public static String date(java.time.LocalDateTime date) {
    return date == null ? "-" : DATE.format(date);
  }

  private record View(OnlineTimeRecord record, int active, int afk, int total) {}

  private enum Action {
    RESET,
    DELETE
  }

  private static final class OverviewHolder implements InventoryHolder {
    private final int page;
    private final int pages;
    private final List<View> entries;
    private Inventory inventory;

    private OverviewHolder(int page, int pages, List<View> entries) {
      this.page = page;
      this.pages = pages;
      this.entries = entries;
    }

    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }

  private static final class DetailHolder implements InventoryHolder {
    private final int page;
    private final View entry;
    private Inventory inventory;

    private DetailHolder(int page, View entry) {
      this.page = page;
      this.entry = entry;
    }

    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }

  private static final class ConfirmHolder implements InventoryHolder {
    private final int page;
    private final View entry;
    private final Action action;
    private Inventory inventory;

    private ConfirmHolder(int page, View entry, Action action) {
      this.page = page;
      this.entry = entry;
      this.action = action;
    }

    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }
}
