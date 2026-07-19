package eu.hunfeld.flunarBauserver.model;

import java.util.UUID;

public record ProjectInfo(
    int id,
    String worldName,
    String name,
    String description,
    UUID createdBy,
    Double x,
    Double y,
    Double z,
    Float yaw,
    Float pitch) {}
