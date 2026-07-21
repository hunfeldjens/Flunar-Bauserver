package eu.hunfeld.flunarbauserver.service;

import eu.hunfeld.flunarbauserver.FlunarBauserver;
import eu.hunfeld.flunarbauserver.chat.PlaceholderBridge;
import eu.hunfeld.flunarbauserver.settings.LabyModSettings;
import net.labymod.serverapi.api.model.component.ServerAPIComponent;
import net.labymod.serverapi.core.model.feature.DiscordRPC;
import net.labymod.serverapi.core.model.feature.Feature;
import net.labymod.serverapi.core.packet.clientbound.game.feature.UpdateFeaturePacket;
import net.labymod.serverapi.server.bukkit.LabyModPlayer;
import net.labymod.serverapi.server.bukkit.LabyModProtocolService;
import net.labymod.serverapi.server.bukkit.event.LabyModPlayerJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public final class LabyModService implements Listener {
  private final FlunarBauserver plugin;
  private final LabyModSettings settings;

  public LabyModService(FlunarBauserver plugin, LabyModSettings settings) {
    this.plugin = plugin;
    this.settings = settings;
    LabyModProtocolService protocol = LabyModProtocolService.get();
    if (protocol == null || !protocol.isInitialized())
      throw new IllegalStateException("LabyModServerAPI ist noch nicht initialisiert");
  }

  @EventHandler
  public void onLabyModJoin(LabyModPlayerJoinEvent event) {
    apply(event.labyModPlayer());
    if (settings.logConnections())
      plugin
          .getLogger()
          .info(
              "LabyMod-Spieler erkannt: "
                  + event.labyModPlayer().getUniqueId()
                  + " ("
                  + event.labyModPlayer().getLabyModVersion()
                  + ")");
  }

  @EventHandler
  public void onWorldChange(PlayerChangedWorldEvent event) {
    LabyModProtocolService protocol = LabyModProtocolService.get();
    if (!protocol.isUsingLabyMod(event.getPlayer().getUniqueId())) return;
    LabyModPlayer player = (LabyModPlayer) protocol.getPlayer(event.getPlayer().getUniqueId());
    if (player != null) apply(player);
  }

  private void apply(LabyModPlayer labyPlayer) {
    Player player = Bukkit.getPlayer(labyPlayer.getUniqueId());
    if (player == null) return;

    if (settings.fancyFont().enabled())
      labyPlayer.sendPacket(
          new UpdateFeaturePacket(
              settings.fancyFont().allowed()
                  ? Feature.FANCY_FONT.enable()
                  : Feature.FANCY_FONT.disable()));

    if (settings.discord().enabled()) {
      String text = text(player, settings.discord().text());
      labyPlayer.sendDiscordRPC(
          settings.discord().showSessionTime()
              ? DiscordRPC.createWithStart(text, System.currentTimeMillis())
              : DiscordRPC.create(text));
    }

    if (settings.playingGameMode().enabled())
      labyPlayer.sendPlayingGameMode(
          icon(settings.playingGameMode().friendNotificationIcon())
              + text(player, settings.playingGameMode().text()));

    if (settings.tabListBanner().enabled() && !settings.tabListBanner().imageUrl().isBlank())
      labyPlayer.sendTabListBanner(settings.tabListBanner().imageUrl());

    if (settings.disabledAddons().enabled() && !settings.disabledAddons().names().isEmpty())
      labyPlayer.disableAddons(settings.disabledAddons().names());

    if (settings.subtitle().enabled())
      labyPlayer.updateSubtitle(
          ServerAPIComponent.text(text(player, settings.subtitle().text())),
          settings.subtitle().size());
    else if (labyPlayer.hasSubtitle()) labyPlayer.resetSubtitle();
  }

  private String text(Player player, String configured) {
    String value =
        configured
            .replace("{player}", player.getName())
            .replace("{world}", player.getWorld().getName())
            .replace("{project}", worldDisplay(player));
    return PlaceholderBridge.apply(player, value);
  }

  private static String icon(String configured) {
    return configured == null || configured.isBlank() ? "" : configured.strip() + " ";
  }

  private static String worldDisplay(Player player) {
    String namespace = player.getWorld().getKey().namespace();
    if (namespace.equals("privat")) return "Privat Map";
    String value =
        namespace.equals("projekt")
            ? player.getWorld().getKey().value()
            : player.getWorld().getName();
    String[] words = value.replace('_', ' ').replace('-', ' ').strip().split("\\s+");
    StringBuilder result = new StringBuilder();
    for (String word : words) {
      if (word.isBlank()) continue;
      if (!result.isEmpty()) result.append(' ');
      result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
    }
    return result.isEmpty() ? "Bauserver" : result.toString();
  }
}
