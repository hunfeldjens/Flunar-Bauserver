package eu.hunfeld.flunarBauserver.commands.chat;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import eu.hunfeld.flunarBauserver.service.PrivateMessageService;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Private Nachrichten, ohne vom Spieler eingegebene MiniMessage-Tags auszuwerten. */
public final class MessageCommand extends BaseCommand {
  private final PrivateMessageService conversations;

  public MessageCommand(BauserverContext context, PrivateMessageService conversations) {
    super(context);
    this.conversations = conversations;
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    if (args.length < 2) {
      context.messages().send(sender, "<gray>Benutzung: <green>/msg <Spieler> <Nachricht>");
      return true;
    }
    Player target = Bukkit.getPlayerExact(args[0]);
    if (target == null) {
      context.messages().send(sender, context.settings().messages().playerNotOnline());
      return true;
    }
    String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
    send(sender, target, message);
    return true;
  }

  public void send(CommandSender sender, Player target, String message) {
    Component senderName = Component.text(sender.getName(), NamedTextColor.YELLOW);
    Component targetName = Component.text(target.getName(), NamedTextColor.YELLOW);
    Component body = Component.text(message, NamedTextColor.WHITE);
    sender.sendMessage(
        context
            .messages()
            .parse(context.settings().prefix() + " <gray>Du <dark_gray>→ ")
            .append(targetName)
            .append(context.messages().parse("<gray>: "))
            .append(body));
    if (target != sender)
      target.sendMessage(
          context
              .messages()
              .parse(context.settings().prefix() + " ")
              .append(senderName)
              .append(context.messages().parse(" <dark_gray>→ <gray>Dir: "))
              .append(body));
    if (sender instanceof Player player)
      conversations.remember(player.getUniqueId(), target.getUniqueId());
  }

  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {
    if (args.length != 1) return List.of();
    String search = args[0].toLowerCase(java.util.Locale.ROOT);
    return Bukkit.getOnlinePlayers().stream()
        .map(Player::getName)
        .filter(name -> name.toLowerCase(java.util.Locale.ROOT).startsWith(search))
        .toList();
  }
}
