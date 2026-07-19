package eu.hunfeld.flunarBauserver.settings;

import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;

/** Optional LabyMod 4 client features. Every feature can be switched off independently. */
public record LabyModSettings(
    boolean enabled,
    boolean logConnections,
    FancyFont fancyFont,
    Discord discord,
    PlayingGameMode playingGameMode,
    Subtitle subtitle,
    TabListBanner tabListBanner,
    ServerListIcon serverListIcon,
    DisabledAddons disabledAddons) {
  public static LabyModSettings from(FileConfiguration config) {
    return new LabyModSettings(
        config.getBoolean("enabled", true),
        config.getBoolean("log-connections", false),
        new FancyFont(
            config.getBoolean("features.fancy-font.enabled", true),
            config.getBoolean("features.fancy-font.allowed", true)),
        new Discord(
            config.getBoolean("features.discord-rich-presence.enabled", true),
            config.getString("features.discord-rich-presence.text", "Flunar Bauserver"),
            config.getBoolean("features.discord-rich-presence.show-session-time", true)),
        new PlayingGameMode(
            config.getBoolean("features.playing-game-mode.enabled", true),
            config.getString("features.playing-game-mode.text", "Flunar.de Bauserver {project}"),
            config.getString("features.playing-game-mode.friend-notification-icon", "◆")),
        new Subtitle(
            config.getBoolean("features.subtitle.enabled", false),
            config.getString("features.subtitle.text", "{player}"),
            Math.max(0.1D, Math.min(3.0D, config.getDouble("features.subtitle.size", 0.8D)))),
        new TabListBanner(
            config.getBoolean("features.tab-list-banner.enabled", false),
            config.getString("features.tab-list-banner.image-url", "")),
        new ServerListIcon(
            config.getBoolean("features.server-list-icon.enabled", false),
            config.getString("features.server-list-icon.image-url", "")),
        new DisabledAddons(
            config.getBoolean("features.disabled-addons.enabled", false),
            List.copyOf(config.getStringList("features.disabled-addons.names"))));
  }

  public record FancyFont(boolean enabled, boolean allowed) {}

  public record Discord(boolean enabled, String text, boolean showSessionTime) {}

  public record PlayingGameMode(boolean enabled, String text, String friendNotificationIcon) {}

  public record Subtitle(boolean enabled, String text, double size) {}

  public record TabListBanner(boolean enabled, String imageUrl) {}

  public record ServerListIcon(boolean enabled, String imageUrl) {}

  public record DisabledAddons(boolean enabled, List<String> names) {}
}
