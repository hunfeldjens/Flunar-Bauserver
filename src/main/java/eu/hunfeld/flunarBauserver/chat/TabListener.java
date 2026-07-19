package eu.hunfeld.flunarBauserver.chat;

import eu.hunfeld.flunarBauserver.FlunarBauserver;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public final class TabListener implements Listener {
  private final FlunarBauserver plugin;
  private final TabService tab;

  public TabListener(FlunarBauserver p, TabService t) {
    plugin = p;
    tab = t;
  }

  @EventHandler
  public void join(PlayerJoinEvent e) {
    tab.updateDelayed();
  }

  @EventHandler
  public void quit(PlayerQuitEvent e) {
    tab.remove(e.getPlayer());
    tab.updateDelayed();
  }

  @EventHandler
  public void command(PlayerCommandPreprocessEvent e) {
    String command = e.getMessage().toLowerCase(java.util.Locale.ROOT);
    if (command.equals("/lp")
        || command.startsWith("/lp ")
        || command.equals("/luckperms")
        || command.startsWith("/luckperms "))
      Bukkit.getScheduler().runTaskLater(plugin, tab::updateAll, 10L);
  }

  @EventHandler
  public void chat(AsyncChatEvent e) {
    Component prefix = tab.prefix(e.getPlayer());
    e.renderer(
        (source, sourceDisplayName, message, viewer) ->
            prefix
                .append(Component.text(source.getName()))
                .append(Component.text(": "))
                .append(message));
  }
}
