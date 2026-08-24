package com.medievaltd.model;

public enum EnemyRole {
    NORMAL(0.85f),
    MINI_BOSS(2.8f),
    BOSS(5.6f),
    FINAL_BOSS(7.5f);

    public final float healthMultiplier;

    EnemyRole(float healthMultiplier) {
        this.healthMultiplier = healthMultiplier;
    }
}
