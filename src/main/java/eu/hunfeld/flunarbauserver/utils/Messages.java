package eu.hunfeld.flunarbauserver.utils;

import eu.hunfeld.flunarbauserver.settings.Settings;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Messages {
  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
  private static final LegacyComponentSerializer LEGACY_AMPERSAND =
      LegacyComponentSerializer.builder()
          .character('&')
          .hexColors()
          .useUnusualXRepeatedCharacterHexFormat()
          .build();
  private final Settings settings;

  public Messages(Settings settings) {
    this.settings = settings;
  }

  public Component parse(String text) {
    return MINI_MESSAGE.deserialize(text == null ? "" : text);
  }

  public Component parseLegacy(String text) {
    return LEGACY_AMPERSAND.deserialize(text == null ? "" : text);
  }

  public Component parse(String text, Map<String, ?> values) {
    String result = text == null ? "" : text;
    for (var entry : values.entrySet())
      result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
    return parse(result);
  }

  public void send(CommandSender sender, String message) {
    sender.sendMessage(parse(settings.prefix() + " " + message));
  }

  public void raw(CommandSender sender, String message) {
    sender.sendMessage(parse(message));
  }

  public void broadcast(String message) {
    Component component = parse(settings.prefix() + " " + message);
    Bukkit.getServer().sendMessage(component);
  }

  public void action(Player player, String message) {
    player.sendActionBar(parse(message));
  }

  public void actionAll(String message) {
    Bukkit.getOnlinePlayers().forEach(player -> action(player, message));
  }

  public void noPermission(CommandSender sender) {
    send(sender, "<gray>Leider kennen wir diesen Befehl nicht.");
  }
}
