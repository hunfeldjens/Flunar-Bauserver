package eu.hunfeld.flunarbauserver.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Consumer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;


public final class LuckPermsBridge {
  private static final List<EventSubscription<?>> SUBSCRIPTIONS = new ArrayList<>();
  private static volatile Provider provider;

  private LuckPermsBridge() {}

  public static String prefix(Player player) {
    Provider active = provider();
    return active == null ? null : active.prefix(player);
  }

  public static Integer highestGroupWeight(Player player) {
    Provider active = provider();
    return active == null ? null : active.highestGroupWeight(player);
  }

  public static void subscribe(Plugin plugin, Consumer<UUID> refresh) {
    Provider active = provider();
    if (active == null) return;
    synchronized (SUBSCRIPTIONS) {
      SUBSCRIPTIONS.add(active.subscribe(plugin, refresh));
    }
  }

  public static void close() {
    synchronized (SUBSCRIPTIONS) {
      SUBSCRIPTIONS.forEach(EventSubscription::close);
      SUBSCRIPTIONS.clear();
    }
    provider = null;
  }

  private static Provider provider() {
    Provider active = provider;
    if (active != null) return active;
    if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) return null;
    synchronized (LuckPermsBridge.class) {
      if (provider == null) provider = ApiProvider.create();
      return provider;
    }
  }

  private interface Provider {
    String prefix(Player player);

    int highestGroupWeight(Player player);

    EventSubscription<?> subscribe(Plugin plugin, Consumer<UUID> refresh);
  }

  private record ApiProvider(LuckPerms api) implements Provider {
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
          .filter(OptionalInt::isPresent)
          .mapToInt(OptionalInt::getAsInt)
          .max()
          .orElse(0);
    }

    @Override
    public EventSubscription<?> subscribe(Plugin plugin, Consumer<UUID> refresh) {
      return api.getEventBus()
          .subscribe(
              plugin,
              net.luckperms.api.event.user.UserDataRecalculateEvent.class,
              event -> refresh.accept(event.getUser().getUniqueId()));
    }
  }
}
