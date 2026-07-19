package eu.hunfeld.flunarBauserver.commands.player;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class FlyCommand extends BaseCommand {
  public FlyCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player sender = player(s);
    if (sender == null) return true;
    if (a.length > 0 && !sender.hasPermission("bauserver.builder")) {
      context.messages().noPermission(sender);
      return true;
    }
    Player t = a.length == 0 ? sender : Bukkit.getPlayerExact(a[0]);
    if (t == null) {
      context.messages().send(s, context.settings().messages().playerNotOnline());
      return true;
    }
    boolean value = !t.getAllowFlight();
    t.setAllowFlight(value);
    if (!value) t.setFlying(false);
    if (t == sender)
      context
          .messages()
          .action(sender, "<gray>Flugmodus → " + (value ? "<green>aktiviert" : "<red>deaktiviert"));
    else {
      context
          .messages()
          .action(
              sender,
              "<gray>Fly von <yellow>"
                  + t.getName()
                  + (value ? " <green>aktiviert" : " <red>deaktiviert"));
      context
          .messages()
          .send(
              t,
              "<gray>Dein Flugmodus wurde von <yellow>"
                  + sender.getName()
                  + (value ? " <green>aktiviert<gray>." : " <red>deaktiviert<gray>."));
    }
    return true;
  }

  @Override
  public java.util.List<String> onTabComplete(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    return a.length == 1
        ? Bukkit.getOnlinePlayers().stream().map(Player::getName).toList()
        : java.util.List.of();
  }
}
