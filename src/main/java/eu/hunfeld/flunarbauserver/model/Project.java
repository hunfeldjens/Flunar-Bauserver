package eu.hunfeld.flunarbauserver.model;

import java.util.UUID;

public record Project(
    String name,
    String description,
    String worldName,
    UUID owner,
    boolean whitelistActive,
    String icon) {
  public Project withIcon(String value) {
    return new Project(name, description, worldName, owner, whitelistActive, value);
  }

  public Project withWhitelist(boolean value) {
    return new Project(name, description, worldName, owner, value, icon);
  }
}
