package eu.hunfeld.flunarbauserver.commands.player;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import java.util.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class GameModeCommand extends BaseCommand {
  public GameModeCommand(BauserverContext c) {
    super(c);
  }

  public boolean onCommand(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    Player p = player(s);
    if (p == null) return true;
    if (a.length < 1) {
      context.messages().send(p, "<gray>Bitte benutze <green>/gm 0|1|2|3 <gray>(oder s/c/a/sp)");
      return true;
    }
    if (a.length > 1 && !p.hasPermission("bauserver.builder")) {
      context.messages().noPermission(p);
      return true;
    }
    Player t = a.length > 1 ? Bukkit.getPlayerExact(a[1]) : p;
    if (t == null) {
      context.messages().send(p, context.settings().messages().playerNotOnline());
      return true;
    }
    GameMode mode =
        switch (a[0].toLowerCase()) {
          case "0", "survival", "s" -> GameMode.SURVIVAL;
          case "1", "creative", "c" -> GameMode.CREATIVE;
          case "2", "adventure", "a" -> GameMode.ADVENTURE;
          case "3", "spectator", "sp" -> GameMode.SPECTATOR;
          default -> null;
        };
    if (mode == null) {
      context.messages().send(p, "<gray>Bitte benutze <green>/gm 0|1|2|3 <gray>(oder s/c/a/sp)");
      return true;
    }
    t.setGameMode(mode);
    String display =
        switch (mode) {
          case SURVIVAL -> "Survival";
          case CREATIVE -> "Creative";
          case ADVENTURE -> "Adventure";
          case SPECTATOR -> "Spectator";
        };
    context.messages().action(t, "<gray>Spielmodus → <green>" + display);
    if (t != p)
      context
          .messages()
          .send(p, "<gray>Spielmodus von <yellow>" + t.getName() + " <gray>→ <green>" + display);
    return true;
  }

  public List<String> onTabComplete(
      @NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
    return a.length == 1
        ? List.of(
            "c",
            "s",
            "a",
            "sp",
            "1",
            "0",
            "2",
            "3",
            "creative",
            "survival",
            "adventure",
            "spectator")
        : a.length == 2
            ? Bukkit.getOnlinePlayers().stream().map(Player::getName).toList()
            : List.of();
  }
}
