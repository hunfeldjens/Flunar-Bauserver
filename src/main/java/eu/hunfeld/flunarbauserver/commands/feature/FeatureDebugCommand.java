package eu.hunfeld.flunarbauserver.commands.feature;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

public final class FeatureDebugCommand extends BaseCommand {
  public FeatureDebugCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    if (a.length > 0)
      context
          .messages()
          .send(
              s,
              "<gray>Feature <green>"
                  + a[0]
                  + "<gray>: <white>"
                  + context.features().enabled(a[0]));
    else
      context
          .features()
          .all()
          .forEach(
              (f, v) -> context.messages().raw(s, (v ? "<green>" : "<red>") + f + "<gray>: " + v));
    return true;
  }
}
