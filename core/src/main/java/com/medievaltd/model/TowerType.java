package com.medievaltd.model;

import com.badlogic.gdx.math.Vector2;

public enum TowerType {
    ARCHER("Лучники", 80, 1.0f, 120f, 12, "Быстрая стрельба по одной цели"),
    ARTILLERY("Артиллерия", 150, 2.5f, 100f, 35, "Мощный урон по области"),
    MAGIC("Магия", 120, 1.8f, 110f, 18, "Замедляет и наносит магический урон");

    public final String displayName;
    public final int baseCost;
    public final float baseFireRate;
    public final float baseRange;
    public final int baseDamage;
    public final String description;

    TowerType(String displayName, int baseCost, float baseFireRate, float baseRange, int baseDamage, String description) {
        this.displayName = displayName;
        this.baseCost = baseCost;
        this.baseFireRate = baseFireRate;
        this.baseRange = baseRange;
        this.baseDamage = baseDamage;
        this.description = description;
    }

    public int upgradeCost(int level) {
        return (int) (baseCost * (0.6f + level * 0.5f));
    }

    public int damageAtLevel(int level) {
        return (int) (baseDamage * (1f + (level - 1) * 0.35f));
    }

    public float rangeAtLevel(int level) {
        return baseRange * (1f + (level - 1) * 0.12f);
    }

    public float fireRateAtLevel(int level) {
        return baseFireRate * (1f - (level - 1) * 0.08f);
    }

    public int sellValue(int level) {
        int total = baseCost;
        for (int i = 1; i < level; i++) {
            total += upgradeCost(i);
        }
        return total / 2;
    }
}
