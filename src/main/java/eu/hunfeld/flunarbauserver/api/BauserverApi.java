package eu.hunfeld.flunarbauserver.api;

import eu.hunfeld.flunarbauserver.model.Project;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;


public interface BauserverApi {

  String prefix();


  String title();


  Component prefixComponent();


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
