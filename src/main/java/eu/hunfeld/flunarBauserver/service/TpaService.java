package eu.hunfeld.flunarBauserver.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TpaService {
  private final Map<UUID, Request> requestsByTarget = new ConcurrentHashMap<>();

  public boolean request(UUID requester, UUID target) {
    Instant now = Instant.now();
    AtomicBoolean created = new AtomicBoolean(false);
    requestsByTarget.compute(
        target,
        (ignored, current) -> {
          if (current != null && current.expiresAt().isAfter(now)) return current;
          created.set(true);
          return new Request(requester, now.plusSeconds(30));
        });
    return created.get();
  }

  public boolean expire(UUID requester, UUID target) {
    AtomicBoolean expired = new AtomicBoolean(false);
    requestsByTarget.computeIfPresent(
        target,
        (ignored, current) -> {
          if (!current.requester().equals(requester)) return current;
          expired.set(true);
          return null;
        });
    return expired.get();
  }

  public Optional<UUID> consume(UUID target) {
    Request request = requestsByTarget.remove(target);
    if (request == null || request.expiresAt().isBefore(Instant.now())) return Optional.empty();
    return Optional.of(request.requester());
  }

  public boolean hasPending(UUID requester, UUID target) {
    Request request = requestsByTarget.get(target);
    if (request == null || request.expiresAt().isBefore(Instant.now())) {
      requestsByTarget.remove(target);
      return false;
    }
    return request.requester().equals(requester);
  }

  public void clear(UUID player) {
    requestsByTarget.remove(player);
    requestsByTarget.entrySet().removeIf(entry -> entry.getValue().requester().equals(player));
  }

  private record Request(UUID requester, Instant expiresAt) {}
}
