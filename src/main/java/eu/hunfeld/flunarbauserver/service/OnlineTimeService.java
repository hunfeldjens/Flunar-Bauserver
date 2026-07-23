package eu.hunfeld.flunarbauserver.service;

import eu.hunfeld.flunarbauserver.FlunarBauserver;
import eu.hunfeld.flunarbauserver.database.OnlineTimeRepository;
import eu.hunfeld.flunarbauserver.settings.Settings;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class OnlineTimeService implements AutoCloseable {
  private final FlunarBauserver plugin;
  private final Settings.OnlineTime settings;
  private final OnlineTimeRepository repository;
  private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
  private BukkitTask task;
  private int secondsSinceDatabaseSave;

  public OnlineTimeService(
      FlunarBauserver plugin, Settings.OnlineTime settings, OnlineTimeRepository repository) {
    this.plugin = plugin;
    this.settings = settings;
    this.repository = repository;
  }

  public void start() {
    long period = settings.tickSeconds() * 20L;
    task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, period, period);
    Bukkit.getOnlinePlayers().forEach(this::join);
  }

  public void join(Player player) {
    sessions.put(player.getUniqueId(), new Session(player.getLocation(), Instant.now()));
    String ip =
        player.getAddress() == null ? "" : player.getAddress().getAddress().getHostAddress();
    repository.recordJoin(player.getUniqueId(), player.getName(), ip);
  }

  public void activity(Player player) {
    Session session = sessions.get(player.getUniqueId());
    if (session != null) session.lastActivity = Instant.now();
  }

  public void quit(Player player) {
    Session session = sessions.remove(player.getUniqueId());
    if (session != null) queue(player.getUniqueId(), player.getName(), session);
  }

  public int sessionActive(UUID uuid) {
    Session value = sessions.get(uuid);
    return value == null ? 0 : value.active;
  }

  public int sessionAfk(UUID uuid) {
    Session value = sessions.get(uuid);
    return value == null ? 0 : value.afk;
  }

  public boolean isAfk(UUID uuid) {
    Session session = sessions.get(uuid);
    return session != null
        && Duration.between(session.lastActivity, Instant.now()).toSeconds()
            >= settings.afkWarningSeconds();
  }

  public void resetSession(UUID uuid) {
    Session session = sessions.get(uuid);
    if (session == null) return;
    session.active = 0;
    session.afk = 0;
    session.lastActivity = Instant.now();
  }

  private void tick() {
    Instant now = Instant.now();
    for (Player player : Bukkit.getOnlinePlayers()) {
      Session session =
          sessions.computeIfAbsent(
              player.getUniqueId(), _ -> new Session(player.getLocation(), now));
      Location current = player.getLocation();
      double movement =
          Math.abs(current.x() - session.lastLocation.x())
              + Math.abs(current.y() - session.lastLocation.y())
              + Math.abs(current.z() - session.lastLocation.z());
      double look =
          Math.abs(current.getYaw() - session.lastLocation.getYaw())
              + Math.abs(current.getPitch() - session.lastLocation.getPitch());
      if (movement >= settings.movementThreshold() || look >= 5) session.lastActivity = now;
      session.lastLocation = current.clone();
      long inactive = Duration.between(session.lastActivity, now).toSeconds();
      if (inactive >= settings.afkWarningSeconds()) session.afk += settings.tickSeconds();
      else session.active += settings.tickSeconds();
      if (inactive >= settings.afkKickSeconds())
        player.kick(Component.text("Du wurdest wegen Inaktivität gekickt."));
    }
    secondsSinceDatabaseSave += settings.tickSeconds();
    if (secondsSinceDatabaseSave
        >= Math.max(settings.tickSeconds(), settings.databaseSaveSeconds())) {
      secondsSinceDatabaseSave = 0;
      queueAllSessions();
      repository.flush();
    }
  }

  @Override
  public void close() {
    if (task != null) task.cancel();
    for (Player player : Bukkit.getOnlinePlayers()) quit(player);
    queueAllSessions();
    repository.flush().join();
    sessions.clear();
  }

  private void queueAllSessions() {
    for (Map.Entry<UUID, Session> entry : sessions.entrySet()) {
      Player player = Bukkit.getPlayer(entry.getKey());
      String name = player == null ? entry.getKey().toString() : player.getName();
      queue(entry.getKey(), name, entry.getValue());
    }
  }

  private void queue(UUID uuid, String name, Session session) {
    if (session.active == 0 && session.afk == 0) return;
    repository.queue(uuid, name, session.active, session.afk);
    session.active = 0;
    session.afk = 0;
  }

  private static final class Session {
    private Location lastLocation;
    private volatile Instant lastActivity;
    private int active;
    private int afk;

    private Session(Location location, Instant now) {
      lastLocation = location.clone();
      lastActivity = now;
    }
  }
}
