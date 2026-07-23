package eu.hunfeld.flunarbauserver.commands.admin;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public final class ServerRestartCommand extends BaseCommand {
  private boolean running;

  public ServerRestartCommand(BauserverContext context) {
    super(context);
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String @NotNull [] args) {
    if (running) {
      context.messages().send(sender, "<red>Es läuft bereits ein Restart-Countdown.");
      return true;
    }
    running = true;
    context.messages().broadcast("<red><bold>Server-Restart angekündigt!");
    context.messages().broadcast("<gray>Der Bauserver stoppt in <red>10 Sekunden<gray>.");
    new BukkitRunnable() {
      private int left = 10;

      @Override
      public void run() {
        if (left <= 0) {
          cancel();
          context.messages().broadcast("<red>Server wird jetzt gestoppt.");
          running = false;
          Bukkit.shutdown();
          return;
        }
        Title title =
            Title.title(
                Component.text("Restart", NamedTextColor.RED),
                context.messages().parse("<gray>Server stoppt in <red>" + left + " <gray>Sekunden"),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO));
        Bukkit.getOnlinePlayers()
            .forEach(
                player -> {
                  player.showTitle(title);
                  player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                });
        left--;
      }
    }.runTaskTimer(context.plugin(), 0L, 20L);
    return true;
  }
}
