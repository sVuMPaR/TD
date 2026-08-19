package com.medievaltd.system;

import com.medievaltd.entity.Enemy;
import com.medievaltd.model.GameLevel;
import com.medievaltd.model.SpawnEntry;
import com.medievaltd.model.WaveDefinition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WaveManager {
    public enum State { WAITING, SPAWNING, COMPLETE, ALL_COMPLETE }

    private final List<WaveDefinition> waves;
    private int currentWaveIndex = -1;
    private State state = State.WAITING;
    private float spawnTimer;
    private int spawnGroupIndex;
    private int spawnedInGroup;
    private float waveDelayTimer;

    public WaveManager(GameLevel level) {
        this.waves = level.waves;
    }

    public void startNextWave() {
        if (state == State.SPAWNING) return;
        if (currentWaveIndex + 1 >= waves.size()) {
            state = State.ALL_COMPLETE;
            return;
        }
        currentWaveIndex++;
        spawnGroupIndex = 0;
        spawnedInGroup = 0;
        spawnTimer = 0;
        state = State.SPAWNING;
    }

    public void update(float delta, List<Enemy> enemies, List<Enemy> spawnQueue) {
        if (state == State.WAITING || state == State.ALL_COMPLETE) return;

        if (state == State.COMPLETE) {
            waveDelayTimer -= delta;
            if (waveDelayTimer <= 0) {
                state = State.WAITING;
            }
            return;
        }

        WaveDefinition wave = waves.get(currentWaveIndex);
        if (spawnGroupIndex >= wave.spawns.size()) {
            if (enemies.stream().noneMatch(Enemy::isAlive)) {
                state = State.COMPLETE;
                waveDelayTimer = wave.delayBeforeNext;
            }
            return;
        }

        SpawnEntry entry = wave.spawns.get(spawnGroupIndex);
        spawnTimer -= delta;
        if (spawnTimer <= 0 && spawnedInGroup < entry.count) {
            spawnQueue.add(new Enemy(entry.type));
            spawnedInGroup++;
            spawnTimer = entry.interval;
        }

        if (spawnedInGroup >= entry.count) {
            spawnGroupIndex++;
            spawnedInGroup = 0;
            spawnTimer = 0.5f;
        }
    }

    public int getCurrentWaveNumber() {
        return currentWaveIndex + 1;
    }

    public int getTotalWaves() {
        return waves.size();
    }

    public State getState() {
        return state;
    }

    public boolean canStartWave() {
        return state == State.WAITING && currentWaveIndex + 1 < waves.size();
    }

    public boolean isAllComplete() {
        return state == State.ALL_COMPLETE;
    }
}
