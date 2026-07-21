package eu.hunfeld.flunarbauserver.gui;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.model.ProjectInfo;
import eu.hunfeld.flunarbauserver.utils.UiSound;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;


public final class ProjectInfoMenu extends AbstractMenu implements Listener {

  public ProjectInfoMenu(BauserverContext context) {
    super(context);
  }

  public void open(Player player, String worldFilter) {
    String filter = context.worlds().cleanName(worldFilter);
    List<ProjectInfo> infos = new ArrayList<>(context.projectInfos().forWorld(filter));
    if (!filter.isBlank()) {
      Location playerLocation = player.getLocation();
      infos.sort(Comparator.comparingDouble(info -> distanceSquared(info, playerLocation)));
    }

    OverviewHolder holder = new OverviewHolder(filter, List.copyOf(infos));
    String title =
        filter.isBlank()
            ? "<dark_green>Projekt-Infos <dark_gray>(Alle Projekte)"
            : "<dark_green>Projekt-Infos <dark_gray>(" + filter + ")";
    Inventory inventory = Bukkit.createInventory(holder, 54, context.messages().parse(title));
    holder.inventory = inventory;
    for (int slot = 45; slot <= 53; slot++) inventory.setItem(slot, DECORATION_ITEM);
    inventory.setItem(
        49, named(Material.BARRIER, "<red><bold>Schließen", List.of("<gray>Klick zum Schließen")));

    if (infos.isEmpty()) {
      inventory.setItem(
          22,
          named(
              Material.PAPER,
              "<gray>Keine Infos vorhanden",
              List.of(
                  filter.isBlank()
                      ? "<dark_gray>Es wurden noch keine Projekt-Infos angelegt."
                      : "<dark_gray>Für dieses Projekt gibt es noch keine Infos.",
                  "",
                  "<gray>Hinzufügen mit:",
                  "<green>/projekt info add <Name> <Beschreibung>")));
    } else {
      for (int slot = 0; slot < Math.min(45, infos.size()); slot++)
        inventory.setItem(slot, infoItem(infos.get(slot), !filter.isBlank(), player));
    }
    show(player, inventory);
  }

  private void openDetail(Player player, String filter, ProjectInfo info) {
    DetailHolder holder = new DetailHolder(filter, info);
    Inventory inventory = filled(holder, 27, "<dark_green>Info: " + info.name());
    inventory.setItem(4, infoItem(info, false, player));
    if (hasPosition(info)) {
      inventory.setItem(
          11,
          named(
              Material.ENDER_PEARL,
              "<aqua><bold>Teleportieren",
              List.of(
                  "<gray>Zur gespeicherten Position",
                  "<gray>auf <yellow>" + info.worldName() + " <gray>teleportieren")));
    } else {
      inventory.setItem(
          11,
          named(
              Material.GRAY_DYE,
              "<gray>Kein Teleport möglich",
              List.of(
                  "<dark_gray>Für diese Info wurde keine", "<dark_gray>Position gespeichert.")));
    }
    if (canManage(player, info))
      inventory.setItem(
          15,
          named(
              Material.TNT,
              "<red><bold>Info löschen",
              List.of(
                  "<gray>Entfernt die Info aus der Datenbank.",
                  "",
                  "<red>⚠ Mit Sicherheitsabfrage")));
    else
      inventory.setItem(
          15,
          named(
              Material.BARRIER,
              "<red>Keine Löschberechtigung",
              List.of("<gray>Du bist für dieses Projekt", "<gray>nicht freigeschaltet.")));
    inventory.setItem(22, named(Material.ARROW, "<yellow>◀ Zurück zur Übersicht"));
    show(player, inventory);
  }

  private void openDelete(Player player, String filter, ProjectInfo info) {
    DeleteHolder holder = new DeleteHolder(filter, info);
    Inventory inventory = filled(holder, 27, "<red>Info löschen?");
    inventory.setItem(
        13,
        named(
            Material.OAK_SIGN,
            "<white><bold>" + info.name(),
            List.of(
                "<gray>Soll diese Info wirklich",
                "<gray>gelöscht werden?",
                "",
                "<dark_gray>Info-ID: <white>#" + info.id())));
    inventory.setItem(
        11,
        named(
            Material.LIME_CONCRETE,
            "<green><bold>Ja, löschen",
            List.of("<gray>Info endgültig aus der", "<gray>Datenbank entfernen")));
    inventory.setItem(
        15,
        named(
            Material.RED_CONCRETE, "<red><bold>Abbrechen", List.of("<gray>Zurück zur Übersicht")));
    show(player, inventory);
  }

