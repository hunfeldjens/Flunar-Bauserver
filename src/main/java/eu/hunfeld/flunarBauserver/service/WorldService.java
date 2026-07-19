package eu.hunfeld.flunarBauserver.service;

import eu.hunfeld.flunarBauserver.FlunarBauserver;
import eu.hunfeld.flunarBauserver.database.ProjectAccessRepository;
import eu.hunfeld.flunarBauserver.database.Sql;
import eu.hunfeld.flunarBauserver.settings.Settings;
import eu.hunfeld.flunarBauserver.utils.Messages;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public final class WorldService {
  private final FlunarBauserver plugin;
  private final Settings settings;
  private final ProjectAccessRepository access;
  private final FeatureService features;
  private final Messages messages;

  public WorldService(
      FlunarBauserver plugin,
      Settings settings,
      ProjectAccessRepository access,
      FeatureService features,
      Messages messages) {
    this.plugin = plugin;
    this.settings = settings;
    this.access = access;
    this.features = features;
    this.messages = messages;
  }

  public String cleanName(String value) {
    return Sql.cleanWorld(value);
  }

  public Optional<World> loaded(String name) {
    String clean = cleanName(name);
    return Bukkit.getWorlds().stream()
        .filter(world -> cleanName(world.getName()).equals(clean))
        .findFirst();
  }

  public World mainWorld() {
    World configured = Bukkit.getWorld(settings.mainWorld());
    return configured != null ? configured : Bukkit.getWorlds().getFirst();
  }

  public World createProject(String name, String type) {
    checkMainThread();
    String clean = validKey(name);
    WorldCreator creator = WorldCreator.ofKey(new NamespacedKey("projekt", clean));
    if (type.equalsIgnoreCase("flat")) creator.type(WorldType.FLAT);
    World world = creator.createWorld();
    if (world == null)
      throw new IllegalStateException("Projektwelt konnte nicht erstellt werden: " + clean);
    return prepare(world);
  }

  public World createVoid(String name) {
    checkMainThread();
    String clean = validKey(name);
    World world =
        WorldCreator.ofKey(new NamespacedKey("projekt", clean))
            .generator(new VoidChunkGenerator())
            .createWorld();
    if (world == null)
      throw new IllegalStateException("Void-Welt konnte nicht erstellt werden: " + clean);
    return prepare(world);
  }

  public World createPrivate(String uuid) {
    checkMainThread();
    String clean = validKey(uuid);
    World world =
        WorldCreator.ofKey(new NamespacedKey("privat", clean)).type(WorldType.FLAT).createWorld();
    if (world == null)
      throw new IllegalStateException("Privatwelt konnte nicht erstellt werden: " + clean);
    return prepare(world);
  }

  public boolean mayEnter(Player player, World world) {
    if (!world.getKey().namespace().equals("projekt")) return true;
    return access.mayEnter(
        player.getUniqueId(), world.getKey().value(), player.hasPermission("bauserver.admin"));
  }

  public CompletableFuture<Boolean> teleport(Player player, Location target) {
    return player.teleportAsync(target);
  }

  public void clearPlayer(Player player) {
    player.getInventory().clear();
    player.getEnderChest().clear();
    player.setGameMode(GameMode.CREATIVE);
    player.setHealth(player.getMaxHealth());
    player.setFoodLevel(20);
    player.setSaturation(20);
    for (PotionEffect effect : player.getActivePotionEffects())
      player.removePotionEffect(effect.getType());
  }

  /** Entfernt sofort alle Spieler, die nach einer Whitelist-Änderung keinen Zugriff mehr haben. */
  public int evacuateUnauthorized(String worldName, String reason) {
    World world = loaded(worldName).orElse(null);
    if (world == null) return 0;
    int moved = 0;
    for (Player player : List.copyOf(world.getPlayers())) {
      if (mayEnter(player, world)) continue;
      clearPlayer(player);
      moveToMain(player, reason);
      moved++;
    }
    return moved;
  }

  /** Teleportiert alle Spieler mit Begründung und entlädt die Projektwelt danach. */
  public int evacuateAndUnload(String worldName, String reason) {
    World world = loaded(worldName).orElse(null);
    if (world == null) return 0;
    int moved = 0;
    for (Player player : List.copyOf(world.getPlayers())) {
      moveToMain(player, reason);
      moved++;
    }
    Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.unloadWorld(world, true), 5L);
    return moved;
  }

  public void moveToMain(Player player, String reason) {
    World main = mainWorld();
    player.teleport(main.getSpawnLocation());
    messages.send(player, reason);
  }

  public List<String> gameruleSummary(World world) {
    return List.of(
        rule("spawn_monsters", world.getGameRuleValue(GameRules.SPAWN_MONSTERS)),
        rule("spawn_mobs", world.getGameRuleValue(GameRules.SPAWN_MOBS)),
        rule("spawn_patrols", world.getGameRuleValue(GameRules.SPAWN_PATROLS)),
        rule("spawn_phantoms", world.getGameRuleValue(GameRules.SPAWN_PHANTOMS)),
        rule("spawn_wandering_traders", world.getGameRuleValue(GameRules.SPAWN_WANDERING_TRADERS)),
        rule("spawn_wardens", world.getGameRuleValue(GameRules.SPAWN_WARDENS)),
        rule("raids", world.getGameRuleValue(GameRules.RAIDS)),
        rule("advance_time", world.getGameRuleValue(GameRules.ADVANCE_TIME)),
        rule("advance_weather", world.getGameRuleValue(GameRules.ADVANCE_WEATHER)),
        rule("random_tick_speed", world.getGameRuleValue(GameRules.RANDOM_TICK_SPEED)),
        rule("mob_griefing", world.getGameRuleValue(GameRules.MOB_GRIEFING)),
        rule("keep_inventory", world.getGameRuleValue(GameRules.KEEP_INVENTORY)),
        rule("tnt_explodes", world.getGameRuleValue(GameRules.TNT_EXPLODES)),
        rule(
            "projectiles_can_break_blocks",
            world.getGameRuleValue(GameRules.PROJECTILES_CAN_BREAK_BLOCKS)),
        rule("immediate_respawn", world.getGameRuleValue(GameRules.IMMEDIATE_RESPAWN)),
        rule("show_death_messages", world.getGameRuleValue(GameRules.SHOW_DEATH_MESSAGES)),
        rule("natural_regeneration", world.getGameRuleValue(GameRules.NATURAL_HEALTH_REGENERATION)),
        rule("fall_damage", world.getGameRuleValue(GameRules.FALL_DAMAGE)),
        rule("pvp", world.getGameRuleValue(GameRules.PVP)),
        rule("locator_bar", world.getGameRuleValue(GameRules.LOCATOR_BAR)),
        rule(
            "show_advancement_messages",
            world.getGameRuleValue(GameRules.SHOW_ADVANCEMENT_MESSAGES)));
  }

  private static String rule(String name, Object value) {
    return "<dark_gray>• <gray>" + name + " <dark_gray>= <white>" + value;
  }

  private String validKey(String name) {
    String clean = cleanName(name).toLowerCase(Locale.ROOT);
    if (clean.isBlank() || !clean.matches("[a-z0-9._-]+"))
      throw new IllegalArgumentException("Ungültiger Weltname");
    return clean;
  }

  private World prepare(World world) {
    features.apply(world);
    return world;
  }

  private void checkMainThread() {
    if (!Bukkit.isPrimaryThread())
      throw new IllegalStateException("Welten dürfen nur auf dem Server-Thread geladen werden");
  }
}
