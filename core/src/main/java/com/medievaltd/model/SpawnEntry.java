package com.medievaltd.model;

public class SpawnEntry {
    public final EnemyType type;
    public final int count;
    public final float interval;
    public final EnemyRole role;

    public SpawnEntry(EnemyType type, int count, float interval) {
        this(type, count, interval, EnemyRole.NORMAL);
    }

    public SpawnEntry(EnemyType type, int count, float interval, EnemyRole role) {
        this.type = type;
        this.count = count;
        this.interval = interval;
        this.role = role;
    }
}
