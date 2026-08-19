package com.medievaltd.model;

import java.util.List;

public class WaveDefinition {
    public final int waveNumber;
    public final List<SpawnEntry> spawns;
    public final float delayBeforeNext;

    public WaveDefinition(int waveNumber, List<SpawnEntry> spawns, float delayBeforeNext) {
        this.waveNumber = waveNumber;
        this.spawns = spawns;
        this.delayBeforeNext = delayBeforeNext;
    }
}
