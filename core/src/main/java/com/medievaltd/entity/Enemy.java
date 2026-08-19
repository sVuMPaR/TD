package com.medievaltd.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.medievaltd.model.DamageType;
import com.medievaltd.model.EnemyRole;
import com.medievaltd.model.EnemyType;
import com.medievaltd.model.Resistances;
import com.medievaltd.util.Assets;

public class Enemy {
    private final EnemyType type;
    private final EnemyRole role;
    private final Resistances resistances;
    private final int maxHealth;
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

    public Enemy(EnemyType type, int adjustedHealth, EnemyRole role, Resistances resistances) {
        this.type = type;
        this.role = role;
        this.resistances = resistances;
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
        if (role == EnemyRole.BOSS || role == EnemyRole.FINAL_BOSS) speed *= 0.7f;
        else if (role == EnemyRole.MINI_BOSS) speed *= 0.85f;
        pathProgress += (speed * delta) / totalLength;

        if (pathProgress >= 1f) {
            pathProgress = 1f;
            alive = false;
            reachedEnd = true;
        }
    }

    public void applySlow(float multiplier, float duration) {
        if (resistances.isSlowImmune()) return;
        slowMultiplier = Math.min(slowMultiplier, multiplier);
        slowTimer = Math.max(slowTimer, duration);
    }

    public void applyDot(float dps, float duration) {
        dotDamage = Math.max(dotDamage, dps);
        dotTimer = Math.max(dotTimer, duration);
    }

    public void block(float duration) {
        if (role == EnemyRole.FINAL_BOSS) return;
        if (role == EnemyRole.BOSS) { duration *= 0.3f; }
        blocked = true;
        blockedTimer = duration;
    }

    public void takeDamage(int rawDamage, DamageType damageType) {
        int actual = resistances.applyTo(rawDamage, damageType);
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
        float baseSize = tex.getRegionWidth();
        float scale = switch (role) {
            case MINI_BOSS -> 1.4f;
            case BOSS -> 1.8f;
            case FINAL_BOSS -> 2.2f;
            default -> 1f;
        };
        float size = baseSize * scale;
        batch.draw(tex, pos.x - size / 2f, pos.y - size / 2f, size, size);

        // Boss aura
        if (role != EnemyRole.NORMAL) {
            Color aura = switch (role) {
                case MINI_BOSS -> new Color(1f, 0.7f, 0.2f, 0.25f);
                case BOSS -> new Color(1f, 0.3f, 0.1f, 0.3f);
                case FINAL_BOSS -> new Color(0.6f, 0.1f, 0.8f, 0.35f);
                default -> Color.CLEAR;
            };
            batch.setColor(aura);
            float auraSize = size * 1.4f;
            batch.draw(assets.circle, pos.x - auraSize / 2f, pos.y - auraSize / 2f, auraSize, auraSize);
            batch.setColor(1, 1, 1, 1);
        }

        // Health bar
        if (alive && health < maxHealth) {
            float barW = Math.max(32, size);
            float barH = role == EnemyRole.NORMAL ? 4 : 6;
            float pct = (float) health / maxHealth;
            batch.setColor(0.15f, 0.15f, 0.15f, 0.85f);
            batch.draw(assets.white, pos.x - barW / 2, pos.y + size / 2 + 4, barW, barH);
            Color barColor = role == EnemyRole.NORMAL ? new Color(0.2f, 0.8f, 0.2f, 1f)
                : new Color(0.9f, 0.3f, 0.1f, 1f);
            batch.setColor(barColor);
            batch.draw(assets.white, pos.x - barW / 2, pos.y + size / 2 + 4, barW * pct, barH);
            batch.setColor(1, 1, 1, 1);
        }

        // Resistance/immunity indicators for bosses
        if (role != EnemyRole.NORMAL && alive) {
            float iconY = pos.y - size / 2 - 10;
            float iconX = pos.x - 14;
            for (var entry : resistances.getMap().entrySet()) {
                if (entry.getValue() >= 1f) {
                    batch.setColor(getElementColor(entry.getKey()));
                    batch.draw(assets.white, iconX, iconY, 8, 8);
                    batch.setColor(1, 1, 1, 1);
                    iconX += 10;
                }
            }
            if (resistances.isSlowImmune()) {
                batch.setColor(0.5f, 0.9f, 1f, 0.8f);
                batch.draw(assets.white, iconX, iconY, 8, 8);
                batch.setColor(1, 1, 1, 1);
            }
        }
    }

    private Color getElementColor(DamageType dt) {
        return switch (dt) {
            case FIRE -> new Color(1f, 0.4f, 0.1f, 0.9f);
            case ICE -> new Color(0.3f, 0.7f, 1f, 0.9f);
            case LIGHTNING -> new Color(1f, 0.95f, 0.3f, 0.9f);
            case PHYSICAL -> new Color(0.6f, 0.55f, 0.5f, 0.9f);
            case MAGIC -> new Color(0.7f, 0.3f, 0.9f, 0.9f);
        };
    }

    public boolean isAlive() { return alive; }
    public boolean hasReachedEnd() { return reachedEnd; }
    public EnemyType getType() { return type; }
    public EnemyRole getRole() { return role; }
    public int getHealth() { return health; }
    public float getPathProgress() { return pathProgress; }
    public boolean isBlocked() { return blocked; }
    public Resistances getResistances() { return resistances; }
}
