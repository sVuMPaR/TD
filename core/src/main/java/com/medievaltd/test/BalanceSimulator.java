package com.medievaltd.test;

import com.medievaltd.entity.Enemy;
import com.medievaltd.entity.Projectile;
import com.medievaltd.entity.Tower;
import com.medievaltd.model.*;
import com.medievaltd.research.ResearchState;
import com.medievaltd.system.GameSession;
import com.medievaltd.system.WaveGenerator;

import java.util.*;

/**
 * Headless balance simulation — runs game logic without LibGDX renderer.
 * Simulates AI tower placement + wave progression, outputs stats.
 */
public class BalanceSimulator {

    public static void main(String[] args) {
        System.out.println("=== MEDIEVAL TD BALANCE SIMULATOR ===\n");

        List<GameLevel> levels = GameLevel.createLevels();
        for (Difficulty diff : Difficulty.values()) {
            System.out.println("━━━ " + diff.displayName.toUpperCase() + " ━━━\n");
            for (int i = 0; i < levels.size(); i++) {
                simulateLevel(levels.get(i), diff, "Карта " + (i + 1));
            }
            simulateLevel(SurvivalMap.create(), diff, "ВЫЖИВАНИЕ");
            System.out.println();
        }
    }

    static void simulateLevel(GameLevel level, Difficulty diff, String label) {
        ResearchState research = new ResearchState();
        int gold = diff.adjustGold(level.startingGold);
        int lives = diff.baseLives;
        List<WaveDefinition> waves = level.generateWaves(diff);
        com.badlogic.gdx.math.Vector2[] path = level.getPathPoints();
        Random rng = new Random(level.mapIndex * 7919L);

        // Simulated towers: place optimal mix
        List<SimTower> towers = planTowers(level, gold, diff, level.mapIndex);
        int goldSpent = towers.stream().mapToInt(t -> t.type.baseCost).sum();
        gold -= goldSpent;

        int totalKills = 0;
        int totalGoldEarned = 0;
        int waveSurvived = 0;
        Map<EnemyType, Integer> killsByType = new EnumMap<>(EnemyType.class);
        Map<EnemyType, Integer> leakedByType = new EnumMap<>(EnemyType.class);
        int bossKills = 0, bossLeaks = 0;
        float totalPathLength = computePathLength(path);

        for (WaveDefinition wave : waves) {
            List<SimEnemy> enemies = new ArrayList<>();
            for (SpawnEntry entry : wave.spawns) {
                int count = entry.role == EnemyRole.NORMAL ? diff.adjustCount(entry.count) : entry.count;
                for (int e = 0; e < count; e++) {
                    int hp = diff.adjustHealth(entry.type.maxHealth);
                    if (entry.role == EnemyRole.MINI_BOSS) hp = (int)(hp * 3f);
                    else if (entry.role == EnemyRole.BOSS) hp = (int)(hp * 6f);
                    else if (entry.role == EnemyRole.FINAL_BOSS) hp = (int)(hp * 12f);
                    if (level.mapIndex != 99 && level.mapIndex >= 2) {
                        hp = (int)(hp * (1f + (level.mapIndex - 1) * 0.25f));
                    }

                    if (level.mapIndex == 99) {
                        int phase = (wave.waveNumber - 1) / 10;
                        hp = (int)(hp * (1f + phase * 0.35f));
                    }

                    Resistances res = WaveGenerator.generateResistances(entry.role, diff, rng);
                    for (var re : entry.type.resistances.getMap().entrySet()) {
                        if (re.getValue() > res.getResistance(re.getKey())) {
                            res.set(re.getKey(), re.getValue());
                        }
                    }
                    enemies.add(new SimEnemy(entry.type, hp, entry.role, res, e * entry.interval));
                }
            }

            // Simulate combat: each tower fires at enemies moving along path
            float simTime = 0;
            float dt = 0.1f;
            float maxSimTime = totalPathLength / 20f + enemies.size() * 1.5f;

            while (simTime < maxSimTime && !enemies.isEmpty()) {
                simTime += dt;
                Iterator<SimEnemy> it = enemies.iterator();
                while (it.hasNext()) {
                    SimEnemy se = it.next();
                    if (se.spawnDelay > 0) { se.spawnDelay -= dt; continue; }
                    float speed = se.type.speed * (se.role != EnemyRole.NORMAL ? 0.75f : 1f);
                    se.progress += (speed * dt) / totalPathLength;

                    if (se.progress >= 1f) {
                        lives -= se.type.damageToBase;
                        leakedByType.merge(se.type, 1, Integer::sum);
                        if (se.role != EnemyRole.NORMAL) bossLeaks++;
                        it.remove();
                        continue;
                    }

                    // Towers fire
                    for (SimTower tower : towers) {
                        tower.cooldown -= dt;
                        if (tower.cooldown > 0) continue;
                        float enemyX = lerpPath(path, se.progress);
                        float towerRange = tower.type.rangeAtLevel(1);
                        if (Math.abs(tower.x - enemyX) > towerRange * 1.2f) continue;

                        int dmg = tower.type.damageAtLevel(1);
                        dmg = se.res.applyTo(dmg, tower.type.damageType);
                        se.hp -= dmg;
                        tower.cooldown = tower.type.baseFireRate;

                        if (se.hp <= 0) {
                            totalKills++;
                            int reward = se.type.goldReward;
                            gold += reward;
                            totalGoldEarned += reward;
                            killsByType.merge(se.type, 1, Integer::sum);
                            if (se.role != EnemyRole.NORMAL) bossKills++;
                            it.remove();
                            break;
                        }
                    }
                }

                if (lives <= 0) break;
            }

            waveSurvived = wave.waveNumber;
            if (lives <= 0) break;

            // Buy more towers mid-game
            if (gold >= 80 && towers.size() < level.buildSpots.size()) {
                TowerType buy = gold >= 150 ? TowerType.ARTILLERY : TowerType.ARCHER;
                int idx = towers.size();
                if (idx < level.buildSpots.size()) {
                    BuildSpot spot = level.buildSpots.get(idx);
                    towers.add(new SimTower(buy, spot.x, spot.y));
                    gold -= buy.baseCost;
                }
            }
        }

        boolean won = lives > 0 && waveSurvived >= waves.size();
        System.out.printf("  %s: %s | Волна %d/%d | Жизни: %d | Золото: %d | Убито: %d | Утекло: %d | Боссы: %d/%d%n",
            label, won ? "ПОБЕДА" : "ПОРАЖЕНИЕ",
            waveSurvived, waves.size(), Math.max(0, lives), gold,
            totalKills, leakedByType.values().stream().mapToInt(Integer::intValue).sum(),
            bossKills, bossKills + bossLeaks);

        if (!leakedByType.isEmpty()) {
            System.out.print("    Утекли: ");
            leakedByType.forEach((type, count) -> System.out.printf("%s×%d ", type.displayName, count));
            System.out.println();
        }

        // DPS analysis
        System.out.printf("    Башни: %d шт, потрачено: %d | Заработано: %d%n",
            towers.size(), goldSpent, totalGoldEarned);
        System.out.println();
    }

