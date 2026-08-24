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
import com.medievaltd.model.Difficulty;
import com.medievaltd.model.GameLevel;
import com.medievaltd.system.CampaignProgress;
import com.medievaltd.system.CampaignSave;
import com.medievaltd.util.GameColors;
import com.medievaltd.util.Settings;

import java.util.List;

public class MenuScreen extends ScreenAdapter {
    private static final float W = 1280, H = 720;
    private final MedievalTDGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(W, H, camera);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont titleFont, font;
    private final GlyphLayout layout = new GlyphLayout();
    private final List<GameLevel> levels;
    private final Rectangle[] levelButtons;
    private final Rectangle[] diffButtons = new Rectangle[3];
    private Difficulty selectedDifficulty = Difficulty.NORMAL;
    private final Rectangle continueBtn = new Rectangle(60, 505, 800, 44);
    private final Rectangle soundBtn = new Rectangle(W - 170, 18, 150, 36);
    private final Rectangle confirmYes = new Rectangle(W / 2f - 220, H / 2f - 50, 200, 48);
    private final Rectangle confirmNo = new Rectangle(W / 2f + 20, H / 2f - 50, 200, 48);
    private Integer pendingLevel;

    public MenuScreen(MedievalTDGame game) {
        this.game = game;
        titleFont = game.createFont(42);
        font = game.createFont(18);
        camera.position.set(W / 2f, H / 2f, 0);
        levels = GameLevel.createLevels();
        levelButtons = new Rectangle[levels.size()];
        for (int i = 0; i < levels.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            levelButtons[i] = new Rectangle(60 + col * 420, 360 - row * 100, 380, 85);
        }
        for (int i = 0; i < 3; i++) {
            diffButtons[i] = new Rectangle(900, 480 - i * 55, 320, 45);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(GameColors.BACKGROUND.r, GameColors.BACKGROUND.g, GameColors.BACKGROUND.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply(); camera.update();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        CampaignSave.Run save = CampaignSave.load();
        if (save != null) {
            shapes.setColor(0.32f, 0.24f, 0.1f, 0.96f);
            shapes.rect(continueBtn.x, continueBtn.y, continueBtn.width, continueBtn.height);
            shapes.setColor(GameColors.UI_GOLD);
            shapes.rect(continueBtn.x, continueBtn.y, continueBtn.width, 3);
        }
        for (int i = 0; i < levelButtons.length; i++) {
            boolean locked = !CampaignProgress.isUnlocked(i);
            shapes.setColor(locked ? 0.12f : 0.2f, locked ? 0.1f : 0.15f, locked ? 0.08f : 0.1f, 0.95f);
            shapes.rect(levelButtons[i].x, levelButtons[i].y, levelButtons[i].width, levelButtons[i].height);
            shapes.setColor(locked ? 0.35f : GameColors.UI_BORDER.r, locked ? 0.3f : GameColors.UI_BORDER.g, locked ? 0.22f : GameColors.UI_BORDER.b, 1f);
            shapes.rect(levelButtons[i].x, levelButtons[i].y, levelButtons[i].width, 3);
        }
        Difficulty[] diffs = Difficulty.values();
        for (int i = 0; i < 3; i++) {
            boolean sel = diffs[i] == selectedDifficulty;
            shapes.setColor(sel ? 0.35f : 0.2f, sel ? 0.25f : 0.15f, sel ? 0.15f : 0.1f, 0.95f);
            shapes.rect(diffButtons[i].x, diffButtons[i].y, diffButtons[i].width, diffButtons[i].height);
            if (sel) {
                shapes.setColor(GameColors.UI_GOLD);
                shapes.rect(diffButtons[i].x, diffButtons[i].y, 4, diffButtons[i].height);
            }
        }
        shapes.setColor(0.25f, 0.2f, 0.15f, 1f);
        shapes.rect(soundBtn.x, soundBtn.y, soundBtn.width, soundBtn.height);
        shapes.setColor(GameColors.UI_BORDER);
        shapes.rect(soundBtn.x, soundBtn.y, soundBtn.width, 3);
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        titleFont.setColor(GameColors.UI_GOLD);
        layout.setText(titleFont, "Medieval TD");
        titleFont.draw(batch, "Medieval TD", W / 2f - layout.width / 2, H - 60);
        font.setColor(GameColors.UI_TEXT);
        layout.setText(font, "Защити королевство от орд тьмы");
        font.draw(batch, "Защити королевство от орд тьмы", W / 2f - layout.width / 2, H - 105);

        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, "Уровни:", 60, 470);
        if (save != null) {
            font.setColor(GameColors.UI_GOLD);
            font.getData().setScale(0.85f);
            font.draw(batch, CampaignSave.label(save), continueBtn.x + 14, continueBtn.y + 28);
            font.getData().setScale(1f);
        }
        for (int i = 0; i < levels.size(); i++) {
            GameLevel lev = levels.get(i);
            Rectangle btn = levelButtons[i];
            boolean locked = !CampaignProgress.isUnlocked(i);
            font.setColor(locked ? 0.55f : GameColors.UI_GOLD.r, locked ? 0.5f : GameColors.UI_GOLD.g, locked ? 0.4f : GameColors.UI_GOLD.b, 1f);
            font.draw(batch, (i + 1) + ". " + lev.name, btn.x + 14, btn.y + 55);
            font.setColor(locked ? 0.45f : GameColors.UI_TEXT.r, locked ? 0.42f : GameColors.UI_TEXT.g, locked ? 0.38f : GameColors.UI_TEXT.b, 1f);
            font.getData().setScale(0.8f);
            String sub = locked ? "Сначала пройдите карту " + i : lev.description + " (" + lev.totalWaves + " волн)";
            font.draw(batch, sub, btn.x + 14, btn.y + 28);
            font.getData().setScale(1f);
        }

        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, "Сложность:", 900, 555);
        for (int i = 0; i < 3; i++) {
            Difficulty d = diffs[i];
            font.setColor(d == selectedDifficulty ? GameColors.UI_GOLD : GameColors.UI_TEXT);
            font.draw(batch, d.displayName, diffButtons[i].x + 15, diffButtons[i].y + 30);
            font.setColor(GameColors.UI_TEXT);
            font.getData().setScale(0.7f);
            String info = switch (d) {
                case EASY -> "x1.3 золота, 25 жизней";
                case NORMAL -> "Стандартный баланс";
                case HARD -> "x0.75 золота, 12 жизней, x1.2 врагов";
            };
            font.draw(batch, info, diffButtons[i].x + 130, diffButtons[i].y + 28);
            font.getData().setScale(1f);
        }

        font.setColor(GameColors.UI_TEXT);
        font.getData().setScale(0.75f);
        font.draw(batch, "Коснитесь уровня для начала", 60, 50);
        font.getData().setScale(1f);
        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, "Монет науки: " + game.getResearch().getGold(), 900, 175);
        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "Исследования >", 900, 150);

        // Survival mode button
        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, "ВЫЖИВАНИЕ (100 волн) >", 60, 115);
        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, Settings.sound() ? "Звук: вкл" : "Звук: выкл", soundBtn.x + 18, soundBtn.y + 24);
        font.getData().setScale(0.7f);
        font.setColor(0.55f, 0.5f, 0.42f, 1f);
        font.draw(batch, "v1.0.0", W - 70, H - 18);
        font.getData().setScale(1f);
        batch.end();

        if (pendingLevel != null) {
            drawConfirm();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            if (pendingLevel != null) pendingLevel = null;
            else Gdx.app.exit();
            return;
        }

        if (!Gdx.input.justTouched()) return;
        Vector2 pos = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        if (pendingLevel != null) {
            if (confirmYes.contains(pos.x, pos.y)) {
                game.getSfx().click();
                startPending();
            } else if (confirmNo.contains(pos.x, pos.y)) {
                game.getSfx().click();
                pendingLevel = null;
            }
            return;
        }
        if (soundBtn.contains(pos.x, pos.y)) {
            Settings.toggleSound();
            game.getSfx().click();
            return;
        }
        if (save != null && continueBtn.contains(pos.x, pos.y)) {
            game.getSfx().click();
            game.resumeCampaign();
            return;
        }
        for (int i = 0; i < levelButtons.length; i++) {
            if (levelButtons[i].contains(pos.x, pos.y)) {
                if (!CampaignProgress.isUnlocked(i)) {
                    game.getSfx().miss();
                    return;
                }
                game.getSfx().click();
                requestStart(i);
                return;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (diffButtons[i].contains(pos.x, pos.y)) {
                selectedDifficulty = diffs[i];
                game.getSfx().click();
            }
        }
        if (pos.x >= 900 && pos.x <= 1220 && pos.y >= 130 && pos.y <= 180) {
            game.getSfx().click();
            game.openResearch();
        }
        if (pos.x >= 60 && pos.x <= 380 && pos.y >= 95 && pos.y <= 125) {
            game.getSfx().click();
            requestStart(-1);
        }
    }

    private void requestStart(int levelIndex) {
        if (CampaignSave.hasSave()) pendingLevel = levelIndex;
        else begin(levelIndex);
    }

    private void startPending() {
        int level = pendingLevel;
        pendingLevel = null;
        begin(level);
    }

    private void begin(int levelIndex) {
        if (levelIndex < 0) game.startSurvival(selectedDifficulty);
        else game.startGame(levelIndex, selectedDifficulty);
    }

    private void drawConfirm() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.65f);
        shapes.rect(0, 0, W, H);
        shapes.setColor(0.22f, 0.16f, 0.1f, 0.98f);
        shapes.rect(W / 2f - 280, H / 2f - 80, 560, 180);
        shapes.setColor(0.3f, 0.45f, 0.2f, 1f);
        shapes.rect(confirmYes.x, confirmYes.y, confirmYes.width, confirmYes.height);
        shapes.setColor(0.4f, 0.2f, 0.15f, 1f);
        shapes.rect(confirmNo.x, confirmNo.y, confirmNo.width, confirmNo.height);
        shapes.end();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(GameColors.UI_GOLD);
        layout.setText(font, "Начать заново?");
        font.draw(batch, "Начать заново?", W / 2f - layout.width / 2f, H / 2f + 70);
        font.setColor(GameColors.UI_TEXT);
        layout.setText(font, "Текущий бой будет потерян");
        font.draw(batch, "Текущий бой будет потерян", W / 2f - layout.width / 2f, H / 2f + 38);
        font.setColor(GameColors.UI_GOLD);
        layout.setText(font, "Да");
        font.draw(batch, "Да", confirmYes.x + (confirmYes.width - layout.width) / 2f, confirmYes.y + 32);
        layout.setText(font, "Нет");
        font.draw(batch, "Нет", confirmNo.x + (confirmNo.width - layout.width) / 2f, confirmNo.y + 32);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void show() {
        camera.position.set(W / 2f, H / 2f, 0);
        camera.update();
    }

    @Override
    public void resize(int w, int h) { viewport.update(w, h, true); }

    @Override
    public void dispose() { batch.dispose(); shapes.dispose(); titleFont.dispose(); font.dispose(); }
}
