package eu.hunfeld.flunarBauserver.model;

import java.time.LocalDateTime;
import java.util.UUID;

/** Ein historischer Ban- oder Kick-Datensatz aus MariaDB. */
public record ModerationRecord(
    long id,
    UUID uuid,
    String name,
    String byName,
    String reason,
    LocalDateTime createdAt,
    boolean active) {}
