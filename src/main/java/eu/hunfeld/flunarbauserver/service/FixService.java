package eu.hunfeld.flunarbauserver.service;

import eu.hunfeld.flunarbauserver.FlunarBauserver;
import eu.hunfeld.flunarbauserver.utils.Messages;
import eu.hunfeld.flunarbauserver.utils.UiSound;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class FixService {
  private static final long MAX_BLOCKS = 2_000_000L;
  private static final int BLOCKS_PER_TICK = 20_000;
  private final FlunarBauserver plugin;
  private final Messages messages;
  private final Map<UUID, Selection> selections = new ConcurrentHashMap<>();
  private final Map<UUID, List<Undo>> undo = new ConcurrentHashMap<>();
  private final Map<UUID, Integer> runningTasks = new ConcurrentHashMap<>();

  public FixService(FlunarBauserver plugin, Messages messages) {
    this.plugin = plugin;
    this.messages = messages;
    Bukkit.getScheduler().runTaskTimer(plugin, this::showMarkers, 10L, 10L);
  }

  public void set(Player player, int position) {
    Selection selection = selections.computeIfAbsent(player.getUniqueId(), _ -> new Selection());
    Location location = player.getLocation().subtract(0, 1, 0).getBlock().getLocation();
    if (position == 1) selection.first = location;
    else selection.second = location;
    messages.send(
        player,
        "<gray>Position <green>"
            + position
            + " <gray>gesetzt: <white>"
            + location.getBlockX()
            + ", "
            + location.getBlockY()
            + ", "
            + location.getBlockZ()
            + " <dark_gray>("
            + location.getWorld().getName()
            + ")");
    UiSound.CLICK.play(player);
    if (position == 1 && selection.second == null)
      messages.send(
          player, "<dark_gray>Jetzt die zweite Ecke mit <white>/fix 2 <dark_gray>setzen.");
    if (position == 2 && selection.first != null)
      messages.send(player, "<dark_gray>Bereich fixen mit <white>/fix");
  }

  public void clear(Player player) {
    cancel(player);
    selections.remove(player.getUniqueId());
    undo.remove(player.getUniqueId());
  }

  public void execute(Player player) {
    Selection s = selections.get(player.getUniqueId());
    if (s == null || s.first == null || s.second == null) {
      messages.send(player, "<red>Setze zuerst /fix 1 und /fix 2.");
      return;
    }
    if (s.first.getWorld() != s.second.getWorld()) {
      messages.send(player, "<red>Die Positionen liegen in verschiedenen Welten.");
      return;
    }
    if (runningTasks.containsKey(player.getUniqueId())) {
      messages.send(player, "<red>Für dich läuft bereits ein Fix.");
      return;
    }
    int minX = Math.min(s.first.getBlockX(), s.second.getBlockX()),
        maxX = Math.max(s.first.getBlockX(), s.second.getBlockX());
    int minY = Math.min(s.first.getBlockY(), s.second.getBlockY()),
        maxY = Math.max(s.first.getBlockY(), s.second.getBlockY());
    int minZ = Math.min(s.first.getBlockZ(), s.second.getBlockZ()),
        maxZ = Math.max(s.first.getBlockZ(), s.second.getBlockZ());
    long volume = (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    if (volume > MAX_BLOCKS) {
      messages.send(
          player, "<red>Bereich zu groß: " + volume + " Blöcke (maximal " + MAX_BLOCKS + ").");
      return;
    }
    List<Undo> changes = new ArrayList<>();
    undo.put(player.getUniqueId(), changes);
    Cursor cursor = new Cursor(s.first.getWorld(), minX, maxX, minY, maxY, minZ, maxZ);
    int task =
        Bukkit.getScheduler()
            .scheduleSyncRepeatingTask(
                plugin,
                () -> {
                  int checked = 0;
                  while (checked++ < BLOCKS_PER_TICK && cursor.next()) {
                    Block block = cursor.block();
                    Material old = block.getType();
                    if ((old == Material.GRASS_BLOCK || old == Material.DIRT_PATH)
                        && block.getRelative(0, 1, 0).getType().isSolid()) {
                      changes.add(
                          new Undo(
                              block.getWorld().getUID(),
                              block.getX(),
                              block.getY(),
                              block.getZ(),
                              old));
                      block.setType(Material.DIRT, false);
                    }
                  }
                  messages.action(
                      player,
                      "<gray>Fix läuft... <yellow>" + changes.size() + " <gray>Blöcke korrigiert");
                  if (cursor.finished) {
                    cancel(player);
                    messages.send(
                        player,
                        "<gray>Fertig! <green>" + changes.size() + " <gray>Blöcke korrigiert.");
                  }
                },
                1L,
                1L);
    runningTasks.put(player.getUniqueId(), task);
  }

  public void undo(Player player) {
    List<Undo> changes = undo.remove(player.getUniqueId());
    if (changes == null || changes.isEmpty()) {
      messages.send(player, "<red>Nichts zum Rückgängigmachen.");
      return;
    }
    if (runningTasks.containsKey(player.getUniqueId())) {
      messages.send(player, "<red>Warte bis der aktuelle Fix beendet ist.");
      return;
    }
    int[] index = {0}, restored = {0};
    int task =
        Bukkit.getScheduler()
            .scheduleSyncRepeatingTask(
                plugin,
                () -> {
                  int amount = 0;
                  while (amount++ < BLOCKS_PER_TICK && index[0] < changes.size()) {
                    Undo u = changes.get(index[0]++);
                    World w = Bukkit.getWorld(u.world);
                    if (w == null) continue;
                    Block b = w.getBlockAt(u.x, u.y, u.z);
                    if (b.getType() == Material.DIRT) {
                      b.setType(u.material, false);
                      restored[0]++;
                    }
                  }
                  if (index[0] >= changes.size()) {
                    cancel(player);
                    messages.send(
                        player,
                        "<gray>Undo fertig: <green>"
                            + restored[0]
                            + " <gray>Blöcke wiederhergestellt.");
                  }
                },
                1L,
                1L);
    runningTasks.put(player.getUniqueId(), task);
  }

  public void cancel(Player player) {
    Integer task = runningTasks.remove(player.getUniqueId());
    if (task != null) Bukkit.getScheduler().cancelTask(task);
  }

  private void showMarkers() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      Selection selection = selections.get(player.getUniqueId());
      if (selection == null) continue;
      marker(player, selection.first);
      marker(player, selection.second);
      if (selection.first != null && selection.second != null)
        outline(player, selection.first, selection.second);
    }
  }

  private static void marker(Player player, Location location) {
    if (location == null
        || player.getWorld() != location.getWorld()
        || player.getLocation().distanceSquared(location) > 128 * 128) return;
    for (int index = 1; index <= 6; index++) {
      Location point = location.clone().add(.5, 1 + index * .4, .5);
      player.spawnParticle(Particle.HAPPY_VILLAGER, point, 1, .1, .1, .1, 0);
    }
  }

  private static void outline(Player player, Location first, Location second) {
    if (first.getWorld() != second.getWorld() || player.getWorld() != first.getWorld()) return;
    double x1 = Math.min(first.x(), second.x());
    double x2 = Math.max(first.x(), second.x()) + 1;
    double y1 = Math.min(first.y(), second.y());
    double y2 = Math.max(first.y(), second.y()) + 1;
    double z1 = Math.min(first.z(), second.z());
    double z2 = Math.max(first.z(), second.z()) + 1;
    if ((x2 - x1) + (y2 - y1) + (z2 - z1) > 160) return;
    Location center = new Location(first.getWorld(), (x1 + x2) / 2, (y1 + y2) / 2, (z1 + z2) / 2);
    double visibleDistance = 100 + (x2 - x1) / 2;
    if (player.getLocation().distanceSquared(center) > visibleDistance * visibleDistance) return;
    for (double x = x1; x <= x2; x += .75) {
      dust(player, x, y1, z1);
      dust(player, x, y1, z2);
      dust(player, x, y2, z1);
      dust(player, x, y2, z2);
    }
    for (double y = y1; y <= y2; y += .75) {
      dust(player, x1, y, z1);
      dust(player, x1, y, z2);
      dust(player, x2, y, z1);
      dust(player, x2, y, z2);
    }
    for (double z = z1; z <= z2; z += .75) {
      dust(player, x1, y1, z);
      dust(player, x1, y2, z);
      dust(player, x2, y1, z);
      dust(player, x2, y2, z);
    }
  }

  private static void dust(Player player, double x, double y, double z) {
    player.spawnParticle(
        Particle.DUST,
        new Location(player.getWorld(), x, y, z),
        1,
        0,
        0,
        0,
        new Particle.DustOptions(org.bukkit.Color.RED, .8f));
  }

  private static final class Selection {
    private Location first, second;
  }

  private record Undo(UUID world, int x, int y, int z, Material material) {}

  private static final class Cursor {
    private final World world;
    private final int minX, maxX, maxY, minZ, maxZ;
    private int x, y, z;
    private boolean started, finished;

    private Cursor(World w, int a, int b, int c, int d, int e, int f) {
      world = w;
      minX = a;
      maxX = b;
      maxY = d;
      minZ = e;
      maxZ = f;
      x = a;
      y = c;
      z = e;
    }

    private boolean next() {
      if (finished) return false;
      if (!started) {
        started = true;
        return true;
      }
      x++;
      if (x > maxX) {
        x = minX;
        z++;
      }
      if (z > maxZ) {
        z = minZ;
        y++;
      }
      if (y > maxY) {
        finished = true;
        return false;
      }
      return true;
    }

    private Block block() {
      return world.getBlockAt(x, y, z);
    }
  }
}
