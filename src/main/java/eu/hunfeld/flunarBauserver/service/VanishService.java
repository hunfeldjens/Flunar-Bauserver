package eu.hunfeld.flunarBauserver.service;

import eu.hunfeld.flunarBauserver.FlunarBauserver;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class VanishService {
  private final FlunarBauserver plugin;
  private final Set<UUID> vanished = ConcurrentHashMap.newKeySet();

  public VanishService(FlunarBauserver p) {
    plugin = p;
  }

  public boolean toggle(Player p) {
    boolean value;
    if (vanished.remove(p.getUniqueId())) value = false;
    else {
      vanished.add(p.getUniqueId());
      value = true;
    }
    for (Player viewer : Bukkit.getOnlinePlayers()) {
      if (viewer == p) continue;
      if (value && !viewer.hasPermission("bauserver.admin")) viewer.hidePlayer(plugin, p);
      else viewer.showPlayer(plugin, p);
    }
    p.setInvisible(value);
    p.setCollidable(!value);
    return value;
  }

  public void apply(Player viewer) {
    for (UUID id : vanished) {
      Player p = Bukkit.getPlayer(id);
      if (p != null && !viewer.hasPermission("bauserver.admin")) viewer.hidePlayer(plugin, p);
    }
  }

  public void remove(Player p) {
    vanished.remove(p.getUniqueId());
  }
}
