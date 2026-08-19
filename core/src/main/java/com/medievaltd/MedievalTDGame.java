package com.medievaltd;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.medievaltd.model.Difficulty;
import com.medievaltd.screen.GameScreen;
import com.medievaltd.screen.MenuScreen;
import com.medievaltd.util.Assets;

public class MedievalTDGame extends Game {
    private Assets assets;

    @Override
    public void create() {
        assets = new Assets();
        assets.load();
        setScreen(new MenuScreen(this));
    }

    public Assets getAssets() { return assets; }

    public BitmapFont createFont(int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("fonts/default.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = size;
        param.characters = FreeTypeFontGenerator.DEFAULT_CHARS
            + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        BitmapFont font = generator.generateFont(param);
        generator.dispose();
        return font;
    }

    @Override
    public void dispose() {
        if (assets != null) assets.dispose();
        super.dispose();
    }

    public void startGame(int levelIndex, Difficulty difficulty) {
        setScreen(new GameScreen(this, levelIndex, difficulty));
    }

    public void returnToMenu() {
        setScreen(new MenuScreen(this));
    }
}
