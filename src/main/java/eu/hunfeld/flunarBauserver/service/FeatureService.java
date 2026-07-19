package eu.hunfeld.flunarBauserver.service;

import eu.hunfeld.flunarBauserver.database.FeatureRepository;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.World;

public final class FeatureService {
  private final FeatureRepository repository;

  public FeatureService(FeatureRepository repository) {
    this.repository = repository;
  }

  public boolean enabled(String name) {
    return repository.enabled(name);
  }

  public void apply(World world) {
    // Feste Bauserver-Regeln aus 04_listener.sk. Sie gelten unabhängig von den
    // umschaltbaren Features in jeder Haupt-, Projekt- und Privatwelt.
    world.setGameRule(GameRules.SPAWN_MONSTERS, false);
    world.setGameRule(GameRules.SPAWN_MOBS, enabled("mobspawn"));
    world.setGameRule(GameRules.SPAWN_PATROLS, false);
    world.setGameRule(GameRules.SPAWN_PHANTOMS, false);
    world.setGameRule(GameRules.SPAWN_WANDERING_TRADERS, false);
    world.setGameRule(GameRules.SPAWN_WARDENS, false);
    world.setGameRule(GameRules.RAIDS, false);
    world.setGameRule(GameRules.RANDOM_TICK_SPEED, 0);
    world.setGameRule(GameRules.SPREAD_VINES, false);
    world.setGameRule(GameRules.TNT_EXPLODES, enabled("explosion"));
    world.setGameRule(GameRules.PROJECTILES_CAN_BREAK_BLOCKS, false);
    world.setGameRule(GameRules.WATER_SOURCE_CONVERSION, false);
    world.setGameRule(GameRules.LAVA_SOURCE_CONVERSION, false);
    world.setGameRule(GameRules.IMMEDIATE_RESPAWN, true);
    world.setGameRule(GameRules.SHOW_DEATH_MESSAGES, false);
    world.setGameRule(GameRules.NATURAL_HEALTH_REGENERATION, true);
    world.setGameRule(GameRules.FALL_DAMAGE, enabled("falldamage"));
    world.setGameRule(GameRules.PVP, false);

    // Umschaltbare Regeln aus /feature.
    boolean dayNight = enabled("daynight");
    world.setGameRule(GameRules.ADVANCE_TIME, dayNight);
    if (!dayNight) world.setTime(6_000L);
    world.setGameRule(GameRules.ADVANCE_WEATHER, enabled("weather"));
    world.setGameRule(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, enabled("firespread") ? 8 : 0);
    world.setGameRule(GameRules.MOB_GRIEFING, enabled("mobgriefing"));
    world.setGameRule(GameRules.KEEP_INVENTORY, enabled("keepinventory"));
    world.setGameRule(GameRules.COMMAND_BLOCK_OUTPUT, enabled("commandblocks"));
    // Die Locator-Bar bleibt standardmäßig aus; sie kann nur bewusst über das
    // entsprechende Feature aktiviert werden.
    world.setGameRule(GameRules.LOCATOR_BAR, enabled("locatorbar"));
    world.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, enabled("advancements"));
  }

  public void applyAll() {
    Bukkit.getWorlds().forEach(this::apply);
  }
}
