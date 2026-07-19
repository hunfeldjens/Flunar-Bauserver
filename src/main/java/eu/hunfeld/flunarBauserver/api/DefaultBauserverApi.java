package eu.hunfeld.flunarBauserver.api;

import eu.hunfeld.flunarBauserver.BauserverContext;
import eu.hunfeld.flunarBauserver.model.Project;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;

public final class DefaultBauserverApi implements BauserverApi {
  private final BauserverContext context;
  private final String prefix;
  private final String title;
  private final Component prefixComponent;
  private final Component titleComponent;

  public DefaultBauserverApi(BauserverContext context) {
    this.context = context;
    this.prefix = context.settings().prefix();
    this.title = context.settings().title();
    this.prefixComponent = context.messages().parse(prefix);
    this.titleComponent = context.messages().parse(title);
  }

  @Override
  public String prefix() {
    return prefix;
  }

  @Override
  public String title() {
    return title;
  }

  @Override
  public Component prefixComponent() {
    return prefixComponent;
  }

  @Override
  public Component titleComponent() {
    return titleComponent;
  }

  @Override
  public boolean databaseReady() {
    return context.database().isReady();
  }

  @Override
  public List<Project> projects() {
    return context.projects().all();
  }

  @Override
  public Optional<Project> projectByWorld(String worldName) {
    return context.projects().byWorld(worldName);
  }

  @Override
  public boolean canEnterProject(UUID playerId, String worldName, boolean bypass) {
    return context.projectAccess().mayEnter(playerId, worldName, bypass);
  }

  @Override
  public boolean featureEnabled(String featureName) {
    return context.features().enabled(featureName);
  }

  @Override
  public CompletableFuture<Boolean> setProjectBan(UUID playerId, String worldName, boolean banned) {
    return context.projectAccess().setBan(playerId, worldName, banned);
  }

  @Override
  public CompletableFuture<Boolean> setProjectWhitelist(
      UUID playerId, String worldName, boolean whitelisted) {
    return context.projectAccess().setWhitelist(playerId, worldName, whitelisted);
  }
}
