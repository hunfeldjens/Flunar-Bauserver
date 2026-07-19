package eu.hunfeld.flunarBauserver.service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Hält ausschließlich die letzte Unterhaltung für /r im Arbeitsspeicher. */
public final class PrivateMessageService {
  private final ConcurrentHashMap<UUID, UUID> replyTargets = new ConcurrentHashMap<>();

  public void remember(UUID first, UUID second) {
    replyTargets.put(first, second);
    replyTargets.put(second, first);
  }

  public Optional<UUID> replyTarget(UUID player) {
    return Optional.ofNullable(replyTargets.get(player));
  }

  public void forget(UUID player) {
    replyTargets.remove(player);
  }
}
