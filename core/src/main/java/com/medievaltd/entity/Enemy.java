package com.medievaltd.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.medievaltd.model.Difficulty;
import com.medievaltd.model.DamageType;
import com.medievaltd.model.EnemyRole;
import com.medievaltd.model.EnemyType;
import com.medievaltd.model.Resistances;
import com.medievaltd.util.Assets;

public class Enemy {
    private final EnemyType type;
    private final EnemyRole role;
    private final Difficulty difficulty;
    private final Resistances resistances;
    private final int maxHealth;
    private int health;
    private float pathProgress;
    private float slowMultiplier = 1f;
    private float slowTimer;
    private float dotDamage;
    private float dotTimer;
    private float dotCarry;
    private boolean alive = true;
    private boolean reachedEnd;
    private boolean blocked;
    private float blockedTimer;

    public Enemy(EnemyType type, int adjustedHealth, EnemyRole role, Resistances resistances, Difficulty difficulty) {
        this.type = type;
        this.role = role;
        this.difficulty = difficulty;
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
            float step = Math.min(delta, dotTimer);
            dotTimer -= delta;
            dotCarry += dotDamage * step;
            int tick = (int) dotCarry;
            if (tick > 0) {
                health -= tick;
                dotCarry -= tick;
            }
            if (dotTimer <= 0) {
                dotTimer = 0;
                dotDamage = 0;
                dotCarry = 0;
            }
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

    public void render(SpriteBatch batch, Assets assets, Vector2[] path, BitmapFont font) {
        if (!alive && !reachedEnd) return;
        Vector2 pos = getPosition(path, new Vector2());
        TextureRegion tex = type.getTexture(assets);
        if (difficulty == Difficulty.HARD) {
            batch.setColor(1f, 0.65f, 0.65f, 1f);
        } else if (difficulty == Difficulty.NORMAL) {
            batch.setColor(0.85f, 0.9f, 1f, 1f);
        }
        float baseSize = tex.getRegionWidth();
        float scale = switch (role) {
            case MINI_BOSS -> 1.4f;
            case BOSS -> 1.8f;
            case FINAL_BOSS -> 2.2f;
            default -> 1f;
        };
        float size = baseSize * scale;
        batch.draw(tex, pos.x - size / 2f, pos.y - size / 2f, size, size);
        batch.setColor(1, 1, 1, 1);

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

        boolean boss = role != EnemyRole.NORMAL;
        if (alive && (boss || health < maxHealth)) {
            float barW = Math.max(36, size);
            float barH = boss ? 7 : 4;
            float pct = (float) health / maxHealth;
            float barY = pos.y + size / 2 + 4;
            batch.setColor(0.15f, 0.15f, 0.15f, 0.85f);
            batch.draw(assets.white, pos.x - barW / 2, barY, barW, barH);
            Color barColor = boss ? new Color(0.9f, 0.3f, 0.1f, 1f) : new Color(0.2f, 0.8f, 0.2f, 1f);
            batch.setColor(barColor);
            batch.draw(assets.white, pos.x - barW / 2, barY, barW * pct, barH);
            batch.setColor(1, 1, 1, 1);
        }

        if (font != null && alive) {
            drawLabels(batch, font, pos, size, boss);
        }
    }

    private void drawLabels(SpriteBatch batch, BitmapFont font, Vector2 pos, float size, boolean boss) {
        GlyphLayout layout = new GlyphLayout();
        float top = pos.y + size / 2f + (boss ? 28 : 18);

        if (boss) {
            String roleName = switch (role) {
                case MINI_BOSS -> "Мини-босс";
                case BOSS -> "Босс";
                case FINAL_BOSS -> "Финальный босс";
                default -> "";
            };
            font.setColor(1f, 0.82f, 0.25f, 1f);
            layout.setText(font, roleName);
            font.draw(batch, roleName, pos.x - layout.width / 2f, top + 16);
            String hp = health + "/" + maxHealth;
            font.setColor(1f, 0.9f, 0.85f, 1f);
            layout.setText(font, hp);
            font.draw(batch, hp, pos.x - layout.width / 2f, top);
        }

        String tags = resistanceTags();
        if (!tags.isEmpty()) {
            font.setColor(0.95f, 0.85f, 0.55f, 1f);
            layout.setText(font, tags);
            font.draw(batch, tags, pos.x - layout.width / 2f, pos.y - size / 2f - 4);
        }
        font.setColor(1, 1, 1, 1);
    }

    private String resistanceTags() {
        StringBuilder sb = new StringBuilder();
        for (var entry : resistances.getMap().entrySet()) {
            float v = entry.getValue();
            if (v < 0.2f) continue;
            if (sb.length() > 0) sb.append("  ");
            if (v >= 1f) sb.append(entry.getKey().shortName).append(" имм");
            else sb.append(entry.getKey().shortName).append(' ').append(Math.round(v * 100)).append('%');
        }
        if (resistances.isSlowImmune()) {
            if (sb.length() > 0) sb.append("  ");
            sb.append("замрд имм");
        }
        return sb.toString();
    }

    public float getDodgeChance() { return 0f; }

    public boolean isSlowed() { return slowTimer > 0; }
    public boolean isAlive() { return alive; }
    public boolean hasReachedEnd() { return reachedEnd; }
    public EnemyType getType() { return type; }
    public EnemyRole getRole() { return role; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public float getPathProgress() { return pathProgress; }
    public boolean isBlocked() { return blocked; }
    public Resistances getResistances() { return resistances; }
}
