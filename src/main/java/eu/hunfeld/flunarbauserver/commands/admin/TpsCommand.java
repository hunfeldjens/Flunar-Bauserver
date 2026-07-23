package eu.hunfeld.flunarbauserver.commands.admin;

import eu.hunfeld.flunarbauserver.*;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import java.util.Locale;
import org.bukkit.*;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

public final class TpsCommand extends BaseCommand {
  public TpsCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String @NotNull [] a) {
    double[] t = Bukkit.getTPS();
    context
        .messages()
        .send(
            s,
            "<gray>TPS der letzten <white>1m<gray>, <white>5m <gray>und <white>15m<gray>: "
                + format(t[0])
                + "<gray>, "
                + format(t[1])
                + "<gray>, "
                + format(t[2]));
    return true;
  }

  private static String format(double value) {
    String color = value >= 18.0D ? "<green>" : value >= 15.0D ? "<yellow>" : "<red>";
    return color + String.format(Locale.ROOT, "%.2f", Math.min(20.0D, value));
  }
}
