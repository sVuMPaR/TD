package com.medievaltd.model;

public class SpawnEntry {
    public final EnemyType type;
    public final int count;
    public final float interval;

    public SpawnEntry(EnemyType type, int count, float interval) {
        this.type = type;
        this.count = count;
        this.interval = interval;
    }
}
