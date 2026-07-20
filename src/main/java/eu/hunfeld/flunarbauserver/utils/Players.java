package eu.hunfeld.flunarbauserver.utils;

import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class Players {
  private Players() {}

  public static Optional<Player> onlineExact(String name) {
    return Optional.ofNullable(Bukkit.getPlayerExact(name));
  }

  public static Optional<OfflinePlayer> known(String name) {
    Player online = Bukkit.getPlayerExact(name);
    if (online != null) return Optional.of(online);
    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
      if (player.getName() != null
          && player.getName().equalsIgnoreCase(name)
          && player.hasPlayedBefore()) return Optional.of(player);
    }
    return Optional.empty();
  }
}
