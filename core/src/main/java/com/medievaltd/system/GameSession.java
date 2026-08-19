package com.medievaltd.system;

import com.badlogic.gdx.math.Vector2;
import com.medievaltd.entity.Enemy;
import com.medievaltd.entity.Projectile;
import com.medievaltd.entity.Tower;
import com.medievaltd.model.*;
import com.medievaltd.research.ResearchState;
import com.medievaltd.util.Assets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameSession {
    public enum Result { PLAYING, VICTORY, DEFEAT }

    private final GameLevel level;
    private final Difficulty difficulty;
    private final ResearchState research;
    private final Vector2[] path;
    private final List<BuildSpot> buildSpots;
    private final List<Tower> towers = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final WaveManager waveManager;

    private int gold;
    private int lives;
    private Result result = Result.PLAYING;
    private int levelReward;
    private boolean defeatRewarded;
    private int defeatReward;

    public GameSession(GameLevel level, Difficulty difficulty, ResearchState research) {
        this.level = level;
        this.difficulty = difficulty;
        this.research = research;
        this.path = level.getPathPoints();
        this.buildSpots = new ArrayList<>();
        for (BuildSpot spot : level.buildSpots) {
            buildSpots.add(new BuildSpot(spot.x, spot.y));
        }
        this.gold = difficulty.adjustGold(level.startingGold);
        this.lives = difficulty.baseLives;
        this.waveManager = new WaveManager(level, difficulty);

        int baseReward = 50 + level.mapIndex * 40;
        this.levelReward = switch (difficulty) {
            case EASY -> (int) (baseReward * 0.7f);
            case NORMAL -> baseReward;
            case HARD -> (int) (baseReward * 1.5f);
        };
    }

    public void update(float delta, Assets assets) {
        if (result != Result.PLAYING) return;

        List<Enemy> spawnQueue = new ArrayList<>();
        waveManager.update(delta, enemies, spawnQueue);
        enemies.addAll(spawnQueue);

        for (Tower tower : towers) {
            float dmgBonus = research.getDamageMultiplier() - 1f;
            float spdBonus = 1f - research.getSpeedMultiplier();
            for (Tower t : towers) {
                if (t.getType() == TowerType.TEMPLE && t != tower) {
                    if (t.getPosition().dst(tower.getPosition()) <= t.getRange()) {
                        dmgBonus += t.getTempleDamageBonus();
                        spdBonus += t.getTempleSpeedBonus();
                    }
                }
            }
            tower.update(delta, enemies, projectiles, path, assets, dmgBonus, spdBonus);
        }

        for (Enemy enemy : enemies) {
            enemy.update(delta, path);
        }

        updateProjectiles(delta);

        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy enemy = it.next();
            if (enemy.hasReachedEnd()) {
                lives -= enemy.getType().damageToBase;
                it.remove();
                if (lives <= 0) { lives = 0; result = Result.DEFEAT; }
            } else if (!enemy.isAlive()) {
                int reward = Math.round(enemy.getType().goldReward * research.getGoldMultiplier());
                gold += reward;
                it.remove();
            }
        }

        if (waveManager.isAllComplete() && enemies.isEmpty() && result == Result.PLAYING) {
            result = Result.VICTORY;
            research.addLevelReward(levelReward);
        }

        if (result == Result.DEFEAT && !defeatRewarded) {
            defeatRewarded = true;
            int wavesCleared = Math.max(0, waveManager.getCurrentWaveNumber() - 1);
            int partialReward = (int) (levelReward * 0.3f * wavesCleared / (float) waveManager.getTotalWaves());
            if (partialReward > 0) {
                research.addLevelReward(partialReward);
                defeatReward = partialReward;
            }
        }
    }

    private void updateProjectiles(float delta) {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update(delta);
            if (!p.isActive()) { it.remove(); continue; }

            List<Enemy> hitEnemies = new ArrayList<>();
            for (Enemy enemy : enemies) {
                if (!enemy.isAlive()) continue;
                Vector2 ePos = enemy.getPosition(path, new Vector2());
                float hitRadius = p.getSplashRadius() > 0 ? p.getSplashRadius() : 20f;
                if (p.getPosition().dst(ePos) <= hitRadius) {
                    hitEnemies.add(enemy);
                    if (!p.isPiercing() && p.getSplashRadius() <= 0) break;
                }
            }

            if (!hitEnemies.isEmpty()) {
                for (Enemy enemy : hitEnemies) {
                    applyProjectileHit(p, enemy);
                }
                if (p.isChaining()) {
                    handleChainLightning(p, hitEnemies.get(0), 2);
                }
                p.deactivate();
                it.remove();
            }
        }
    }

    private void handleChainLightning(Projectile p, Enemy first, int bounces) {
        Enemy current = first;
        for (int i = 0; i < bounces; i++) {
            Enemy next = null;
            float bestDist = 120f;
            Vector2 curPos = current.getPosition(path, new Vector2());
            for (Enemy enemy : enemies) {
                if (!enemy.isAlive() || enemy == current) continue;
                float d = curPos.dst(enemy.getPosition(path, new Vector2()));
                if (d < bestDist) { bestDist = d; next = enemy; }
            }
            if (next == null) break;
            int dmg = applyResearchDamage(p.getDamage() / 2, p.getDamageType(), next);
            next.takeDamage(dmg, p.getDamageType());
            current = next;
        }
    }

    private void applyProjectileHit(Projectile p, Enemy enemy) {
        int raw = applyResearchDamage(p.getDamage(), p.getDamageType(), enemy);
        // Penetration: reduce effective resistance
        float pen = research.getPenetration(p.getDamageType());
        int finalDmg = (int) Math.max(1, raw * (1f + pen));
        enemy.takeDamage(finalDmg, p.getDamageType());

        if (p.getDamageType() == DamageType.ICE) {
            enemy.applySlow(0.4f, 2.5f);
        }
        if (p.getDamageType() == DamageType.FIRE) {
            enemy.applyDot(8f, 3f);
        }
    }

    private int applyResearchDamage(int base, DamageType dt, Enemy enemy) {
        float mult = 1f;
        mult *= research.getDamageMultiplier();
        if (enemy.getRole() == EnemyRole.BOSS || enemy.getRole() == EnemyRole.MINI_BOSS
            || enemy.getRole() == EnemyRole.FINAL_BOSS) {
            mult *= research.getBonusVsBosses();
        }
        if (enemy.isBlocked()) {
            mult *= research.getBonusVsBlocked();
        }
        if (enemy.isSlowed()) {
            mult *= research.getBonusVsSlowed();
        }
        return Math.max(1, Math.round(base * mult));
    }

    public boolean placeTower(BuildSpot spot, TowerType type) {
        int cost = type.freeToPlace ? 0 : type.baseCost;
        if (spot.occupied || gold < cost) return false;
        gold -= cost;
        spot.occupied = true;
        towers.add(new Tower(type, spot.x, spot.y, research));
        return true;
    }

    public boolean upgradeTower(Tower tower) {
        if (tower.getLevel() >= 3) return false;
        int cost = tower.getType().upgradeCost(tower.getLevel());
        if (gold < cost) return false;
        gold -= cost;
        tower.upgrade();
        return true;
    }

    public boolean sellTower(Tower tower) {
        int value = tower.getType().sellValue(tower.getLevel());
        gold += value;
        for (BuildSpot spot : buildSpots) {
            if (spot.x == tower.getPosition().x && spot.y == tower.getPosition().y) {
                spot.occupied = false;
                break;
            }
        }
        return towers.remove(tower);
    }

    public void startWave() { waveManager.startNextWave(); }

    public GameLevel getLevel() { return level; }
    public Difficulty getDifficulty() { return difficulty; }
    public Vector2[] getPath() { return path; }
    public List<BuildSpot> getBuildSpots() { return buildSpots; }
    public List<Tower> getTowers() { return towers; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public WaveManager getWaveManager() { return waveManager; }
    public int getGold() { return gold; }
    public int getLives() { return lives; }
    public Result getResult() { return result; }
    public int getLevelReward() { return levelReward; }
    public int getDefeatReward() { return defeatReward; }
}
