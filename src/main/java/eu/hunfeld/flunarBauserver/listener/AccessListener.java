package eu.hunfeld.flunarBauserver.listener;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.chat.LuckPermsBridge;
import eu.hunfeld.flunarBauserver.model.ActiveBan;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class AccessListener implements Listener {
  private final BauserverContext context;

  public AccessListener(BauserverContext context) {
    this.context = context;
    LuckPermsBridge luckPerms = new LuckPermsBridge();
    luckPerms.subscribe(context.plugin(), this::scheduleAccessCheck);
    // Zusätzlicher Cache-Check als sichere Rückfallebene, falls Rechte durch ein
    // anderes Permission-Plugin oder ohne LuckPerms-Event geändert werden.
    Bukkit.getScheduler().runTaskTimer(context.plugin(), this::enforceOnlineAccess, 40L, 40L);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void login(PlayerLoginEvent event) {
    Player player = event.getPlayer();
    if (!player.hasPermission(context.settings().accessPermission())) {
      event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, deniedScreen());
      return;
    }
    if (!context.database().isReady() && !player.hasPermission("bauserver.admin.bypass")) {
      event.disallow(
          PlayerLoginEvent.Result.KICK_OTHER,
          context
              .messages()
              .parse(
                  "<red>Der Bauserver initialisiert gerade seine Daten. Bitte versuche es gleich erneut."));
      return;
    }
    ActiveBan ban = context.moderation().activeBan(player.getUniqueId()).orElse(null);
    if (ban != null && !player.hasPermission("bauserver.ban.bypass")) {
      event.disallow(
          PlayerLoginEvent.Result.KICK_BANNED,
          context
              .messages()
              .parse(
                  "<aqua><bold>Flunar.de</bold> <dark_gray>» <white>Bauserver\n\n<red>Du wurdest permanent ausgeschlossen.\n\n<gray>Grund: <white>"
                      + ban.reason()
                      + "\n<gray>Von: <white>"
                      + ban.byName()));
    }
  }

  @EventHandler
  public void join(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    event.joinMessage(context.messages().parse("<gray>[<green>+<gray>] " + player.getName()));
    player.setGameMode(GameMode.CREATIVE);
    welcome(player);
    context
        .plugin()
        .getServer()
        .getScheduler()
        .runTaskLater(context.plugin(), () -> welcome(player), 40L);
    context.vanish().apply(player);
    if (player.getWorld() == context.worlds().mainWorld()) {
      Location spawn = context.settings().spawn().location(context.worlds().mainWorld());
      context
          .plugin()
          .getServer()
          .getScheduler()
          .runTaskLater(context.plugin(), () -> player.teleportAsync(spawn), 2L);
    }
    enforceWorld(player);
  }

  private void welcome(Player player) {
    if (!player.isOnline()) return;
    context
        .messages()
        .action(
            player,
            "<gold>Willkommen auf " + context.settings().title() + " <gray>- <yellow>Bauserver");
  }

  @EventHandler
  public void quit(PlayerQuitEvent event) {
    event.quitMessage(
        context.messages().parse("<gray>[<red>-<gray>] " + event.getPlayer().getName()));
    context.tpa().clear(event.getPlayer().getUniqueId());
    context.vanish().remove(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void teleport(PlayerTeleportEvent event) {
    if (event.getTo() == null
        || event.getTo().getWorld() == null
        || event.getTo().getWorld() == event.getFrom().getWorld()) return;
    String world = event.getTo().getWorld().getName();
    if (context.backups().safeWorldLocked(world)) {
      event.setCancelled(true);
      context
          .messages()
          .send(event.getPlayer(), "<red>Diese Welt ist während des sicheren Backups gesperrt.");
      return;
    }
    if (!context.worlds().mayEnter(event.getPlayer(), event.getTo().getWorld())) {
      event.setCancelled(true);
      context
          .messages()
          .send(event.getPlayer(), "<red>Du hast keinen Zugriff auf diese Projektwelt.");
    }
  }

  private void enforceWorld(Player player) {
    if (player.getWorld() == context.worlds().mainWorld()) return;
    String world = player.getWorld().getName();
    if (context.backups().safeWorldLocked(world)
        || !context.worlds().mayEnter(player, player.getWorld())) {
      context.worlds().clearPlayer(player);
      player.teleportAsync(context.worlds().mainWorld().getSpawnLocation());
      context.messages().send(player, "<red>Deine letzte Welt ist für dich nicht zugänglich.");
    }
  }

  private void scheduleAccessCheck(UUID uuid) {
    Bukkit.getScheduler()
        .runTask(
            context.plugin(),
            () -> {
              Player player = Bukkit.getPlayer(uuid);
              if (player != null) enforceAccess(player);
            });
  }

  private void enforceOnlineAccess() {
    Bukkit.getOnlinePlayers().forEach(this::enforceAccess);
  }

  private void enforceAccess(Player player) {
    if (player.hasPermission(context.settings().accessPermission())) return;
    player.kick(deniedScreen());
  }

  private Component deniedScreen() {
    return context
        .messages()
        .parse(
            "<aqua><bold>Flunar.de</bold> <dark_gray>» <white>Bauserver\n\n"
                + "<gray>Hoppla! Du bist leider noch kein <yellow>Teammitglied<gray>.\n"
                + "<white>Damit wir unsere Projekte schützen können, ist der\n"
                + "<white>Direktzugriff nur für autorisierte Teammitglieder erlaubt.\n\n"
                + "<aqua>Lust mitzubauen?\n"
                + "<gray>Wir suchen immer talentierte Unterstützung!\n"
                + "<white>Bewirb dich jetzt unter: <blue>dc.flunar.de");
  }
}
