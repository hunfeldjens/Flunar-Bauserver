package eu.hunfeld.flunarbauserver.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record OnlineTimeRecord(
    UUID uuid,
    String name,
    int activeSeconds,
    int afkSeconds,
    int joins,
    LocalDateTime firstSeen,
    LocalDateTime lastSeen) {
  public int totalSeconds() {
    return activeSeconds + afkSeconds;
  }
}
