package eu.hunfeld.flunarbauserver.model;

import java.time.LocalDateTime;
import java.util.UUID;


public record ModerationRecord(
    long id,
    UUID uuid,
    String name,
    String byName,
    String reason,
    LocalDateTime createdAt,
    boolean active) {}
