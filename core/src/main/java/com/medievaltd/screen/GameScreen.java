package com.medievaltd.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.medievaltd.MedievalTDGame;
import com.medievaltd.entity.Tower;
import com.medievaltd.model.BuildSpot;
import com.medievaltd.model.GameLevel;
import com.medievaltd.model.TowerType;
import com.medievaltd.system.GameSession;
import com.medievaltd.ui.HudRenderer;
import com.medievaltd.util.GameColors;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_WIDTH = 1280;
    private static final float WORLD_HEIGHT = 720;

    private final MedievalTDGame game;
    private final GameSession session;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font;
    private final HudRenderer hud;

    private TowerType selectedTowerType = TowerType.ARCHER;
    private Tower selectedTower;
    private BuildSpot hoveredSpot;
    private String message;
    private float messageTimer;

    public GameScreen(MedievalTDGame game, int levelIndex) {
        this.game = game;
        GameLevel level = GameLevel.createLevels().get(levelIndex);
        this.session = new GameSession(level);
        this.font = game.createFont(18);
        this.hud = new HudRenderer(font);
    }

    @Override
    public void show() {
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        session.update(delta, game.getAssets());

        handleInput();

        if (messageTimer > 0) {
            messageTimer -= delta;
        }

        Gdx.gl.glClearColor(GameColors.BACKGROUND.r, GameColors.BACKGROUND.g, GameColors.BACKGROUND.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();

        renderWorld();
        renderHud();

        if (session.getResult() != GameSession.Result.PLAYING) {
            renderEndScreen();
        }
    }

    private void renderWorld() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawMap(batch);
        drawBuildSpots(batch);
        drawPath(batch);

        for (Tower tower : session.getTowers()) {
            if (tower == selectedTower) {
                tower.renderRange(batch, game.getAssets());
            }
            tower.render(batch, game.getAssets());
        }

        session.getEnemies().forEach(e -> e.render(batch, game.getAssets(), session.getPath()));
        session.getProjectiles().forEach(p -> p.render(batch));
        batch.end();
    }

    private void drawMap(SpriteBatch batch) {
        for (int x = 0; x < WORLD_WIDTH; x += 64) {
            for (int y = 0; y < WORLD_HEIGHT; y += 64) {
                batch.draw(game.getAssets().tileGrass, x, y, 64, 64);
            }
        }
    }

    private void drawPath(SpriteBatch batch) {
        Vector2[] path = session.getPath();
        for (int i = 0; i < path.length - 1; i++) {
            Vector2 a = path[i];
            Vector2 b = path[i + 1];
            float thickness = 48f;
            Vector2 mid = new Vector2(a).add(b).scl(0.5f);
            float length = a.dst(b);
            float angle = new Vector2(b).sub(a).angleDeg();
            batch.draw(game.getAssets().tilePath, mid.x - length / 2, mid.y - thickness / 2, length / 2, thickness / 2,
                length, thickness, 1, 1, angle);
        }
    }

    private void drawBuildSpots(SpriteBatch batch) {
        for (BuildSpot spot : session.getBuildSpots()) {
            if (!spot.occupied) {
                float alpha = spot == hoveredSpot ? 0.9f : 0.5f;
                batch.setColor(1, 1, 1, alpha);
                batch.draw(game.getAssets().tileBuildSpot, spot.x - 28, spot.y - 28, 56, 56);
                batch.setColor(1, 1, 1, 1);
            }
        }
    }

    private void renderHud() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        hud.renderPanelBackgrounds(shapes, WORLD_WIDTH, WORLD_HEIGHT);
        hud.renderButtons(shapes, game.getAssets(), selectedTowerType,
            session.getWaveManager().canStartWave());
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        hud.renderText(batch, session.getGold(), session.getLives(),
            session.getWaveManager().getCurrentWaveNumber(),
            session.getWaveManager().getTotalWaves(),
            session.getWaveManager().canStartWave(), selectedTowerType);

        if (messageTimer > 0 && message != null) {
            font.setColor(GameColors.UI_GOLD);
            font.getData().setScale(1.2f);
            font.draw(batch, message, WORLD_WIDTH / 2f - 100, WORLD_HEIGHT / 2f + 40);
            font.getData().setScale(1f);
        }
        batch.end();
    }

    private void renderEndScreen() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(0, 0, 0, 0.6f);
        batch.draw(game.getAssets().white, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.setColor(1, 1, 1, 1);

        boolean victory = session.getResult() == GameSession.Result.VICTORY;
        font.getData().setScale(2f);
        font.setColor(victory ? GameColors.UI_GOLD : GameColors.UI_HEALTH);
        font.draw(batch, victory ? "ПОБЕДА!" : "ПОРАЖЕНИЕ", WORLD_WIDTH / 2f - 120, WORLD_HEIGHT / 2f + 40);
        font.getData().setScale(1f);
        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "Нажмите ESC для возврата в меню", WORLD_WIDTH / 2f - 160, WORLD_HEIGHT / 2f - 20);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.returnToMenu();
        }
    }

    private void handleInput() {
        if (session.getResult() != GameSession.Result.PLAYING) return;

        Vector2 worldPos = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        hoveredSpot = null;
        for (BuildSpot spot : session.getBuildSpots()) {
            if (!spot.occupied && worldPos.dst(spot.x, spot.y) < 32) {
                hoveredSpot = spot;
                break;
            }
        }

        selectedTower = null;
        for (Tower tower : session.getTowers()) {
            if (worldPos.dst(tower.getPosition()) < 28) {
                selectedTower = tower;
                break;
            }
        }

        if (Gdx.input.justTouched()) {
            if (hud.getMenuButton().contains(worldPos.x, worldPos.y)) {
                game.returnToMenu();
                return;
            }
            if (hud.getWaveButton().contains(worldPos.x, worldPos.y) && session.getWaveManager().canStartWave()) {
                session.startWave();
                showMessage("Волна " + session.getWaveManager().getCurrentWaveNumber() + "!");
                return;
            }
            for (int i = 0; i < TowerType.values().length; i++) {
                if (hud.getTowerButton(i).contains(worldPos.x, worldPos.y)) {
                    selectedTowerType = TowerType.values()[i];
                    return;
                }
            }
            if (selectedTower != null) {
                if (hud.getUpgradeButton().contains(worldPos.x, worldPos.y)) {
                    if (session.upgradeTower(selectedTower)) {
                        showMessage("Башня улучшена!");
                    }
                    return;
                }
                if (hud.getSellButton().contains(worldPos.x, worldPos.y)) {
                    session.sellTower(selectedTower);
                    selectedTower = null;
                    showMessage("Башня продана");
                    return;
                }
            }
            if (hoveredSpot != null) {
                if (session.placeTower(hoveredSpot, selectedTowerType)) {
                    showMessage("Построено: " + selectedTowerType.displayName);
                }
            }
        }
    }

    private void showMessage(String msg) {
        message = msg;
        messageTimer = 2f;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }
}
