package eu.hunfeld.flunarBauserver.utils;

import eu.hunfeld.flunarBauserver.settings.Settings;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Messages {
  private final MiniMessage mini = MiniMessage.miniMessage();
  private final LegacyComponentSerializer legacyAmpersand =
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
    return mini.deserialize(text == null ? "" : text);
  }

  /** Parst spielerfreundliche Farbcodes wie &a, &l oder &#12ABEF. */
  public Component parseLegacy(String text) {
    return legacyAmpersand.deserialize(text == null ? "" : text);
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

  public void sound(Player player, UiSound sound) {
    player.playSound(player.getLocation(), sound.sound, sound.volume, sound.pitch);
  }

  public enum UiSound {
    OPEN(Sound.BLOCK_BARREL_OPEN, .5f, 1.3f),
    CLICK(Sound.UI_BUTTON_CLICK, .5f, 1f),
    CONFIRM(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .8f, 1.2f),
    CANCEL(Sound.ENTITY_VILLAGER_NO, .8f, 1f),
    TELEPORT(Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, .9f, 1f),
    ERROR(Sound.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER, .8f, 1f),
    NAV(Sound.ITEM_BOOK_PAGE_TURN, .6f, 1f),
    TOGGLE_ON(Sound.BLOCK_NOTE_BLOCK_PLING, .7f, 1.6f),
    TOGGLE_OFF(Sound.BLOCK_NOTE_BLOCK_PLING, .7f, .6f);
    private final Sound sound;
    private final float volume;
    private final float pitch;

    UiSound(Sound sound, float volume, float pitch) {
      this.sound = sound;
      this.volume = volume;
      this.pitch = pitch;
    }
  }
}
