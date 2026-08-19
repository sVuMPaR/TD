package com.medievaltd.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.medievaltd.util.Assets;

public enum EnemyType {
    GOBLIN("Гоблин", 40, 55f, 5, 8),
    WOLF("Волк", 55, 85f, 8, 12),
    ORC("Орк", 120, 40f, 15, 18),
    KNIGHT("Рыцарь", 200, 35f, 25, 30),
    DRAGON("Дракон", 500, 28f, 40, 80);

    public final String displayName;
    public final int maxHealth;
    public final float speed;
    public final int damageToBase;
    public final int goldReward;

    EnemyType(String displayName, int maxHealth, float speed, int damageToBase, int goldReward) {
        this.displayName = displayName;
        this.maxHealth = maxHealth;
        this.speed = speed;
        this.damageToBase = damageToBase;
        this.goldReward = goldReward;
    }

    public TextureRegion getTexture(Assets assets) {
        return switch (this) {
            case GOBLIN -> assets.enemyGoblin;
            case WOLF -> assets.enemyWolf;
            case ORC -> assets.enemyOrc;
            case KNIGHT -> assets.enemyKnight;
            case DRAGON -> assets.enemyDragon;
        };
    }
}
