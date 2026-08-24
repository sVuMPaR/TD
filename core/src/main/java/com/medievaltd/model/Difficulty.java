package com.medievaltd.model;

public enum Difficulty {
    EASY("Лёгкая", 1.3f, 0.75f, 25, 0.8f),
    NORMAL("Средняя", 1.0f, 1.0f, 20, 1.0f),
    HARD("Сложная", 0.75f, 1.35f, 12, 1.2f);

    public final String displayName;
    public final float goldMultiplier;
    public final float enemyHpMultiplier;
    public final int baseLives;
    public final float enemyCountMultiplier;

    Difficulty(String displayName, float goldMultiplier, float enemyHpMultiplier, int baseLives, float enemyCountMultiplier) {
        this.displayName = displayName;
        this.goldMultiplier = goldMultiplier;
        this.enemyHpMultiplier = enemyHpMultiplier;
        this.baseLives = baseLives;
        this.enemyCountMultiplier = enemyCountMultiplier;
    }

    public int adjustGold(int baseGold) {
        return Math.round(baseGold * goldMultiplier);
    }

    public int adjustHealth(int baseHp) {
        return Math.round(baseHp * enemyHpMultiplier);
    }

    public int adjustCount(int baseCount) {
        return Math.max(1, Math.round(baseCount * enemyCountMultiplier));
    }
}
