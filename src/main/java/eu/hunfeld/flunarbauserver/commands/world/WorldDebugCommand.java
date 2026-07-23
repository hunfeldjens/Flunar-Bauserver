package eu.hunfeld.flunarbauserver.commands.world;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import eu.hunfeld.flunarbauserver.database.Sql;
import eu.hunfeld.flunarbauserver.model.Project;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class WorldDebugCommand extends BaseCommand {
  public WorldDebugCommand(BauserverContext context) {
    super(context);
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String @NotNull [] args) {
    Player player = player(sender);
    if (player == null || !requireDatabase(player)) return true;
    String world = Sql.cleanWorld(player.getWorld().getName());
    var project = context.projects().byWorld(world);
    context.messages().raw(player, "<red>WL-DEBUG <dark_gray>– <gray>Welt: <yellow>" + world);
    context.messages().raw(player, "<gray>Projekt vorhanden: <white>" + project.isPresent());
    context
        .messages()
        .raw(
            player,
            "<gray>Whitelist aktiv: <white>"
                + project.map(Project::whitelistActive).orElse(false));
    Player target = args.length == 0 ? player : Bukkit.getPlayerExact(args[0]);
    if (target != null) {
      context.messages().raw(player, "<gray>Spieler: <green>" + target.getName());
      context
          .messages()
          .raw(
              player,
              "<gray>Owner: <white>" + context.projects().isOwner(target.getUniqueId(), world));
      context
          .messages()
          .raw(
              player,
              "<gray>Whitelist: <white>"
                  + context.projectAccess().isWhitelisted(target.getUniqueId(), world));
      context
          .messages()
          .raw(
              player,
              "<gray>Projekt-Ban: <white>"
                  + context.projectAccess().isBanned(target.getUniqueId(), world));
    }
    return true;
  }
}
