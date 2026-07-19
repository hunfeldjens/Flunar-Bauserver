package eu.hunfeld.flunarBauserver.commands.project;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.database.Sql;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

abstract class AbstractProjectSubcommand implements ProjectSubcommand {
  protected final BauserverContext context;

  protected AbstractProjectSubcommand(BauserverContext context) {
    this.context = context;
  }

  protected boolean require(CommandSender sender, String permission) {
    if (sender.hasPermission(permission)) return true;
    context.messages().noPermission(sender);
    return false;
  }

  protected boolean database(Player player) {
    if (context.database().isReady()) return true;
    context.messages().send(player, "<red>Die Datenbank ist momentan nicht verfügbar.");
    return false;
  }

  protected String currentWorld(Player player) {
    return Sql.cleanWorld(player.getWorld().getName());
  }

  protected void complete(CompletableFuture<Boolean> future, Player player, String success) {
    future.whenComplete(
        (ok, error) ->
            Bukkit.getScheduler()
                .runTask(
                    context.plugin(),
                    () ->
                        context
                            .messages()
                            .send(
                                player,
                                error == null && Boolean.TRUE.equals(ok)
                                    ? success
                                    : "<red>Die Aktion konnte nicht gespeichert werden.")));
  }

  protected static List<String> filter(List<String> values, String input) {
    String prefix = input.toLowerCase(Locale.ROOT);
    return values.stream()
        .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix))
        .toList();
  }
}
