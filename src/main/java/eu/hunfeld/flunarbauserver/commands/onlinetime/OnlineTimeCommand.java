package eu.hunfeld.flunarbauserver.commands.onlinetime;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.commands.BaseCommand;
import eu.hunfeld.flunarbauserver.gui.OnlineTimeMenu;
import eu.hunfeld.flunarbauserver.model.OnlineTimeRecord;
import eu.hunfeld.flunarbauserver.service.OnlineTimeService;
import eu.hunfeld.flunarbauserver.utils.Players;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class OnlineTimeCommand extends BaseCommand {
  private final OnlineTimeService service;
  private final OnlineTimeMenu menu;

  public OnlineTimeCommand(
      BauserverContext context, OnlineTimeService service, OnlineTimeMenu menu) {
    super(context);
    this.service = service;
    this.menu = menu;
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    if (!requireDatabase(sender)) return true;
    if (args.length == 0) {
      if (!(sender instanceof Player player)) {
        context.messages().send(sender, "<red>Dieser Befehl ist nur ingame verfügbar.");
        return true;
      }
      if (player.hasPermission("bauserver.admin")) menu.open(player, 1);
      else show(sender, player);
      return true;
    }

    if (args[0].equalsIgnoreCase("export")) {
      if (!require(sender, "bauserver.admin")) return true;
      export(sender);
      return true;
    }
    if (args[0].equalsIgnoreCase("reset")) {
      if (!require(sender, "bauserver.admin")) return true;
      if (args.length < 2) {
        context.messages().send(sender, "<green>/onlinezeit reset <Spieler>");
        return true;
      }
      OfflinePlayer target = Players.known(args[1]).orElse(null);
      if (target == null) {
        context.messages().send(sender, "<red>Spieler nicht gefunden.");
        return true;
      }
      context
          .onlineTime()
          .reset(target.getUniqueId())
          .whenComplete(
              (success, error) ->
                  Bukkit.getScheduler()
                      .runTask(
                          context.plugin(),
                          () -> {
                            if (error != null || !Boolean.TRUE.equals(success)) {
                              context
                                  .messages()
                                  .send(sender, "<red>Die Aktion konnte nicht gespeichert werden.");
                              return;
                            }
                            service.resetSession(target.getUniqueId());
                            context
                                .messages()
                                .send(
                                    sender,
                                    "<gray>Onlinezeit von <green>"
                                        + target.getName()
                                        + " <gray>wurde zurückgesetzt.");
                          }));
      return true;
    }

    OfflinePlayer target = Players.known(args[0]).orElse(null);
    if (target == null) context.messages().send(sender, "<red>Spieler nicht gefunden.");
    else show(sender, target);
    return true;
  }

  private void show(CommandSender sender, OfflinePlayer target) {
    OnlineTimeRecord record = context.onlineTime().get(target.getUniqueId()).orElse(null);
    if (record == null) {
      context.messages().send(sender, "<red>Spieler nicht gefunden.");
      return;
    }
    int active = record.activeSeconds() + service.sessionActive(record.uuid());
    int afk = record.afkSeconds() + service.sessionAfk(record.uuid());
    int total = active + afk;
    context.messages().raw(sender, "");
    context
        .messages()
        .raw(
            sender,
            "<dark_gray><strikethrough>                                        </strikethrough>");
    context.messages().send(sender, "<gray>Name: <yellow>" + record.name());
    if (!sender.hasPermission("bauserver.admin")) {
      context
          .messages()
          .send(sender, "<gray>Gesamte Onlinezeit: <green>" + OnlineTimeMenu.teamTime(total));
      context
          .messages()
          .raw(
              sender,
              "<dark_gray><strikethrough>                                        </strikethrough>");
      return;
    }
    Player online = target.getPlayer();
    String world = online == null ? "-" : online.getWorld().getName();
    String session =
        online == null ? "-" : OnlineTimeMenu.dhm(service.sessionActive(record.uuid()));
    context.messages().send(sender, "<gray>Anzahl der Verbindungen: <yellow>" + record.joins());
    context.messages().send(sender, "<gray>Aktuelles Projekt: <yellow>" + world);
    context.messages().send(sender, "<gray>Aktive Session: <green>" + session);
    if (online != null && service.isAfk(record.uuid()))
      context
          .messages()
          .send(
              sender,
              "<gray>AFK Session: <red>" + OnlineTimeMenu.dhm(service.sessionAfk(record.uuid())));
    context
        .messages()
        .send(sender, "<gray>Erster Login: <yellow>" + OnlineTimeMenu.date(record.firstSeen()));
    context
        .messages()
        .send(sender, "<gray>Letzter Login: <yellow>" + OnlineTimeMenu.date(record.lastSeen()));
    context
        .messages()
        .send(
            sender,
            "<gray>Aktiv Zeit: <green>"
                + OnlineTimeMenu.dhm(active)
                + " <gray>- AFK: <red>"
                + OnlineTimeMenu.dhm(afk));
    context.messages().send(sender, "<gray>Gesamt Zeit: <green>" + OnlineTimeMenu.full(total));
    context
        .messages()
        .raw(
            sender,
            "<dark_gray><strikethrough>                                        </strikethrough>");
  }

  private void export(CommandSender sender) {
    List<OnlineTimeRecord> records = context.onlineTime().all();
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            context.plugin(),
            () -> {
              try {
                StringBuilder yaml = new StringBuilder("# Flunar Onlinezeit Export\n");
                yaml.append("# Erstellt am: ").append(LocalDateTime.now()).append("\n\n");
                for (OnlineTimeRecord record : records) {
                  int active = record.activeSeconds();
                  int afk = record.afkSeconds();
                  int total = active + afk;
                  yaml.append(record.name())
                      .append(":\n")
                      .append("  uuid: '")
                      .append(record.uuid())
                      .append("'\n")
                      .append("  joins: ")
                      .append(record.joins())
                      .append('\n')
                      .append("  onlinetime_aktiv_sek: ")
                      .append(active)
                      .append('\n')
                      .append("  onlinetime_aktiv: '")
                      .append(OnlineTimeMenu.dhm(active))
                      .append("'\n")
                      .append("  onlinetime_afk_sek: ")
                      .append(afk)
                      .append('\n')
                      .append("  onlinetime_afk: '")
                      .append(OnlineTimeMenu.dhm(afk))
                      .append("'\n")
                      .append("  onlinetime_gesamt_sek: ")
                      .append(total)
                      .append('\n')
                      .append("  onlinetime_gesamt: '")
                      .append(OnlineTimeMenu.full(total))
                      .append("'\n")
                      .append("  first_seen: '")
                      .append(OnlineTimeMenu.date(record.firstSeen()))
                      .append("'\n")
                      .append("  last_seen: '")
                      .append(OnlineTimeMenu.date(record.lastSeen()))
                      .append("'\n\n");
                }
                Path directory = context.plugin().getDataFolder().toPath().resolve("exports");
                Files.createDirectories(directory);
                Files.writeString(
                    directory.resolve("onlinezeit_export.yml"),
                    yaml.toString(),
                    StandardCharsets.UTF_8);
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> {
                          context.messages().send(sender, "<green>Export abgeschlossen!");
                          context
                              .messages()
                              .send(
                                  sender,
                                  "<gray>Datei: <yellow>plugins/Flunar-Bauserver/exports/onlinezeit_export.yml");
                        });
              } catch (Exception exception) {
                Bukkit.getScheduler()
                    .runTask(
                        context.plugin(),
                        () -> context.messages().send(sender, "<red>Export fehlgeschlagen."));
              }
            });
  }

  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {
    if (args.length == 1 && sender.hasPermission("bauserver.admin"))
      return filter(List.of("reset", "export"), args[0]);
    if (args.length == 2
        && args[0].equalsIgnoreCase("reset")
        && sender.hasPermission("bauserver.admin"))
      return Bukkit.getOnlinePlayers().stream()
          .map(Player::getName)
          .filter(
              name -> name.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
          .toList();
    return List.of();
  }

  private static List<String> filter(List<String> values, String input) {
    String lower = input.toLowerCase(Locale.ROOT);
    return values.stream()
        .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lower))
        .toList();
  }
}
