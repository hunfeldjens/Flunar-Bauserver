package eu.hunfeld.flunarBauserver.service;

import eu.hunfeld.flunarBauserver.BauserverContext;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class BackupCoordinator {
  private final BauserverContext context;
  private BukkitTask countdown;

  public BackupCoordinator(BauserverContext context) {
    this.context = context;
  }

  public void start(CommandSender sender, boolean safe) {
    if (!context.backups().reserve(safe)) {
      context.messages().send(sender, "<red>Ein Backup läuft bereits.");
      return;
    }
    context
        .messages()
        .broadcast(
            "<yellow>"
                + sender.getName()
                + " <gray>hat ein "
                + (safe ? "<green>Safe" : "<red>UnSafe")
                + " Backup <gray>gestartet.");
    countdown =
        new BukkitRunnable() {
          private int seconds = 60;

          @Override
          public void run() {
            if (seconds == 60 || seconds == 30 || seconds <= 10)
              context
                  .messages()
                  .broadcast("<yellow>Backup startet in <white>" + seconds + " Sekunden<yellow>.");
            if (seconds-- > 0) return;
            cancel();
            countdown = null;
            execute(safe);
          }
        }.runTaskTimer(context.plugin(), 0L, 20L);
  }

  public void cancel(CommandSender sender) {
    if (!context.backups().running()) {
      context.messages().send(sender, "<gray>Es läuft kein Backup.");
      return;
    }
    if (countdown != null) {
      countdown.cancel();
      countdown = null;
    }
    context.backups().cancel();
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-on");
    context.messages().broadcast("<red>Backup-Abbruch wurde angefordert.");
  }

  private void execute(boolean safe) {
    if (safe) {
      World fallback = context.worlds().mainWorld();
      for (Player player : Bukkit.getOnlinePlayers()) player.teleport(fallback.getSpawnLocation());
      for (World world : new ArrayList<>(Bukkit.getWorlds()))
        if (world != fallback) Bukkit.unloadWorld(world, true);
    }
    Bukkit.savePlayers();
    Bukkit.getWorlds().forEach(World::save);
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-off");
    context
        .backups()
        .executeReserved()
        .whenComplete(
            (code, error) ->
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> {
                          Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-on");
                          context
                              .messages()
                              .broadcast(
                                  error == null && code == 0
                                      ? "<green>Backup erfolgreich abgeschlossen."
                                      : "<red>Backup fehlgeschlagen (Exit " + code + ").");
                        }));
  }
}
