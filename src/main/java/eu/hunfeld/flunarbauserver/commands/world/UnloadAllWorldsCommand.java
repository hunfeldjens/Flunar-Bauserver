package eu.hunfeld.flunarbauserver.commands.world;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class UnloadAllWorldsCommand extends BaseCommand {
  public UnloadAllWorldsCommand(BauserverContext context) {
    super(context);
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    World fallback = context.worlds().mainWorld();
    List<World> targets = new ArrayList<>(Bukkit.getWorlds());
    targets.remove(fallback);
    int players = 0;
    for (World world : targets) {
      for (Player player : new ArrayList<>(world.getPlayers())) {
        player.teleport(fallback.getSpawnLocation());
        players++;
      }
      Bukkit.unloadWorld(world, true);
    }
    context
        .messages()
        .send(
            sender,
            "<green>Fertig! <gray>"
                + targets.size()
                + " Welten entladen; "
                + players
                + " Spieler verschoben.");
    return true;
  }
}
