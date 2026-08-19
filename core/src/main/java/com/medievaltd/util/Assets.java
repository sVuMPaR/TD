package com.medievaltd.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {
    public Texture whitePixel;
    public TextureRegion white;
    public TextureRegion circle;
    public TextureRegion towerArcher;
    public TextureRegion towerArtillery;
    public TextureRegion towerMagic;
    public TextureRegion enemyGoblin;
    public TextureRegion enemyOrc;
    public TextureRegion enemyWolf;
    public TextureRegion enemyKnight;
    public TextureRegion enemyDragon;
    public TextureRegion projectileArrow;
    public TextureRegion projectileCannonball;
    public TextureRegion projectileMagic;
    public TextureRegion tileGrass;
    public TextureRegion tilePath;
    public TextureRegion tileBuildSpot;

    public void load() {
        whitePixel = new Texture(createPixmap(1, 1, Color.WHITE));
        white = new TextureRegion(whitePixel);

        circle = new TextureRegion(createCircleTexture(64, Color.WHITE));
        towerArcher = new TextureRegion(createTowerTexture(48, new Color(0.35f, 0.55f, 0.25f, 1f), new Color(0.6f, 0.45f, 0.2f, 1f)));
        towerArtillery = new TextureRegion(createTowerTexture(48, new Color(0.4f, 0.35f, 0.3f, 1f), new Color(0.25f, 0.25f, 0.28f, 1f)));
        towerMagic = new TextureRegion(createTowerTexture(48, new Color(0.35f, 0.2f, 0.55f, 1f), new Color(0.7f, 0.5f, 0.9f, 1f)));
        enemyGoblin = new TextureRegion(createCircleTexture(32, new Color(0.3f, 0.65f, 0.25f, 1f)));
        enemyOrc = new TextureRegion(createCircleTexture(36, new Color(0.45f, 0.55f, 0.3f, 1f)));
        enemyWolf = new TextureRegion(createCircleTexture(28, new Color(0.55f, 0.55f, 0.6f, 1f)));
        enemyKnight = new TextureRegion(createCircleTexture(40, new Color(0.5f, 0.5f, 0.55f, 1f)));
        enemyDragon = new TextureRegion(createCircleTexture(52, new Color(0.75f, 0.2f, 0.15f, 1f)));
        projectileArrow = new TextureRegion(createRectTexture(16, 4, new Color(0.55f, 0.4f, 0.2f, 1f)));
        projectileCannonball = new TextureRegion(createCircleTexture(12, new Color(0.2f, 0.2f, 0.22f, 1f)));
        projectileMagic = new TextureRegion(createCircleTexture(14, new Color(0.6f, 0.3f, 0.95f, 1f)));
        tileGrass = new TextureRegion(createRectTexture(64, 64, new Color(0.22f, 0.38f, 0.18f, 1f)));
        tilePath = new TextureRegion(createRectTexture(64, 64, new Color(0.55f, 0.42f, 0.28f, 1f)));
        tileBuildSpot = new TextureRegion(createRectTexture(64, 64, new Color(0.28f, 0.45f, 0.22f, 0.6f)));
    }

    private Pixmap createPixmap(int w, int h, Color color) {
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        return pixmap;
    }

    private Texture createRectTexture(int w, int h, Color color) {
        Pixmap pixmap = createPixmap(w, h, color);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createCircleTexture(int size, Color color) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        pixmap.setColor(color);
        int r = size / 2 - 2;
        pixmap.fillCircle(size / 2, size / 2, r);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createTowerTexture(int size, Color base, Color top) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        int cx = size / 2;
        pixmap.setColor(base);
        pixmap.fillRectangle(cx - 10, 8, 20, size - 16);
        pixmap.setColor(top);
        pixmap.fillCircle(cx, size - 18, 12);
        pixmap.fillTriangle(cx - 14, size - 26, cx + 14, size - 26, cx, size - 6);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void dispose() {
        whitePixel.dispose();
        circle.getTexture().dispose();
        towerArcher.getTexture().dispose();
        towerArtillery.getTexture().dispose();
        towerMagic.getTexture().dispose();
        enemyGoblin.getTexture().dispose();
        enemyOrc.getTexture().dispose();
        enemyWolf.getTexture().dispose();
        enemyKnight.getTexture().dispose();
        enemyDragon.getTexture().dispose();
        projectileArrow.getTexture().dispose();
        projectileCannonball.getTexture().dispose();
        projectileMagic.getTexture().dispose();
        tileGrass.getTexture().dispose();
        tilePath.getTexture().dispose();
        tileBuildSpot.getTexture().dispose();
    }
}
