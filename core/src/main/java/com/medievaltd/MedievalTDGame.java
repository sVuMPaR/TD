package com.medievaltd;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.medievaltd.model.Difficulty;
import com.medievaltd.research.ResearchState;
import com.medievaltd.screen.GameScreen;
import com.medievaltd.screen.MenuScreen;
import com.medievaltd.screen.ResearchScreen;
import com.medievaltd.system.CampaignSave;
import com.medievaltd.util.Assets;
import com.medievaltd.util.Settings;
import com.medievaltd.util.Smoke;
import com.medievaltd.util.Sfx;

public class MedievalTDGame extends Game {
    private Assets assets;
    private Sfx sfx;
    private final ResearchState research = new ResearchState();

    @Override
    public void create() {
        assets = new Assets();
        assets.load();
        sfx = new Sfx();
        sfx.load();
        Settings.load();
        research.load();
        Gdx.input.setCatchKey(com.badlogic.gdx.Input.Keys.BACK, true);
        if (Smoke.on()) {
            setScreen(new GameScreen(this, 0, Difficulty.NORMAL));
        } else {
            setScreen(new MenuScreen(this));
        }
    }

    public Assets getAssets() { return assets; }

    public Sfx getSfx() { return sfx; }

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
    public void setScreen(com.badlogic.gdx.Screen screen) {
        com.badlogic.gdx.Screen prev = getScreen();
        super.setScreen(screen);
        if (prev != null) prev.dispose();
    }

    @Override
    public void dispose() {
        research.save();
        setScreen(null);
        if (sfx != null) sfx.dispose();
        if (assets != null) assets.dispose();
    }

    public void startGame(int levelIndex, Difficulty difficulty) {
        CampaignSave.clear();
        setScreen(new GameScreen(this, levelIndex, difficulty));
    }

    public void startSurvival(Difficulty difficulty) {
        CampaignSave.clear();
        setScreen(new GameScreen(this, -1, difficulty));
    }

    public void resumeCampaign() {
        CampaignSave.Run run = CampaignSave.load();
        if (run == null) {
            setScreen(new MenuScreen(this));
            return;
        }
        setScreen(GameScreen.resume(this, run));
    }

    public void returnToMenu() {
        setScreen(new MenuScreen(this));
    }

    public void openResearch() {
        setScreen(new ResearchScreen(this));
    }

    public ResearchState getResearch() {
        return research;
    }
}
