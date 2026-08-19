package com.medievaltd.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.medievaltd.model.DamageType;
import com.medievaltd.model.TempleUpgradeChoice;
import com.medievaltd.model.TowerType;
import com.medievaltd.util.Assets;

import java.util.ArrayList;
import java.util.List;

public class Tower {
    private final TowerType type;
    private final Vector2 position;
    private int level = 1;
    private float fireCooldown;
    private TempleUpgradeChoice templeChoice;

    // Barracks soldiers
    private final List<Soldier> soldiers = new ArrayList<>();
    private static final int MAX_SOLDIERS = 3;

    public Tower(TowerType type, float x, float y) {
        this.type = type;
        this.position = new Vector2(x, y);
        if (type == TowerType.BARRACKS) {
            for (int i = 0; i < 2; i++) {
                soldiers.add(new Soldier());
            }
        }
    }

    public void update(float delta, List<Enemy> enemies, List<Projectile> projectiles,
                       Vector2[] path, Assets assets, float damageBonus, float speedBonus) {
        if (type == TowerType.TEMPLE || type == TowerType.BARRACKS) {
            updateBarracks(delta, enemies, path);
            return;
        }

        fireCooldown -= delta;
        if (fireCooldown > 0) return;

        Enemy target = findTarget(enemies, path);
        if (target == null) return;

        float effectiveFireRate = type.fireRateAtLevel(level) * (1f - speedBonus);
        fireCooldown = Math.max(0.2f, effectiveFireRate);

        int damage = (int) (type.damageAtLevel(level) * (1f + damageBonus));
        Vector2 targetPos = target.getPosition(path, new Vector2());

        Projectile proj = switch (type) {
            case ARCHER -> Projectile.arrow(new Vector2(position), targetPos, damage, assets);
            case ARTILLERY -> Projectile.cannonball(new Vector2(position), targetPos, damage, assets);
            case BALLISTA -> Projectile.bolt(new Vector2(position), targetPos, damage, assets);
            case MAGIC -> Projectile.magic(new Vector2(position), targetPos, damage, DamageType.MAGIC, assets);
            case FIRE_MAGIC -> Projectile.magic(new Vector2(position), targetPos, damage, DamageType.FIRE, assets);
            case ICE_MAGIC -> Projectile.magic(new Vector2(position), targetPos, damage, DamageType.ICE, assets);
            case LIGHTNING_MAGIC -> Projectile.magic(new Vector2(position), targetPos, damage, DamageType.LIGHTNING, assets);
            case ICE_TOWER -> Projectile.magic(new Vector2(position), targetPos, damage, DamageType.ICE, assets);
            default -> null;
        };
        if (proj != null) projectiles.add(proj);
    }

    private void updateBarracks(float delta, List<Enemy> enemies, Vector2[] path) {
        if (type != TowerType.BARRACKS) return;
        float range = type.rangeAtLevel(level);
        for (Soldier soldier : soldiers) {
            if (!soldier.alive) {
                soldier.respawnTimer -= delta;
                if (soldier.respawnTimer <= 0) {
                    soldier.alive = true;
                    soldier.hp = 30 + level * 10;
                }
                continue;
            }
            for (Enemy enemy : enemies) {
                if (!enemy.isAlive()) continue;
                Vector2 ePos = enemy.getPosition(path, new Vector2());
                if (position.dst(ePos) <= range) {
                    enemy.block(0.5f);
                    enemy.takeDamage(5 + level * 3, DamageType.PHYSICAL);
                    break;
                }
            }
        }
    }

    private Enemy findTarget(List<Enemy> enemies, Vector2[] path) {
        Enemy best = null;
        float bestProgress = -1;
        float range = type.rangeAtLevel(level);
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            Vector2 enemyPos = enemy.getPosition(path, new Vector2());
            if (position.dst(enemyPos) <= range && enemy.getPathProgress() > bestProgress) {
                bestProgress = enemy.getPathProgress();
                best = enemy;
            }
        }
        return best;
    }

    public boolean upgrade() {
        if (level >= 3) return false;
        level++;
        if (type == TowerType.BARRACKS && level == 3 && soldiers.size() < MAX_SOLDIERS) {
            soldiers.add(new Soldier());
        }
        return true;
    }

    public void setTempleChoice(TempleUpgradeChoice choice) {
        this.templeChoice = choice;
    }

    public TempleUpgradeChoice getTempleChoice() {
        return templeChoice;
    }

    public float getTempleDamageBonus() {
        if (type != TowerType.TEMPLE) return 0;
        if (templeChoice == TempleUpgradeChoice.ATTACK_DAMAGE) {
            return 0.2f * level;
        }
        return 0;
    }

    public float getTempleSpeedBonus() {
        if (type != TowerType.TEMPLE) return 0;
        if (templeChoice == TempleUpgradeChoice.ATTACK_SPEED) {
            return 0.15f * level;
        }
        return 0;
    }

    public void render(SpriteBatch batch, Assets assets) {
        TextureRegion tex = assets.getTowerTexture(type);
        float size = tex.getRegionWidth();
        batch.draw(tex, position.x - size / 2f, position.y - size / 2f, size, size);

        if (level > 1) {
            batch.setColor(1f, 0.85f, 0.2f, 0.8f);
            for (int i = 0; i < level - 1; i++) {
                batch.draw(assets.white, position.x - 8 + i * 8, position.y + size / 2 + 2, 6, 6);
            }
            batch.setColor(1, 1, 1, 1);
        }
    }

    public void renderRange(SpriteBatch batch, Assets assets) {
        float range = type.rangeAtLevel(level);
        batch.setColor(0.4f, 0.7f, 1f, 0.12f);
        batch.draw(assets.circle, position.x - range, position.y - range, range * 2, range * 2);
        batch.setColor(1, 1, 1, 1);
    }

    public TowerType getType() { return type; }
    public Vector2 getPosition() { return position; }
    public int getLevel() { return level; }
    public float getRange() { return type.rangeAtLevel(level); }
    public List<Soldier> getSoldiers() { return soldiers; }

    public static class Soldier {
        public boolean alive = true;
        public int hp = 40;
        public float respawnTimer = 0;
    }
}
