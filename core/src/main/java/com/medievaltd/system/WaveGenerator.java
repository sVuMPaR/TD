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

    // Survival mode uses all enemy types, rotating through phases
    private static final EnemyType[] ALL_ENEMIES = EnemyType.values();
    private static final EnemyType[] ALL_BOSSES = {
        EnemyType.DRAGON, EnemyType.GOLEM, EnemyType.WYVERN,
        EnemyType.KNIGHT, EnemyType.DARK_MAGE
    };

    public static List<WaveDefinition> generate(int totalWaves, int mapIndex, Difficulty difficulty) {
        if (mapIndex == 99) {
            return generateSurvival(totalWaves, difficulty);
        }
        return generateCampaign(totalWaves, mapIndex, difficulty);
    }

    private static List<WaveDefinition> generateCampaign(int totalWaves, int mapIndex, Difficulty difficulty) {
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
            boolean isMiniBossWave = !isBossWave && !isFinalWave && w > 1;

            List<SpawnEntry> spawns = new ArrayList<>();
            float progress = (float) w / totalWaves;
            int baseCount = 4 + (int) (progress * 10);

            EnemyType mainType = enemyPool[rng.nextInt(enemyPool.length)];
            spawns.add(new SpawnEntry(mainType, baseCount, 0.6f + (1f - progress) * 0.4f));

            if (progress > 0.3f && enemyPool.length > 1) {
                EnemyType second;
                int attempts = 0;
                do { second = enemyPool[rng.nextInt(enemyPool.length)]; attempts++; }
                while (second == mainType && attempts < 5);
                if (second != mainType) spawns.add(new SpawnEntry(second, baseCount / 3, 0.8f));
            }

            if (isMiniBossWave) {
                spawns.add(new SpawnEntry(miniBossPool[rng.nextInt(miniBossPool.length)], 1, 0f, EnemyRole.MINI_BOSS));
            }
            if (isBossWave && !isFinalWave) {
                spawns.add(new SpawnEntry(bossPool[rng.nextInt(bossPool.length)], 1, 0f, EnemyRole.BOSS));
            }
            if (isFinalWave) {
                spawns.add(new SpawnEntry(finalBossType, 1, 0f, EnemyRole.FINAL_BOSS));
            }

            waves.add(new WaveDefinition(w, spawns, isBossWave || isFinalWave ? 6f : 4f));
        }
        return waves;
    }

    private static List<WaveDefinition> generateSurvival(int totalWaves, Difficulty difficulty) {
        Random rng = new Random(42L + difficulty.ordinal());
        List<WaveDefinition> waves = new ArrayList<>();

        for (int w = 1; w <= totalWaves; w++) {
            boolean isBossWave = (w % 5 == 0);
            boolean isFinalWave = (w == totalWaves);
            boolean isMiniBossWave = !isBossWave && !isFinalWave && w > 1;

            int phase = (w - 1) / 10;
            List<SpawnEntry> spawns = new ArrayList<>();
            int baseCount = 5 + w / 5;
            int typeCount = Math.min(1 + phase, 4);

            // Pick enemies from expanding pool
            List<EnemyType> available = new ArrayList<>();
            for (int i = 0; i < ALL_ENEMIES.length && available.size() < 3 + phase; i++) {
                available.add(ALL_ENEMIES[(i + phase * 3) % ALL_ENEMIES.length]);
            }

            for (int t = 0; t < typeCount && t < available.size(); t++) {
                int count = t == 0 ? baseCount : baseCount / 2;
                float interval = Math.max(0.25f, 0.7f - phase * 0.04f);
                spawns.add(new SpawnEntry(available.get(t), count, interval));
            }

            if (isMiniBossWave) {
                EnemyType mb = ALL_BOSSES[rng.nextInt(ALL_BOSSES.length)];
                spawns.add(new SpawnEntry(mb, 1, 0f, EnemyRole.MINI_BOSS));
            }
            if (isBossWave && !isFinalWave) {
                EnemyType boss = ALL_BOSSES[rng.nextInt(ALL_BOSSES.length)];
                int bossCount = 1 + phase / 4; // multiple bosses in late waves
                spawns.add(new SpawnEntry(boss, bossCount, 1.5f, EnemyRole.BOSS));
            }
            if (isFinalWave) {
                spawns.add(new SpawnEntry(EnemyType.DRAGON, 1, 0f, EnemyRole.FINAL_BOSS));
                spawns.add(new SpawnEntry(EnemyType.GOLEM, 2, 1f, EnemyRole.BOSS));
                spawns.add(new SpawnEntry(EnemyType.WYVERN, 2, 1f, EnemyRole.BOSS));
            }

            waves.add(new WaveDefinition(w, spawns, isBossWave || isFinalWave ? 6f : 3.5f));
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
