package eu.hunfeld.flunarbauserver.listener;

import eu.hunfeld.flunarbauserver.service.BlockDisplayService;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;

public final class BlockDisplayListener implements Listener {
  private final BlockDisplayService s;

  public BlockDisplayListener(BlockDisplayService s) {
    this.s = s;
  }

  @EventHandler
  public void quit(PlayerQuitEvent e) {
    s.clear(e.getPlayer());
  }
}
