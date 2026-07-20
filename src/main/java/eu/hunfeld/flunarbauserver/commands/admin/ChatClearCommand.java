package eu.hunfeld.flunarbauserver.commands.admin;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.*;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

public final class ChatClearCommand extends BaseCommand {
  public ChatClearCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    for (int i = 0; i < 800; i++)
      Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(" "));
    Bukkit.getOnlinePlayers()
        .forEach(
            player -> {
              context
                  .messages()
                  .raw(
                      player,
                      "<gray><strikethrough>                                                  </strikethrough>");
              context
                  .messages()
                  .send(player, "<gray>" + s.getName() + " <gray>hat den Chat geleert.");
              context
                  .messages()
                  .raw(
                      player,
                      "<gray><strikethrough>                                                  </strikethrough>");
            });
    return true;
  }
}
