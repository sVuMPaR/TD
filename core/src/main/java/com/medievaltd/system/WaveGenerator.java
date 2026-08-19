package com.medievaltd.system;

import com.medievaltd.model.*;

import java.util.*;

public class WaveGenerator {
    private static final DamageType[] ALL_DAMAGE_TYPES = DamageType.values();

    // Per-map enemy pools — each map uses a distinct set
    private static final EnemyType[][] MAP_ENEMY_POOLS = {
        // Map 1 — Forest Outpost: basic
        { EnemyType.GOBLIN, EnemyType.WOLF, EnemyType.ORC },
        // Map 2 — Bridge: fire+physical
        { EnemyType.ORC, EnemyType.FIRE_IMP, EnemyType.KNIGHT },
        // Map 3 — Frozen Gorge: ice+magic
        { EnemyType.FROST_ELEMENTAL, EnemyType.DARK_MAGE, EnemyType.WOLF },
        // Map 4 — Gates of Darkness: all heavy
        { EnemyType.GOLEM, EnemyType.WYVERN, EnemyType.KNIGHT, EnemyType.DARK_MAGE },
    };

    private static final EnemyType[][] MAP_MINI_BOSS_POOLS = {
        { EnemyType.ORC, EnemyType.WOLF },
        { EnemyType.KNIGHT, EnemyType.FIRE_IMP },
        { EnemyType.FROST_ELEMENTAL, EnemyType.DARK_MAGE },
        { EnemyType.GOLEM, EnemyType.WYVERN },
    };

    private static final EnemyType[][] MAP_BOSS_POOLS = {
        { EnemyType.ORC, EnemyType.KNIGHT },
        { EnemyType.DRAGON, EnemyType.KNIGHT },
        { EnemyType.DRAGON, EnemyType.GOLEM },
        { EnemyType.DRAGON, EnemyType.GOLEM, EnemyType.WYVERN },
    };

    private static final EnemyType[] FINAL_BOSSES = {
        EnemyType.DRAGON, EnemyType.GOLEM, EnemyType.WYVERN, EnemyType.DRAGON
    };

    public static List<WaveDefinition> generate(int totalWaves, int mapIndex, Difficulty difficulty) {
        Random rng = new Random(mapIndex * 1000L + difficulty.ordinal());
        List<WaveDefinition> waves = new ArrayList<>();
        int poolIdx = Math.max(0, Math.min(mapIndex - 1, MAP_ENEMY_POOLS.length - 1));
        EnemyType[] enemyPool = MAP_ENEMY_POOLS[poolIdx];
        EnemyType[] miniBossPool = MAP_MINI_BOSS_POOLS[poolIdx];
        EnemyType[] bossPool = MAP_BOSS_POOLS[poolIdx];
        EnemyType finalBossType = FINAL_BOSSES[poolIdx];

        for (int w = 1; w <= totalWaves; w++) {
            boolean isBossWave = (w % 5 == 0);
            boolean isFinalWave = (w == totalWaves);
            boolean isMiniBossWave = !isBossWave && !isFinalWave;

            List<SpawnEntry> spawns = new ArrayList<>();
            float progress = (float) w / totalWaves;
            int baseCount = 5 + (int) (progress * 12);

            EnemyType mainType = enemyPool[rng.nextInt(enemyPool.length)];
            spawns.add(new SpawnEntry(mainType, baseCount, 0.5f + (1f - progress) * 0.4f));

            if (progress > 0.25f && enemyPool.length > 1) {
                EnemyType second;
                int attempts = 0;
                do {
                    second = enemyPool[rng.nextInt(enemyPool.length)];
                    attempts++;
                } while (second == mainType && attempts < 5);
                if (second != mainType) {
                    spawns.add(new SpawnEntry(second, baseCount / 2, 0.7f));
                }
            }

            if (isMiniBossWave) {
                EnemyType mb = miniBossPool[rng.nextInt(miniBossPool.length)];
                spawns.add(new SpawnEntry(mb, 1, 0f, EnemyRole.MINI_BOSS));
            }

            if (isBossWave && !isFinalWave) {
                EnemyType bt = bossPool[rng.nextInt(bossPool.length)];
                spawns.add(new SpawnEntry(bt, 1, 0f, EnemyRole.BOSS));
            }

            if (isFinalWave) {
                spawns.add(new SpawnEntry(finalBossType, 1, 0f, EnemyRole.FINAL_BOSS));
            }

            float delay = isBossWave || isFinalWave ? 6f : 4f;
            waves.add(new WaveDefinition(w, spawns, delay));
        }

        return waves;
    }

    public static Resistances generateResistances(EnemyRole role, Difficulty difficulty, Random rng) {
        return switch (difficulty) {
            case EASY -> easyResistances(role, rng);
            case NORMAL -> normalResistances(role, rng);
            case HARD -> hardResistances(role, rng);
        };
    }

    private static Resistances easyResistances(EnemyRole role, Random rng) {
        return switch (role) {
            case NORMAL, MINI_BOSS -> Resistances.none();
            case BOSS -> pickUniqueResistances(rng, 1, false, false);
            case FINAL_BOSS -> pickUniqueResistances(rng, 2, false, false);
        };
    }

    private static Resistances normalResistances(EnemyRole role, Random rng) {
        return switch (role) {
            case NORMAL -> Resistances.none();
            case MINI_BOSS -> pickUniqueResistances(rng, 1, false, false);
            case BOSS -> pickUniqueResistances(rng, 2, false, false);
            case FINAL_BOSS -> pickUniqueResistances(rng, 1, true, false);
        };
    }

    private static Resistances hardResistances(EnemyRole role, Random rng) {
        return switch (role) {
            case NORMAL -> pickUniqueResistances(rng, 1, false, false);
            case MINI_BOSS -> pickUniqueResistances(rng, 2, false, false);
            case BOSS -> pickUniqueResistances(rng, 1, true, false);
            case FINAL_BOSS -> pickUniqueResistances(rng, 1, true, true);
        };
    }

    /**
     * Picks `count` unique DamageType values (no repeats), then applies:
     * - first one as immunity (1.0) if withImmunity is true, rest as resistance (0.2–0.5)
     * - slow immunity if slowImmune is true
     */
    private static Resistances pickUniqueResistances(Random rng, int count, boolean withImmunity, boolean slowImmune) {
        List<DamageType> pool = new ArrayList<>(Arrays.asList(ALL_DAMAGE_TYPES));
        Collections.shuffle(pool, rng);
        Resistances r = new Resistances();
        for (int i = 0; i < Math.min(count, pool.size()); i++) {
            DamageType dt = pool.get(i);
            if (i == 0 && withImmunity) {
                r.setImmune(dt);
            } else {
                r.set(dt, 0.20f + rng.nextFloat() * 0.30f);
            }
        }
        r.setSlowImmune(slowImmune);
        return r;
    }
}
