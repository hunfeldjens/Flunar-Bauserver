package eu.hunfeld.flunarbauserver.gui;

import eu.hunfeld.flunarbauserver.BauserverContext;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


abstract class AbstractMenu {
  protected static final ItemStack DECORATION_ITEM = decorationItem();

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

  private static ItemStack decorationItem() {
    ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    ItemMeta meta = item.getItemMeta();
    meta.displayName(Component.text(" "));
    item.setItemMeta(meta);
    return item;
  }
}
