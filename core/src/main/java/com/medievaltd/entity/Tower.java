package com.medievaltd.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.medievaltd.model.TowerType;
import com.medievaltd.util.Assets;

import java.util.List;

public class Tower {
    private final TowerType type;
    private final Vector2 position;
    private int level = 1;
    private float fireCooldown;

    public Tower(TowerType type, float x, float y) {
        this.type = type;
        this.position = new Vector2(x, y);
    }

    public void update(float delta, List<Enemy> enemies, List<Projectile> projectiles, Vector2[] path, Assets assets) {
        fireCooldown -= delta;
        if (fireCooldown > 0) return;

        Enemy target = findTarget(enemies, path);
        if (target == null) return;

        fireCooldown = type.fireRateAtLevel(level);
        int damage = type.damageAtLevel(level);
        Vector2 targetPos = target.getPosition(path, new Vector2());

        switch (type) {
            case ARCHER -> projectiles.add(Projectile.arrow(new Vector2(position), targetPos, damage, assets));
            case ARTILLERY -> projectiles.add(Projectile.cannonball(new Vector2(position), targetPos, damage, assets));
            case MAGIC -> projectiles.add(Projectile.magic(new Vector2(position), targetPos, damage, assets));
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
        return true;
    }

    public void render(SpriteBatch batch, Assets assets) {
        TextureRegion tex = switch (type) {
            case ARCHER -> assets.towerArcher;
            case ARTILLERY -> assets.towerArtillery;
            case MAGIC -> assets.towerMagic;
        };
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
}
