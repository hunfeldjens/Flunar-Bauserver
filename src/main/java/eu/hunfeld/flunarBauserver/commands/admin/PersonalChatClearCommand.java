package eu.hunfeld.flunarBauserver.commands.admin;

import eu.hunfeld.flunarBauserver.*;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PersonalChatClearCommand extends BaseCommand {
  public PersonalChatClearCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player p = player(s);
    if (p != null) {
      for (int i = 0; i < 300; i++) p.sendMessage(" ");
      context
          .messages()
          .raw(
              p,
              "<gray><strikethrough>                                                  </strikethrough>");
      context.messages().send(p, "<gray>Du hast deinen Chat geleert.");
      context
          .messages()
          .raw(
              p,
              "<gray><strikethrough>                                                  </strikethrough>");
    }
    return true;
  }
}
