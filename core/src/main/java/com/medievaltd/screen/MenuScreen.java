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
import com.medievaltd.model.GameLevel;
import com.medievaltd.util.GameColors;

import java.util.List;

public class MenuScreen extends ScreenAdapter {
    private static final float WIDTH = 1280;
    private static final float HEIGHT = 720;

    private final MedievalTDGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(WIDTH, HEIGHT, camera);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont titleFont;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private final List<GameLevel> levels;
    private final Rectangle[] levelButtons;

    public MenuScreen(MedievalTDGame game) {
        this.game = game;
        this.titleFont = game.createFont(48);
        this.font = game.createFont(22);
        this.levels = GameLevel.createLevels();
        this.levelButtons = new Rectangle[levels.size()];
        for (int i = 0; i < levels.size(); i++) {
            levelButtons[i] = new Rectangle(440, 420 - i * 120, 400, 90);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(GameColors.BACKGROUND.r, GameColors.BACKGROUND.g, GameColors.BACKGROUND.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Rectangle btn : levelButtons) {
            shapes.setColor(0.2f, 0.15f, 0.1f, 0.95f);
            shapes.rect(btn.x, btn.y, btn.width, btn.height);
            shapes.setColor(GameColors.UI_BORDER);
            shapes.rect(btn.x, btn.y, btn.width, 3);
        }
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        titleFont.setColor(GameColors.UI_GOLD);
        layout.setText(titleFont, "Medieval TD");
        titleFont.draw(batch, "Medieval TD", WIDTH / 2f - layout.width / 2, HEIGHT - 80);

        font.setColor(GameColors.UI_TEXT);
        layout.setText(font, "Защити королевство от орд тьмы");
        font.draw(batch, "Защити королевство от орд тьмы", WIDTH / 2f - layout.width / 2, HEIGHT - 130);

        for (int i = 0; i < levels.size(); i++) {
            GameLevel level = levels.get(i);
            Rectangle btn = levelButtons[i];
            font.setColor(GameColors.UI_GOLD);
            font.draw(batch, (i + 1) + ". " + level.name, btn.x + 20, btn.y + 58);
            font.setColor(GameColors.UI_TEXT);
            font.getData().setScale(0.8f);
            font.draw(batch, level.description, btn.x + 20, btn.y + 28);
            font.getData().setScale(1f);
        }

        font.setColor(GameColors.UI_TEXT);
        font.getData().setScale(0.75f);
        font.draw(batch, "Коснитесь уровня для начала | Ландшафт: горизонталь", 380, 60);
        font.getData().setScale(1f);
        batch.end();

        if (Gdx.input.justTouched()) {
            Vector2 pos = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            for (int i = 0; i < levelButtons.length; i++) {
                if (levelButtons[i].contains(pos.x, pos.y)) {
                    game.startGame(i);
                    return;
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) game.startGame(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && levels.size() > 1) game.startGame(1);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        titleFont.dispose();
        font.dispose();
    }
}
