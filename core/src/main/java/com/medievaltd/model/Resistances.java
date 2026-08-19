package com.medievaltd.model;

import java.util.EnumMap;
import java.util.Map;

public class Resistances {
    private final Map<DamageType, Float> map = new EnumMap<>(DamageType.class);
    private boolean slowImmune;

    public Resistances() {}

    public Resistances set(DamageType type, float pct) {
        map.put(type, Math.max(0f, Math.min(1f, pct)));
        return this;
    }

    public Resistances setImmune(DamageType type) {
        map.put(type, 1f);
        return this;
    }

    public Resistances setSlowImmune(boolean immune) {
        this.slowImmune = immune;
        return this;
    }

    public boolean isSlowImmune() {
        return slowImmune;
    }

    public float getResistance(DamageType type) {
        return map.getOrDefault(type, 0f);
    }

    public boolean isImmune(DamageType type) {
        return getResistance(type) >= 1f;
    }

    public int applyTo(int rawDamage, DamageType type) {
        float res = getResistance(type);
        if (res >= 1f) return 0;
        return Math.max(1, Math.round(rawDamage * (1f - res)));
    }

    public Resistances copy() {
        Resistances r = new Resistances();
        r.map.putAll(this.map);
        r.slowImmune = this.slowImmune;
        return r;
    }

    public static Resistances none() {
        return new Resistances();
    }

    public Map<DamageType, Float> getMap() {
        return map;
    }
}
