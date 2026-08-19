package com.medievaltd.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.medievaltd.model.DamageType;
import com.medievaltd.model.EnemyType;
import com.medievaltd.util.Assets;

public class Enemy {
    private final EnemyType type;
    private int maxHealth;
    private int health;
    private float pathProgress;
    private float slowMultiplier = 1f;
    private float slowTimer;
    private float dotDamage;
    private float dotTimer;
    private boolean alive = true;
    private boolean reachedEnd;
    private boolean blocked;
    private float blockedTimer;

    public Enemy(EnemyType type, int adjustedHealth) {
        this.type = type;
        this.maxHealth = adjustedHealth;
        this.health = adjustedHealth;
    }

    public void update(float delta, Vector2[] path) {
        if (!alive) return;

        if (slowTimer > 0) {
            slowTimer -= delta;
            if (slowTimer <= 0) slowMultiplier = 1f;
        }

        if (dotTimer > 0) {
            dotTimer -= delta;
            health -= Math.round(dotDamage * delta);
            if (health <= 0) { health = 0; alive = false; return; }
        }

        if (blocked) {
            blockedTimer -= delta;
            if (blockedTimer <= 0) blocked = false;
            return;
        }

        float totalLength = computePathLength(path);
        float speed = type.speed * slowMultiplier;
        pathProgress += (speed * delta) / totalLength;

        if (pathProgress >= 1f) {
            pathProgress = 1f;
            alive = false;
            reachedEnd = true;
        }
    }

    public void applySlow(float multiplier, float duration) {
        slowMultiplier = Math.min(slowMultiplier, multiplier);
        slowTimer = Math.max(slowTimer, duration);
    }

    public void applyDot(float dps, float duration) {
        dotDamage = Math.max(dotDamage, dps);
        dotTimer = Math.max(dotTimer, duration);
    }

    public void block(float duration) {
        blocked = true;
        blockedTimer = duration;
    }

    public void takeDamage(int rawDamage, DamageType damageType) {
        int actual = type.resistances.applyTo(rawDamage, damageType);
        health -= actual;
        if (health <= 0) { health = 0; alive = false; }
    }

    public Vector2 getPosition(Vector2[] path, Vector2 out) {
        float totalLength = computePathLength(path);
        float target = pathProgress * totalLength;
        float accumulated = 0;
        for (int i = 0; i < path.length - 1; i++) {
            float segLen = path[i].dst(path[i + 1]);
            if (accumulated + segLen >= target) {
                float t = (target - accumulated) / segLen;
                out.set(path[i]).lerp(path[i + 1], t);
                return out;
            }
            accumulated += segLen;
        }
        out.set(path[path.length - 1]);
        return out;
    }

    private float computePathLength(Vector2[] path) {
        float length = 0;
        for (int i = 0; i < path.length - 1; i++) length += path[i].dst(path[i + 1]);
        return length;
    }

    public void render(SpriteBatch batch, Assets assets, Vector2[] path) {
        if (!alive && !reachedEnd) return;
        Vector2 pos = getPosition(path, new Vector2());
        TextureRegion tex = type.getTexture(assets);
        float size = tex.getRegionWidth();
        batch.draw(tex, pos.x - size / 2f, pos.y - size / 2f, size, size);

        if (alive && health < maxHealth) {
            float barW = 32, barH = 4;
            float pct = (float) health / maxHealth;
            batch.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            batch.draw(assets.white, pos.x - barW / 2, pos.y + size / 2 + 4, barW, barH);
            batch.setColor(0.2f, 0.8f, 0.2f, 1f);
            batch.draw(assets.white, pos.x - barW / 2, pos.y + size / 2 + 4, barW * pct, barH);
            batch.setColor(1, 1, 1, 1);
        }
    }

    public boolean isAlive() { return alive; }
    public boolean hasReachedEnd() { return reachedEnd; }
    public EnemyType getType() { return type; }
    public int getHealth() { return health; }
    public float getPathProgress() { return pathProgress; }
    public boolean isBlocked() { return blocked; }
}
