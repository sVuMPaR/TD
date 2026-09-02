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
import com.medievaltd.entity.Enemy;
import com.medievaltd.entity.Tower;
import com.medievaltd.model.*;
import com.medievaltd.system.CampaignSave;
import com.medievaltd.system.GameSession;
import com.medievaltd.system.WaveManager;
import com.medievaltd.util.FloatingText;
import com.medievaltd.util.GameColors;
import com.medievaltd.util.Settings;
import com.medievaltd.util.Smoke;

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
    private final BitmapFont smallFont;
    private final GlyphLayout layout = new GlyphLayout();

    private final List<TowerType> availableTowers = new ArrayList<>();
    private final Rectangle[] towerButtons;
    private final Rectangle towerPanel = new Rectangle();
    private final Rectangle towerInfoCard = new Rectangle();
    private BuildSpot buildTarget;

    private final Rectangle inspectPanel = new Rectangle(975, 405, 290, 300);
    private final Rectangle waveButton = new Rectangle(1050, 20, 200, 50);
    private final Rectangle upgradeButton = new Rectangle(985, 415, 130, 36);
    private final Rectangle sellButton = new Rectangle(1125, 415, 130, 36);
    private final Rectangle menuButton = new Rectangle(20, 14, 100, 36);
    private final Rectangle speedButton = new Rectangle(930, 20, 100, 50);
    private final Rectangle templeSpeedBtn = new Rectangle(985, 458, 130, 32);
    private final Rectangle templeDamageBtn = new Rectangle(1125, 458, 130, 32);

    private boolean paused;
    private final Rectangle pauseResume = new Rectangle(W / 2f - 150, H / 2f - 10, 300, 48);
    private final Rectangle pauseQuit = new Rectangle(W / 2f - 150, H / 2f - 78, 300, 48);
    private final Rectangle pauseMute = new Rectangle(W / 2f - 150, H / 2f - 140, 300, 44);
    private final Rectangle endMenuBtn = new Rectangle(W / 2f - 220, H / 2f - 150, 200, 48);
    private final Rectangle endResearchBtn = new Rectangle(W / 2f + 20, H / 2f - 150, 200, 48);
    private TowerType selectedTowerType;
    private Tower selectedTower;
    private BuildSpot hoveredSpot;
    private String message;
    private float messageTimer;
    private final Vector2 pointer = new Vector2();
    private int smokePhase;
    private float smokeAge;
    private boolean smokeShopOk;
    private boolean smokeHpOk;
    private boolean smokeSpeedOk;
    private boolean smokeCombatOk;

    public GameScreen(MedievalTDGame game, int levelIndex, Difficulty difficulty) {
        this.game = game;
        GameLevel level = levelIndex < 0 ? SurvivalMap.create() : GameLevel.createLevels().get(levelIndex);
        this.session = new GameSession(level, difficulty, game.getResearch(), game.getSfx(), levelIndex);
        this.mapLevel = level.mapIndex;
        this.font = game.createFont(16);
        this.smallFont = game.createFont(12);

        for (TowerType t : TowerType.values()) {
            if (t.isAvailableAtLevel(mapLevel)) {
                availableTowers.add(t);
            }
        }
        selectedTowerType = null;

        towerButtons = new Rectangle[availableTowers.size()];
        for (int i = 0; i < availableTowers.size(); i++) {
            towerButtons[i] = new Rectangle();
        }
    }

    public static GameScreen resume(MedievalTDGame game, CampaignSave.Run run) {
        GameScreen screen = new GameScreen(game, run.levelIndex, run.difficulty);
        screen.session.restore(run);
        return screen;
    }

    @Override
    public void show() {
        camera.position.set(W / 2f, H / 2f, 0);
        camera.update();
        if (mapLevel == 1 && session.getTowers().isEmpty()
            && session.getWaveManager().getCurrentWaveNumber() <= 0 && !Settings.tutorialDone()) {
            showMessage("Коснитесь зелёной клетки — выберите башню. Затем «След. волна»");
            messageTimer = 6f;
        }
        if (Smoke.on()) {
            beginSmoke();
        }
    }

    @Override
    public void render(float delta) {
        if (!paused) {
            float sim = Math.min(delta, 0.05f) * Settings.speed();
            session.update(sim, game.getAssets());
        }
        pointer.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointer);
        layoutTowerPanel(isTowerPanelOpen());
        if (!Smoke.on()) handleInput();
        if (!paused && messageTimer > 0) messageTimer -= delta;
        if (Smoke.on()) tickSmoke(delta);

        Gdx.gl.glClearColor(GameColors.BACKGROUND.r, GameColors.BACKGROUND.g, GameColors.BACKGROUND.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply(); camera.update();

        renderWorld();
        renderHud();

        if (paused && session.getResult() == GameSession.Result.PLAYING) {
            renderPause();
        }

        if (session.getResult() != GameSession.Result.PLAYING) {
            renderEndScreen();
        }
    }

    private boolean isTowerPanelOpen() {
        return buildTarget != null && !buildTarget.occupied;
    }

    private void layoutTowerPanel(boolean open) {
        if (!open || buildTarget == null) {
            towerPanel.set(0, 0, 0, 0);
            towerInfoCard.set(0, 0, 0, 0);
            for (Rectangle r : towerButtons) r.set(-100, -100, 0, 0);
            return;
        }
        float panelW = 252;
        float panelH = availableTowers.size() * 48f + 18f;
        float infoW = 300;
        float infoH = 128;
        float minY = 78;
        float maxY = H - 8;

        float px = buildTarget.x + 36;
        if (px + panelW + 8 + infoW > W - 8) {
            px = buildTarget.x - 36 - panelW;
        }
        if (px < 8) px = 8;
        if (px + panelW > W - 8) px = W - 8 - panelW;

        float py = buildTarget.y - panelH * 0.45f;
        if (py < minY) py = minY;
        if (py + panelH > maxY) py = maxY - panelH;

        towerPanel.set(px, py, panelW, panelH);

        float ix = px + panelW + 8;
        float iy = py + panelH - infoH;
        if (ix + infoW > W - 8) {
            ix = px - 8 - infoW;
            if (ix < 8) {
                ix = Math.max(8, px);
                iy = py - 8 - infoH;
                if (iy < minY) iy = py + panelH + 8;
            }
        }
        if (iy < minY) iy = minY;
        if (iy + infoH > maxY) iy = maxY - infoH;
        if (ix + infoW > W - 8) ix = W - 8 - infoW;
        towerInfoCard.set(ix, iy, infoW, infoH);

        for (int i = 0; i < availableTowers.size(); i++) {
            towerButtons[i].set(px + 8, py + panelH - 54 - i * 48, 236, 44);
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
                float alpha = spot == buildTarget ? 1f : (spot == hoveredSpot ? 0.9f : 0.55f);
                batch.setColor(1, 1, 1, alpha);
                batch.draw(game.getAssets().tileBuildSpot, spot.x - 28, spot.y - 28, 56, 56);
                batch.setColor(1, 1, 1, 1);
            }
        }
        batch.end();

        if (selectedTower != null) {
            drawRange(selectedTower);
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Tower tower : session.getTowers()) {
            tower.render(batch, game.getAssets());
        }

        TowerType ghostType = previewTowerType();
        if (ghostType != null && buildTarget != null && !buildTarget.occupied) {
            var tex = game.getAssets().getTowerTexture(ghostType);
            float size = tex.getRegionWidth();
            batch.setColor(1f, 1f, 1f, 0.55f);
            batch.draw(tex, buildTarget.x - size / 2f, buildTarget.y - size / 2f, size, size);
            batch.setColor(1, 1, 1, 1);
        }

        session.getEnemies().forEach(e -> e.render(batch, game.getAssets(), session.getPath(), smallFont));
        session.getProjectiles().forEach(p -> p.render(batch));

        for (FloatingText ft : session.getFloatingTexts()) {
            font.setColor(1f, 0.85f, 0.35f, ft.alpha());
            layout.setText(font, ft.text);
            font.draw(batch, ft.text, ft.pos.x - layout.width / 2f, ft.drawY());
        }
        font.setColor(1, 1, 1, 1);
        batch.end();
    }

    private void drawRange(Tower tower) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        float r = tower.getRange();
        float x = tower.getPosition().x;
        float y = tower.getPosition().y;
        boolean temple = tower.getType() == TowerType.TEMPLE;
        boolean barracks = tower.getType() == TowerType.BARRACKS;
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (temple) shapes.setColor(1f, 0.82f, 0.2f, 0.06f);
        else if (barracks) shapes.setColor(0.3f, 0.85f, 0.4f, 0.07f);
        else shapes.setColor(0.45f, 0.75f, 1f, 0.05f);
        shapes.circle(x, y, r, 48);
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        if (temple) shapes.setColor(1f, 0.82f, 0.2f, 0.55f);
        else if (barracks) shapes.setColor(0.35f, 0.9f, 0.45f, 0.55f);
        else shapes.setColor(0.55f, 0.85f, 1f, 0.5f);
        shapes.circle(x, y, r, 48);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderHud() {
        boolean panelOpen = isTowerPanelOpen();
        boolean canStart = session.getWaveManager().canStartWave();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(GameColors.UI_PANEL);
        shapes.rect(0, 0, W, 70);
        shapes.setColor(GameColors.UI_BORDER);
        shapes.rect(0, 68, W, 2);

        shapes.setColor(canStart ? 0.3f : 0.18f, canStart ? 0.5f : 0.14f, canStart ? 0.25f : 0.1f, 1f);
        shapes.rect(waveButton.x, waveButton.y, waveButton.width, waveButton.height);
        boolean boosted = Settings.speed() > 1;
        shapes.setColor(boosted ? 0.35f : 0.25f, boosted ? 0.28f : 0.2f, boosted ? 0.12f : 0.15f, 1f);
        shapes.rect(speedButton.x, speedButton.y, speedButton.width, speedButton.height);

        if (panelOpen) {
            shapes.setColor(0.16f, 0.12f, 0.08f, 0.94f);
            shapes.rect(towerPanel.x, towerPanel.y, towerPanel.width, towerPanel.height);
            for (int i = 0; i < availableTowers.size(); i++) {
                boolean sel = availableTowers.get(i) == selectedTowerType;
                shapes.setColor(sel ? 0.3f : 0.18f, sel ? 0.22f : 0.14f, sel ? 0.12f : 0.1f, 0.95f);
                shapes.rect(towerButtons[i].x, towerButtons[i].y, towerButtons[i].width, towerButtons[i].height);
                if (sel) {
                    shapes.setColor(GameColors.UI_BORDER);
                    shapes.rect(towerButtons[i].x, towerButtons[i].y, 3, towerButtons[i].height);
                }
            }
            shapes.setColor(0.16f, 0.12f, 0.08f, 0.94f);
            shapes.rect(towerInfoCard.x, towerInfoCard.y, towerInfoCard.width, towerInfoCard.height);
            shapes.setColor(GameColors.UI_BORDER);
            shapes.rect(towerInfoCard.x, towerInfoCard.y, towerInfoCard.width, 2);
        }

        if (selectedTower != null) {
            shapes.setColor(0.16f, 0.12f, 0.08f, 0.94f);
            shapes.rect(inspectPanel.x, inspectPanel.y, inspectPanel.width, inspectPanel.height);
            shapes.setColor(GameColors.UI_BORDER);
            shapes.rect(inspectPanel.x, inspectPanel.y, inspectPanel.width, 2);
            shapes.setColor(0.25f, 0.2f, 0.15f, 1f);
            shapes.rect(upgradeButton.x, upgradeButton.y, upgradeButton.width, upgradeButton.height);
            shapes.rect(sellButton.x, sellButton.y, sellButton.width, sellButton.height);
            if (selectedTower.getType() == TowerType.TEMPLE && selectedTower.getTempleChoice() == null) {
                shapes.setColor(0.3f, 0.25f, 0.1f, 1f);
                shapes.rect(templeSpeedBtn.x, templeSpeedBtn.y, templeSpeedBtn.width, templeSpeedBtn.height);
                shapes.rect(templeDamageBtn.x, templeDamageBtn.y, templeDamageBtn.width, templeDamageBtn.height);
            }
        }

        shapes.setColor(0.25f, 0.2f, 0.15f, 1f);
        shapes.rect(menuButton.x, menuButton.y, menuButton.width, menuButton.height);
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "Меню", menuButton.x + 22, menuButton.y + 24);
        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, "Золото: " + session.getGold(), 140, 48);
        font.setColor(GameColors.UI_HEALTH);
        font.draw(batch, "Жизни: " + session.getLives(), 290, 48);
        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "Волна: " + Math.max(session.getWaveManager().getCurrentWaveNumber(), 0)
            + "/" + session.getWaveManager().getTotalWaves(), 430, 48);
        font.draw(batch, session.getDifficulty().displayName, 590, 48);
        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, "Наука: " + game.getResearch().getGold(), 720, 48);

        font.setColor(canStart ? GameColors.UI_GOLD : GameColors.UI_TEXT);
        String wl = canStart ? "След. волна" : "Бой...";
        layout.setText(font, wl);
        font.draw(batch, wl, waveButton.x + (waveButton.width - layout.width) / 2, waveButton.y + 30);
        String sp = "x" + Settings.speed();
        font.setColor(Settings.speed() > 1 ? GameColors.UI_GOLD : GameColors.UI_TEXT);
        layout.setText(font, sp);
        font.draw(batch, sp, speedButton.x + (speedButton.width - layout.width) / 2, speedButton.y + 30);

        if (panelOpen) {
            for (int i = 0; i < availableTowers.size(); i++) {
                TowerType t = availableTowers.get(i);
                int cost = t.freeToPlace ? 0 : t.baseCost;
                var icon = game.getAssets().getTowerTexture(t);
                batch.draw(icon, towerButtons[i].x + 2, towerButtons[i].y + 2, 40, 40);
                font.setColor(session.getGold() >= cost ? GameColors.UI_TEXT : GameColors.UI_HEALTH);
                font.draw(batch, t.displayName + (cost > 0 ? " (" + cost + ")" : " (0)"),
                    towerButtons[i].x + 46, towerButtons[i].y + 28);
            }
            drawTowerInfoCard();
        }

        if (selectedTower != null) {
            drawInspectedTower();
            int upCost = selectedTower.nextUpgradeCost();
            font.setColor(upCost < 0 ? GameColors.UI_TEXT
                : (session.getGold() >= upCost ? GameColors.UI_GOLD : GameColors.UI_HEALTH));
            String upLabel = upCost < 0 ? "Макс" : "Улучш. " + upCost;
            font.getData().setScale(0.8f);
            font.draw(batch, upLabel, upgradeButton.x + 6, upgradeButton.y + 24);
            font.setColor(GameColors.UI_TEXT);
            font.draw(batch, "Продать " + selectedTower.getType().sellValue(selectedTower.getLevel()),
                sellButton.x + 6, sellButton.y + 24);
            font.getData().setScale(1f);
            if (selectedTower.getType() == TowerType.TEMPLE && selectedTower.getTempleChoice() == null) {
                int activate = selectedTower.nextUpgradeCost();
                font.setColor(session.getGold() >= activate ? GameColors.UI_GOLD : GameColors.UI_HEALTH);
                font.draw(batch, "+Скорость " + activate, templeSpeedBtn.x + 6, templeSpeedBtn.y + 22);
                font.draw(batch, "+Урон " + activate, templeDamageBtn.x + 10, templeDamageBtn.y + 22);
            }
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
        boolean victory = session.getResult() == GameSession.Result.VICTORY;
        boolean campaignDone = victory && session.getLevel().mapIndex == 4;
        if (victory) {
            endMenuBtn.set(W / 2f - 220, H / 2f - 150, 200, 48);
            endResearchBtn.set(W / 2f + 20, H / 2f - 150, 200, 48);
        } else {
            endMenuBtn.set(W / 2f - 100, H / 2f - 130, 200, 48);
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(0, 0, 0, 0.6f);
        batch.draw(game.getAssets().white, 0, 0, W, H);
        batch.setColor(1, 1, 1, 1);
        font.getData().setScale(2.5f);
        font.setColor(victory ? GameColors.UI_GOLD : GameColors.UI_HEALTH);
        layout.setText(font, victory ? "ПОБЕДА!" : "ПОРАЖЕНИЕ");
        font.draw(batch, victory ? "ПОБЕДА!" : "ПОРАЖЕНИЕ", W / 2f - layout.width / 2, H / 2f + 40);
        font.getData().setScale(1f);
        if (victory) {
            String reward = "Награда: +" + session.getLevelReward() + " монет науки";
            layout.setText(font, reward);
            font.setColor(GameColors.UI_GOLD);
            font.draw(batch, reward, W / 2f - layout.width / 2, H / 2f - 10);
            if (campaignDone) {
                layout.setText(font, "Кампания пройдена!");
                font.draw(batch, "Кампания пройдена!", W / 2f - layout.width / 2, H / 2f - 36);
            }
        } else if (session.getDefeatReward() > 0) {
            String reward = "Утешение: +" + session.getDefeatReward() + " монет науки";
            layout.setText(font, reward);
            font.setColor(GameColors.UI_GOLD);
            font.draw(batch, reward, W / 2f - layout.width / 2, H / 2f - 10);
        }
        batch.end();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.22f, 0.16f, 0.1f, 1f);
        shapes.rect(endMenuBtn.x, endMenuBtn.y, endMenuBtn.width, endMenuBtn.height);
        if (victory) {
            shapes.setColor(0.3f, 0.45f, 0.2f, 1f);
            shapes.rect(endResearchBtn.x, endResearchBtn.y, endResearchBtn.width, endResearchBtn.height);
        }
        shapes.setColor(GameColors.UI_BORDER);
        shapes.rect(endMenuBtn.x, endMenuBtn.y, endMenuBtn.width, 3);
        if (victory) {
            shapes.rect(endResearchBtn.x, endResearchBtn.y, endResearchBtn.width, 3);
        }
        shapes.end();

        batch.begin();
        font.setColor(GameColors.UI_GOLD);
        if (victory) {
            layout.setText(font, "В меню");
            font.draw(batch, "В меню", endMenuBtn.x + (endMenuBtn.width - layout.width) / 2f, endMenuBtn.y + 32);
            layout.setText(font, "Исследования");
            font.draw(batch, "Исследования",
                endResearchBtn.x + (endResearchBtn.width - layout.width) / 2f, endResearchBtn.y + 32);
        } else {
            layout.setText(font, "В меню");
            font.draw(batch, "В меню", endMenuBtn.x + (endMenuBtn.width - layout.width) / 2f, endMenuBtn.y + 32);
        }
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            game.returnToMenu();
            return;
        }
        if (victory && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.openResearch();
            return;
        }
        if (!Gdx.input.justTouched()) return;
        if (victory) {
            if (endResearchBtn.contains(pointer.x, pointer.y)) game.openResearch();
            else if (endMenuBtn.contains(pointer.x, pointer.y)) game.returnToMenu();
        } else {
            game.returnToMenu();
        }
    }

    private void handleInput() {
        if (session.getResult() != GameSession.Result.PLAYING) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
            || Gdx.input.isKeyJustPressed(Input.Keys.BACK)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            paused = !paused;
            game.getSfx().pause();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            Settings.cycleSpeed();
            game.getSfx().click();
            return;
        }
        if (paused) {
            if (!Gdx.input.justTouched()) return;
            if (pauseResume.contains(pointer.x, pointer.y)) {
                paused = false;
                game.getSfx().click();
            } else if (pauseMute.contains(pointer.x, pointer.y)) {
                Settings.toggleSound();
                game.getSfx().click();
            } else if (pauseQuit.contains(pointer.x, pointer.y)) {
                game.getSfx().click();
                session.checkpoint();
                game.returnToMenu();
            }
            return;
        }

        Vector2 wp = pointer;

        hoveredSpot = null;
        for (BuildSpot spot : session.getBuildSpots()) {
            if (!spot.occupied && wp.dst(spot.x, spot.y) < 32) {
                hoveredSpot = spot; break;
            }
        }

        if (!Gdx.input.justTouched()) return;

        if (menuButton.contains(wp.x, wp.y)) {
            paused = true;
            game.getSfx().pause();
            return;
        }
        if (speedButton.contains(wp.x, wp.y)) {
            Settings.cycleSpeed();
            game.getSfx().click();
            return;
        }
        if (waveButton.contains(wp.x, wp.y) && session.getWaveManager().canStartWave()) {
            session.startWave();
            Settings.markTutorialDone();
            showMessage("Волна " + session.getWaveManager().getCurrentWaveNumber() + "!");
            return;
        }
        if (isTowerPanelOpen()) {
            if (towerInfoCard.contains(wp.x, wp.y)) return;
            for (int i = 0; i < availableTowers.size(); i++) {
                if (towerButtons[i].contains(wp.x, wp.y)) {
                    selectedTowerType = availableTowers.get(i);
                    game.getSfx().click();
                    tryPlaceOnBuildTarget();
                    return;
                }
            }
            if (towerPanel.contains(wp.x, wp.y)) return;
        }
        if (selectedTower != null) {
            if (upgradeButton.contains(wp.x, wp.y)) {
                if (selectedTower.getType() == TowerType.TEMPLE && selectedTower.getTempleChoice() == null) {
                    showMessage("Сначала выберите бафф храма");
                } else if (session.upgradeTower(selectedTower)) {
                    showMessage("Башня улучшена!");
                } else if (selectedTower.getLevel() >= 3) {
                    showMessage("Максимальный уровень");
                } else {
                    showMessage("Недостаточно золота");
                }
                return;
            }
            if (sellButton.contains(wp.x, wp.y)) {
                session.sellTower(selectedTower);
                showMessage("Башня продана");
                selectedTower = null;
                return;
            }
            if (selectedTower.getType() == TowerType.TEMPLE && selectedTower.getTempleChoice() == null) {
                if (templeSpeedBtn.contains(wp.x, wp.y)) {
                    if (session.chooseTempleUpgrade(selectedTower, TempleUpgradeChoice.ATTACK_SPEED)) {
                        showMessage("Храм: +Скорость атаки");
                    } else {
                        showMessage("Нужно " + selectedTower.nextUpgradeCost() + " золота");
                    }
                    return;
                }
                if (templeDamageBtn.contains(wp.x, wp.y)) {
                    if (session.chooseTempleUpgrade(selectedTower, TempleUpgradeChoice.ATTACK_DAMAGE)) {
                        showMessage("Храм: +Сила атаки");
                    } else {
                        showMessage("Нужно " + selectedTower.nextUpgradeCost() + " золота");
                    }
                    return;
                }
            }
        }

        for (Tower tower : session.getTowers()) {
            if (wp.dst(tower.getPosition()) < 28) {
                selectedTower = tower;
                closeBuildMenu();
                return;
            }
        }

        if (inspectPanel.contains(wp.x, wp.y)) return;

        if (hoveredSpot != null) {
            if (hoveredSpot == buildTarget) {
                closeBuildMenu();
            } else {
                openBuildMenu(hoveredSpot);
            }
            return;
        }

        if (!towerPanel.contains(wp.x, wp.y) && !towerInfoCard.contains(wp.x, wp.y)
            && !inspectPanel.contains(wp.x, wp.y)) {
            selectedTower = null;
            closeBuildMenu();
        }
    }

    private void openBuildMenu(BuildSpot spot) {
        buildTarget = spot;
        selectedTower = null;
        selectedTowerType = null;
        layoutTowerPanel(true);
    }

    private void closeBuildMenu() {
        buildTarget = null;
        selectedTowerType = null;
        layoutTowerPanel(false);
    }

    private void tryPlaceOnBuildTarget() {
        if (buildTarget == null || selectedTowerType == null) return;
        if (session.placeTower(buildTarget, selectedTowerType)) {
            showMessage(selectedTowerType.displayName);
            BuildSpot placed = buildTarget;
            for (Tower tower : session.getTowers()) {
                if (tower.getPosition().x == placed.x && tower.getPosition().y == placed.y) {
                    selectedTower = tower;
                    break;
                }
            }
            closeBuildMenu();
            return;
        }
        int cost = selectedTowerType.freeToPlace ? 0 : selectedTowerType.baseCost;
        showMessage(session.getGold() < cost ? "Недостаточно золота" : "Нельзя построить");
    }

    private void renderPause() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(0, 0, 0, 0.62f);
        batch.draw(game.getAssets().white, 0, 0, W, H);
        batch.setColor(1, 1, 1, 1);
        batch.end();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.22f, 0.16f, 0.1f, 0.96f);
        shapes.rect(pauseResume.x, pauseResume.y, pauseResume.width, pauseResume.height);
        shapes.rect(pauseQuit.x, pauseQuit.y, pauseQuit.width, pauseQuit.height);
        shapes.rect(pauseMute.x, pauseMute.y, pauseMute.width, pauseMute.height);
        shapes.setColor(GameColors.UI_BORDER);
        shapes.rect(pauseResume.x, pauseResume.y, pauseResume.width, 3);
        shapes.rect(pauseQuit.x, pauseQuit.y, pauseQuit.width, 3);
        shapes.rect(pauseMute.x, pauseMute.y, pauseMute.width, 3);
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.getData().setScale(2f);
        font.setColor(GameColors.UI_GOLD);
        layout.setText(font, "ПАУЗА");
        font.draw(batch, "ПАУЗА", W / 2f - layout.width / 2f, H / 2f + 80);
        font.getData().setScale(1f);
        font.setColor(GameColors.UI_TEXT);
        layout.setText(font, "Продолжить");
        font.draw(batch, "Продолжить", pauseResume.x + (pauseResume.width - layout.width) / 2f, pauseResume.y + 32);
        layout.setText(font, "В меню");
        font.draw(batch, "В меню", pauseQuit.x + (pauseQuit.width - layout.width) / 2f, pauseQuit.y + 34);
        layout.setText(font, Settings.sound() ? "Звук: вкл" : "Звук: выкл");
        font.draw(batch, Settings.sound() ? "Звук: вкл" : "Звук: выкл",
            pauseMute.x + (pauseMute.width - layout.width) / 2f, pauseMute.y + 30);
        font.getData().setScale(0.75f);
        font.setColor(0.75f, 0.85f, 0.6f, 1f);
        layout.setText(font, "прогресс сохранится");
        font.draw(batch, "прогресс сохранится", pauseQuit.x + (pauseQuit.width - layout.width) / 2f, pauseQuit.y + 16);
        font.getData().setScale(1f);
        batch.end();
    }

    private void drawInspectedTower() {
        Tower t = selectedTower;
        TowerType type = t.getType();
        float x = inspectPanel.x + 12;
        float y = inspectPanel.y + inspectPanel.height - 24;
        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, type.displayName + "  ур." + t.getLevel() + "/3", x, y);
        var icon = game.getAssets().getTowerTexture(type);
        batch.draw(icon, inspectPanel.x + inspectPanel.width - 56, inspectPanel.y + inspectPanel.height - 56, 44, 44);
        font.setColor(GameColors.UI_TEXT);
        font.getData().setScale(0.85f);
        y -= 24;
        if (type == TowerType.BARRACKS) {
            font.draw(batch, "Бойцы: " + t.aliveSoldierCount() + "/" + t.getSoldiers().size(), x, y);
            y -= 20;
            font.draw(batch, "HP бойца: " + t.soldierMaxHp() + "   Урон: " + t.soldierDamage(), x, y);
            y -= 20;
            font.draw(batch, "Патруль: " + (int) t.getRange(), x, y);
        } else if (type == TowerType.TEMPLE) {
            String buff = t.getTempleChoice() == null
                ? "не выбран, " + t.nextUpgradeCost() + " золота"
                : t.getTempleChoice().displayName;
            font.draw(batch, "Бафф: " + buff, x, y);
            y -= 20;
            if (t.getTempleChoice() == TempleUpgradeChoice.ATTACK_DAMAGE) {
                font.draw(batch, "Урон башен: +" + (int) (t.getTempleDamageBonus() * 100) + "%", x, y);
                y -= 20;
            } else if (t.getTempleChoice() == TempleUpgradeChoice.ATTACK_SPEED) {
                font.draw(batch, "Скорость: +" + (int) (t.getTempleSpeedBonus() * 100) + "%", x, y);
                y -= 20;
            }
            font.draw(batch, "Радиус: " + (int) t.getRange(), x, y);
        } else {
            font.draw(batch, "Урон: " + t.currentDamage() + "   Радиус: " + (int) t.getRange(), x, y);
            y -= 20;
            font.draw(batch, String.format(java.util.Locale.US, "Выстрел: %.2f с", t.currentFireRate()), x, y);
            if (type == TowerType.ICE_TOWER || type == TowerType.ICE_MAGIC) {
                y -= 20;
                int pct = Math.round((1f - type.iceSlow()) * 100);
                if (type == TowerType.ICE_TOWER) {
                    font.draw(batch, "Пачка: -" + pct + "%   область " + (int) type.iceSplash(), x, y);
                } else {
                    font.draw(batch, "Одна цель: -" + pct + "%   " + type.iceSlowTime() + " с", x, y);
                }
            }
        }
        y -= 22;
        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, type.description, x, y);
        font.getData().setScale(1f);
    }

    private TowerType previewTowerType() {
        if (isTowerPanelOpen()) {
            for (int i = 0; i < availableTowers.size(); i++) {
                if (towerButtons[i].contains(pointer.x, pointer.y)) {
                    return availableTowers.get(i);
                }
            }
        }
        return selectedTowerType;
    }

    private void drawTowerInfoCard() {
        TowerType t = previewTowerType();
        float x = towerInfoCard.x + 10;
        float top = towerInfoCard.y + towerInfoCard.height - 22;
        if (t == null) {
            font.setColor(GameColors.UI_TEXT);
            font.draw(batch, "Выберите башню", x, top - 20);
            font.getData().setScale(0.85f);
            font.draw(batch, "Коснитесь типа — сразу поставится", x, top - 44);
            font.getData().setScale(1f);
            return;
        }
        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, t.displayName, x, top);
        var icon = game.getAssets().getTowerTexture(t);
        batch.draw(icon, towerInfoCard.x + towerInfoCard.width - 58, towerInfoCard.y + towerInfoCard.height - 56, 48, 48);
        int cost = t.freeToPlace ? 0 : t.baseCost;
        font.setColor(session.getGold() >= cost ? GameColors.UI_TEXT : GameColors.UI_HEALTH);
        font.draw(batch, cost == 0 ? "Бесплатно" : cost + " золота", x, top - 22);
        font.setColor(GameColors.UI_TEXT);
        font.getData().setScale(0.85f);
        font.draw(batch, t.statLine(), x, top - 44);
        String fire = t.fireLine();
        float descY = top - 64;
        if (fire != null) {
            font.draw(batch, fire, x, top - 64);
            descY = top - 84;
        }
        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, t.description, x, descY);
        font.getData().setScale(1f);
    }

    private void beginSmoke() {
        Settings.setSpeed(1);
        Settings.cycleSpeed();
        Settings.cycleSpeed();
        smokeSpeedOk = Settings.speed() == 4;

        List<BuildSpot> spots = session.getBuildSpots();
        if (spots.isEmpty()) {
            Gdx.app.error("Smoke", "no build spots");
            return;
        }
        openBuildMenu(spots.get(0));
        boolean panel = isTowerPanelOpen() && towerButtons.length > 0 && towerButtons[0].width > 0;
        selectedTowerType = TowerType.ARCHER;
        tryPlaceOnBuildTarget();
        smokeShopOk = panel && !session.getTowers().isEmpty();

        TowerType[] more = {
            TowerType.ARCHER, TowerType.ARCHER, TowerType.BARRACKS, TowerType.TEMPLE, TowerType.MAGIC
        };
        int i = 0;
        for (BuildSpot spot : spots) {
            if (spot.occupied) continue;
            if (i >= more.length) break;
            session.placeTower(spot, more[i++]);
        }
        session.startWave();
        Gdx.app.log("Smoke", "shop=" + smokeShopOk + " speed=" + Settings.speed()
            + " towers=" + session.getTowers().size() + " gold=" + session.getGold());
    }

    private void tickSmoke(float delta) {
        if (smokePhase >= 9) return;
        smokeAge += delta;
        if (!smokeHpOk && !session.getEnemies().isEmpty()) {
            Enemy e = session.getEnemies().get(0);
            int expected = session.getDifficulty().adjustHealth(e.getType().maxHealth);
            expected = (int) (expected * e.getRole().healthMultiplier);
            smokeHpOk = e.getMaxHealth() == expected;
            Gdx.app.log("Smoke", e.getType() + " " + e.getRole()
                + " hp=" + e.getMaxHealth() + " expected=" + expected);
        }
        boolean ended = session.getResult() != GameSession.Result.PLAYING
            || session.getWaveManager().getState() == WaveManager.State.COMPLETE
            || session.getWaveManager().getState() == WaveManager.State.ALL_COMPLETE;
        if (smokeAge > 16f || (smokeHpOk && ended && smokeAge > 3f)) {
            smokeCombatOk = session.getLives() > 0 && session.getResult() != GameSession.Result.DEFEAT;
            boolean ok = smokeShopOk && smokeHpOk && smokeSpeedOk && smokeCombatOk;
            smokePhase = 9;
            Gdx.app.log("Smoke", (ok ? "PASS" : "FAIL")
                + " shop=" + smokeShopOk + " hp=" + smokeHpOk + " speed=" + smokeSpeedOk
                + " combat=" + smokeCombatOk + " lives=" + session.getLives()
                + " result=" + session.getResult()
                + " wave=" + session.getWaveManager().getCurrentWaveNumber()
                + " enemies=" + session.getEnemies().size());
            Gdx.app.exit();
        }
    }

    private void showMessage(String msg) { message = msg; messageTimer = 2f; }

    @Override
    public void pause() {
        session.checkpoint();
    }

    @Override
    public void hide() {
        session.checkpoint();
    }

    @Override
    public void resize(int w, int h) { viewport.update(w, h, true); }

    @Override
    public void dispose() { batch.dispose(); shapes.dispose(); font.dispose(); smallFont.dispose(); }
}
