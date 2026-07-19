package eu.hunfeld.flunarBauserver.commands.moderation;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import eu.hunfeld.flunarBauserver.gui.ModerationHistoryMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class KickHistoryCommand extends BaseCommand {
  private final ModerationHistoryMenu menu;

  public KickHistoryCommand(BauserverContext context, ModerationHistoryMenu menu) {
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
    if (player != null && requireDatabase(player))
      menu.open(player, ModerationHistoryMenu.Type.KICK, 1);
    return true;
  }
}
