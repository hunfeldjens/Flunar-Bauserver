package eu.hunfeld.flunarBauserver.gui;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.model.ModerationHistoryPage;
import eu.hunfeld.flunarBauserver.model.ModerationRecord;
import eu.hunfeld.flunarBauserver.utils.Messages;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

/** Seitenweise Ban- und Kick-Historie mit einer gebündelten Datenbankabfrage pro Seite. */
public final class ModerationHistoryMenu implements Listener {
  private static final int PAGE_SIZE = 45;
  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
  private final BauserverContext context;

  public ModerationHistoryMenu(BauserverContext context) {
    this.context = context;
  }

  public void open(Player player, Type type, int requestedPage) {
    context.messages().action(player, "<gray>Historie wird geladen …");
    var future =
        type == Type.BAN
            ? context.moderation().banHistory(requestedPage, PAGE_SIZE)
            : context.moderation().kickHistory(requestedPage, PAGE_SIZE);
    future.whenComplete(
        (page, error) ->
            Bukkit.getScheduler()
                .runTask(
                    context.plugin(),
                    () -> {
                      if (!player.isOnline()) return;
                      if (error != null || page == null) {
                        context
                            .messages()
                            .send(player, "<red>Die Historie konnte nicht geladen werden.");
                        context.messages().sound(player, Messages.UiSound.ERROR);
                        return;
                      }
                      show(player, type, page);
                    }));
  }

  private void show(Player player, Type type, ModerationHistoryPage page) {
    Holder holder = new Holder(type, page.page(), page.pages());
    String name = type == Type.BAN ? "Ban-Historie" : "Kick-Historie";
    Inventory inventory =
        Bukkit.createInventory(
            holder,
            54,
            context
                .messages()
                .parse("<dark_red>" + name + " <gray>(" + page.page() + "/" + page.pages() + ")"));
    holder.inventory = inventory;
    for (int slot = 0; slot < page.entries().size(); slot++)
      inventory.setItem(slot, head(type, page.entries().get(slot)));
    if (page.entries().isEmpty())
      inventory.setItem(
          22,
          named(
              Material.BARRIER,
              "<red>Keine Einträge vorhanden",
              List.of("<gray>In dieser Historie ist noch nichts gespeichert.")));
    for (int slot = 45; slot < 54; slot++) inventory.setItem(slot, filler());
    if (page.page() > 1)
      inventory.setItem(
          45,
          named(
              Material.ARROW,
              "<yellow>◀ Vorherige Seite",
              List.of("<gray>Seite " + (page.page() - 1))));
    inventory.setItem(
        49,
        named(
            Material.PAPER,
            "<gold>Seite " + page.page() + " / " + page.pages(),
            List.of("<gray>" + page.totalEntries() + " Einträge insgesamt")));
    if (page.page() < page.pages())
      inventory.setItem(
          53,
          named(
              Material.ARROW,
              "<yellow>Nächste Seite ▶",
              List.of("<gray>Seite " + (page.page() + 1))));
    player.openInventory(inventory);
    context.messages().sound(player, Messages.UiSound.OPEN);
  }

  @EventHandler
  public void click(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder(false) instanceof Holder holder)) return;
    event.setCancelled(true);
    if (!(event.getWhoClicked() instanceof Player player)
        || !player.hasPermission("bauserver.team")) return;
    int slot = event.getRawSlot();
    if (slot == 45 && holder.page > 1) {
      context.messages().sound(player, Messages.UiSound.NAV);
      open(player, holder.type, holder.page - 1);
    } else if (slot == 53 && holder.page < holder.pages) {
      context.messages().sound(player, Messages.UiSound.NAV);
      open(player, holder.type, holder.page + 1);
    }
  }

  private ItemStack head(Type type, ModerationRecord record) {
    String action = type == Type.BAN ? "Gebannt" : "Gekickt";
    List<String> lore = new ArrayList<>();
    lore.add("<gray>Name: <yellow>" + record.name());
    lore.add("<gray>" + action + " am: <white>" + date(record));
    lore.add("<gray>Von: <green>" + record.byName());
    lore.add("");
    lore.add("<gray>Grund:");
    lore.addAll(wrap(record.reason(), "<white>"));
    if (type == Type.BAN) {
      lore.add("");
      lore.add(record.active() ? "<red>● Ban ist noch aktiv" : "<green>● Ban wurde aufgehoben");
    }
    ItemStack item = named(Material.PLAYER_HEAD, "<yellow>" + record.name(), lore);
    if (record.uuid() != null) {
      SkullMeta meta = (SkullMeta) item.getItemMeta();
      OfflinePlayer owner = Bukkit.getOfflinePlayer(record.uuid());
      meta.setOwningPlayer(owner);
      item.setItemMeta(meta);
    }
    return item;
  }

  private static String date(ModerationRecord record) {
    return record.createdAt() == null ? "Unbekannt" : DATE.format(record.createdAt());
  }

  private static List<String> wrap(String text, String prefix) {
    String[] words = text.strip().split("\\s+");
    List<String> lines = new ArrayList<>();
    StringBuilder line = new StringBuilder();
    for (String word : words) {
      if (!line.isEmpty() && line.length() + word.length() + 1 > 38) {
        lines.add(prefix + line);
        line.setLength(0);
      }
      if (!line.isEmpty()) line.append(' ');
      line.append(word);
    }
    if (!line.isEmpty()) lines.add(prefix + line);
    return lines.isEmpty() ? List.of(prefix + "Kein Grund angegeben") : lines;
  }

  private ItemStack filler() {
    return named(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
  }

  private ItemStack named(Material material, String name, List<String> lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    meta.displayName(context.messages().parse(name));
    meta.lore(lore.stream().map(context.messages()::parse).toList());
    item.setItemMeta(meta);
    return item;
  }

  public enum Type {
    BAN,
    KICK
  }

  private static final class Holder implements InventoryHolder {
    private final Type type;
    private final int page;
    private final int pages;
    private Inventory inventory;

    private Holder(Type type, int page, int pages) {
      this.type = type;
      this.page = page;
      this.pages = pages;
    }

    @Override
    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }
}
