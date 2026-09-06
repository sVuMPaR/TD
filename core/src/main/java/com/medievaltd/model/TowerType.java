package com.medievaltd.model;

public enum TowerType {
    ARCHER("Лучники", 80, 1.0f, 120f, 12, DamageType.PHYSICAL,
        "Быстрая стрельба по одной цели", false),
    ARTILLERY("Артиллерия", 150, 2.5f, 100f, 35, DamageType.PHYSICAL,
        "Мощный урон по области", false),
    BALLISTA("Баллиста", 130, 2.0f, 140f, 28, DamageType.PHYSICAL,
        "Пробивает нескольких врагов", false),
    MAGIC("Магия", 120, 1.8f, 110f, 18, DamageType.MAGIC,
        "Универсальный магический урон", false),
    FIRE_MAGIC("Огонь", 140, 2.0f, 100f, 14, DamageType.FIRE,
        "Поджигает — урон со временем", false),
    ICE_MAGIC("Маг льда", 140, 1.9f, 130f, 16, DamageType.ICE,
        "Одна цель: сильный удар и долгое замедление", false),
    LIGHTNING_MAGIC("Молния", 160, 1.6f, 115f, 22, DamageType.LIGHTNING,
        "Цепная молния (3 цели)", false),
    ICE_TOWER("Ледяная башня", 110, 2.4f, 92f, 7, DamageType.ICE,
        "Морозит пачку вокруг удара. Урона мало.", false),
    BARRACKS("Казарма", 100, 0f, 100f, 0, DamageType.PHYSICAL,
        "Воины держат врагов у дороги", false),
    TEMPLE("Храм", 0, 0f, 320f, 0, DamageType.PHYSICAL,
        "Бафф соседних башен (бесплатно)", true);

    public final String displayName;
    public final int baseCost;
    public final float baseFireRate;
    public final float baseRange;
    public final int baseDamage;
    public final DamageType damageType;
    public final String description;
    public final boolean freeToPlace;

    TowerType(String displayName, int baseCost, float baseFireRate, float baseRange,
              int baseDamage, DamageType damageType, String description, boolean freeToPlace) {
        this.displayName = displayName;
        this.baseCost = baseCost;
        this.baseFireRate = baseFireRate;
        this.baseRange = baseRange;
        this.baseDamage = baseDamage;
        this.damageType = damageType;
        this.description = description;
        this.freeToPlace = freeToPlace;
    }

    public int upgradeCost(int level) {
        if (this == TEMPLE) {
            return 60 + (level - 1) * 40;
        }
        return (int) (baseCost * (0.6f + level * 0.5f));
    }

    public int damageAtLevel(int level) {
        return (int) (baseDamage * (1f + (level - 1) * 0.35f));
    }

    public float rangeAtLevel(int level) {
        return baseRange * (1f + (level - 1) * 0.12f);
    }

    public float fireRateAtLevel(int level) {
        if (baseFireRate <= 0) return 0;
        return baseFireRate * (1f - (level - 1) * 0.08f);
    }

    public int sellValue(int level) {
        int total = baseCost;
        for (int i = 1; i < level; i++) {
            total += upgradeCost(i);
        }
        return total / 2;
    }

    public String statLine() {
        if (this == TEMPLE) return "Радиус баффа: " + (int) baseRange;
        if (this == BARRACKS) return "Патруль: " + (int) baseRange + "   Бойцы: 2-3";
        if (this == ICE_TOWER) return "Урон: " + baseDamage + "   Радиус: " + (int) baseRange + "   Обл.: " + (int) iceSplash();
        if (this == ICE_MAGIC) return "Урон: " + baseDamage + "   Радиус: " + (int) baseRange + "   Одна цель";
        return "Урон: " + baseDamage + "   Радиус: " + (int) baseRange;
    }

    /** Pack freeze radius. Magus has none. */
    public float iceSplash() {
        return this == ICE_TOWER ? 56f : 0f;
    }

    public float iceSlow() {
        return switch (this) {
            case ICE_TOWER -> 0.40f;
            case ICE_MAGIC -> 0.35f;
            default -> 1f;
        };
    }

    public float iceSlowTime() {
        return switch (this) {
            case ICE_TOWER -> 2.8f;
            case ICE_MAGIC -> 3.6f;
            default -> 0f;
        };
    }

    public String fireLine() {
        if (baseFireRate <= 0) return null;
        return String.format(java.util.Locale.US, "Выстрел: %.1f с", baseFireRate);
    }

    public boolean isElementalMagic() {
        return this == FIRE_MAGIC || this == ICE_MAGIC || this == LIGHTNING_MAGIC;
    }

    public boolean isAvailableAtLevel(int mapLevel) {
        if (isElementalMagic()) return mapLevel >= 3;
        if (this == MAGIC) return mapLevel < 3;
        if (this == BALLISTA || this == ICE_TOWER) return mapLevel >= 2;
        return true;
    }
}
