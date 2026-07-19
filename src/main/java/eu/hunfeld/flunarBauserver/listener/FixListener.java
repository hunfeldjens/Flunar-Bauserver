package eu.hunfeld.flunarBauserver.listener;

import eu.hunfeld.flunarBauserver.service.FixService;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;

public final class FixListener implements Listener {
  private final FixService s;

  public FixListener(FixService s) {
    this.s = s;
  }

  @EventHandler
  public void quit(PlayerQuitEvent e) {
    s.clear(e.getPlayer());
  }
}
