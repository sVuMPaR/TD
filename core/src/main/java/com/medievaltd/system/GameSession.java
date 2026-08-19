package com.medievaltd.system;

import com.badlogic.gdx.math.Vector2;
import com.medievaltd.entity.Enemy;
import com.medievaltd.entity.Projectile;
import com.medievaltd.entity.Tower;
import com.medievaltd.model.BuildSpot;
import com.medievaltd.model.GameLevel;
import com.medievaltd.model.TowerType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameSession {
    public enum Result { PLAYING, VICTORY, DEFEAT }

    private final GameLevel level;
    private final Vector2[] path;
    private final List<BuildSpot> buildSpots;
    private final List<Tower> towers = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final WaveManager waveManager;

    private int gold;
    private int lives;
    private Result result = Result.PLAYING;

    public GameSession(GameLevel level) {
        this.level = level;
        this.path = level.getPathPoints();
        this.buildSpots = new ArrayList<>();
        for (BuildSpot spot : level.buildSpots) {
            buildSpots.add(new BuildSpot(spot.x, spot.y));
        }
        this.gold = level.startingGold;
        this.lives = level.startingLives;
        this.waveManager = new WaveManager(level);
    }

    public void update(float delta, com.medievaltd.util.Assets assets) {
        if (result != Result.PLAYING) return;

        List<Enemy> spawnQueue = new ArrayList<>();
        waveManager.update(delta, enemies, spawnQueue);
        enemies.addAll(spawnQueue);

        for (Tower tower : towers) {
            tower.update(delta, enemies, projectiles, path, assets);
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
                if (lives <= 0) {
                    lives = 0;
                    result = Result.DEFEAT;
                }
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
            if (!p.isActive()) {
                it.remove();
                continue;
            }

            boolean hit = false;
            for (Enemy enemy : enemies) {
                if (!enemy.isAlive()) continue;
                Vector2 enemyPos = enemy.getPosition(path, new Vector2());

                if (p.getKind() == Projectile.Kind.CANNONBALL) {
                    if (p.getPosition().dst(enemyPos) <= p.getSplashRadius()) {
                        applyProjectileHit(p, enemy);
                        hit = true;
                    }
                } else if (p.getPosition().dst(enemyPos) <= 20f) {
                    applyProjectileHit(p, enemy);
                    hit = true;
                    break;
                }
            }

            if (hit) {
                p.deactivate();
                it.remove();
            }
        }
    }

    private void applyProjectileHit(Projectile p, Enemy enemy) {
        enemy.takeDamage(p.getDamage());
        if (p.getKind() == Projectile.Kind.MAGIC) {
            enemy.applySlow(0.5f, 2f);
        }
    }

    public boolean placeTower(BuildSpot spot, TowerType type) {
        if (spot.occupied || gold < type.baseCost) return false;
        gold -= type.baseCost;
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

    public void startWave() {
        waveManager.startNextWave();
    }

    public GameLevel getLevel() { return level; }
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
