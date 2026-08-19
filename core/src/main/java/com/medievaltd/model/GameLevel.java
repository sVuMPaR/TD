package com.medievaltd.model;

import com.badlogic.gdx.math.Vector2;
import com.medievaltd.system.WaveGenerator;

import java.util.ArrayList;
import java.util.List;

public class GameLevel {
    public final String name;
    public final String description;
    public final int startingGold;
    public final int startingLives;
    public final int mapIndex;
    public final int totalWaves;
    public final float[] pathX;
    public final float[] pathY;
    public final List<BuildSpot> buildSpots;

    public GameLevel(String name, String description, int startingGold, int startingLives,
                     int mapIndex, int totalWaves, float[] pathX, float[] pathY,
                     List<BuildSpot> buildSpots) {
        this.name = name;
        this.description = description;
        this.startingGold = startingGold;
        this.startingLives = startingLives;
        this.mapIndex = mapIndex;
        this.totalWaves = totalWaves;
        this.pathX = pathX;
        this.pathY = pathY;
        this.buildSpots = buildSpots;
    }

    public List<WaveDefinition> generateWaves(Difficulty difficulty) {
        return WaveGenerator.generate(totalWaves, mapIndex, difficulty);
    }

    public Vector2[] getPathPoints() {
        Vector2[] points = new Vector2[pathX.length];
        for (int i = 0; i < pathX.length; i++) {
            points[i] = new Vector2(pathX[i], pathY[i]);
        }
        return points;
    }

    public static List<GameLevel> createLevels() {
        List<GameLevel> levels = new ArrayList<>();

        levels.add(new GameLevel(
            "Застава Лесного Короля",
            "15 волн. Обучение основам обороны",
            350, 20, 1, 15,
            new float[]{0, 180, 180, 420, 420, 680, 680, 960, 960, 1280},
            new float[]{360, 360, 180, 180, 520, 520, 240, 240, 400, 400},
            List.of(
                new BuildSpot(280, 280), new BuildSpot(280, 440),
                new BuildSpot(520, 120), new BuildSpot(520, 360),
                new BuildSpot(760, 160), new BuildSpot(760, 400),
                new BuildSpot(880, 280), new BuildSpot(1040, 480),
                new BuildSpot(100, 480), new BuildSpot(400, 300)
            )
        ));

        levels.add(new GameLevel(
            "Мост через пропасть",
            "20 волн. Баллиста и ледяная башня",
            400, 20, 2, 20,
            new float[]{0, 320, 320, 560, 560, 800, 800, 1080, 1080, 1280},
            new float[]{200, 200, 480, 480, 160, 160, 440, 440, 280, 280},
            List.of(
                new BuildSpot(220, 320), new BuildSpot(420, 100),
                new BuildSpot(480, 360), new BuildSpot(640, 520),
                new BuildSpot(720, 240), new BuildSpot(920, 160),
                new BuildSpot(960, 380), new BuildSpot(1120, 360),
                new BuildSpot(160, 100), new BuildSpot(640, 300)
            )
        ));

        levels.add(new GameLevel(
            "Замёрзшее ущелье",
            "20 волн. Стихийная магия!",
            420, 20, 3, 20,
            new float[]{0, 200, 200, 500, 500, 750, 750, 1000, 1000, 1280},
            new float[]{500, 500, 200, 200, 450, 450, 150, 150, 350, 350},
            List.of(
                new BuildSpot(300, 340), new BuildSpot(100, 380),
                new BuildSpot(350, 120), new BuildSpot(600, 320),
                new BuildSpot(630, 520), new BuildSpot(850, 300),
                new BuildSpot(900, 100), new BuildSpot(1100, 250),
                new BuildSpot(1100, 440), new BuildSpot(450, 520)
            )
        ));

        levels.add(new GameLevel(
            "Врата Тьмы",
            "25 волн. Финальное сражение!",
            450, 18, 4, 25,
            new float[]{0, 160, 160, 400, 400, 640, 640, 900, 900, 1100, 1100, 1280},
            new float[]{400, 400, 600, 600, 200, 200, 500, 500, 300, 300, 500, 500},
            List.of(
                new BuildSpot(260, 480), new BuildSpot(260, 300),
                new BuildSpot(500, 400), new BuildSpot(520, 120),
                new BuildSpot(740, 340), new BuildSpot(740, 580),
                new BuildSpot(980, 200), new BuildSpot(980, 420),
                new BuildSpot(1180, 400), new BuildSpot(1050, 580),
                new BuildSpot(360, 520), new BuildSpot(860, 120)
            )
        ));

        return levels;
    }
}