  @EventHandler
  public void click(InventoryClickEvent event) {
    InventoryHolder holder = event.getInventory().getHolder(false);
    if (!(holder instanceof OverviewHolder)
        && !(holder instanceof DetailHolder)
        && !(holder instanceof DeleteHolder)) return;
    event.setCancelled(true);
    if (!(event.getWhoClicked() instanceof Player player)) return;
    int slot = event.getRawSlot();
    if (slot < 0 || slot >= event.getInventory().getSize()) return;

    if (holder instanceof OverviewHolder overview) {
      if (slot == 49) {
        UiSound.CLICK.play(player);
        player.closeInventory();
        return;
      }
      if (slot >= 0 && slot < Math.min(45, overview.infos.size())) {
        UiSound.CLICK.play(player);
        openDetail(player, overview.filter, overview.infos.get(slot));
      }
      return;
    }

    if (holder instanceof DetailHolder detail) {
      if (slot == 11 && hasPosition(detail.info)) teleport(player, detail.info);
      else if (slot == 15) {
        if (!canManage(player, detail.info)) {
          UiSound.ERROR.play(player);
          context.messages().send(player, "<red>Du darfst Infos dieses Projekts nicht löschen.");
          return;
        }
        UiSound.CLICK.play(player);
        openDelete(player, detail.filter, detail.info);
      } else if (slot == 22) {
        UiSound.NAV.play(player);
        open(player, detail.filter);
      }
      return;
    }

    DeleteHolder delete = (DeleteHolder) holder;
    if (slot == 11) {
      if (!canManage(player, delete.info)) {
        player.closeInventory();
        UiSound.ERROR.play(player);
        context.messages().send(player, "<red>Du darfst Infos dieses Projekts nicht löschen.");
        return;
      }
      UiSound.CONFIRM.play(player);
      context
          .projectInfos()
          .delete(delete.info.id())
          .thenAccept(
              success ->
                  Bukkit.getScheduler()
                      .runTask(
                          context.plugin(),
                          () -> {
                            if (!player.isOnline()) return;
                            if (success)
                              context
                                  .messages()
                                  .send(
                                      player,
                                      "<gray>Projekt-Info <white>#"
                                          + delete.info.id()
                                          + " <gray>wurde <red>gelöscht<gray>.");
                            else
                              context
                                  .messages()
                                  .send(
                                      player,
                                      "<red>Die Projekt-Info konnte nicht gelöscht werden.");
                            open(player, delete.filter);
                          }));
    } else if (slot == 15) {
      UiSound.CANCEL.play(player);
      openDetail(player, delete.filter, delete.info);
    }
  }

  private void teleport(Player player, ProjectInfo info) {
    if (context.backups().safeWorldLocked(info.worldName())) {
      context
          .messages()
          .action(player, "<red>Diese Welt ist während des sicheren Backups gesperrt.");
      UiSound.ERROR.play(player);
      player.closeInventory();
      return;
    }
    if (!context
        .projectAccess()
        .mayEnter(
            player.getUniqueId(), info.worldName(), player.hasPermission("bauserver.admin"))) {
      context.messages().action(player, "<red>Du hast keinen Zugriff auf dieses Projekt.");
      UiSound.ERROR.play(player);
      player.closeInventory();
      return;
    }

    player.closeInventory();
    World world;
    try {
      world =
          context
              .worlds()
              .loaded(info.worldName())
              .orElseGet(() -> context.worlds().createProject(info.worldName(), "normal"));
    } catch (RuntimeException exception) {
      context.messages().action(player, "<red>Fehler beim Laden der Welt!");
      UiSound.ERROR.play(player);
      return;
    }
    if (player.getWorld() != world) context.worlds().clearPlayer(player);
    context.teleports().remember(player);
    Location target =
        new Location(
            world,
            info.x(),
            info.y(),
            info.z(),
            info.yaw() == null ? 0 : info.yaw(),
            info.pitch() == null ? 0 : info.pitch());
    context
        .worlds()
        .teleport(player, target)
        .thenAccept(
            success ->
                Bukkit.getScheduler()
                    .runTaskLater(
                        context.plugin(),
                        () -> {
                          if (!success || !player.isOnline()) return;
                          context
                              .messages()
                              .action(
                                  player,
                                  "<gold>Teleportiert zu <yellow>"
                                      + info.name()
                                      + " <dark_gray>("
                                      + info.worldName()
                                      + ")");
                          UiSound.TELEPORT.play(player);
                        },
                        2L));
  }

