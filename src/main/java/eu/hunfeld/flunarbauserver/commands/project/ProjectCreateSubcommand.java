package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.database.Sql;
import eu.hunfeld.flunarbauserver.model.Project;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

final class ProjectCreateSubcommand extends AbstractProjectSubcommand {
  ProjectCreateSubcommand(BauserverContext context) {
    super(context);
  }

  @Override
  @SuppressWarnings("resource")
  public void execute(Player player, String[] args) {
    if (!require(player, "bauserver.srbuilder") || !database(player)) return;
    if (args.length < 5) {
      context
          .messages()
          .send(player, "<gray>/projekt create <Name> <normal|flat|air> <Icon> <Beschreibung>");
      return;
    }
    String name = args[1],
        worldName = Sql.cleanWorld(name),
        type = args[2].toLowerCase(Locale.ROOT),
        icon = args[3].toLowerCase(Locale.ROOT);
    String description = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
    Material material = Material.matchMaterial(icon);
    if (!List.of("normal", "flat", "air").contains(type)
        || material == null
        || !material.isItem()) {
      context.messages().send(player, "<red>Typ oder Icon ist ungültig.");
      return;
    }
    if (context.backups().safeWorldLocked(worldName)
        || context.projects().exists(worldName)
        || context.worlds().loaded(worldName).isPresent()) {
      context
          .messages()
          .send(player, "<red>Projekt kann momentan nicht erstellt werden oder existiert bereits.");
      return;
    }
    World world;
    try {
      world =
          type.equals("air")
              ? context.worlds().createVoid(worldName)
              : context.worlds().createProject(worldName, type);
      if (type.equals("air")) {
        world.getBlockAt(0, 99, 0).setType(Material.STONE);
        world.setSpawnLocation(0, 100, 0);
      }
    } catch (RuntimeException exception) {
      context
          .messages()
          .send(player, "<red>Welt konnte nicht erstellt werden: " + exception.getMessage());
      return;
    }
    Project project = new Project(name, description, worldName, player.getUniqueId(), true, icon);
    context
        .projects()
        .save(project)
        .thenCompose(
            ok ->
                ok
                    ? context.projectAccess().setWhitelist(player.getUniqueId(), worldName, true)
                    : CompletableFuture.completedFuture(false))
        .whenComplete(
            (ok, error) ->
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> {
                          if (error != null || !Boolean.TRUE.equals(ok)) {
                            context
                                .messages()
                                .send(
                                    player,
                                    "<red>Welt erstellt, Datenbankeintrag fehlgeschlagen; bitte Konsole prüfen.");
                            return;
                          }
                          context
                              .messages()
                              .send(
                                  player,
                                  "<gray>Projekt <green>"
                                      + name
                                      + " <gray>wurde erstellt. <dark_gray>(Icon: "
                                      + icon
                                      + ")");
                          context
                              .messages()
                              .send(player, "<gray>Projektwelt: <yellow>" + worldName);
                          context
                              .messages()
                              .send(
                                  player,
                                  "<gray>Whitelist ist <green>aktiv<gray>. Du wurdest automatisch eingetragen.");
                          context.messages().send(player, "<green>Gesetzte Spielregeln:");
                          context
                              .worlds()
                              .gameruleSummary(world)
                              .forEach(line -> context.messages().send(player, line));
                          Component teleport =
                              context
                                  .messages()
                                  .parse(
                                      context.settings().prefix()
                                          + " <gray>Klicke hier: <green><bold>[Zum Projekt teleportieren]</bold>")
                                  .clickEvent(ClickEvent.runCommand("/projekt join " + worldName))
                                  .hoverEvent(
                                      HoverEvent.showText(
                                          context
                                              .messages()
                                              .parse("<green>Projekt " + name + " betreten")));
                          player.sendMessage(teleport);
                        }));
  }

  @Override
  public List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("bauserver.srbuilder")) return List.of();
    return switch (args.length) {
      case 2 -> filter(List.of("<ProjektName>"), args[1]);
      case 3 -> filter(List.of("normal", "flat", "air"), args[2]);
      case 4 ->
          filter(
              List.of(
                  "map",
                  "grass_block",
                  "diamond_boots",
                  "iron_pickaxe",
                  "oak_sign",
                  "chest",
                  "beacon"),
              args[3]);
      default -> List.of();
    };
  }
}
