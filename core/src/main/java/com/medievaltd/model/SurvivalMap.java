package com.medievaltd.model;

import com.badlogic.gdx.math.Vector2;
import com.medievaltd.system.WaveGenerator;

import java.util.List;

public class SurvivalMap {
    public static final int TOTAL_WAVES = 100;

    public static GameLevel create() {
        return new GameLevel(
            "Арена выживания",
            "100 волн. Бесконечная мощь врагов!",
            400, 30, 99, TOTAL_WAVES,
            new float[]{0, 200, 200, 460, 460, 720, 720, 980, 980, 1280},
            new float[]{360, 360, 160, 160, 520, 520, 220, 220, 420, 420},
            List.of(
                new BuildSpot(100, 480), new BuildSpot(300, 280),
                new BuildSpot(300, 440), new BuildSpot(560, 100),
                new BuildSpot(560, 360), new BuildSpot(560, 580),
                new BuildSpot(820, 140), new BuildSpot(820, 380),
                new BuildSpot(1080, 300), new BuildSpot(1080, 520),
                new BuildSpot(140, 240), new BuildSpot(420, 520),
                new BuildSpot(680, 480), new BuildSpot(900, 540),
                new BuildSpot(1180, 200)
            )
        );
    }
}
