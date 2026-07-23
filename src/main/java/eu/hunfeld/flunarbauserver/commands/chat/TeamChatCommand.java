package eu.hunfeld.flunarbauserver.commands.chat;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;


public final class TeamChatCommand extends BaseCommand {
  private static final String PERMISSION = "bauserver.builder";

  public TeamChatCommand(BauserverContext context) {
    super(context);
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String @NotNull [] args) {
    if (args.length == 0) {
      context.messages().send(sender, "<gray>Benutzung: <green>/tc <Nachricht>");
      return true;
    }
    Component message =
        context
            .messages()
            .parse("<dark_gray>[<aqua><bold>Teamchat</bold><dark_gray>] <yellow>")
            .append(Component.text(sender.getName(), NamedTextColor.YELLOW))
            .append(context.messages().parse(" <dark_gray>» <white>"))
            .append(Component.text(String.join(" ", Arrays.asList(args)), NamedTextColor.WHITE));
    Bukkit.getOnlinePlayers().stream()
        .filter(player -> player.hasPermission(PERMISSION))
        .forEach(player -> player.sendMessage(message));
    if (!(sender instanceof org.bukkit.entity.Player)) sender.sendMessage(message);
    return true;
  }
}
