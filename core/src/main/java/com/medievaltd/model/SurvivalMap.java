package com.medievaltd.model;

public class SurvivalMap {
    public static final int TOTAL_WAVES = 100;

    public static GameLevel create() {
        return new GameLevel(
            "Арена выживания",
            "100 волн. Бесконечная мощь врагов!",
            500, 30, 99, TOTAL_WAVES,
            new float[]{0, 200, 200, 460, 460, 720, 720, 980, 980, 1280},
            new float[]{360, 360, 160, 160, 520, 520, 220, 220, 420, 420},
            GameLevel.spotsAlongRoad(
                new float[]{0, 200, 200, 460, 460, 720, 720, 980, 980, 1280},
                new float[]{360, 360, 160, 160, 520, 520, 220, 220, 420, 420}
            )
        );
    }
}
