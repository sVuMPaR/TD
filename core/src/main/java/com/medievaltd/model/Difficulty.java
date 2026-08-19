package com.medievaltd.model;

public enum Difficulty {
    EASY("Лёгкая", 1.5f, 0.6f, 30, 0.7f),
    NORMAL("Средняя", 1.2f, 0.85f, 22, 0.9f),
    HARD("Сложная", 0.9f, 1.15f, 15, 1.1f);

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
