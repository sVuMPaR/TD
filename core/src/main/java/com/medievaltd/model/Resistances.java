package com.medievaltd.model;

import java.util.EnumMap;
import java.util.Map;

public class Resistances {
    private final Map<DamageType, Float> map = new EnumMap<>(DamageType.class);

    public Resistances() {}

    public Resistances set(DamageType type, float pct) {
        map.put(type, Math.max(0f, Math.min(0.9f, pct)));
        return this;
    }

    public float getResistance(DamageType type) {
        return map.getOrDefault(type, 0f);
    }

    public int applyTo(int rawDamage, DamageType type) {
        float res = getResistance(type);
        return Math.max(1, Math.round(rawDamage * (1f - res)));
    }

    public static Resistances none() {
        return new Resistances();
    }
}
