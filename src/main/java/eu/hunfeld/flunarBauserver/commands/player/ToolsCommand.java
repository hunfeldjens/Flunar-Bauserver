package eu.hunfeld.flunarBauserver.commands.player;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import eu.hunfeld.flunarBauserver.gui.ToolsMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ToolsCommand extends BaseCommand {
  private final ToolsMenu menu;

  public ToolsCommand(BauserverContext context, ToolsMenu menu) {
    super(context);
    this.menu = menu;
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    Player player = player(sender);
    if (player != null) menu.open(player);
    return true;
  }
}
