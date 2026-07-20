package eu.hunfeld.flunarbauserver.model;

import java.util.UUID;

public record ActiveBan(UUID uuid, String reason, String byName) {}
