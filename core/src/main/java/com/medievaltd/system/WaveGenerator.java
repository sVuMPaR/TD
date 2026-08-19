package com.medievaltd.system;

import com.medievaltd.model.*;

import java.util.*;

public class WaveGenerator {
    private static final DamageType[] RESISTANCE_POOL = {
        DamageType.PHYSICAL, DamageType.FIRE, DamageType.ICE, DamageType.LIGHTNING, DamageType.MAGIC
    };

    private static final EnemyType[] EARLY_ENEMIES = {
        EnemyType.GOBLIN, EnemyType.WOLF
    };
    private static final EnemyType[] MID_ENEMIES = {
        EnemyType.ORC, EnemyType.FIRE_IMP, EnemyType.FROST_ELEMENTAL
    };
    private static final EnemyType[] LATE_ENEMIES = {
        EnemyType.KNIGHT, EnemyType.DARK_MAGE, EnemyType.WYVERN
    };
    private static final EnemyType[] BOSS_POOL = {
        EnemyType.DRAGON, EnemyType.GOLEM, EnemyType.WYVERN, EnemyType.KNIGHT
    };
    private static final EnemyType[] MINI_BOSS_POOL = {
        EnemyType.ORC, EnemyType.KNIGHT, EnemyType.FROST_ELEMENTAL, EnemyType.DARK_MAGE
    };

    public static List<WaveDefinition> generate(int totalWaves, int mapIndex, Difficulty difficulty) {
        Random rng = new Random(mapIndex * 1000L + difficulty.ordinal());
        List<WaveDefinition> waves = new ArrayList<>();

        for (int w = 1; w <= totalWaves; w++) {
            boolean isBossWave = (w % 5 == 0);
            boolean isFinalWave = (w == totalWaves);
            boolean isMiniBossWave = !isBossWave && !isFinalWave;

            List<SpawnEntry> spawns = new ArrayList<>();
            float progress = (float) w / totalWaves;

            // Regular enemies for the wave
            int baseCount = 6 + (int) (progress * 12);
            EnemyType[] pool = pickPool(progress);
            EnemyType mainType = pool[rng.nextInt(pool.length)];
            spawns.add(new SpawnEntry(mainType, baseCount, 0.5f + (1f - progress) * 0.5f));

            if (progress > 0.3f && pool.length > 1) {
                EnemyType secondType = pool[rng.nextInt(pool.length)];
                if (secondType != mainType) {
                    spawns.add(new SpawnEntry(secondType, baseCount / 2, 0.6f));
                }
            }

            // Mini-boss at end of every non-5th wave
            if (isMiniBossWave) {
                EnemyType mbType = MINI_BOSS_POOL[rng.nextInt(MINI_BOSS_POOL.length)];
                spawns.add(new SpawnEntry(mbType, 1, 0f, EnemyRole.MINI_BOSS));
            }

            // Boss every 5th wave
            if (isBossWave && !isFinalWave) {
                EnemyType bossType = BOSS_POOL[rng.nextInt(BOSS_POOL.length)];
                spawns.add(new SpawnEntry(bossType, 1, 0f, EnemyRole.BOSS));
            }

            // Final boss
            if (isFinalWave) {
                EnemyType bossType = EnemyType.DRAGON;
                spawns.add(new SpawnEntry(bossType, 1, 0f, EnemyRole.FINAL_BOSS));
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
            case BOSS -> randomResistances(rng, 1, false, false);
            case FINAL_BOSS -> randomResistances(rng, 2, false, false);
        };
    }

    private static Resistances normalResistances(EnemyRole role, Random rng) {
        return switch (role) {
            case NORMAL -> Resistances.none();
            case MINI_BOSS -> randomResistances(rng, 1, false, false);
            case BOSS -> randomResistances(rng, 2, false, false);
            case FINAL_BOSS -> randomResistancesWithImmunity(rng, 1, 1, false);
        };
    }

    private static Resistances hardResistances(EnemyRole role, Random rng) {
        return switch (role) {
            case NORMAL -> randomResistances(rng, 1, false, false);
            case MINI_BOSS -> randomResistances(rng, 2, false, false);
            case BOSS -> randomResistancesWithImmunity(rng, 0, 1, false);
            case FINAL_BOSS -> randomResistancesWithImmunity(rng, 0, 1, true);
        };
    }

    private static Resistances randomResistances(Random rng, int count, boolean immune, boolean slowImmune) {
        Resistances r = new Resistances();
        List<DamageType> pool = new ArrayList<>(List.of(RESISTANCE_POOL));
        Collections.shuffle(pool, rng);
        for (int i = 0; i < Math.min(count, pool.size()); i++) {
            float val = immune ? 1f : 0.2f + rng.nextFloat() * 0.3f;
            r.set(pool.get(i), val);
        }
        r.setSlowImmune(slowImmune);
        return r;
    }

    private static Resistances randomResistancesWithImmunity(Random rng, int resCount, int immuneCount, boolean slowImmune) {
        Resistances r = new Resistances();
        List<DamageType> pool = new ArrayList<>(List.of(RESISTANCE_POOL));
        Collections.shuffle(pool, rng);
        int idx = 0;
        for (int i = 0; i < immuneCount && idx < pool.size(); i++, idx++) {
            r.setImmune(pool.get(idx));
        }
        for (int i = 0; i < resCount && idx < pool.size(); i++, idx++) {
            r.set(pool.get(idx), 0.2f + rng.nextFloat() * 0.3f);
        }
        r.setSlowImmune(slowImmune);
        return r;
    }

    private static EnemyType[] pickPool(float progress) {
        if (progress < 0.3f) return EARLY_ENEMIES;
        if (progress < 0.65f) return MID_ENEMIES;
        return LATE_ENEMIES;
    }
}
