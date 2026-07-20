package eu.hunfeld.flunarbauserver.commands;

import eu.hunfeld.flunarbauserver.BauserverContext;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {
  protected final BauserverContext context;

  protected BaseCommand(BauserverContext context) {
    this.context = context;
  }

  protected Player player(CommandSender sender) {
    if (sender instanceof Player player) return player;
    context.messages().send(sender, "<red>Dieser Befehl kann nur im Spiel verwendet werden.");
    return null;
  }

  protected boolean require(CommandSender sender, String permission) {
    if (sender.hasPermission(permission)) return true;
    context.messages().noPermission(sender);
    return false;
  }

  protected boolean requireDatabase(CommandSender sender) {
    if (context.database().isReady()) return true;
    context
        .messages()
        .send(
            sender,
            "<red>Die Datenbank ist momentan nicht verfügbar. Die Aktion wurde sicher abgebrochen.");
    return false;
  }

  protected void complete(CompletableFuture<Boolean> future, CommandSender sender, String success) {
    future.whenComplete(
        (result, error) ->
            Bukkit.getScheduler()
                .runTask(
                    context.plugin(),
                    () -> {
                      if (error != null || !Boolean.TRUE.equals(result))
                        context
                            .messages()
                            .send(sender, "<red>Die Aktion konnte nicht gespeichert werden.");
                      else context.messages().send(sender, success);
                    }));
  }

  protected <T> void onMain(CompletableFuture<T> future, Consumer<T> success) {
    future.whenComplete(
        (result, error) ->
            Bukkit.getScheduler()
                .runTask(
                    context.plugin(),
                    () -> {
                      if (error == null) success.accept(result);
                    }));
  }

  @Override
  public @Nullable List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {
    return List.of();
  }
}
