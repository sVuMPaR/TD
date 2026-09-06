package com.medievaltd.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.medievaltd.model.DamageType;
import com.medievaltd.util.Assets;

public class Projectile {
    public enum Kind { ARROW, CANNONBALL, MAGIC, BOLT }

    private final Kind kind;
    private final DamageType damageType;
    private final Vector2 position = new Vector2();
    private final Vector2 origin = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Vector2 tmp = new Vector2();
    private final int damage;
    private final TextureRegion texture;
    private final Enemy target;
    private final float speed;
    private final float slowMultiplier;
    private final float splashRadius;
    private final float slowDuration;
    private boolean active = true;
    private boolean reachedTarget;
    private float lifetime = 3f;

    private Projectile(Kind kind, DamageType damageType, Vector2 start, Enemy target,
                       int damage, TextureRegion texture, float slowMultiplier,
                       float splashRadius, float slowDuration) {
        this.kind = kind;
        this.damageType = damageType;
        this.position.set(start);
        this.origin.set(start);
        this.damage = damage;
        this.texture = texture;
        this.target = target;
        this.speed = kind == Kind.CANNONBALL ? 320f : kind == Kind.BOLT ? 520f : 440f;
        this.slowMultiplier = slowMultiplier;
        this.splashRadius = splashRadius;
        this.slowDuration = slowDuration;
    }

    public static Projectile arrow(Vector2 start, Enemy target, int damage, Assets a) {
        return new Projectile(Kind.ARROW, DamageType.PHYSICAL, start, target, damage, a.projectileArrow, 1f, 0f, 0f);
    }

    public static Projectile cannonball(Vector2 start, Enemy target, int damage, Assets a) {
        return new Projectile(Kind.CANNONBALL, DamageType.PHYSICAL, start, target, damage, a.projectileCannonball, 1f, 48f, 0f);
    }

    public static Projectile bolt(Vector2 start, Enemy target, int damage, Assets a) {
        return new Projectile(Kind.BOLT, DamageType.PHYSICAL, start, target, damage, a.projectileBolt, 1f, 0f, 0f);
    }

    public static Projectile magic(Vector2 start, Enemy target, int damage, DamageType dt, Assets a) {
        return magic(start, target, damage, dt, a, 1f, 0f, 0f);
    }

    public static Projectile magic(Vector2 start, Enemy target, int damage, DamageType dt, Assets a,
                                   float slow, float splash, float slowTime) {
        TextureRegion tex = switch (dt) {
            case FIRE -> a.projectileFire;
            case ICE -> a.projectileIce;
            case LIGHTNING -> a.projectileLightning;
            default -> a.projectileMagic;
        };
        return new Projectile(Kind.MAGIC, dt, start, target, damage, tex, slow, splash, slowTime);
    }

    public void update(float delta, Vector2[] path) {
        if (!active) return;
        lifetime -= delta;
        if (lifetime <= 0) {
            active = false;
            return;
        }

        if (target != null && target.isAlive() && !target.hasReachedEnd()) {
            Vector2 aim = target.getPosition(path, tmp);
            Vector2 dir = aim.sub(position);
            float dist = dir.len();
            if (dist <= 16f || dist <= speed * delta) {
                position.set(target.getPosition(path, tmp));
                reachedTarget = true;
                return;
            }
            velocity.set(dir.scl(speed / dist));
        }

        position.mulAdd(velocity, delta);
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        float w = texture.getRegionWidth();
        float h = texture.getRegionHeight();
        float angle = velocity.angleDeg();
        float scale = splashRadius >= 50f ? 1.55f : 1f;
        batch.draw(texture, position.x - w / 2f, position.y - h / 2f,
            w / 2f, h / 2f, w, h, scale, scale, angle);
    }

    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
    public boolean hasReachedTarget() { return reachedTarget; }
    public Enemy getTarget() { return target; }
    public Kind getKind() { return kind; }
    public DamageType getDamageType() { return damageType; }
    public Vector2 getPosition() { return position; }
    public Vector2 getOrigin() { return origin; }
    public int getDamage() { return damage; }
    public float getSlowMultiplier() { return slowMultiplier; }
    public float getSlowDuration() { return slowDuration; }

    public float getSplashRadius() {
        return splashRadius;
    }

    public boolean isPiercing() {
        return kind == Kind.BOLT;
    }

    public boolean isChaining() {
        return damageType == DamageType.LIGHTNING;
    }
}
