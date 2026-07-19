package eu.hunfeld.flunarBauserver.commands.project;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import eu.hunfeld.flunarBauserver.gui.ProjectInfoMenu;
import eu.hunfeld.flunarBauserver.gui.ProjectMenu;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Thin router only. All project behavior lives in one class per subcommand. */
public final class ProjectCommand extends BaseCommand {
  private final ProjectSubcommand overview;
  private final ProjectSubcommand help;
  private final Map<String, ProjectSubcommand> subcommands = new LinkedHashMap<>();

  public ProjectCommand(BauserverContext context, ProjectMenu menu, ProjectInfoMenu infoMenu) {
    super(context);
    overview = new ProjectOverviewSubcommand(context, menu);
    help = new ProjectHelpSubcommand(context);
    subcommands.put("admin", new ProjectAdminSubcommand(context, menu));
    subcommands.put("autoload", new ProjectAutoloadSubcommand(context));
    subcommands.put("create", new ProjectCreateSubcommand(context));
    subcommands.put("join", new ProjectJoinSubcommand(context));
    subcommands.put("remove", new ProjectRemoveSubcommand(context));
    subcommands.put("tp", new ProjectTeleportSubcommand(context));
    subcommands.put("info", new ProjectInfoSubcommand(context, infoMenu));
    subcommands.put("seticon", new ProjectSetIconSubcommand(context));
    subcommands.put("whitelist", new ProjectWhitelistSubcommand(context));
    subcommands.put("kick", new ProjectKickSubcommand(context));
    subcommands.put("ban", new ProjectBanSubcommand(context));
    subcommands.put("unban", new ProjectUnbanSubcommand(context));
    subcommands.put("export", new ProjectExportSubcommand(context));
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    Player player = player(sender);
    if (player == null) return true;
    if (args.length == 0) overview.execute(player, args);
    else subcommands.getOrDefault(args[0].toLowerCase(Locale.ROOT), help).execute(player, args);
    return true;
  }

  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {
    if (!(sender instanceof Player player)) return List.of();
    if (args.length == 1) {
      List<String> available = new ArrayList<>();
      if (player.hasPermission("bauserver.team")) available.addAll(List.of("join", "kick"));
      if (player.hasPermission("bauserver.builder")) available.addAll(List.of("info", "ban"));
      if (player.hasPermission("bauserver.srbuilder"))
        available.addAll(
            List.of("unban", "autoload", "create", "remove", "seticon", "whitelist", "admin"));
      if (player.hasPermission("bauserver.admin")) available.addAll(List.of("tp", "export"));
      return AbstractProjectSubcommand.filter(available, args[0]);
    }
    ProjectSubcommand subcommand = subcommands.get(args[0].toLowerCase(Locale.ROOT));
    return subcommand == null ? List.of() : subcommand.tabComplete(player, args);
  }
}
