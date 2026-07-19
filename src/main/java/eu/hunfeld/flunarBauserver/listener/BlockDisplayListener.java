package eu.hunfeld.flunarBauserver.listener;

import eu.hunfeld.flunarBauserver.service.BlockDisplayService;
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
