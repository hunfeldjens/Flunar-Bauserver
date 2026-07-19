package eu.hunfeld.flunarBauserver.service;

import eu.hunfeld.flunarBauserver.FlunarBauserver;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class TeleportService {
  private final FlunarBauserver plugin;
  private final Map<UUID, Location> backLocations = new ConcurrentHashMap<>();

  public TeleportService(FlunarBauserver plugin) {
    this.plugin = plugin;
  }

  public void remember(Player player) {
    backLocations.put(player.getUniqueId(), player.getLocation().clone());
  }

  public void remember(Player player, Location location) {
    if (location != null) backLocations.put(player.getUniqueId(), location.clone());
  }

  public Optional<Location> backLocation(Player player) {
    return Optional.ofNullable(backLocations.get(player.getUniqueId())).map(Location::clone);
  }

  public void clear(Player player) {
    backLocations.remove(player.getUniqueId());
  }

  public FlunarBauserver plugin() {
    return plugin;
  }
}