  private boolean canManage(Player player, ProjectInfo info) {
    return player.hasPermission("bauserver.admin")
        || context.projects().isOwner(player.getUniqueId(), info.worldName())
        || context.projectAccess().isWhitelisted(player.getUniqueId(), info.worldName());
  }

  private ItemStack infoItem(ProjectInfo info, boolean showDistance, Player player) {
    List<String> lore = new ArrayList<>(wrap(info.description(), "<gray>"));
    lore.add("");
    lore.add("<dark_gray>Projekt: <yellow>" + info.worldName());
    lore.add("<dark_gray>Info-ID: <white>#" + info.id());
    OfflinePlayer creator = Bukkit.getOfflinePlayer(info.createdBy());
    String creatorName = creator.getName();
    if (creatorName != null) lore.add("<dark_gray>Erstellt von: <white>" + creatorName);
    if (hasPosition(info)) {
      lore.add(
          "<dark_gray>Position: <white>"
              + Math.round(info.x())
              + "<dark_gray>, <white>"
              + Math.round(info.y())
              + "<dark_gray>, <white>"
              + Math.round(info.z()));
      if (showDistance)
        lore.add(
            "<dark_gray>Entfernung: <aqua>"
                + Math.round(Math.sqrt(distanceSquared(info, player.getLocation())))
                + " Blöcke");
    } else lore.add("<dark_gray>Position: <gray>nicht gespeichert");
    lore.add("");
    lore.add("<aqua>Klick: Optionen öffnen <dark_gray>(Teleport / Löschen)");
    return named(Material.OAK_SIGN, "<green><bold>" + info.name(), lore);
  }

  private Inventory filled(InventoryHolder holder, int size, String title) {
    Inventory inventory = Bukkit.createInventory(holder, size, context.messages().parse(title));
    if (holder instanceof DetailHolder detail) detail.inventory = inventory;
    if (holder instanceof DeleteHolder delete) delete.inventory = inventory;
    for (int slot = 0; slot < size; slot++) inventory.setItem(slot, DECORATION_ITEM);
    return inventory;
  }

  private void show(Player player, Inventory inventory) {
    player.openInventory(inventory);
    UiSound.OPEN.play(player);
  }

  private static List<String> wrap(String value, String color) {
    if (value == null || value.isBlank()) return List.of(color);
    String[] words = value.trim().split("\\s+");
    List<String> lines = new ArrayList<>();
    StringBuilder line = new StringBuilder(color);
    for (int index = 0; index < words.length; index++) {
      if (index > 0 && index % 6 == 0) {
        lines.add(line.toString());
        line = new StringBuilder(color);
      }
      if (line.length() > color.length()) line.append(' ');
      line.append(words[index]);
    }
    lines.add(line.toString());
    return lines;
  }

  private static boolean hasPosition(ProjectInfo info) {
    return info.x() != null && info.y() != null && info.z() != null;
  }

  private static double distanceSquared(ProjectInfo info, Location location) {
    if (!hasPosition(info)) return Double.MAX_VALUE;
    double x = info.x() - location.x();
    double y = info.y() - location.y();
    double z = info.z() - location.z();
    return x * x + y * y + z * z;
  }

  private static final class OverviewHolder implements InventoryHolder {
    private final String filter;
    private final List<ProjectInfo> infos;
    private Inventory inventory;

    private OverviewHolder(String filter, List<ProjectInfo> infos) {
      this.filter = filter;
      this.infos = infos;
    }

    @Override
    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }

  private static final class DetailHolder implements InventoryHolder {
    private final String filter;
    private final ProjectInfo info;
    private Inventory inventory;

    private DetailHolder(String filter, ProjectInfo info) {
      this.filter = filter;
      this.info = info;
    }

    @Override
    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }

  private static final class DeleteHolder implements InventoryHolder {
    private final String filter;
    private final ProjectInfo info;
    private Inventory inventory;

    private DeleteHolder(String filter, ProjectInfo info) {
      this.filter = filter;
      this.info = info;
    }

    @Override
    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }
}
