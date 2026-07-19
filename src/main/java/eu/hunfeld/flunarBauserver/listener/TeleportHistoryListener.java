package eu.hunfeld.flunarBauserver.listener;

import eu.hunfeld.flunarBauserver.service.TeleportService;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class TeleportHistoryListener implements Listener {
  private final TeleportService service;

  public TeleportHistoryListener(TeleportService s) {
    service = s;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void teleport(PlayerTeleportEvent e) {
    service.remember(e.getPlayer(), e.getFrom());
  }

  @EventHandler
  public void death(PlayerDeathEvent e) {
    service.remember(e.getPlayer(), e.getPlayer().getLocation());
  }
}
