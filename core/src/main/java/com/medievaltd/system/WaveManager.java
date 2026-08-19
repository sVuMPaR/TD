package com.medievaltd.system;

import com.medievaltd.entity.Enemy;
import com.medievaltd.model.*;

import java.util.List;
import java.util.Random;

public class WaveManager {
    public enum State { WAITING, SPAWNING, COMPLETE, ALL_COMPLETE }

    private final List<WaveDefinition> waves;
    private final Difficulty difficulty;
    private final Random rng;
    private int currentWaveIndex = -1;
    private State state = State.WAITING;
    private float spawnTimer;
    private int spawnGroupIndex;
    private int spawnedInGroup;
    private float waveDelayTimer;

    private final boolean isSurvival;
    private final int mapIndex;

    public WaveManager(GameLevel level, Difficulty difficulty) {
        this.waves = level.generateWaves(difficulty);
        this.difficulty = difficulty;
        this.rng = new Random(level.mapIndex * 7919L);
        this.isSurvival = level.mapIndex == 99;
        this.mapIndex = level.mapIndex;
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
        int adjustedCount = entry.role == EnemyRole.NORMAL
            ? difficulty.adjustCount(entry.count) : entry.count;
        spawnTimer -= delta;

        if (spawnTimer <= 0 && spawnedInGroup < adjustedCount) {
            int hp = difficulty.adjustHealth(entry.type.maxHealth);
            if (entry.role == EnemyRole.MINI_BOSS) hp = (int) (hp * 3.5f);
            else if (entry.role == EnemyRole.BOSS) hp = (int) (hp * 8f);
            else if (entry.role == EnemyRole.FINAL_BOSS) hp = (int) (hp * 15f);

            // Survival scaling: +35% HP per 10-wave phase
            if (isSurvival) {
                int phase = (currentWaveIndex) / 10;
                hp = (int) (hp * (1f + phase * 0.35f));
            }

            Resistances res = WaveGenerator.generateResistances(entry.role, difficulty, rng);
            // Merge with base type resistances
            Resistances base = entry.type.resistances;
            for (var e : base.getMap().entrySet()) {
                float existing = res.getResistance(e.getKey());
                if (e.getValue() > existing) {
                    res.set(e.getKey(), e.getValue());
                }
            }

            Enemy enemy = new Enemy(entry.type, hp, entry.role, res, difficulty);
            spawnQueue.add(enemy);
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
