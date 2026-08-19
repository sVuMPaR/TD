package com.medievaltd.system;

import com.medievaltd.entity.Enemy;
import com.medievaltd.model.Difficulty;
import com.medievaltd.model.GameLevel;
import com.medievaltd.model.SpawnEntry;
import com.medievaltd.model.WaveDefinition;

import java.util.List;

public class WaveManager {
    public enum State { WAITING, SPAWNING, COMPLETE, ALL_COMPLETE }

    private final List<WaveDefinition> waves;
    private final Difficulty difficulty;
    private int currentWaveIndex = -1;
    private State state = State.WAITING;
    private float spawnTimer;
    private int spawnGroupIndex;
    private int spawnedInGroup;
    private float waveDelayTimer;

    public WaveManager(GameLevel level, Difficulty difficulty) {
        this.waves = level.waves;
        this.difficulty = difficulty;
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
            if (waveDelayTimer <= 0) state = State.WAITING;
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
        int adjustedCount = difficulty.adjustCount(entry.count);
        spawnTimer -= delta;

        if (spawnTimer <= 0 && spawnedInGroup < adjustedCount) {
            int hp = difficulty.adjustHealth(entry.type.maxHealth);
            spawnQueue.add(new Enemy(entry.type, hp));
            spawnedInGroup++;
            spawnTimer = entry.interval;
        }

        if (spawnedInGroup >= adjustedCount) {
            spawnGroupIndex++;
            spawnedInGroup = 0;
            spawnTimer = 0.5f;
        }
    }

    public int getCurrentWaveNumber() { return currentWaveIndex + 1; }
    public int getTotalWaves() { return waves.size(); }
    public State getState() { return state; }
    public boolean canStartWave() { return state == State.WAITING && currentWaveIndex + 1 < waves.size(); }
    public boolean isAllComplete() { return state == State.ALL_COMPLETE; }
}
