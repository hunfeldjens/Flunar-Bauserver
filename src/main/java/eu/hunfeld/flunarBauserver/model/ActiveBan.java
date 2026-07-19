package eu.hunfeld.flunarBauserver.model;

import java.util.UUID;

public record ActiveBan(UUID uuid, String reason, String byName) {}
