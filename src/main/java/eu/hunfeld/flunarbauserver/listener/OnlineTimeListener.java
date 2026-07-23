package eu.hunfeld.flunarbauserver.listener;

import eu.hunfeld.flunarbauserver.service.OnlineTimeService;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;

public final class OnlineTimeListener implements Listener {
  private final OnlineTimeService s;

  public OnlineTimeListener(OnlineTimeService s) {
    this.s = s;
  }

  @EventHandler
  public void join(PlayerJoinEvent e) {
    s.join(e.getPlayer());
  }

  @EventHandler
  public void quit(PlayerQuitEvent e) {
    s.quit(e.getPlayer());
  }

  @EventHandler
  public void chat(AsyncChatEvent e) {
    s.activity(e.getPlayer());
  }

  @EventHandler
  public void command(PlayerCommandPreprocessEvent e) {
    s.activity(e.getPlayer());
  }

  @EventHandler
  public void interact(PlayerInteractEvent e) {
    s.activity(e.getPlayer());
  }

  @EventHandler
  public void blockBreak(BlockBreakEvent e) {
    s.activity(e.getPlayer());
  }

  @EventHandler
  public void blockPlace(BlockPlaceEvent e) {
    s.activity(e.getPlayer());
  }
}
