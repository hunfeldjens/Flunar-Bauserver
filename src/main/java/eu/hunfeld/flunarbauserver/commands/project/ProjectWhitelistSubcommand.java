package eu.hunfeld.flunarbauserver.commands.project;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.utils.Players;
import java.util.*;
import org.bukkit.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

final class ProjectWhitelistSubcommand extends AbstractProjectSubcommand {
  ProjectWhitelistSubcommand(BauserverContext c) {
    super(c);
  }

  public void execute(Player p, String[] a) {
    if (!require(p, "bauserver.srbuilder") || !database(p)) return;
    String world = currentWorld(p);
    if (!context.projects().exists(world)) {
      context.messages().send(p, "<red>Du stehst auf keiner Projektwelt.");
      return;
    }
    if (a.length < 2) {
      usage(p);
      return;
    }
    String action = a[1].toLowerCase();
    if (action.equals("on") || action.equals("off")) {
      boolean active = action.equals("on");
      boolean currentlyActive = context.projects().byWorld(world).orElseThrow().whitelistActive();
      if (currentlyActive == active) {
        context
            .messages()
            .send(
                p,
                active
                    ? "<yellow>Die Whitelist ist für dieses Projekt bereits aktiviert."
                    : "<yellow>Die Whitelist ist für dieses Projekt bereits deaktiviert.");
        return;
      }
      context
          .projects()
          .setWhitelistActive(world, active)
          .whenComplete(
              (ok, _) ->
                  Bukkit.getScheduler()
                      .runTask(
                          context.plugin(),
                          () -> {
                            if (!Boolean.TRUE.equals(ok)) {
                              context
                                  .messages()
                                  .send(p, "<red>Die Whitelist konnte nicht gespeichert werden.");
                              return;
                            }
                            context
                                .messages()
                                .send(
                                    p,
                                    "<gray>Whitelist für <green>"
                                        + world
                                        + (active
                                            ? " <green>aktiviert<gray>."
                                            : " <red>deaktiviert<gray>."));
                            if (active) {
                              int moved =
                                  context
                                      .worlds()
                                      .evacuateUnauthorized(
                                          world,
                                          "<gray>Die Welt <green>"
                                              + world
                                              + " <gray>ist jetzt nur noch für freigeschaltete Spieler zugänglich.");
                              if (moved > 0)
                                context
                                    .messages()
                                    .send(
                                        p,
                                        "<yellow>"
                                            + moved
                                            + " <gray>nicht freigeschaltete Spieler wurden entfernt.");
                            }
                          }));
      return;
    }
    if (a.length < 3 || (!action.equals("add") && !action.equals("remove"))) {
      usage(p);
      return;
    }
    OfflinePlayer target = Players.known(a[2]).orElse(null);
    if (target == null) {
      context.messages().send(p, "<red>Spieler nicht gefunden.");
      return;
    }
    boolean add = action.equals("add");
    String targetName = target.getName() == null ? a[2] : target.getName();
    boolean whitelisted = context.projectAccess().isWhitelisted(target.getUniqueId(), world);
    if (add == whitelisted) {
      context
          .messages()
          .send(
              p,
              add
                  ? "<yellow>" + targetName + " <gray>steht bereits auf dieser Whitelist."
                  : "<yellow>" + targetName + " <gray>steht nicht auf dieser Whitelist.");
      return;
    }
    context
        .projectAccess()
        .setWhitelist(target.getUniqueId(), world, add)
        .whenComplete(
            (ok, _) ->
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> {
                          if (!Boolean.TRUE.equals(ok)) {
                            boolean desiredState =
                                context.projectAccess().isWhitelisted(target.getUniqueId(), world);
                            if (desiredState == add) {
                              context
                                  .messages()
                                  .send(
                                      p,
                                      add
                                          ? "<yellow>"
                                              + targetName
                                              + " <gray>steht bereits auf dieser Whitelist."
                                          : "<yellow>"
                                              + targetName
                                              + " <gray>steht nicht auf dieser Whitelist.");
                              return;
                            }
                            context
                                .messages()
                                .send(
                                    p,
                                    "<red>Der Whitelist-Eintrag konnte nicht gespeichert werden.");
                            return;
                          }
                          context
                              .messages()
                              .send(
                                  p,
                                  "<green>"
                                      + targetName
                                      + (add
                                          ? " <gray>wurde für <green>"
                                              + world
                                              + " <gray>freigeschaltet."
                                          : " <gray>wurde von der Whitelist für <green>"
                                              + world
                                              + " <gray>entfernt."));
                          Player online = Bukkit.getPlayer(target.getUniqueId());
                          if (online == null) return;
                          context
                              .messages()
                              .send(
                                  online,
                                  add
                                      ? "<gray>Du wurdest auf <green>"
                                          + world
                                          + " <gray>freigeschaltet."
                                      : "<gray>Du wurdest von der Whitelist für <green>"
                                          + world
                                          + " <gray>entfernt.");
                          if (!add
                              && context
                                  .worlds()
                                  .cleanName(online.getWorld().getName())
                                  .equals(world)
                              && !context.worlds().mayEnter(online, online.getWorld())) {
                            context.worlds().clearPlayer(online);
                            context
                                .worlds()
                                .moveToMain(
                                    online,
                                    "<gray>Du wurdest aus <green>"
                                        + world
                                        + " <gray>entfernt, weil du nicht mehr freigeschaltet bist.");
                          }
                        }));
  }

  private void usage(Player p) {
    context.messages().send(p, "<gray>/projekt whitelist <on|off|add|remove> [Spieler]");
  }

  public List<String> tabComplete(Player p, String[] a) {
    if (a.length == 2) return filter(List.of("on", "off", "add", "remove"), a[1]);
    if (a.length == 3)
      return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), a[2]);
    return List.of();
  }
}
