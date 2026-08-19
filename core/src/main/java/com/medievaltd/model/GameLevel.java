package com.medievaltd.model;

import com.badlogic.gdx.math.Vector2;
import com.medievaltd.util.Assets;

import java.util.ArrayList;
import java.util.List;

public class GameLevel {
    public final String name;
    public final String description;
    public final int startingGold;
    public final int startingLives;
    public final float[] pathX;
    public final float[] pathY;
    public final List<BuildSpot> buildSpots;
    public final List<WaveDefinition> waves;

    public GameLevel(String name, String description, int startingGold, int startingLives,
                     float[] pathX, float[] pathY, List<BuildSpot> buildSpots, List<WaveDefinition> waves) {
        this.name = name;
        this.description = description;
        this.startingGold = startingGold;
        this.startingLives = startingLives;
        this.pathX = pathX;
        this.pathY = pathY;
        this.buildSpots = buildSpots;
        this.waves = waves;
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

        float[] path1X = {0, 180, 180, 420, 420, 680, 680, 960, 960, 1280};
        float[] path1Y = {360, 360, 180, 180, 520, 520, 240, 240, 400, 400};

        List<BuildSpot> spots1 = List.of(
            new BuildSpot(280, 280), new BuildSpot(280, 440),
            new BuildSpot(520, 120), new BuildSpot(520, 360),
            new BuildSpot(760, 160), new BuildSpot(760, 400),
            new BuildSpot(880, 280), new BuildSpot(1040, 480)
        );

        List<WaveDefinition> waves1 = List.of(
            new WaveDefinition(1, List.of(new SpawnEntry(EnemyType.GOBLIN, 8, 0.8f)), 3f),
            new WaveDefinition(2, List.of(new SpawnEntry(EnemyType.GOBLIN, 10, 0.6f), new SpawnEntry(EnemyType.WOLF, 4, 1.2f)), 4f),
            new WaveDefinition(3, List.of(new SpawnEntry(EnemyType.ORC, 6, 1.0f), new SpawnEntry(EnemyType.WOLF, 6, 0.7f)), 5f),
            new WaveDefinition(4, List.of(new SpawnEntry(EnemyType.KNIGHT, 4, 1.5f), new SpawnEntry(EnemyType.ORC, 8, 0.8f)), 5f),
            new WaveDefinition(5, List.of(new SpawnEntry(EnemyType.DRAGON, 1, 0f), new SpawnEntry(EnemyType.KNIGHT, 6, 1.0f)), 6f)
        );

        levels.add(new GameLevel(
            "Застава Лесного Короля",
            "Первая линия обороны у границы королевства",
            250, 20, path1X, path1Y, spots1, waves1
        ));

        float[] path2X = {0, 320, 320, 560, 560, 800, 800, 1080, 1080, 1280};
        float[] path2Y = {200, 200, 480, 480, 160, 160, 440, 440, 280, 280};

        List<BuildSpot> spots2 = List.of(
            new BuildSpot(220, 320), new BuildSpot(420, 100),
            new BuildSpot(480, 360), new BuildSpot(640, 520),
            new BuildSpot(720, 240), new BuildSpot(920, 160),
            new BuildSpot(960, 380), new BuildSpot(1120, 360)
        );

        List<WaveDefinition> waves2 = List.of(
            new WaveDefinition(1, List.of(new SpawnEntry(EnemyType.WOLF, 10, 0.5f)), 3f),
            new WaveDefinition(2, List.of(new SpawnEntry(EnemyType.GOBLIN, 12, 0.4f), new SpawnEntry(EnemyType.ORC, 5, 1.0f)), 4f),
            new WaveDefinition(3, List.of(new SpawnEntry(EnemyType.KNIGHT, 6, 1.2f)), 5f),
            new WaveDefinition(4, List.of(new SpawnEntry(EnemyType.ORC, 10, 0.6f), new SpawnEntry(EnemyType.KNIGHT, 4, 1.0f)), 5f),
            new WaveDefinition(5, List.of(new SpawnEntry(EnemyType.DRAGON, 2, 2.0f), new SpawnEntry(EnemyType.KNIGHT, 8, 0.8f)), 6f),
            new WaveDefinition(6, List.of(new SpawnEntry(EnemyType.DRAGON, 3, 1.5f)), 8f)
        );

        levels.add(new GameLevel(
            "Мост через пропасть",
            "Узкий проход — идеален для артиллерии",
            300, 15, path2X, path2Y, spots2, waves2
        ));

        return levels;
    }
}
