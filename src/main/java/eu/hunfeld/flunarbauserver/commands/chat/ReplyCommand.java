package eu.hunfeld.flunarbauserver.commands.chat;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import eu.hunfeld.flunarbauserver.service.PrivateMessageService;
import java.util.Arrays;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ReplyCommand extends BaseCommand {
  private final PrivateMessageService conversations;
  private final MessageCommand messages;

  public ReplyCommand(
      BauserverContext context, PrivateMessageService conversations, MessageCommand messages) {
    super(context);
    this.conversations = conversations;
    this.messages = messages;
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    Player player = player(sender);
    if (player == null) return true;
    if (args.length == 0) {
      context.messages().send(player, "<gray>Benutzung: <green>/r <Nachricht>");
      return true;
    }
    UUID targetId = conversations.replyTarget(player.getUniqueId()).orElse(null);
    Player target = targetId == null ? null : Bukkit.getPlayer(targetId);
    if (target == null) {
      context.messages().send(player, "<red>Du hast keinen erreichbaren Antwortpartner.");
      return true;
    }
    messages.send(player, target, String.join(" ", Arrays.asList(args)));
    return true;
  }
}