    static List<SimTower> planTowers(GameLevel level, int gold, Difficulty diff, int mapIndex) {
        List<SimTower> towers = new ArrayList<>();
        // Greedy placement: prioritize archers first, then mix
        TowerType[] priority = mapIndex >= 3
            ? new TowerType[]{TowerType.ARCHER, TowerType.ARTILLERY, TowerType.FIRE_MAGIC, TowerType.ICE_TOWER}
            : new TowerType[]{TowerType.ARCHER, TowerType.ARTILLERY, TowerType.MAGIC, TowerType.ARCHER};

        int budget = gold;
        int spotIdx = 0;
        for (TowerType type : priority) {
            if (spotIdx >= level.buildSpots.size()) break;
            int cost = type.freeToPlace ? 0 : type.baseCost;
            if (budget >= cost) {
                BuildSpot spot = level.buildSpots.get(spotIdx++);
                towers.add(new SimTower(type, spot.x, spot.y));
                budget -= cost;
            }
        }
        // Fill remaining budget with archers
        while (spotIdx < level.buildSpots.size() && budget >= TowerType.ARCHER.baseCost) {
            BuildSpot spot = level.buildSpots.get(spotIdx++);
            towers.add(new SimTower(TowerType.ARCHER, spot.x, spot.y));
            budget -= TowerType.ARCHER.baseCost;
        }
        return towers;
    }

    static float computePathLength(com.badlogic.gdx.math.Vector2[] path) {
        float len = 0;
        for (int i = 0; i < path.length - 1; i++) len += path[i].dst(path[i + 1]);
        return len;
    }

    static float lerpPath(com.badlogic.gdx.math.Vector2[] path, float progress) {
        float total = computePathLength(path);
        float target = progress * total;
        float acc = 0;
        for (int i = 0; i < path.length - 1; i++) {
            float seg = path[i].dst(path[i + 1]);
            if (acc + seg >= target) {
                float t = (target - acc) / seg;
                return path[i].x + (path[i + 1].x - path[i].x) * t;
            }
            acc += seg;
        }
        return path[path.length - 1].x;
    }

    static class SimTower {
        TowerType type;
        float x, y;
        float cooldown;
        SimTower(TowerType type, float x, float y) {
            this.type = type; this.x = x; this.y = y;
        }
    }

    static class SimEnemy {
        EnemyType type;
        int hp;
        EnemyRole role;
        Resistances res;
        float progress;
        float spawnDelay;
        SimEnemy(EnemyType type, int hp, EnemyRole role, Resistances res, float delay) {
            this.type = type; this.hp = hp; this.role = role; this.res = res; this.spawnDelay = delay;
        }
    }
}
