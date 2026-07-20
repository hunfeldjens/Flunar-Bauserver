package eu.hunfeld.flunarbauserver.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public enum UiSound {
  OPEN(Sound.BLOCK_BARREL_OPEN, .5f, 1.3f),
  CLICK(Sound.UI_BUTTON_CLICK, .5f, 1f),
  CONFIRM(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .8f, 1.2f),
  CANCEL(Sound.ENTITY_VILLAGER_NO, .8f, 1f),
  TELEPORT(Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, .9f, 1f),
  WORLD_UNLOAD(Sound.BLOCK_ENDER_CHEST_CLOSE, .8f, .7f),
  ERROR(Sound.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER, .8f, 1f),
  NAV(Sound.ITEM_BOOK_PAGE_TURN, .6f, 1f),
  TOGGLE_ON(Sound.BLOCK_NOTE_BLOCK_PLING, .7f, 1.6f),
  TOGGLE_OFF(Sound.BLOCK_NOTE_BLOCK_PLING, .7f, .6f);

  private final Sound sound;
  private final float volume;
  private final float pitch;

  UiSound(Sound sound, float volume, float pitch) {
    this.sound = sound;
    this.volume = volume;
    this.pitch = pitch;
  }

  public void play(Player player) {
    player.playSound(player.getLocation(), sound, volume, pitch);
  }
}
