package eu.hunfeld.flunarBauserver.commands.teleport;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpaAcceptCommand extends BaseCommand {
  public TpaAcceptCommand(BauserverContext context) {
    super(context);
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    Player target = player(sender);
    if (target == null) return true;
    var requesterId = context.tpa().consume(target.getUniqueId());
    if (requesterId.isEmpty()) {
      context.messages().send(target, "<red>Du hast keine offene Teleportanfrage.");
      return true;
    }
    Player requester = Bukkit.getPlayer(requesterId.get());
    if (requester == null) {
      context.messages().send(target, "<red>Der Spieler ist nicht mehr online.");
      return true;
    }
    if (context.backups().safeWorldLocked(target.getWorld().getName())) {
      context
          .messages()
          .send(target, "<red>Safe Backup läuft: TPA in Projekt-/Privatwelten ist gesperrt.");
      context
          .messages()
          .send(requester, "<red>Safe Backup läuft: TPA in Projekt-/Privatwelten ist gesperrt.");
      return true;
    }
    if (!context.worlds().mayEnter(requester, target.getWorld())) {
      context.messages().send(target, "<red>Der Spieler hat keinen Zugriff auf dein Projekt.");
      context.messages().send(requester, "<red>Du hast keinen Zugriff auf dieses Projekt.");
      return true;
    }
    context.teleports().remember(requester);
    context
        .worlds()
        .teleport(requester, target.getLocation())
        .thenAccept(
            ok ->
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> {
                          if (ok) {
                            context
                                .messages()
                                .send(
                                    requester,
                                    "<gray>Anfrage an <green>"
                                        + target.getName()
                                        + " <gray>angenommen.");
                            context
                                .messages()
                                .send(
                                    target,
                                    "<gray>Anfrage von <green>"
                                        + requester.getName()
                                        + " <gray>angenommen.");
                            Bukkit.getScheduler()
                                .runTaskLater(
                                    context.plugin(),
                                    () -> {
                                      requester.playSound(
                                          requester.getLocation(),
                                          Sound.BLOCK_BEACON_POWER_SELECT,
                                          1f,
                                          1f);
                                      target.playSound(
                                          target.getLocation(),
                                          Sound.BLOCK_BEACON_POWER_SELECT,
                                          1f,
                                          1f);
                                    },
                                    2L);
                          }
                        }));
    return true;
  }
}
