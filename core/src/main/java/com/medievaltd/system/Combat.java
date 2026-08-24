package com.medievaltd.system;

import com.badlogic.gdx.math.MathUtils;

/**
 * Hit chance is a roll, not ballistic leading. Dodge and accuracy research
 * can plug in later without changing projectile motion.
 */
public final class Combat {
    public static final float BASE_HIT_CHANCE = 0.95f;

    private Combat() {}

    public static boolean rollHit(float accuracy, float dodge) {
        float chance = MathUtils.clamp(accuracy * (1f - dodge), 0.05f, 0.99f);
        return MathUtils.random() < chance;
    }
}
