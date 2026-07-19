package eu.hunfeld.flunarBauserver.chat;

import eu.hunfeld.flunarBauserver.FlunarBauserver;
import eu.hunfeld.flunarBauserver.utils.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class TabService {
  private final FlunarBauserver plugin;
  private final Messages messages;
  private final PlaceholderBridge placeholders = new PlaceholderBridge();
  private final LuckPermsBridge luckPerms = new LuckPermsBridge();
  private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();

  public TabService(FlunarBauserver plugin, Messages messages) {
    this.plugin = plugin;
    this.messages = messages;
    luckPerms.subscribe(
        plugin,
        uuid ->
            Bukkit.getScheduler()
                .runTask(
                    plugin,
                    () -> {
                      Player player = Bukkit.getPlayer(uuid);
                      if (player != null) update(player);
                    }));
  }

  public Component prefix(Player player) {
    String value = luckPerms.prefix(player);
    if (value == null) value = placeholders.apply(player, "%luckperms_prefix%");
    if (value.equals("%luckperms_prefix%")) value = "";
    return legacy.deserialize(org.bukkit.ChatColor.translateAlternateColorCodes('&', value));
  }

  public void update(Player player) {
    Component prefix = prefix(player);
    player.playerListName(prefix.append(Component.text(player.getName(), NamedTextColor.WHITE)));
    Integer directWeight = luckPerms.highestGroupWeight(player);
    int weight =
        directWeight == null
            ? parseWeight(placeholders.apply(player, "%luckperms_highest_group_weight%"))
            : directWeight;
    // Seit 26.x wird playerListOrder vor der Scoreboard-Team-Sortierung ausgewertet.
    // Größere LuckPerms-Gewichte sollen deshalb weiter oben stehen.
    player.setPlayerListOrder(Math.max(1, weight + 1));
    updateTeam(player, prefix, weight);
    Component header =
        messages.parse(
            "\n<dark_gray><strikethrough>        </strikethrough> <bold><gradient:#1FADFF:#ABE0FF>Flunar.de</gradient></bold> <dark_gray><strikethrough>        </strikethrough>\n<white>Bauserver <dark_gray>• <gray>Online: <aqua>"
                + Bukkit.getOnlinePlayers().size()
                + "<dark_gray>/<aqua>"
                + Bukkit.getMaxPlayers()
                + "\n");
    Component footer =
        messages.parse(
            "\n<dark_gray>Projektbau <white>• <aqua>Kreativität <white>• <gray>Teamwork\n<gray>Discord: <aqua>dc.flunar.de\n");
    player.sendPlayerListHeaderAndFooter(header, footer);
  }

  public void updateAll() {
    Bukkit.getOnlinePlayers().forEach(this::update);
  }

  /** Entfernt den Spieler aus seinem Sortier-Team, damit keine alten Nametags zurückbleiben. */
  public void remove(Player player) {
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    Team current = scoreboard.getEntryTeam(player.getName());
    if (current == null || !current.getName().startsWith("bs_")) return;
    current.removeEntry(player.getName());
    if (current.getEntries().isEmpty()) current.unregister();
  }

  public void updateDelayed() {
    Bukkit.getScheduler().runTaskLater(plugin, this::updateAll, 5L);
  }

  private static int parseWeight(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  private void updateTeam(Player player, Component prefix, int weight) {
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    String teamName = teamName(player, weight);
    Team current = scoreboard.getEntryTeam(player.getName());
    if (current != null && !current.getName().equals(teamName)) {
      current.removeEntry(player.getName());
      if (current.getName().startsWith("bs_") && current.getEntries().isEmpty())
        current.unregister();
    }
    Team team = scoreboard.getTeam(teamName);
    if (team == null) team = scoreboard.registerNewTeam(teamName);
    team.prefix(prefix);
    team.color(NamedTextColor.WHITE);
    if (!team.hasEntry(player.getName())) team.addEntry(player.getName());
  }

  private static String teamName(Player player, int weight) {
    int order = Math.max(0, Math.min(9999, 9999 - weight));
    return String.format(
        java.util.Locale.ROOT, "bs_%04d_%08x", order, player.getUniqueId().hashCode());
  }
}
