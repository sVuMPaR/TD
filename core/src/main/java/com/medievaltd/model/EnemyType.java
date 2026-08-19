package com.medievaltd.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.medievaltd.util.Assets;

public enum EnemyType {
    GOBLIN("Гоблин", 40, 55f, 5, 8, Resistances.none()),
    WOLF("Волк", 55, 85f, 8, 12, Resistances.none()),
    ORC("Орк", 120, 40f, 15, 18, new Resistances().set(DamageType.PHYSICAL, 0.15f)),
    KNIGHT("Рыцарь", 200, 35f, 25, 30, new Resistances().set(DamageType.PHYSICAL, 0.25f)),
    DRAGON("Дракон", 500, 28f, 40, 80, new Resistances().set(DamageType.FIRE, 0.5f)),
    FROST_ELEMENTAL("Ледяной элементаль", 160, 38f, 20, 25,
        new Resistances().set(DamageType.ICE, 0.6f)),
    FIRE_IMP("Огненный бес", 90, 70f, 10, 15,
        new Resistances().set(DamageType.FIRE, 0.5f)),
    GOLEM("Голем", 350, 22f, 30, 50,
        new Resistances().set(DamageType.PHYSICAL, 0.35f).set(DamageType.LIGHTNING, 0.3f)),
    DARK_MAGE("Тёмный маг", 180, 42f, 20, 35,
        new Resistances().set(DamageType.MAGIC, 0.4f).set(DamageType.FIRE, 0.2f)),
    WYVERN("Виверна", 280, 50f, 30, 45,
        new Resistances().set(DamageType.ICE, 0.3f).set(DamageType.PHYSICAL, 0.2f));

    public final String displayName;
    public final int maxHealth;
    public final float speed;
    public final int damageToBase;
    public final int goldReward;
    public final Resistances resistances;

    EnemyType(String displayName, int maxHealth, float speed, int damageToBase,
              int goldReward, Resistances resistances) {
        this.displayName = displayName;
        this.maxHealth = maxHealth;
        this.speed = speed;
        this.damageToBase = damageToBase;
        this.goldReward = goldReward;
        this.resistances = resistances;
    }

    public TextureRegion getTexture(Assets assets) {
        return switch (this) {
            case GOBLIN -> assets.enemyGoblin;
            case WOLF -> assets.enemyWolf;
            case ORC -> assets.enemyOrc;
            case KNIGHT -> assets.enemyKnight;
            case DRAGON -> assets.enemyDragon;
            case FROST_ELEMENTAL -> assets.enemyFrostElemental;
            case FIRE_IMP -> assets.enemyFireImp;
            case GOLEM -> assets.enemyGolem;
            case DARK_MAGE -> assets.enemyDarkMage;
            case WYVERN -> assets.enemyWyvern;
        };
    }
}
