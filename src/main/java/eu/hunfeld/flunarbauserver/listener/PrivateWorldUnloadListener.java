package eu.hunfeld.flunarbauserver.listener;

import eu.hunfeld.flunarbauserver.FlunarBauserver;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;


public final class PrivateWorldUnloadListener implements Listener {
  private static final long UNLOAD_DELAY_TICKS = 5L * 60L * 20L;
  private final FlunarBauserver plugin;
  private final Map<NamespacedKey, BukkitTask> pending = new HashMap<>();

  public PrivateWorldUnloadListener(FlunarBauserver plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void worldChange(PlayerChangedWorldEvent event) {
    cancel(event.getPlayer().getWorld());
    queueWhenEmpty(event.getFrom());
  }

  @EventHandler
  public void join(PlayerJoinEvent event) {
    cancel(event.getPlayer().getWorld());
  }

  @EventHandler
  public void quit(PlayerQuitEvent event) {
    queueWhenEmpty(event.getPlayer().getWorld());
  }

  private void queueWhenEmpty(World world) {
    if (!isPrivate(world)) return;
    Bukkit.getScheduler()
        .runTask(
            plugin,
            () -> {
              if (!world.getPlayers().isEmpty()) return;
              cancel(world);
              BukkitTask task =
                  Bukkit.getScheduler()
                      .runTaskLater(
                          plugin,
                          () -> {
                            pending.remove(world.getKey());
                            World loaded = Bukkit.getWorld(world.getKey());
                            if (loaded == null || !loaded.getPlayers().isEmpty()) return;
                            if (Bukkit.unloadWorld(loaded, true))
                              plugin
                                  .getLogger()
                                  .info(
                                      "Leere Privatwelt "
                                          + loaded.getKey()
                                          + " wurde nach 5 Minuten gespeichert und entladen.");
                          },
                          UNLOAD_DELAY_TICKS);
              pending.put(world.getKey(), task);
            });
  }

  private void cancel(World world) {
    if (!isPrivate(world)) return;
    BukkitTask task = pending.remove(world.getKey());
    if (task != null) task.cancel();
  }

  private static boolean isPrivate(World world) {
    return world.getKey().namespace().equals("privat");
  }
}
