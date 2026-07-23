package eu.hunfeld.flunarbauserver.commands.world;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PrivateWorldCommand extends BaseCommand {
  public PrivateWorldCommand(BauserverContext context) {
    super(context);
  }

  @Override
  @SuppressWarnings("resource")
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String @NotNull [] args) {
    Player player = player(sender);
    if (player == null || !requireDatabase(player)) return true;
    String worldName =
        context.privateWorlds().get(player.getUniqueId()).orElse(player.getUniqueId().toString());
    if (context.backups().safeWorldLocked(worldName)) {
      context
          .messages()
          .send(player, "<red>Private Welten sind während des sicheren Backups gesperrt.");
      return true;
    }
    Runnable enter =
        () -> {
          World world;
          try {
            world =
                context
                    .worlds()
                    .loaded(worldName)
                    .orElseGet(() -> context.worlds().createPrivate(worldName));
          } catch (RuntimeException exception) {
            context.messages().send(player, "<red>Deine Privatwelt konnte nicht geladen werden.");
            return;
          }
          context.teleports().remember(player);
          context.worlds().teleport(player, world.getSpawnLocation());
          context.messages().action(player, "<gold>Willkommen auf deiner privaten Welt");
        };
    if (context.privateWorlds().get(player.getUniqueId()).isPresent()) enter.run();
    else
      context
          .privateWorlds()
          .set(player.getUniqueId(), worldName)
          .whenComplete(
              (saved, error) ->
                  Bukkit.getScheduler()
                      .runTask(
                          context.plugin(),
                          () -> {
                            if (error == null && Boolean.TRUE.equals(saved)) enter.run();
                            else
                              context
                                  .messages()
                                  .send(
                                      player,
                                      "<red>Die Privatwelt konnte nicht gespeichert werden.");
                          }));
    return true;
  }
}
