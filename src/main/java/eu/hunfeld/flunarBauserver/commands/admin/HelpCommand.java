package eu.hunfeld.flunarBauserver.commands.admin;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.commands.BaseCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class HelpCommand extends BaseCommand {
  public HelpCommand(BauserverContext context) {
    super(context);
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    Player player = player(sender);
    if (player == null) return true;
    String category = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
    switch (category) {
      case "projekt", "project", "projekte" -> projects(player);
      case "world", "welt", "welten" -> world(player);
      case "tools", "bau", "bauen" -> tools(player);
      case "player", "spieler", "tp" -> players(player);
      case "team" -> team(player);
      case "mod", "moderation", "ban", "kick" -> moderation(player);
      case "admin" -> admin(player);
      default -> overview(player);
    }
    return true;
  }

  private void projects(Player player) {
    header(player, "<green>Projekte");
    if (player.hasPermission("bauserver.builder")) {
      line(player, "/projekt", "Projekt-GUI öffnen");
      line(player, "/projekt info", "Projekt-Info-GUI öffnen");
      line(player, "/projekt info -all", "Alle Projekte anzeigen");
      line(player, "/projekt ban <Spieler>", "Spieler vom Projekt bannen");
      line(player, "/privat", "Eigene private Welt betreten");
    }
    line(player, "/projekt kick <Spieler>", "Spieler aus Welt kicken");
    if (player.hasPermission("bauserver.srbuilder")) {
      blank(player);
      line(player, "/projekt unban <Spieler>", "Projektban aufheben");
      line(player, "/projekt whitelist on|off", "Projekt-Whitelist schalten");
      line(player, "/projekt whitelist add|remove <Spieler>", "Whitelist verwalten");
      line(player, "/projekt create <Name> <Typ> <Icon> <Beschr.>", "Projekt erstellen");
      line(player, "/projekt seticon <Item>", "Projekt-Icon ändern");
      line(player, "/projekt admin", "Verwaltungs-GUI");
    }
    if (player.hasPermission("bauserver.admin")) {
      blank(player);
      line(player, "/projekt tp <Welt>", "Direkt zur Projektwelt teleportieren");
    }
    footer(player);
  }

  private void world(Player player) {
    header(player, "<green>Welt & Umgebung");
    line(player, "/world", "Zurück zur Hauptwelt");
    if (player.hasPermission("bauserver.builder")) {
      line(player, "/back", "Letzte Position vor Teleport/Tod");
      blank(player);
      line(player, "/sun", "Sonnig");
      line(player, "/rain", "Regen");
      line(player, "/storm", "Gewitter");
      line(player, "/tag", "Tag setzen <dark_gray>(Alias: /day)");
      line(player, "/nacht", "Nacht setzen <dark_gray>(Alias: /night)");
    }
    footer(player);
  }

  private void tools(Player player) {
    if (!player.hasPermission("bauserver.builder")) {
      context.messages().noPermission(player);
      return;
    }
    header(player, "<green>Bau-Tools");
    line(player, "/tools", "Operator-Items GUI <dark_gray>(Alias: /opitems)");
    line(player, "/workbench", "Werkbank öffnen <dark_gray>(Alias: /wb, /craft)");
    line(player, "/anvil", "Amboss öffnen");
    line(player, "/enderchest", "Enderkiste öffnen <dark_gray>(Alias: /ec)");
    line(player, "/hideEntity", "Entity unsichtbar machen <dark_gray>(Alias: /he)");
    line(player, "/showEntity", "Entity sichtbar machen <dark_gray>(Alias: /se)");
    line(player, "/size <Wert>", "Spielergröße ändern <dark_gray>(Alias: /scale)");
    blank(player);
    context
        .messages()
        .send(
            player,
            "<gray>Netherite-Aufzug: Springen = <green>Hoch <dark_gray>| <gray>Schleichen = <green>Runter");
    footer(player);
  }

  private void players(Player player) {
    header(player, "<green>Spieler & Teleport");
    line(player, "/tpa <Spieler>", "Teleport-Anfrage senden");
    line(player, "/tpaccept", "Anfrage annehmen");
    line(player, "/tpadeny", "Anfrage ablehnen");
    line(player, "/msg <Spieler> <Text>", "Private Nachricht senden");
    line(player, "/r <Text>", "Auf die letzte private Nachricht antworten");
    line(player, "/bs", "Bauserver-Menü-Item erhalten");
    line(player, "/onlinetime", "Gesamte Onlinezeit anzeigen");
    blank(player);
    if (player.hasPermission("bauserver.builder")) {
      line(player, "/fly [Spieler]", "Flugmodus umschalten");
      line(player, "/gm <Modus> [Spieler]", "Spielmodus wechseln");
      line(player, "/speed <0-10|off>", "Geschwindigkeit ändern");
    }
    line(player, "/ping [Spieler]", "Ping anzeigen <dark_gray>(Alias: /ms)");
    line(player, "/tps", "Server-TPS anzeigen");
    line(player, "/me", "Eigene Spieler-Infos");
    footer(player);
  }

  private void team(Player player) {
    header(player, "<aqua>Team-Übersicht");
    line(player, "/world", "Zur Hauptwelt");
    line(player, "/projekt join <Projekt>", "Freigeschaltetes Projekt direkt betreten");
    line(player, "/tpa <Spieler>", "Teleport-Anfrage");
    line(player, "/projekt kick <Spieler>", "Spieler aus Projektwelt kicken");
    line(player, "/onlinetime", "Gesamte Onlinezeit anzeigen");
    blank(player);
    line(player, "/invsee <Spieler>", "Inventar einsehen");
    line(player, "/endersee <Spieler>", "Enderkiste einsehen <dark_gray>(Alias: /ecsee)");
    line(player, "/me <Spieler>", "Spieler-Infos anzeigen");
    line(player, "/cc", "Chat für alle leeren");
    line(player, "/pcc", "Eigenen Chat leeren");
    line(player, "/tc <Text>", "Nachricht nur an Builder senden");
    footer(player);
  }

  private void moderation(Player player) {
    header(player, "<aqua>Moderation");
    line(player, "/kick <Spieler> <ID>", "Spieler vom Server kicken");
    line(
        player,
        "/ban <Spieler> <ID>",
        "Spieler permanent bannen <dark_gray>(Bypass: bauserver.ban.bypass)");
    line(player, "/unban <Spieler>", "Spieler entbannen");
    line(player, "/banhistory", "Gespeicherte Ban-Historie als GUI");
    line(player, "/kickhistory", "Gespeicherte Kick-Historie als GUI");
    line(player, "/reasons", "Alle Grund-IDs anzeigen");
    blank(player);
    line(player, "/projekt kick <Spieler>", "Ab bauserver.team");
    if (player.hasPermission("bauserver.builder"))
      line(player, "/projekt ban <Spieler>", "Ab bauserver.builder");
    if (player.hasPermission("bauserver.srbuilder"))
      line(player, "/projekt unban <Spieler>", "Ab bauserver.srbuilder");
    footer(player);
  }

  private void admin(Player player) {
    if (!player.hasPermission("bauserver.admin")) {
      context.messages().noPermission(player);
      return;
    }
    header(player, "<red>Admin");
    line(player, "/projekt tp <Welt>", "Direkt teleportieren");
    line(player, "/projekt admin", "Admin-GUI mit Permission-gefilterten Icons");
    line(player, "/backup", "Backup-GUI öffnen");
    line(player, "/serverrestart", "10s Countdown, dann Server stoppen");
    line(player, "/vanish", "Komplett unsichtbar");
    line(player, "/broadcast <Text>", "Server-Broadcast <dark_gray>(Alias: /bro)");
    line(player, "/unloadallworlds", "Alle Welten entladen");
    line(player, "/reset", "Unwichtige Variablen bereinigen");
    line(player, "/feature", "Feature-Toggles GUI");
    blank(player);
    line(player, "/onlinetime <Spieler>", "Detailansicht / GUI");
    line(player, "/onlinetime reset <Spieler>", "Onlinezeit zurücksetzen");
    line(player, "/onlinetime export", "Onlinezeit-Daten exportieren");
    footer(player);
  }

  private void overview(Player player) {
    header(player, "<green>Hilfe");
    context.messages().send(player, "<gray>Verfügbare Hilfe-Kategorien:");
    blank(player);
    line(player, "/help world", "Welt & Navigation");
    line(player, "/help player", "Teleport & Infos");
    line(player, "/help team", "Team-Befehle");
    line(player, "/help mod", "Kick, Ban & Moderation");
    if (player.hasPermission("bauserver.builder")) {
      line(player, "/help projekt", "Projekte & private Welten");
      line(player, "/help tools", "Bau-Tools & Operator-Items");
    }
    if (player.hasPermission("bauserver.admin"))
      line(player, "/help admin", "Admin-Befehle & Onlinezeit");
    footer(player);
  }

  private void header(Player player, String title) {
    context
        .messages()
        .raw(
            player,
            "<dark_gray><strikethrough>               </strikethrough><dark_green>【 "
                + title
                + " <dark_green>】<dark_gray><strikethrough>               </strikethrough>");
    blank(player);
  }

  private void footer(Player player) {
    blank(player);
    context
        .messages()
        .raw(
            player,
            "<dark_gray><strikethrough>                                                </strikethrough>");
  }

  private void line(Player player, String command, String description) {
    context.messages().send(player, "<green>" + command + " <gray>– " + description);
  }

  private void blank(Player player) {
    context.messages().raw(player, "");
  }

  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String alias,
      @NotNull String[] args) {
    if (args.length != 1 || !(sender instanceof Player player)) return List.of();
    List<String> categories = new ArrayList<>(List.of("world", "player", "team", "mod"));
    if (player.hasPermission("bauserver.builder")) categories.addAll(List.of("projekt", "tools"));
    if (player.hasPermission("bauserver.admin")) categories.add("admin");
    String input = args[0].toLowerCase(Locale.ROOT);
    return categories.stream().filter(value -> value.startsWith(input)).toList();
  }
}
