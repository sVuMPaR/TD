package com.medievaltd.util;

import com.badlogic.gdx.math.Vector2;

public class FloatingText {
    public final Vector2 pos = new Vector2();
    public final String text;
    public float age;
    public final float lifetime;

    public FloatingText(String text, float x, float y) {
        this(text, x, y, 0.9f);
    }

    public FloatingText(String text, float x, float y, float lifetime) {
        this.text = text;
        this.pos.set(x, y);
        this.lifetime = lifetime;
    }

    public boolean update(float delta) {
        age += delta;
        return age >= lifetime;
    }

    public float alpha() {
        return Math.max(0f, 1f - age / lifetime);
    }

    public float drawY() {
        return pos.y + age * 46f;
    }
}
