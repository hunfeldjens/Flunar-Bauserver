package eu.hunfeld.flunarBauserver.chat;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/** Optional LuckPerms integration. No database calls are performed here. */
public final class LuckPermsBridge {
  private volatile Provider provider;

  public String prefix(Player player) {
    Provider active = provider();
    return active == null ? null : active.prefix(player);
  }

  public Integer highestGroupWeight(Player player) {
    Provider active = provider();
    return active == null ? null : active.highestGroupWeight(player);
  }

  public void subscribe(Plugin plugin, java.util.function.Consumer<java.util.UUID> refresh) {
    Provider active = provider();
    if (active != null) active.subscribe(plugin, refresh);
  }

  private Provider provider() {
    Provider active = provider;
    if (active != null) return active;
    if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) return null;
    synchronized (this) {
      if (provider == null) provider = ApiProvider.create();
      return provider;
    }
  }

  private interface Provider {
    String prefix(Player player);

    int highestGroupWeight(Player player);

    void subscribe(Plugin plugin, java.util.function.Consumer<java.util.UUID> refresh);
  }

  private static final class ApiProvider implements Provider {
    private final LuckPerms api;

    private ApiProvider(LuckPerms api) {
      this.api = api;
    }

    private static Provider create() {
      RegisteredServiceProvider<LuckPerms> registration =
          Bukkit.getServicesManager().getRegistration(LuckPerms.class);
      return registration == null ? null : new ApiProvider(registration.getProvider());
    }

    @Override
    public String prefix(Player player) {
      String prefix = api.getPlayerAdapter(Player.class).getMetaData(player).getPrefix();
      return prefix == null ? "" : prefix;
    }

    @Override
    public int highestGroupWeight(Player player) {
      PlayerAdapter<Player> adapter = api.getPlayerAdapter(Player.class);
      User user = adapter.getUser(player);
      return user.getInheritedGroups(adapter.getQueryOptions(player)).stream()
          .map(Group::getWeight)
          .filter(java.util.OptionalInt::isPresent)
          .mapToInt(java.util.OptionalInt::getAsInt)
          .max()
          .orElse(0);
    }

    @Override
    public void subscribe(Plugin plugin, java.util.function.Consumer<java.util.UUID> refresh) {
      api.getEventBus()
          .subscribe(
              plugin,
              net.luckperms.api.event.user.UserDataRecalculateEvent.class,
              event -> refresh.accept(event.getUser().getUniqueId()));
    }
  }
}
