package eu.hunfeld.flunarbauserver.listener;

import eu.hunfeld.flunarbauserver.utils.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.command.UnknownCommandEvent;

public final class UnknownCommandListener implements Listener {
  private final Messages m;

  public UnknownCommandListener(Messages m) {
    this.m = m;
  }

  @EventHandler
  public void unknown(UnknownCommandEvent e) {
    if (e.getSender() instanceof Player) {
      e.message(null);
      m.noPermission(e.getSender());
    }
  }
}
