package eu.hunfeld.flunarbauserver.gui;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.model.Project;
import eu.hunfeld.flunarbauserver.utils.UiSound;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

@SuppressWarnings({"resource", "SpellCheckingInspection"})
public final class ProjectMenu extends AbstractMenu implements Listener {
  private static final int PAGE_SIZE = 18;
  private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("[0-9a-fA-F]{6}");

  public ProjectMenu(BauserverContext context) {
    super(context);
  }

  public void open(Player player, int requestedPage, boolean admin) {
    List<Project> projects = context.projects().all();
    int pages = Math.max(1, (projects.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    int page = Math.clamp(requestedPage, 0, pages - 1);
    Holder holder = new Holder(page, admin, projects);
    Inventory inventory =
        Bukkit.createInventory(
            holder,
            27,
            context
                .messages()
                .parse(
                    admin
                        ? "<red>Bauprojekte (Admin) <dark_gray>– Seite " + (page + 1) + "/" + pages
                        : "Bauprojekte <dark_gray>– Seite " + (page + 1) + "/" + pages));
    holder.inventory = inventory;
    for (int slot = 18; slot <= 26; slot++) inventory.setItem(slot, DECORATION_ITEM);
    int start = page * PAGE_SIZE;
    for (int slot = 0; slot < PAGE_SIZE && start + slot < projects.size(); slot++) {
      Project project = projects.get(start + slot);
      Material material = Material.matchMaterial(project.icon());
      if (material == null || !material.isItem()) material = Material.MAP;
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(context.messages().parse(project.name()));
      List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
      wrapLegacy(project.description()).stream()
          .map(context.messages()::parseLegacy)
          .forEach(lore::add);
      lore.add(context.messages().parse("<dark_gray>Welt: <yellow>" + project.worldName()));
      lore.add(context.messages().parse(""));
      if (project.whitelistActive()) {
        lore.add(context.messages().parse("<dark_gray>Whitelist: <green>Aktiv"));
        if (player.hasPermission("bauserver.admin"))
          lore.add(context.messages().parse("<green>✔ Du hast Zugriff <dark_gray>(Admin)"));
        else if (player.getUniqueId().equals(project.owner()))
          lore.add(context.messages().parse("<green>✔ Du hast Zugriff <dark_gray>(Owner)"));
        else if (context.projectAccess().isWhitelisted(player.getUniqueId(), project.worldName()))
          lore.add(context.messages().parse("<green>✔ Du bist gewhitelistet"));
        else lore.add(context.messages().parse("<red>✘ Du bist nicht gewhitelistet"));
      } else {
        lore.add(
            context
                .messages()
                .parse("<dark_gray>Whitelist: <red>Inaktiv <dark_gray>(frei zugänglich)"));
      }
      List<String> names =
          context.projectAccess().whitelisted(project.worldName()).stream()
              .map(Bukkit::getOfflinePlayer)
              .map(
                  offline ->
                      java.util.Objects.requireNonNullElse(
                          offline.getName(), offline.getUniqueId().toString()))
              .sorted(String.CASE_INSENSITIVE_ORDER)
              .toList();
      if (!names.isEmpty()) {
        lore.add(context.messages().parse(""));
        lore.add(context.messages().parse("<dark_gray>Gewhitelistet:"));
        names.stream()
            .limit(8)
            .map(name -> context.messages().parse("<dark_gray>• <white>" + name))
            .forEach(lore::add);
        if (names.size() > 8)
          lore.add(
              context
                  .messages()
                  .parse("<dark_gray>… und <white>" + (names.size() - 8) + " <dark_gray>weitere"));
      }
      lore.add(context.messages().parse(""));
      lore.add(
          context
              .messages()
              .parse(admin ? "<gold>Klick: Projekt verwalten" : "<aqua>Klick: Projekt beitreten"));
      meta.lore(lore);
      item.setItemMeta(meta);
      inventory.setItem(slot, item);
    }
    if (page > 0)
      inventory.setItem(
          18,
          named(
              Material.ARROW,
              "<yellow>◀ Vorherige Seite",
              List.of("<gray>Zu Seite <white>" + page)));
    inventory.setItem(
        22,
        named(
            Material.BARRIER,
            "<red><bold>Schließen",
            List.of("<gray>Seite <white>" + (page + 1) + "<gray>/<white>" + pages)));
    if (page + 1 < pages)
      inventory.setItem(
          26,
          named(
              Material.ARROW,
              "<yellow>Nächste Seite ▶",
              List.of("<gray>Zu Seite <white>" + (page + 2))));
    player.openInventory(inventory);
    UiSound.OPEN.play(player);
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    InventoryHolder inventoryHolder = event.getInventory().getHolder(false);
    if (!(inventoryHolder instanceof Holder)
        && !(inventoryHolder instanceof AdminHolder)
        && !(inventoryHolder instanceof ConfirmHolder)) return;
    event.setCancelled(true);
    if (!(event.getWhoClicked() instanceof Player player)) return;
    if (inventoryHolder instanceof AdminHolder admin) {
      clickAdmin(player, event.getRawSlot(), admin.project);
      return;
    }
    if (inventoryHolder instanceof ConfirmHolder confirm) {
      clickConfirm(player, event.getRawSlot(), confirm);
      return;
    }
    Holder holder = (Holder) inventoryHolder;
    int slot = event.getRawSlot();
    if (slot == 22) {
      UiSound.CLICK.play(player);
      player.closeInventory();
      return;
    }
    if (slot == 18 && holder.page > 0) {
      UiSound.CLICK.play(player);
      open(player, holder.page - 1, holder.admin);
      return;
    }
    if (slot == 26 && (holder.page + 1) * PAGE_SIZE < holder.projects.size()) {
      UiSound.CLICK.play(player);
      open(player, holder.page + 1, holder.admin);
      return;
    }
    if (slot < 0 || slot >= PAGE_SIZE) return;
    int index = holder.page * PAGE_SIZE + slot;
    if (index >= holder.projects.size()) return;
    Project project = holder.projects.get(index);
    UiSound.CLICK.play(player);
    if (holder.admin) {
      openAdmin(player, project);
      return;
    }
    if (context.backups().safeWorldLocked(project.worldName())) {
      context.messages().send(player, "<red>Diese Welt ist während des sicheren Backups gesperrt.");
      UiSound.ERROR.play(player);
      return;
    }
    if (!context
        .projectAccess()
        .mayEnter(
            player.getUniqueId(),
            project.worldName(),
            player.hasPermission("bauserver.admin"))) {
      context.messages().send(player, "<red>Du hast keinen Zugriff auf dieses Projekt.");
      UiSound.ERROR.play(player);
      return;
    }
    player.closeInventory();
    World world;
    try {
      world =
          context
              .worlds()
              .loaded(project.worldName())
              .orElseGet(() -> context.worlds().createProject(project.worldName(), "normal"));
    } catch (RuntimeException exception) {
      context.messages().send(player, "<red>Die Projektwelt konnte nicht geladen werden.");
      return;
    }
    context.teleports().remember(player);
    if (player.getWorld() != world) context.worlds().clearPlayer(player);
    context
        .worlds()
        .teleport(player, world.getSpawnLocation())
        .thenAccept(
            ok ->
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> {
                          if (ok) {
                            context
                                .messages()
                                .action(
                                    player,
                                    "<gold>Projekt <yellow>"
                                        + project.worldName()
                                        + " <gold>betreten");
                            UiSound.TELEPORT.play(player);
                          }
                        }));
  }

  private void openAdmin(Player player, Project project) {
    AdminHolder holder = new AdminHolder(project);
    Inventory inventory =
        Bukkit.createInventory(
            holder, 27, context.messages().parse("<red>Verwaltung: " + project.name()));
    holder.inventory = inventory;
    inventory.setItem(
        10,
        named(
            context.autoload().contains(project.worldName())
                ? Material.LIME_CONCRETE
                : Material.RED_CONCRETE,
            "<green>Autoload umschalten"));
    inventory.setItem(11, named(Material.MINECART, "<red>Welt entladen"));
    inventory.setItem(12, named(Material.ENDER_PEARL, "<aqua>Teleportieren"));
    if (context.settings().templates().containsKey(project.worldName()))
      inventory.setItem(13, named(Material.LODESTONE, "<gold>Ins Template übertragen"));
    inventory.setItem(14, named(Material.ENDER_CHEST, "<gold>Welt exportieren"));
    inventory.setItem(
        15,
        named(
            project.whitelistActive() ? Material.LIME_CONCRETE : Material.RED_CONCRETE,
            "<yellow>Whitelist umschalten"));
    inventory.setItem(16, named(Material.TNT, "<red>Projekt aus DB löschen"));
    inventory.setItem(22, named(Material.ARROW, "<yellow>Zurück"));
    player.openInventory(inventory);
  }

  private void clickAdmin(Player player, int slot, Project project) {
    if (!player.hasPermission("bauserver.srbuilder")) {
      player.closeInventory();
      context.messages().noPermission(player);
      return;
    }
    if (slot == 22) {
      open(player, 0, true);
      return;
    }
    if (slot == 10) {
      boolean active = !context.autoload().contains(project.worldName());
      context
          .autoload()
          .set(project.worldName(), active)
          .whenComplete(
              (ok, _) ->
                  Bukkit.getScheduler()
                      .runTask(
                          context.plugin(),
                          () -> {
                            if (!Boolean.TRUE.equals(ok)) {
                              if (context.autoload().contains(project.worldName()) == active) {
                                context
                                    .messages()
                                    .send(
                                        player,
                                        active
                                            ? "<yellow>Autoload ist für diese Welt bereits aktiviert."
                                            : "<yellow>Autoload ist für diese Welt bereits deaktiviert.");
                                return;
                              }
                              context
                                  .messages()
                                  .send(
                                      player,
                                      "<red>Der Autoload konnte nicht geändert werden. Bitte versuche es erneut.");
                              return;
                            }
                            context
                                .messages()
                                .action(
                                    player,
                                    (active
                                            ? "<green>Autoload für <yellow>"
                                            : "<red>Autoload für <yellow>")
                                        + project.worldName()
                                        + (active ? " <green>aktiviert." : " <red>deaktiviert."));
                            openAdmin(
                                player,
                                context.projects().byWorld(project.worldName()).orElse(project));
                          }));
      return;
    }
    if (slot == 11) {
      World world = context.worlds().loaded(project.worldName()).orElse(null);
      if (world == null) {
        context.messages().send(player, "<red>Die Welt ist nicht geladen.");
        return;
      }
      int moved =
          context
              .worlds()
              .evacuateAndUnload(
                  project.worldName(),
                  "<gray>Die Projektwelt <green>"
                      + project.worldName()
                      + " <gray>wurde von <yellow>"
                      + player.getName()
                      + " <gray>entladen.");
      context
          .messages()
          .send(
              player,
              "<gray>Welt wird entladen. <yellow>"
                  + moved
                  + " <gray>Spieler wurden zur Hauptwelt teleportiert.");
      UiSound.WORLD_UNLOAD.play(player);
      return;
    }
    if (slot == 12) {
      player.closeInventory();
      teleport(player, project);
      return;
    }
    if (slot == 13
        && player.hasPermission("bauserver.admin")
        && context.settings().templates().containsKey(project.worldName())) {
      openConfirm(player, project, Action.TEMPLATE);
      return;
    }
    if (slot == 15) {
      boolean active = !project.whitelistActive();
      context
          .projects()
          .setWhitelistActive(project.worldName(), active)
          .whenComplete(
              (ok, _) ->
                  Bukkit.getScheduler()
                      .runTask(
                          context.plugin(),
                          () -> {
                            if (!Boolean.TRUE.equals(ok)) {
                              boolean current =
                                  context
                                      .projects()
                                      .byWorld(project.worldName())
                                      .map(Project::whitelistActive)
                                      .orElse(!active);
                              if (current == active) {
                                context
                                    .messages()
                                    .send(
                                        player,
                                        active
                                            ? "<yellow>Die Whitelist ist bereits aktiviert."
                                            : "<yellow>Die Whitelist ist bereits deaktiviert.");
                                return;
                              }
                              context
                                  .messages()
                                  .send(
                                      player,
                                      "<red>Die Whitelist konnte nicht geändert werden. Bitte versuche es erneut.");
                              return;
                            }
                            context
                                .messages()
                                .action(
                                    player,
                                    (active
                                            ? "<green>Whitelist für <yellow>"
                                            : "<red>Whitelist für <yellow>")
                                        + project.worldName()
                                        + (active ? " <green>aktiviert." : " <red>deaktiviert."));
                            if (active)
                              context
                                  .worlds()
                                  .evacuateUnauthorized(
                                      project.worldName(),
                                      "<gray>Die Welt <green>"
                                          + project.worldName()
                                          + " <gray>ist jetzt nur noch für freigeschaltete Spieler zugänglich.");
                            openAdmin(
                                player,
                                context.projects().byWorld(project.worldName()).orElse(project));
                          }));
      return;
    }
    if (slot == 14) openConfirm(player, project, Action.EXPORT);
    if (slot == 16) openConfirm(player, project, Action.DELETE);
  }

  private void openConfirm(Player player, Project project, Action action) {
    ConfirmHolder holder = new ConfirmHolder(project, action);
    String title =
        action == Action.DELETE
            ? "<red>Projekt wirklich löschen?"
            : action == Action.EXPORT
                ? "<gold>Welt wirklich exportieren?"
                : "<gold>Template wirklich überschreiben?";
    Inventory inventory = Bukkit.createInventory(holder, 27, context.messages().parse(title));
    holder.inventory = inventory;
    inventory.setItem(11, named(Material.LIME_CONCRETE, "<green>Ja, bestätigen"));
    inventory.setItem(13, named(Material.OAK_SIGN, "<white>" + project.name()));
    inventory.setItem(15, named(Material.RED_CONCRETE, "<red>Abbrechen"));
    player.openInventory(inventory);
  }

  private void clickConfirm(Player player, int slot, ConfirmHolder holder) {
    if (slot == 15) {
      openAdmin(player, holder.project);
      return;
    }
    if (slot != 11) return;
    player.closeInventory();
    if (holder.action == Action.DELETE) {
      context
          .projects()
          .delete(holder.project.name())
          .whenComplete(
              (ok, _) ->
                  Bukkit.getScheduler()
                      .runTask(
                          context.plugin(),
                          () -> {
                            if (Boolean.TRUE.equals(ok)) {
                              context.autoload().set(holder.project.worldName(), false);
                              context
                                  .worlds()
                                  .evacuateAndUnload(
                                      holder.project.worldName(),
                                      "<gray>Das Projekt <green>"
                                          + holder.project.name()
                                          + " <gray>wurde aus der Datenbank entfernt und die Welt entladen.");
                              context
                                  .messages()
                                  .send(
                                      player,
                                      "<green>Projekt wurde aus der Datenbank entfernt. Weltdaten bleiben erhalten.");
                              UiSound.WORLD_UNLOAD.play(player);
                              open(player, 0, true);
                            } else {
                              context
                                  .messages()
                                  .send(player, "<red>Projekt konnte nicht entfernt werden.");
                              UiSound.ERROR.play(player);
                            }
                          }));
    } else if (holder.action == Action.EXPORT) {
      context.messages().send(player, "<yellow>Export wurde gestartet.");
      context
          .worldTransfers()
          .export("projekt", holder.project.worldName(), holder.project.worldName())
          .whenComplete(
              (code, error) ->
                  Bukkit.getScheduler()
                      .runTask(
                          context.plugin(),
                          () ->
                              context
                                  .messages()
                                  .send(
                                      player,
                                      error == null && code == 0
                                          ? "<green>Export abgeschlossen."
                                          : "<red>Export fehlgeschlagen (Exit " + code + ").")));
    } else {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
      context.messages().send(player, "<yellow>Template-Übertragung wurde gestartet.");
      context
          .worldTransfers()
          .pushTemplate(holder.project.worldName())
          .whenComplete(
              (code, error) ->
                  Bukkit.getScheduler()
                      .runTask(
                          context.plugin(),
                          () ->
                              context
                                  .messages()
                                  .send(
                                      player,
                                      error == null && code == 0
                                          ? "<green>Template wurde aktualisiert."
                                          : "<red>Template-Übertragung fehlgeschlagen (Exit "
                                              + code
                                              + ").")));
    }
  }

  private void teleport(Player player, Project project) {
    if (context.backups().safeWorldLocked(project.worldName())) {
      context.messages().send(player, "<red>Welt ist während des Backups gesperrt.");
      return;
    }
    try {
      World world =
          context
              .worlds()
              .loaded(project.worldName())
              .orElseGet(() -> context.worlds().createProject(project.worldName(), "normal"));
      context.teleports().remember(player);
      context.worlds().clearPlayer(player);
      context
          .worlds()
          .teleport(player, world.getSpawnLocation())
          .thenAccept(
              success ->
                  Bukkit.getScheduler()
                      .runTask(
                          context.plugin(),
                          () -> {
                            if (!success) {
                              UiSound.ERROR.play(player);
                              return;
                            }
                            context
                                .messages()
                                .action(
                                    player,
                                    "<gold>Projekt <yellow>"
                                        + project.worldName()
                                        + " <gold>betreten");
                            UiSound.TELEPORT.play(player);
                          }));
    } catch (RuntimeException exception) {
      context.messages().send(player, "<red>Welt konnte nicht geladen werden.");
      UiSound.ERROR.play(player);
    }
  }

  private static List<String> wrapLegacy(String text) {
    if (text == null || text.isBlank()) return List.of("&7");
    String[] words = text.strip().split("\\s+");
    List<String> lines = new ArrayList<>();
    String activeCodes = "&7";
    for (int start = 0; start < words.length; start += 6) {
      int end = Math.min(words.length, start + 6);
      StringBuilder line = new StringBuilder(activeCodes);
      for (int index = start; index < end; index++) {
        if (index > start) line.append(' ');
        line.append(words[index]);
        activeCodes = activeLegacyCodes(words[index], activeCodes);
      }
      lines.add(line.toString());
    }
    return lines;
  }

  private static String activeLegacyCodes(String text, String current) {
    String active = current;
    for (int index = 0; index + 1 < text.length(); index++) {
      if (text.charAt(index) != '&') continue;
      char code = Character.toLowerCase(text.charAt(index + 1));
      if (code == '#' && index + 7 < text.length()) {
        String hex = text.substring(index + 2, index + 8);
        if (HEX_COLOR_PATTERN.matcher(hex).matches()) {
          active = "&#" + hex;
          index += 7;
        }
      } else if ("0123456789abcdef".indexOf(code) >= 0) {
        active = "&" + code;
        index++;
      } else if ("klmno".indexOf(code) >= 0) {
        String format = "&" + code;
        if (!active.contains(format)) active = active.concat(format);
        index++;
      } else if (code == 'r') {
        active = "&7";
        index++;
      }
    }
    return active;
  }

  private static final class Holder implements InventoryHolder {
    private final int page;
    private final boolean admin;
    private final List<Project> projects;
    private Inventory inventory;

    private Holder(int page, boolean admin, List<Project> projects) {
      this.page = page;
      this.admin = admin;
      this.projects = List.copyOf(projects);
    }

    @Override
    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }

  private static final class AdminHolder implements InventoryHolder {
    private final Project project;
    private Inventory inventory;

    private AdminHolder(Project project) {
      this.project = project;
    }

    @Override
    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }

  private static final class ConfirmHolder implements InventoryHolder {
    private final Project project;
    private final Action action;
    private Inventory inventory;

    private ConfirmHolder(Project project, Action action) {
      this.project = project;
      this.action = action;
    }

    @Override
    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }

  private enum Action {
    DELETE,
    EXPORT,
    TEMPLATE
  }
}
