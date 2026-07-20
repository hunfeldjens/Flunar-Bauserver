package eu.hunfeld.flunarbauserver.commands.player;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import eu.hunfeld.flunarbauserver.gui.BuilderServerMenu;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BauserverMenuCommand extends BaseCommand {
  private final BuilderServerMenu menu;

  public BauserverMenuCommand(BauserverContext context, BuilderServerMenu menu) {
    super(context);
    this.menu = menu;
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
      menu.give(player);
      return true;
    }
    if (args[0].equalsIgnoreCase("menu")) {
      menu.open(player);
      return true;
    }
    if (!menu.open(player, args[0]))
      context
          .messages()
          .send(
              player,
              "<gray>Verfügbar: <green>/bs menu|" + String.join("|", menu.available(player)));
    return true;
  }

  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {
    if (!(sender instanceof Player player) || args.length != 1) return List.of();
    List<String> options = new java.util.ArrayList<>();
    options.add("menu");
    options.addAll(menu.available(player));
    String search = args[0].toLowerCase(java.util.Locale.ROOT);
    return options.stream()
        .filter(value -> value.toLowerCase(java.util.Locale.ROOT).startsWith(search))
        .toList();
  }
}
