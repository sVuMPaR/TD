package com.medievaltd.model;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class GameLevel {
    public final String name;
    public final String description;
    public final int startingGold;
    public final int startingLives;
    public final int mapIndex;
    public final float[] pathX;
    public final float[] pathY;
    public final List<BuildSpot> buildSpots;
    public final List<WaveDefinition> waves;

    public GameLevel(String name, String description, int startingGold, int startingLives,
                     int mapIndex, float[] pathX, float[] pathY,
                     List<BuildSpot> buildSpots, List<WaveDefinition> waves) {
        this.name = name;
        this.description = description;
        this.startingGold = startingGold;
        this.startingLives = startingLives;
        this.mapIndex = mapIndex;
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

        // --- Level 1: Tutorial (universal magic only) ---
        levels.add(new GameLevel(
            "Застава Лесного Короля",
            "Первая линия обороны. Изучи основы",
            250, 20, 1,
            new float[]{0, 180, 180, 420, 420, 680, 680, 960, 960, 1280},
            new float[]{360, 360, 180, 180, 520, 520, 240, 240, 400, 400},
            List.of(
                new BuildSpot(280, 280), new BuildSpot(280, 440),
                new BuildSpot(520, 120), new BuildSpot(520, 360),
                new BuildSpot(760, 160), new BuildSpot(760, 400),
                new BuildSpot(880, 280), new BuildSpot(1040, 480)
            ),
            List.of(
                new WaveDefinition(1, List.of(new SpawnEntry(EnemyType.GOBLIN, 8, 0.8f)), 3f),
                new WaveDefinition(2, List.of(
                    new SpawnEntry(EnemyType.GOBLIN, 10, 0.6f),
                    new SpawnEntry(EnemyType.WOLF, 4, 1.2f)), 4f),
                new WaveDefinition(3, List.of(
                    new SpawnEntry(EnemyType.ORC, 6, 1.0f),
                    new SpawnEntry(EnemyType.WOLF, 6, 0.7f)), 5f),
                new WaveDefinition(4, List.of(
                    new SpawnEntry(EnemyType.KNIGHT, 4, 1.5f),
                    new SpawnEntry(EnemyType.ORC, 8, 0.8f)), 5f),
                new WaveDefinition(5, List.of(
                    new SpawnEntry(EnemyType.DRAGON, 1, 0f),
                    new SpawnEntry(EnemyType.KNIGHT, 6, 1.0f)), 6f)
            )
        ));

        // --- Level 2: Unlock Ballista & Ice Tower ---
        levels.add(new GameLevel(
            "Мост через пропасть",
            "Узкий проход — идеален для баллисты",
            300, 20, 2,
            new float[]{0, 320, 320, 560, 560, 800, 800, 1080, 1080, 1280},
            new float[]{200, 200, 480, 480, 160, 160, 440, 440, 280, 280},
            List.of(
                new BuildSpot(220, 320), new BuildSpot(420, 100),
                new BuildSpot(480, 360), new BuildSpot(640, 520),
                new BuildSpot(720, 240), new BuildSpot(920, 160),
                new BuildSpot(960, 380), new BuildSpot(1120, 360)
            ),
            List.of(
                new WaveDefinition(1, List.of(new SpawnEntry(EnemyType.WOLF, 10, 0.5f)), 3f),
                new WaveDefinition(2, List.of(
                    new SpawnEntry(EnemyType.GOBLIN, 12, 0.4f),
                    new SpawnEntry(EnemyType.ORC, 5, 1.0f)), 4f),
                new WaveDefinition(3, List.of(new SpawnEntry(EnemyType.KNIGHT, 6, 1.2f)), 5f),
                new WaveDefinition(4, List.of(
                    new SpawnEntry(EnemyType.ORC, 10, 0.6f),
                    new SpawnEntry(EnemyType.FIRE_IMP, 6, 0.8f)), 5f),
                new WaveDefinition(5, List.of(
                    new SpawnEntry(EnemyType.DRAGON, 2, 2.0f),
                    new SpawnEntry(EnemyType.KNIGHT, 8, 0.8f)), 6f),
                new WaveDefinition(6, List.of(
                    new SpawnEntry(EnemyType.DRAGON, 3, 1.5f)), 8f)
            )
        ));

        // --- Level 3: Elemental magic unlocked (fire/ice/lightning replace universal) ---
        levels.add(new GameLevel(
            "Замёрзшее ущелье",
            "Ледяные элементали сопротивляются холоду!",
            320, 20, 3,
            new float[]{0, 200, 200, 500, 500, 750, 750, 1000, 1000, 1280},
            new float[]{500, 500, 200, 200, 450, 450, 150, 150, 350, 350},
            List.of(
                new BuildSpot(300, 340), new BuildSpot(100, 380),
                new BuildSpot(350, 120), new BuildSpot(600, 320),
                new BuildSpot(630, 520), new BuildSpot(850, 300),
                new BuildSpot(900, 100), new BuildSpot(1100, 250),
                new BuildSpot(1100, 440)
            ),
            List.of(
                new WaveDefinition(1, List.of(
                    new SpawnEntry(EnemyType.WOLF, 12, 0.5f),
                    new SpawnEntry(EnemyType.FROST_ELEMENTAL, 3, 1.5f)), 4f),
                new WaveDefinition(2, List.of(
                    new SpawnEntry(EnemyType.ORC, 8, 0.8f),
                    new SpawnEntry(EnemyType.FIRE_IMP, 6, 0.6f)), 4f),
                new WaveDefinition(3, List.of(
                    new SpawnEntry(EnemyType.KNIGHT, 6, 1.0f),
                    new SpawnEntry(EnemyType.FROST_ELEMENTAL, 5, 1.2f)), 5f),
                new WaveDefinition(4, List.of(
                    new SpawnEntry(EnemyType.GOLEM, 3, 2.0f),
                    new SpawnEntry(EnemyType.ORC, 10, 0.6f)), 5f),
                new WaveDefinition(5, List.of(
                    new SpawnEntry(EnemyType.DARK_MAGE, 5, 1.0f),
                    new SpawnEntry(EnemyType.KNIGHT, 8, 0.8f)), 6f),
                new WaveDefinition(6, List.of(
                    new SpawnEntry(EnemyType.DRAGON, 2, 2.0f),
                    new SpawnEntry(EnemyType.GOLEM, 4, 1.5f)), 7f),
                new WaveDefinition(7, List.of(
                    new SpawnEntry(EnemyType.WYVERN, 3, 1.5f),
                    new SpawnEntry(EnemyType.DARK_MAGE, 6, 0.8f)), 8f)
            )
        ));

        // --- Level 4: All towers, heavy resistances ---
        levels.add(new GameLevel(
            "Врата Тьмы",
            "Финальное сражение. Враги сильнее к стихиям!",
            350, 18, 4,
            new float[]{0, 160, 160, 400, 400, 640, 640, 900, 900, 1100, 1100, 1280},
            new float[]{400, 400, 600, 600, 200, 200, 500, 500, 300, 300, 500, 500},
            List.of(
                new BuildSpot(260, 480), new BuildSpot(260, 300),
                new BuildSpot(500, 400), new BuildSpot(520, 120),
                new BuildSpot(740, 340), new BuildSpot(740, 580),
                new BuildSpot(980, 200), new BuildSpot(980, 420),
                new BuildSpot(1180, 400), new BuildSpot(1050, 580)
            ),
            List.of(
                new WaveDefinition(1, List.of(
                    new SpawnEntry(EnemyType.ORC, 12, 0.6f),
                    new SpawnEntry(EnemyType.WOLF, 8, 0.4f)), 4f),
                new WaveDefinition(2, List.of(
                    new SpawnEntry(EnemyType.FROST_ELEMENTAL, 8, 0.8f),
                    new SpawnEntry(EnemyType.FIRE_IMP, 10, 0.5f)), 4f),
                new WaveDefinition(3, List.of(
                    new SpawnEntry(EnemyType.GOLEM, 5, 1.5f),
                    new SpawnEntry(EnemyType.KNIGHT, 8, 0.8f)), 5f),
                new WaveDefinition(4, List.of(
                    new SpawnEntry(EnemyType.DARK_MAGE, 8, 0.8f),
                    new SpawnEntry(EnemyType.WYVERN, 4, 1.5f)), 5f),
                new WaveDefinition(5, List.of(
                    new SpawnEntry(EnemyType.DRAGON, 3, 1.5f),
                    new SpawnEntry(EnemyType.GOLEM, 6, 1.0f)), 6f),
                new WaveDefinition(6, List.of(
                    new SpawnEntry(EnemyType.WYVERN, 6, 1.0f),
                    new SpawnEntry(EnemyType.FROST_ELEMENTAL, 6, 0.8f)), 7f),
                new WaveDefinition(7, List.of(
                    new SpawnEntry(EnemyType.KNIGHT, 10, 0.6f),
                    new SpawnEntry(EnemyType.DARK_MAGE, 8, 0.7f)), 6f),
                new WaveDefinition(8, List.of(
                    new SpawnEntry(EnemyType.DRAGON, 5, 1.0f),
                    new SpawnEntry(EnemyType.GOLEM, 4, 1.5f),
                    new SpawnEntry(EnemyType.WYVERN, 5, 1.2f)), 10f)
            )
        ));

        return levels;
    }
}
