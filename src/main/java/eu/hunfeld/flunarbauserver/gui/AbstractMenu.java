package eu.hunfeld.flunarbauserver.gui;

import eu.hunfeld.flunarbauserver.BauserverContext;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Shared item construction for Bauserver inventory menus. */
abstract class AbstractMenu {
  protected final BauserverContext context;

  protected AbstractMenu(BauserverContext context) {
    this.context = context;
  }

  protected final ItemStack named(Material material, String name) {
    return named(material, name, List.of());
  }

  protected final ItemStack named(Material material, String name, List<String> lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    meta.displayName(context.messages().parse(name));
    if (!lore.isEmpty()) meta.lore(lore.stream().map(context.messages()::parse).toList());
    item.setItemMeta(meta);
    return item;
  }
}
