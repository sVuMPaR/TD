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

        float[] p1x = {0, 180, 180, 420, 420, 680, 680, 960, 960, 1280};
        float[] p1y = {360, 360, 180, 180, 520, 520, 240, 240, 400, 400};
        levels.add(new GameLevel(
            "Застава Лесного Короля",
            "15 волн. Обучение основам обороны",
            350, 20, 1, 15, p1x, p1y, spotsAlongRoad(p1x, p1y)
        ));

        float[] p2x = {0, 320, 320, 560, 560, 800, 800, 1080, 1080, 1280};
        float[] p2y = {200, 200, 480, 480, 160, 160, 440, 440, 280, 280};
        levels.add(new GameLevel(
            "Мост через пропасть",
            "20 волн. Баллиста и ледяная башня",
            400, 20, 2, 20, p2x, p2y, spotsAlongRoad(p2x, p2y)
        ));

        float[] p3x = {0, 200, 200, 500, 500, 750, 750, 1000, 1000, 1280};
        float[] p3y = {500, 500, 200, 200, 450, 450, 150, 150, 350, 350};
        levels.add(new GameLevel(
            "Замёрзшее ущелье",
            "20 волн. Стихийная магия!",
            420, 20, 3, 20, p3x, p3y, spotsAlongRoad(p3x, p3y)
        ));

        float[] p4x = {0, 160, 160, 400, 400, 640, 640, 900, 900, 1100, 1100, 1280};
        float[] p4y = {400, 400, 600, 600, 200, 200, 500, 500, 300, 300, 500, 500};
        levels.add(new GameLevel(
            "Врата Тьмы",
            "25 волн. Финальное сражение!",
            450, 18, 4, 25, p4x, p4y, spotsAlongRoad(p4x, p4y)
        ));

        return levels;
    }

    /** Places build slots just off the road so towers actually cover the path. */
    public static List<BuildSpot> spotsAlongRoad(float[] pathX, float[] pathY) {
        List<BuildSpot> spots = new ArrayList<>();
        final float offset = 56f;
        final float minDist = 86f;
        for (int i = 0; i < pathX.length - 1; i++) {
            float x1 = pathX[i], y1 = pathY[i];
            float x2 = pathX[i + 1], y2 = pathY[i + 1];
            float dx = x2 - x1, dy = y2 - y1;
            float len = (float) Math.hypot(dx, dy);
            if (len < 70f) continue;
            float nx = -dy / len, ny = dx / len;
            int samples = len > 220f ? 2 : 1;
            for (int s = 0; s < samples; s++) {
                float t = samples == 1 ? 0.5f : (s == 0 ? 0.32f : 0.68f);
                float cx = x1 + dx * t;
                float cy = y1 + dy * t;
                tryAddSpot(spots, cx + nx * offset, cy + ny * offset, minDist);
                tryAddSpot(spots, cx - nx * offset, cy - ny * offset, minDist);
            }
        }
        return spots;
    }

    private static void tryAddSpot(List<BuildSpot> spots, float x, float y, float minDist) {
        if (x < 70 || x > 1210 || y < 100 || y > 660) return;
        for (BuildSpot s : spots) {
            if (Vector2.dst(s.x, s.y, x, y) < minDist) return;
        }
        spots.add(new BuildSpot(x, y));
    }
}
