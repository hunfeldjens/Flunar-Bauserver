package eu.hunfeld.flunarbauserver.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlaceholderBridge {
  public String apply(Player player, String text) {
    if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
      return text.replace("%luckperms_prefix%", "")
          .replace("%luckperms_highest_group_weight%", "0");
    return PlaceholderAPI.setPlaceholders(player, text);
  }
}
