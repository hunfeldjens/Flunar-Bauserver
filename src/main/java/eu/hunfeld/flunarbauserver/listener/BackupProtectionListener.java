package eu.hunfeld.flunarbauserver.listener;

import eu.hunfeld.flunarbauserver.service.BackupService;
import org.bukkit.event.*;
import org.bukkit.event.block.*;

public final class BackupProtectionListener implements Listener {
  private final BackupService backup;

  public BackupProtectionListener(BackupService b) {
    backup = b;
  }

  @EventHandler(ignoreCancelled = true)
  public void breakBlock(BlockBreakEvent e) {
    if (backup.safeWorldLocked(e.getBlock().getWorld().getName())) e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void place(BlockPlaceEvent e) {
    if (backup.safeWorldLocked(e.getBlock().getWorld().getName())) e.setCancelled(true);
  }
}
