package com.medievaltd.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.medievaltd.util.Assets;

public class Projectile {
    public enum Kind { ARROW, CANNONBALL, MAGIC }

    private final Kind kind;
    private final Vector2 position;
    private final Vector2 velocity;
    private final int damage;
    private final TextureRegion texture;
    private boolean active = true;
    private float lifetime = 3f;

    private Projectile(Kind kind, Vector2 start, Vector2 target, int damage, TextureRegion texture) {
        this.kind = kind;
        this.position = new Vector2(start);
        this.damage = damage;
        this.texture = texture;

        Vector2 dir = new Vector2(target).sub(start);
        float speed = kind == Kind.CANNONBALL ? 280f : 420f;
        if (dir.len2() < 1f) {
            dir.set(1, 0);
        }
        this.velocity = dir.nor().scl(speed);
    }

    public static Projectile arrow(Vector2 start, Vector2 target, int damage, Assets assets) {
        return new Projectile(Kind.ARROW, start, target, damage, assets.projectileArrow);
    }

    public static Projectile cannonball(Vector2 start, Vector2 target, int damage, Assets assets) {
        return new Projectile(Kind.CANNONBALL, start, target, damage, assets.projectileCannonball);
    }

    public static Projectile magic(Vector2 start, Vector2 target, int damage, Assets assets) {
        return new Projectile(Kind.MAGIC, start, target, damage, assets.projectileMagic);
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
        batch.draw(texture, position.x - w / 2f, position.y - h / 2f, w / 2f, h / 2f, w, h, 1, 1, angle);
    }

    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
    public Kind getKind() { return kind; }
    public Vector2 getPosition() { return position; }
    public int getDamage() { return damage; }
    public float getSplashRadius() {
        return kind == Kind.CANNONBALL ? 48f : 0f;
    }
}
