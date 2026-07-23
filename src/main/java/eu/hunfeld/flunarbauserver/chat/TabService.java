package eu.hunfeld.flunarbauserver.chat;

import eu.hunfeld.flunarbauserver.FlunarBauserver;
import eu.hunfeld.flunarbauserver.settings.Settings;
import eu.hunfeld.flunarbauserver.utils.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@SuppressWarnings("SpellCheckingInspection")
public final class TabService {
  private static final LegacyComponentSerializer LEGACY_AMPERSAND =
      LegacyComponentSerializer.legacyAmpersand();

  private final FlunarBauserver plugin;
  private final Messages messages;
  private final Settings.TabList tabList;

  public TabService(FlunarBauserver plugin, Messages messages, Settings.TabList tabList) {
    this.plugin = plugin;
    this.messages = messages;
    this.tabList = tabList;
    LuckPermsBridge.subscribe(
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
    String value = LuckPermsBridge.prefix(player);
    if (value == null) value = PlaceholderBridge.apply(player, "%luckperms_prefix%");
    if (value.equals("%luckperms_prefix%")) value = "";
    return LEGACY_AMPERSAND.deserialize(value);
  }

  public void update(Player player) {
    Component prefix = prefix(player);
    player.playerListName(prefix.append(Component.text(player.getName(), NamedTextColor.WHITE)));
    Integer directWeight = LuckPermsBridge.highestGroupWeight(player);
    int weight =
        directWeight == null
            ? parseWeight(PlaceholderBridge.apply(player, "%luckperms_highest_group_weight%"))
            : directWeight;
    player.setPlayerListOrder(Math.max(1, weight + 1));
    updateTeam(player, prefix, weight);
    player.sendPlayerListHeaderAndFooter(
        messages.parse(tabText(tabList.header())), messages.parse(tabText(tabList.footer())));
  }

  public void updateAll() {
    Bukkit.getOnlinePlayers().forEach(this::update);
  }


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

  private static String tabText(String value) {
    return value
        .replace("{online}", Integer.toString(Bukkit.getOnlinePlayers().size()))
        .replace("{max_players}", Integer.toString(Bukkit.getMaxPlayers()));
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
    int order = Math.clamp(9999 - weight, 0, 9999);
    return String.format(
        java.util.Locale.ROOT, "bs_%04d_%08x", order, player.getUniqueId().hashCode());
  }
}
