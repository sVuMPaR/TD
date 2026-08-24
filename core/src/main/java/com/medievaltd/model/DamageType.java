package com.medievaltd.model;

public enum DamageType {
    PHYSICAL("Физический", "Физ"),
    FIRE("Огонь", "Огонь"),
    ICE("Лёд", "Лёд"),
    LIGHTNING("Молния", "Молния"),
    MAGIC("Магия", "Магия");

    public final String displayName;
    public final String shortName;

    DamageType(String displayName, String shortName) {
        this.displayName = displayName;
        this.shortName = shortName;
    }
}
