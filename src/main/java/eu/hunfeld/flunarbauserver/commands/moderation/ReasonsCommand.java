package eu.hunfeld.flunarbauserver.commands.moderation;

import eu.hunfeld.flunarbauserver.*;
import java.util.Map;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

public final class ReasonsCommand extends BaseCommand {
  public ReasonsCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    context.messages().send(s, "<dark_gray>--- <green>Gründe (ID → Text) <dark_gray>---");
    context.settings().banReasons().entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(
            entry ->
                context
                    .messages()
                    .raw(
                        s,
                        "<gray>#" + entry.getKey() + " <dark_gray>→ <white>" + entry.getValue()));
    return true;
  }
}
