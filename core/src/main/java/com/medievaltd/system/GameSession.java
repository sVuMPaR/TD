package com.medievaltd.system;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.medievaltd.entity.Enemy;
import com.medievaltd.entity.Projectile;
import com.medievaltd.entity.Tower;
import com.medievaltd.model.*;
import com.medievaltd.research.ResearchState;
import com.medievaltd.util.Assets;
import com.medievaltd.util.FloatingText;
import com.medievaltd.util.Sfx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameSession {
    public enum Result { PLAYING, VICTORY, DEFEAT }

    private final GameLevel level;
    private final Difficulty difficulty;
    private final ResearchState research;
    private final Sfx sfx;
    private final int levelIndex;
    private WaveManager.State lastWaveState = WaveManager.State.WAITING;
    private final Vector2[] path;
    private final List<BuildSpot> buildSpots;
    private final List<Tower> towers = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private final WaveManager waveManager;

    private int gold;
    private int lives;
    private Result result = Result.PLAYING;
    private int levelReward;
    private boolean defeatRewarded;
    private int defeatReward;

    public GameSession(GameLevel level, Difficulty difficulty, ResearchState research) {
        this(level, difficulty, research, null, 0);
    }

    public GameSession(GameLevel level, Difficulty difficulty, ResearchState research, Sfx sfx) {
        this(level, difficulty, research, sfx, level.mapIndex == 99 ? -1 : Math.max(0, level.mapIndex - 1));
    }

    public GameSession(GameLevel level, Difficulty difficulty, ResearchState research, Sfx sfx, int levelIndex) {
        this.level = level;
        this.difficulty = difficulty;
        this.research = research;
        this.sfx = sfx;
        this.levelIndex = levelIndex;
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
        if (waveManager.getState() != lastWaveState) {
            lastWaveState = waveManager.getState();
            if (lastWaveState != WaveManager.State.SPAWNING) checkpoint();
        }

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
            tower.update(delta, enemies, projectiles, path, assets, dmgBonus, spdBonus, floatingTexts, sfx);
        }

        for (Enemy enemy : enemies) {
            enemy.update(delta, path);
        }

        updateProjectiles(delta);

        Iterator<FloatingText> ft = floatingTexts.iterator();
        while (ft.hasNext()) {
            if (ft.next().update(delta)) ft.remove();
        }

        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy enemy = it.next();
            if (enemy.hasReachedEnd()) {
                lives -= enemy.getType().damageToBase;
                it.remove();
                if (sfx != null) sfx.leak();
                if (lives <= 0) {
                    lives = 0;
                    result = Result.DEFEAT;
                    CampaignSave.clear();
                    if (sfx != null) sfx.lose();
                }
            } else if (!enemy.isAlive()) {
                int reward = Math.round(enemy.getType().goldReward * research.getGoldMultiplier());
                gold += reward;
                it.remove();
            }
        }

        if (waveManager.isAllComplete() && enemies.isEmpty() && result == Result.PLAYING) {
            result = Result.VICTORY;
            research.addLevelReward(levelReward);
            CampaignProgress.recordVictory(level.mapIndex);
            CampaignSave.clear();
            if (sfx != null) sfx.win();
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
            p.update(delta, path);
            if (!p.isActive()) { it.remove(); continue; }
            if (!p.hasReachedTarget()) continue;

            Vector2 impact = p.getPosition();
            List<Enemy> hitEnemies = new ArrayList<>();
            Enemy primary = p.getTarget();
            if (primary != null && primary.isAlive() && !primary.hasReachedEnd()) {
                hitEnemies.add(primary);
            }

            if (p.isPiercing()) {
                collectPierceHits(p, impact, hitEnemies);
            } else {
                float splash = p.getSplashRadius();
                if (splash > 0) {
                    for (Enemy enemy : enemies) {
                        if (!enemy.isAlive() || hitEnemies.contains(enemy)) continue;
                        Vector2 ePos = enemy.getPosition(path, new Vector2());
                        if (impact.dst(ePos) <= splash) {
                            hitEnemies.add(enemy);
                        }
                    }
                }
            }

            for (Enemy enemy : hitEnemies) {
                applyProjectileHit(p, enemy);
            }
            if (!hitEnemies.isEmpty() && sfx != null) sfx.hit();
            if (p.isChaining() && !hitEnemies.isEmpty()) {
                handleChainLightning(p, hitEnemies.get(0), 2);
            }
            p.deactivate();
            it.remove();
        }
    }

    private static final int BALLISTA_PIERCE = 3;
    private static final float BALLISTA_WIDTH = 18f;
    private static final float BALLISTA_THROUGH = 72f;

    private void collectPierceHits(Projectile p, Vector2 impact, List<Enemy> hitEnemies) {
        Vector2 from = p.getOrigin();
        float dx = impact.x - from.x;
        float dy = impact.y - from.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1f) return;
        dx /= len;
        dy /= len;
        float endX = impact.x + dx * BALLISTA_THROUGH;
        float endY = impact.y + dy * BALLISTA_THROUGH;

        List<Enemy> along = new ArrayList<>();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive() || enemy.hasReachedEnd()) continue;
            Vector2 ePos = enemy.getPosition(path, new Vector2());
            if (distToSegment(ePos.x, ePos.y, from.x, from.y, endX, endY) <= BALLISTA_WIDTH) {
                along.add(enemy);
            }
        }
        if (along.isEmpty()) return;
        along.sort((a, b) -> Float.compare(
            a.getPosition(path, new Vector2()).dst2(from),
            b.getPosition(path, new Vector2()).dst2(from)));
        hitEnemies.clear();
        for (Enemy enemy : along) {
            hitEnemies.add(enemy);
            if (hitEnemies.size() >= BALLISTA_PIERCE) break;
        }
    }

    private static float distToSegment(float px, float py, float ax, float ay, float bx, float by) {
        float abx = bx - ax;
        float aby = by - ay;
        float apx = px - ax;
        float apy = py - ay;
        float ab2 = abx * abx + aby * aby;
        if (ab2 < 0.0001f) return Vector2.dst(px, py, ax, ay);
        float t = MathUtils.clamp((apx * abx + apy * aby) / ab2, 0f, 1f);
        return Vector2.dst(px, py, ax + t * abx, ay + t * aby);
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

        if (p.getSlowMultiplier() < 1f) {
            enemy.applySlow(p.getSlowMultiplier(), p.getSlowDuration() > 0f ? p.getSlowDuration() : 2.5f);
        }
        if (p.getDamageType() == DamageType.FIRE) {
            enemy.applyDot(8f, 3f);
        }
    }

    private int applyResearchDamage(int base, DamageType dt, Enemy enemy) {
        float mult = 1f;
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
        if (sfx != null) sfx.place();
        checkpoint();
        return true;
    }

    public boolean upgradeTower(Tower tower) {
        if (tower.getType() == TowerType.TEMPLE && tower.getTempleChoice() == null) return false;
        if (tower.getLevel() >= 3) return false;
        int cost = tower.getType().upgradeCost(tower.getLevel());
        if (gold < cost) return false;
        gold -= cost;
        tower.upgrade();
        if (sfx != null) sfx.upgrade();
        checkpoint();
        return true;
    }

    public boolean chooseTempleUpgrade(Tower tower, TempleUpgradeChoice choice) {
        if (tower.getType() != TowerType.TEMPLE || tower.getTempleChoice() != null) return false;
        if (tower.getLevel() != 1) return false;
        int cost = tower.nextUpgradeCost();
        if (cost < 0 || gold < cost) return false;
        gold -= cost;
        tower.setTempleChoice(choice);
        tower.upgrade();
        if (sfx != null) sfx.upgrade();
        checkpoint();
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
        boolean removed = towers.remove(tower);
        if (removed && sfx != null) sfx.sell();
        if (removed) checkpoint();
        return removed;
    }

    public void startWave() {
        waveManager.startNextWave();
        if (sfx != null) sfx.wave();
        checkpoint();
    }

    public void checkpoint() {
        if (result == Result.PLAYING) CampaignSave.write(this);
    }

    public void restore(CampaignSave.Run run) {
        gold = run.gold;
        lives = run.lives;
        for (CampaignSave.TowerRec rec : run.towers) {
            Tower tower = new Tower(rec.type, rec.x, rec.y, research);
            for (int i = 1; i < rec.level; i++) tower.upgrade();
            if (rec.temple != null) tower.setTempleChoice(rec.temple);
            towers.add(tower);
            for (BuildSpot spot : buildSpots) {
                if (Math.abs(spot.x - rec.x) < 1f && Math.abs(spot.y - rec.y) < 1f) {
                    spot.occupied = true;
                    break;
                }
            }
        }
        waveManager.restoreCompleted(run.completedWave);
        lastWaveState = waveManager.getState();
    }

    public int getLevelIndex() { return levelIndex; }

    public GameLevel getLevel() { return level; }
    public Difficulty getDifficulty() { return difficulty; }
    public Vector2[] getPath() { return path; }
    public List<BuildSpot> getBuildSpots() { return buildSpots; }
    public List<Tower> getTowers() { return towers; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<FloatingText> getFloatingTexts() { return floatingTexts; }
    public WaveManager getWaveManager() { return waveManager; }
    public int getGold() { return gold; }
    public int getLives() { return lives; }
    public Result getResult() { return result; }
    public int getLevelReward() { return levelReward; }
    public int getDefeatReward() { return defeatReward; }
}
