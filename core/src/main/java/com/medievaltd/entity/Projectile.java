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
    private final Vector2 position;
    private final Vector2 velocity;
    private final int damage;
    private final TextureRegion texture;
    private boolean active = true;
    private float lifetime = 3f;

    private Projectile(Kind kind, DamageType damageType, Vector2 start, Vector2 target,
                       int damage, TextureRegion texture) {
        this.kind = kind;
        this.damageType = damageType;
        this.position = new Vector2(start);
        this.damage = damage;
        this.texture = texture;
        Vector2 dir = new Vector2(target).sub(start);
        float speed = kind == Kind.CANNONBALL ? 280f : kind == Kind.BOLT ? 500f : 420f;
        if (dir.len2() < 1f) dir.set(1, 0);
        this.velocity = dir.nor().scl(speed);
    }

    public static Projectile arrow(Vector2 start, Vector2 target, int damage, Assets a) {
        return new Projectile(Kind.ARROW, DamageType.PHYSICAL, start, target, damage, a.projectileArrow);
    }

    public static Projectile cannonball(Vector2 start, Vector2 target, int damage, Assets a) {
        return new Projectile(Kind.CANNONBALL, DamageType.PHYSICAL, start, target, damage, a.projectileCannonball);
    }

    public static Projectile bolt(Vector2 start, Vector2 target, int damage, Assets a) {
        return new Projectile(Kind.BOLT, DamageType.PHYSICAL, start, target, damage, a.projectileBolt);
    }

    public static Projectile magic(Vector2 start, Vector2 target, int damage, DamageType dt, Assets a) {
        TextureRegion tex = switch (dt) {
            case FIRE -> a.projectileFire;
            case ICE -> a.projectileIce;
            case LIGHTNING -> a.projectileLightning;
            default -> a.projectileMagic;
        };
        return new Projectile(Kind.MAGIC, dt, start, target, damage, tex);
    }

    public void update(float delta) {
        if (!active) return;
        position.mulAdd(velocity, delta);
        lifetime -= delta;
        if (lifetime <= 0) active = false;
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        float w = texture.getRegionWidth();
        float h = texture.getRegionHeight();
        float angle = velocity.angleDeg();
        batch.draw(texture, position.x - w / 2f, position.y - h / 2f,
            w / 2f, h / 2f, w, h, 1, 1, angle);
    }

    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
    public Kind getKind() { return kind; }
    public DamageType getDamageType() { return damageType; }
    public Vector2 getPosition() { return position; }
    public int getDamage() { return damage; }

    public float getSplashRadius() {
        if (kind == Kind.CANNONBALL) return 48f;
        if (damageType == DamageType.ICE) return 40f;
        return 0f;
    }

    public boolean isPiercing() {
        return kind == Kind.BOLT;
    }

    public boolean isChaining() {
        return damageType == DamageType.LIGHTNING;
    }
}
