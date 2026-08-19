package com.medievaltd.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.medievaltd.MedievalTDGame;
import com.medievaltd.entity.Tower;
import com.medievaltd.model.*;
import com.medievaltd.system.GameSession;
import com.medievaltd.util.GameColors;

import java.util.ArrayList;
import java.util.List;

public class GameScreen extends ScreenAdapter {
    private static final float W = 1280, H = 720;

    private final MedievalTDGame game;
    private final GameSession session;
    private final int mapLevel;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(W, H, camera);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private final List<TowerType> availableTowers = new ArrayList<>();
    private final Rectangle[] towerButtons;
    private final Rectangle waveButton = new Rectangle(1050, 20, 200, 50);
    private final Rectangle upgradeButton = new Rectangle(1050, 620, 95, 40);
    private final Rectangle sellButton = new Rectangle(1155, 620, 95, 40);
    private final Rectangle menuButton = new Rectangle(20, 20, 120, 40);
    private final Rectangle templeSpeedBtn = new Rectangle(1050, 560, 115, 35);
    private final Rectangle templeDamageBtn = new Rectangle(1175, 560, 100, 35);

    private TowerType selectedTowerType;
    private Tower selectedTower;
    private BuildSpot hoveredSpot;
    private String message;
    private float messageTimer;

    public GameScreen(MedievalTDGame game, int levelIndex, Difficulty difficulty) {
        this.game = game;
        GameLevel level = GameLevel.createLevels().get(levelIndex);
        this.session = new GameSession(level, difficulty);
        this.mapLevel = level.mapIndex;
        this.font = game.createFont(16);

        for (TowerType t : TowerType.values()) {
            if (t.isAvailableAtLevel(mapLevel)) {
                availableTowers.add(t);
            }
        }
        selectedTowerType = availableTowers.isEmpty() ? TowerType.ARCHER : availableTowers.get(0);

        towerButtons = new Rectangle[availableTowers.size()];
        for (int i = 0; i < availableTowers.size(); i++) {
            towerButtons[i] = new Rectangle(10, H - 55 - i * 50, 200, 44);
        }
    }

