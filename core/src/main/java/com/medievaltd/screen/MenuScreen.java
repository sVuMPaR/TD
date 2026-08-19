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
import com.medievaltd.util.GameColors;

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

    public MenuScreen(MedievalTDGame game) {
        this.game = game;
        titleFont = game.createFont(42);
        font = game.createFont(18);
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
        for (Rectangle btn : levelButtons) {
            shapes.setColor(0.2f, 0.15f, 0.1f, 0.95f);
            shapes.rect(btn.x, btn.y, btn.width, btn.height);
            shapes.setColor(GameColors.UI_BORDER);
            shapes.rect(btn.x, btn.y, btn.width, 3);
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
        for (int i = 0; i < levels.size(); i++) {
            GameLevel lev = levels.get(i);
            Rectangle btn = levelButtons[i];
            font.setColor(GameColors.UI_GOLD);
            font.draw(batch, (i + 1) + ". " + lev.name, btn.x + 14, btn.y + 55);
            font.setColor(GameColors.UI_TEXT);
            font.getData().setScale(0.8f);
            font.draw(batch, lev.description + " (" + lev.totalWaves + " волн)", btn.x + 14, btn.y + 28);
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
        font.draw(batch, "Исследования →", 900, 150);

        // Survival mode button
        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, "ВЫЖИВАНИЕ (100 волн) →", 60, 115);
        batch.end();

        if (Gdx.input.justTouched()) {
            Vector2 pos = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            for (int i = 0; i < levelButtons.length; i++) {
                if (levelButtons[i].contains(pos.x, pos.y)) {
                    game.startGame(i, selectedDifficulty);
                    return;
                }
            }
            for (int i = 0; i < 3; i++) {
                if (diffButtons[i].contains(pos.x, pos.y)) {
                    selectedDifficulty = diffs[i];
                }
            }
            // Research button area
            if (pos.x >= 900 && pos.x <= 1220 && pos.y >= 130 && pos.y <= 180) {
                game.openResearch();
            }
            // Survival mode
            if (pos.x >= 60 && pos.x <= 380 && pos.y >= 95 && pos.y <= 125) {
                game.startSurvival(selectedDifficulty);
            }
        }
    }

    @Override
    public void resize(int w, int h) { viewport.update(w, h); }

    @Override
    public void dispose() { batch.dispose(); shapes.dispose(); titleFont.dispose(); font.dispose(); }
}
