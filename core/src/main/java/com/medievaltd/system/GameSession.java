package com.medievaltd.system;

import com.badlogic.gdx.math.Vector2;
import com.medievaltd.entity.Enemy;
import com.medievaltd.entity.Projectile;
import com.medievaltd.entity.Tower;
import com.medievaltd.model.*;
import com.medievaltd.util.Assets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameSession {
    public enum Result { PLAYING, VICTORY, DEFEAT }

    private final GameLevel level;
    private final Difficulty difficulty;
    private final Vector2[] path;
    private final List<BuildSpot> buildSpots;
    private final List<Tower> towers = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final WaveManager waveManager;

    private int gold;
    private int lives;
    private Result result = Result.PLAYING;

    public GameSession(GameLevel level, Difficulty difficulty) {
        this.level = level;
        this.difficulty = difficulty;
        this.path = level.getPathPoints();
        this.buildSpots = new ArrayList<>();
        for (BuildSpot spot : level.buildSpots) {
            buildSpots.add(new BuildSpot(spot.x, spot.y));
        }
        this.gold = difficulty.adjustGold(level.startingGold);
        this.lives = difficulty.baseLives;
        this.waveManager = new WaveManager(level, difficulty);
    }

    public void update(float delta, Assets assets) {
        if (result != Result.PLAYING) return;

        List<Enemy> spawnQueue = new ArrayList<>();
        waveManager.update(delta, enemies, spawnQueue);
        enemies.addAll(spawnQueue);

        for (Tower tower : towers) {
            float dmgBonus = 0, spdBonus = 0;
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
                gold += enemy.getType().goldReward;
                it.remove();
            }
        }

        if (waveManager.isAllComplete() && enemies.isEmpty()) {
            result = Result.VICTORY;
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
                Vector2 ePos = enemy.getPosition(path, new Vector2());
                float d = curPos.dst(ePos);
                if (d < bestDist) {
                    bestDist = d;
                    next = enemy;
                }
            }
            if (next == null) break;
            next.takeDamage(p.getDamage() / 2, p.getDamageType());
            current = next;
        }
    }

    private void applyProjectileHit(Projectile p, Enemy enemy) {
        enemy.takeDamage(p.getDamage(), p.getDamageType());
        if (p.getDamageType() == DamageType.ICE) {
            enemy.applySlow(0.4f, 2.5f);
        }
        if (p.getDamageType() == DamageType.FIRE) {
            enemy.applyDot(8f, 3f);
        }
    }

    public boolean placeTower(BuildSpot spot, TowerType type) {
        int cost = type.freeToPlace ? 0 : type.baseCost;
        if (spot.occupied || gold < cost) return false;
        gold -= cost;
        spot.occupied = true;
        towers.add(new Tower(type, spot.x, spot.y));
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
}
