package eu.hunfeld.flunarbauserver.gui;

import eu.hunfeld.flunarbauserver.BauserverContext;
import eu.hunfeld.flunarbauserver.service.BackupCoordinator;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

public final class BackupMenu extends AbstractMenu implements Listener {
  private final BackupCoordinator coordinator;

  public BackupMenu(BauserverContext c, BackupCoordinator b) {
    super(c);
    coordinator = b;
  }

  public void open(Player p) {
    Holder h = new Holder();
    Inventory i = Bukkit.createInventory(h, 27, context.messages().parse("<dark_gray>Backup"));
    h.inventory = i;
    i.setItem(11, named(Material.EMERALD_BLOCK, "<green>Safe Backup"));
    i.setItem(15, named(Material.REDSTONE_BLOCK, "<red>UnSafe Backup"));
    i.setItem(22, named(Material.BARRIER, "<red>Schließen"));
    p.openInventory(i);
  }

  @EventHandler
  public void click(InventoryClickEvent e) {
    if (!(e.getInventory().getHolder(false) instanceof Holder)) return;
    e.setCancelled(true);
    if (!(e.getWhoClicked() instanceof Player p)) return;
    if (e.getRawSlot() == 22) {
      p.closeInventory();
      return;
    }
    if (e.getRawSlot() == 11 || e.getRawSlot() == 15) {
      p.closeInventory();
      coordinator.start(p, e.getRawSlot() == 11);
    }
  }

  private static final class Holder implements InventoryHolder {
    private Inventory inventory;

    public @NotNull Inventory getInventory() {
      return inventory;
    }
  }
}
