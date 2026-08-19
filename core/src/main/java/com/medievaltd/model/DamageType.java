package com.medievaltd.model;

public enum DamageType {
    PHYSICAL("Физический"),
    FIRE("Огонь"),
    ICE("Лёд"),
    LIGHTNING("Молния"),
    MAGIC("Магия");

    public final String displayName;

    DamageType(String displayName) {
        this.displayName = displayName;
    }
}
