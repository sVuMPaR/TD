package com.medievaltd.model;

public enum TempleUpgradeChoice {
    ATTACK_SPEED("Скорость атаки", "+15% скорости"),
    ATTACK_DAMAGE("Сила атаки", "+20% урона");

    public final String displayName;
    public final String description;

    TempleUpgradeChoice(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