    @Override
    public void show() {
        camera.position.set(W / 2f, H / 2f, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        session.update(delta, game.getAssets());
        handleInput();
        if (messageTimer > 0) messageTimer -= delta;

        Gdx.gl.glClearColor(GameColors.BACKGROUND.r, GameColors.BACKGROUND.g, GameColors.BACKGROUND.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply(); camera.update();

        renderWorld();
        renderHud();

        if (session.getResult() != GameSession.Result.PLAYING) {
            renderEndScreen();
        }
    }

    private void renderWorld() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (int x = 0; x < W; x += 64)
            for (int y = 0; y < H; y += 64)
                batch.draw(game.getAssets().tileGrass, x, y, 64, 64);

        Vector2[] path = session.getPath();
        for (int i = 0; i < path.length - 1; i++) {
            Vector2 a = path[i]; Vector2 b = path[i + 1];
            float thickness = 48f;
            Vector2 mid = new Vector2(a).add(b).scl(0.5f);
            float length = a.dst(b);
            float angle = new Vector2(b).sub(a).angleDeg();
            batch.draw(game.getAssets().tilePath, mid.x - length / 2, mid.y - thickness / 2,
                length / 2, thickness / 2, length, thickness, 1, 1, angle);
        }

        for (BuildSpot spot : session.getBuildSpots()) {
            if (!spot.occupied) {
                float alpha = spot == hoveredSpot ? 0.9f : 0.5f;
                batch.setColor(1, 1, 1, alpha);
                batch.draw(game.getAssets().tileBuildSpot, spot.x - 28, spot.y - 28, 56, 56);
                batch.setColor(1, 1, 1, 1);
            }
        }

        for (Tower tower : session.getTowers()) {
            if (tower == selectedTower) tower.renderRange(batch, game.getAssets());
            tower.render(batch, game.getAssets());
        }

        session.getEnemies().forEach(e -> e.render(batch, game.getAssets(), session.getPath()));
        session.getProjectiles().forEach(p -> p.render(batch));
        batch.end();
    }

    private void renderHud() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Bottom bar
        shapes.setColor(GameColors.UI_PANEL);
        shapes.rect(0, 0, W, 70);
        shapes.setColor(GameColors.UI_BORDER);
        shapes.rect(0, 68, W, 2);

        // Wave button
        boolean canStart = session.getWaveManager().canStartWave();
        shapes.setColor(canStart ? 0.3f : 0.18f, canStart ? 0.5f : 0.14f, canStart ? 0.25f : 0.1f, 1f);
        shapes.rect(waveButton.x, waveButton.y, waveButton.width, waveButton.height);

        // Tower buttons
        for (int i = 0; i < availableTowers.size(); i++) {
            boolean sel = availableTowers.get(i) == selectedTowerType;
            shapes.setColor(sel ? 0.3f : 0.18f, sel ? 0.22f : 0.14f, sel ? 0.12f : 0.1f, 0.92f);
            shapes.rect(towerButtons[i].x, towerButtons[i].y, towerButtons[i].width, towerButtons[i].height);
            if (sel) {
                shapes.setColor(GameColors.UI_BORDER);
                shapes.rect(towerButtons[i].x, towerButtons[i].y, 3, towerButtons[i].height);
            }
        }

        // Action buttons
        shapes.setColor(0.25f, 0.2f, 0.15f, 1f);
        shapes.rect(upgradeButton.x, upgradeButton.y, upgradeButton.width, upgradeButton.height);
        shapes.rect(sellButton.x, sellButton.y, sellButton.width, sellButton.height);
        shapes.rect(menuButton.x, menuButton.y, menuButton.width, menuButton.height);

        if (selectedTower != null && selectedTower.getType() == TowerType.TEMPLE && selectedTower.getTempleChoice() == null) {
            shapes.setColor(0.3f, 0.25f, 0.1f, 1f);
            shapes.rect(templeSpeedBtn.x, templeSpeedBtn.y, templeSpeedBtn.width, templeSpeedBtn.height);
            shapes.rect(templeDamageBtn.x, templeDamageBtn.y, templeDamageBtn.width, templeDamageBtn.height);
        }
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, "Золото: " + session.getGold(), 20, 50);
        font.setColor(GameColors.UI_HEALTH);
        font.draw(batch, "Жизни: " + session.getLives(), 180, 50);
        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "Волна: " + Math.max(session.getWaveManager().getCurrentWaveNumber(), 0)
            + "/" + session.getWaveManager().getTotalWaves(), 340, 50);
        font.draw(batch, session.getDifficulty().displayName, 520, 50);

        font.setColor(canStart ? GameColors.UI_GOLD : GameColors.UI_TEXT);
        String wl = canStart ? "След. волна" : "Бой...";
        layout.setText(font, wl);
        font.draw(batch, wl, waveButton.x + (waveButton.width - layout.width) / 2, waveButton.y + 30);

        for (int i = 0; i < availableTowers.size(); i++) {
            TowerType t = availableTowers.get(i);
            int cost = t.freeToPlace ? 0 : t.baseCost;
            font.setColor(session.getGold() >= cost ? GameColors.UI_TEXT : GameColors.UI_HEALTH);
            font.draw(batch, t.displayName + (cost > 0 ? " (" + cost + ")" : " (0)"),
                towerButtons[i].x + 8, towerButtons[i].y + 28);
        }

        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "Меню", menuButton.x + 28, menuButton.y + 26);
        font.draw(batch, "Улучш.", upgradeButton.x + 8, upgradeButton.y + 26);
        font.draw(batch, "Продать", sellButton.x + 6, sellButton.y + 26);

        if (selectedTower != null && selectedTower.getType() == TowerType.TEMPLE && selectedTower.getTempleChoice() == null) {
            font.setColor(GameColors.UI_GOLD);
            font.draw(batch, "+Скорость", templeSpeedBtn.x + 6, templeSpeedBtn.y + 24);
            font.draw(batch, "+Урон", templeDamageBtn.x + 10, templeDamageBtn.y + 24);
        }

        if (selectedTower != null) {
            font.setColor(GameColors.UI_TEXT);
            TowerType st = selectedTower.getType();
            String info = st.displayName + " ур." + selectedTower.getLevel();
            if (st == TowerType.TEMPLE && selectedTower.getTempleChoice() != null) {
                info += " [" + selectedTower.getTempleChoice().displayName + "]";
            }
            font.draw(batch, info, 1050, 670);
        }

        if (messageTimer > 0 && message != null) {
            font.setColor(GameColors.UI_GOLD);
            font.getData().setScale(1.3f);
            layout.setText(font, message);
            font.draw(batch, message, W / 2f - layout.width / 2, H / 2f + 40);
            font.getData().setScale(1f);
        }
        batch.end();
    }

    private void renderEndScreen() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(0, 0, 0, 0.6f);
        batch.draw(game.getAssets().white, 0, 0, W, H);
        batch.setColor(1, 1, 1, 1);
        boolean victory = session.getResult() == GameSession.Result.VICTORY;
        font.getData().setScale(2.5f);
        font.setColor(victory ? GameColors.UI_GOLD : GameColors.UI_HEALTH);
        layout.setText(font, victory ? "ПОБЕДА!" : "ПОРАЖЕНИЕ");
        font.draw(batch, victory ? "ПОБЕДА!" : "ПОРАЖЕНИЕ", W / 2f - layout.width / 2, H / 2f + 40);
        font.getData().setScale(1f);
        font.setColor(GameColors.UI_TEXT);
        layout.setText(font, "Нажмите ESC или коснитесь для возврата в меню");
        font.draw(batch, "Нажмите ESC или коснитесь для возврата в меню",
            W / 2f - layout.width / 2, H / 2f - 20);
        batch.end();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.justTouched()) {
            game.returnToMenu();
        }
    }

    private void handleInput() {
        if (session.getResult() != GameSession.Result.PLAYING) return;
        Vector2 wp = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        hoveredSpot = null;
        for (BuildSpot spot : session.getBuildSpots()) {
            if (!spot.occupied && wp.dst(spot.x, spot.y) < 32) {
                hoveredSpot = spot; break;
            }
        }

        Tower prevSelected = selectedTower;
        selectedTower = null;
        for (Tower tower : session.getTowers()) {
            if (wp.dst(tower.getPosition()) < 28) { selectedTower = tower; break; }
        }

        if (!Gdx.input.justTouched()) return;

        if (menuButton.contains(wp.x, wp.y)) { game.returnToMenu(); return; }
        if (waveButton.contains(wp.x, wp.y) && session.getWaveManager().canStartWave()) {
            session.startWave();
            showMessage("Волна " + session.getWaveManager().getCurrentWaveNumber() + "!");
            return;
        }
        for (int i = 0; i < availableTowers.size(); i++) {
            if (towerButtons[i].contains(wp.x, wp.y)) {
                selectedTowerType = availableTowers.get(i);
                return;
            }
        }
        if (prevSelected != null) {
            if (upgradeButton.contains(wp.x, wp.y)) {
                if (session.upgradeTower(prevSelected)) showMessage("Башня улучшена!");
                selectedTower = prevSelected;
                return;
            }
            if (sellButton.contains(wp.x, wp.y)) {
                session.sellTower(prevSelected);
                showMessage("Башня продана");
                return;
            }
            if (prevSelected.getType() == TowerType.TEMPLE && prevSelected.getTempleChoice() == null) {
                if (templeSpeedBtn.contains(wp.x, wp.y)) {
                    prevSelected.setTempleChoice(TempleUpgradeChoice.ATTACK_SPEED);
                    showMessage("Храм: +Скорость атаки");
                    selectedTower = prevSelected;
                    return;
                }
                if (templeDamageBtn.contains(wp.x, wp.y)) {
                    prevSelected.setTempleChoice(TempleUpgradeChoice.ATTACK_DAMAGE);
                    showMessage("Храм: +Сила атаки");
                    selectedTower = prevSelected;
                    return;
                }
            }
        }
        if (hoveredSpot != null) {
            if (session.placeTower(hoveredSpot, selectedTowerType)) {
                showMessage(selectedTowerType.displayName);
            }
        }
    }

    private void showMessage(String msg) { message = msg; messageTimer = 2f; }

    @Override
    public void resize(int w, int h) { viewport.update(w, h); }

    @Override
    public void dispose() { batch.dispose(); shapes.dispose(); font.dispose(); }
}
