package eu.hunfeld.flunarbauserver.api;

import eu.hunfeld.flunarbauserver.model.Project;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;

/**
 * Stable Java entry point for other Flunar plugins. Read methods are cache-only and thread-safe.
 */
public interface BauserverApi {
  /** Liefert den konfigurierten Bauserver-Prefix als MiniMessage-String. */
  String prefix();

  /** Liefert den konfigurierten Bauserver-Titel als MiniMessage-String. */
  String title();

  /** Liefert den bereits geparsten Bauserver-Prefix für Adventure-Ausgaben. */
  Component prefixComponent();

  /** Liefert den bereits geparsten Bauserver-Titel für Adventure-Ausgaben. */
  Component titleComponent();

  boolean databaseReady();

  List<Project> projects();

  Optional<Project> projectByWorld(String worldName);

  boolean canEnterProject(UUID playerId, String worldName, boolean administrativeBypass);

  boolean featureEnabled(String featureName);

  CompletableFuture<Boolean> setProjectBan(UUID playerId, String worldName, boolean banned);

  CompletableFuture<Boolean> setProjectWhitelist(
      UUID playerId, String worldName, boolean whitelisted);
}
