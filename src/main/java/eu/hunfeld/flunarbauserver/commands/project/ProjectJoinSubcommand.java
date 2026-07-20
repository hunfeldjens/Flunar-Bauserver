package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.model.Project;
import eu.hunfeld.flunarbauserver.utils.UiSound;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

final class ProjectJoinSubcommand extends AbstractProjectSubcommand {
  ProjectJoinSubcommand(BauserverContext context) {
    super(context);
  }

  @Override
  public void execute(Player player, String[] args) {
    if (!database(player)) return;
    if (args.length < 2) {
      context.messages().send(player, "<gray>Benutzung: <green>/projekt join <Projekt>");
      return;
    }
    Project project = context.projects().byWorld(args[1]).orElse(null);
    if (project == null) {
      context.messages().send(player, "<red>Projekt nicht gefunden.");
      return;
    }
    if (!context
        .projectAccess()
        .mayEnter(
            player.getUniqueId(), project.worldName(), player.hasPermission("bauserver.admin"))) {
      context.messages().send(player, "<red>Du hast keinen Zugriff auf dieses Projekt.");
      return;
    }
    try {
      World world =
          context
              .worlds()
              .loaded(project.worldName())
              .orElseGet(() -> context.worlds().createProject(project.worldName(), "normal"));
      context.teleports().remember(player);
      if (player.getWorld() != world) context.worlds().clearPlayer(player);
      context
          .worlds()
          .teleport(player, world.getSpawnLocation())
          .thenAccept(
              success ->
                  Bukkit.getScheduler()
                      .runTask(
                          context.plugin(),
                          () -> {
                            if (success) {
                              context
                                  .messages()
                                  .action(
                                      player,
                                      "<gold>Projekt <yellow>"
                                          + project.worldName()
                                          + " <gold>betreten");
                              UiSound.TELEPORT.play(player);
                            } else {
                              UiSound.ERROR.play(player);
                            }
                          }));
    } catch (RuntimeException exception) {
      context.messages().send(player, "<red>Die Projektwelt konnte nicht geladen werden.");
    }
  }

  @Override
  public List<String> tabComplete(Player player, String[] args) {
    if (args.length != 2) return List.of();
    return filter(
        context.projects().all().stream()
            .filter(
                project ->
                    context
                        .projectAccess()
                        .mayEnter(
                            player.getUniqueId(),
                            project.worldName(),
                            player.hasPermission("bauserver.admin")))
            .map(Project::worldName)
            .toList(),
        args[1]);
  }
}
