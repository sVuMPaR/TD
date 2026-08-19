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
    public TextureRegion towerBallista;
    public TextureRegion towerMagic;
    public TextureRegion towerFire;
    public TextureRegion towerIce;
    public TextureRegion towerLightning;
    public TextureRegion towerIceTower;
    public TextureRegion towerBarracks;
    public TextureRegion towerTemple;

    public TextureRegion enemyGoblin;
    public TextureRegion enemyOrc;
    public TextureRegion enemyWolf;
    public TextureRegion enemyKnight;
    public TextureRegion enemyDragon;
    public TextureRegion enemyFrostElemental;
    public TextureRegion enemyFireImp;
    public TextureRegion enemyGolem;
    public TextureRegion enemyDarkMage;
    public TextureRegion enemyWyvern;

    public TextureRegion projectileArrow;
    public TextureRegion projectileCannonball;
    public TextureRegion projectileMagic;
    public TextureRegion projectileFire;
    public TextureRegion projectileIce;
    public TextureRegion projectileLightning;
    public TextureRegion projectileBolt;

    public TextureRegion tileGrass;
    public TextureRegion tilePath;
    public TextureRegion tileBuildSpot;

    public TextureRegion soldierIcon;

    public void load() {
        whitePixel = new Texture(createPixmap(1, 1, Color.WHITE));
        white = new TextureRegion(whitePixel);
        circle = new TextureRegion(createCircleTexture(64, Color.WHITE));

        towerArcher = tr(createDetailedTower(48, c(0.35f, 0.55f, 0.25f), c(0.6f, 0.45f, 0.2f), c(0.8f, 0.75f, 0.6f)));
        towerArtillery = tr(createDetailedTower(48, c(0.4f, 0.35f, 0.3f), c(0.25f, 0.25f, 0.28f), c(0.5f, 0.45f, 0.4f)));
        towerBallista = tr(createDetailedTower(48, c(0.5f, 0.4f, 0.25f), c(0.35f, 0.3f, 0.22f), c(0.7f, 0.6f, 0.45f)));
        towerMagic = tr(createDetailedTower(48, c(0.35f, 0.2f, 0.55f), c(0.7f, 0.5f, 0.9f), c(0.9f, 0.8f, 1f)));
        towerFire = tr(createDetailedTower(48, c(0.55f, 0.15f, 0.1f), c(1f, 0.5f, 0.1f), c(1f, 0.85f, 0.3f)));
        towerIce = tr(createDetailedTower(48, c(0.2f, 0.35f, 0.55f), c(0.5f, 0.8f, 1f), c(0.85f, 0.95f, 1f)));
        towerLightning = tr(createDetailedTower(48, c(0.25f, 0.2f, 0.45f), c(0.9f, 0.85f, 0.3f), c(1f, 1f, 0.6f)));
        towerIceTower = tr(createDetailedTower(48, c(0.15f, 0.3f, 0.45f), c(0.4f, 0.7f, 0.9f), c(0.7f, 0.9f, 1f)));
        towerBarracks = tr(createDetailedTower(48, c(0.45f, 0.3f, 0.15f), c(0.6f, 0.4f, 0.2f), c(0.85f, 0.65f, 0.35f)));
        towerTemple = tr(createTempleTower(48));

        enemyGoblin = tr(createDetailedEnemy(32, c(0.3f, 0.65f, 0.25f)));
        enemyWolf = tr(createDetailedEnemy(28, c(0.55f, 0.55f, 0.6f)));
        enemyOrc = tr(createDetailedEnemy(36, c(0.45f, 0.55f, 0.3f)));
        enemyKnight = tr(createDetailedEnemy(40, c(0.5f, 0.5f, 0.55f)));
        enemyDragon = tr(createDetailedEnemy(52, c(0.75f, 0.2f, 0.15f)));
        enemyFrostElemental = tr(createDetailedEnemy(38, c(0.4f, 0.7f, 0.9f)));
        enemyFireImp = tr(createDetailedEnemy(26, c(0.9f, 0.35f, 0.1f)));
        enemyGolem = tr(createDetailedEnemy(46, c(0.45f, 0.42f, 0.38f)));
        enemyDarkMage = tr(createDetailedEnemy(34, c(0.4f, 0.15f, 0.5f)));
        enemyWyvern = tr(createDetailedEnemy(44, c(0.5f, 0.6f, 0.35f)));

        projectileArrow = tr(createRectTexture(16, 4, c(0.55f, 0.4f, 0.2f)));
        projectileCannonball = tr(createCircleTexture(12, c(0.2f, 0.2f, 0.22f)));
        projectileMagic = tr(createCircleTexture(14, c(0.6f, 0.3f, 0.95f)));
        projectileFire = tr(createCircleTexture(14, c(1f, 0.5f, 0.1f)));
        projectileIce = tr(createCircleTexture(14, c(0.4f, 0.8f, 1f)));
        projectileLightning = tr(createCircleTexture(12, c(0.95f, 0.9f, 0.3f)));
        projectileBolt = tr(createRectTexture(20, 5, c(0.5f, 0.38f, 0.22f)));

        tileGrass = tr(createGrassTile(64));
        tilePath = tr(createPathTile(64));
        tileBuildSpot = tr(createRectTexture(64, 64, c(0.28f, 0.45f, 0.22f, 0.6f)));

        soldierIcon = tr(createDetailedEnemy(24, c(0.65f, 0.5f, 0.3f)));
    }

    private TextureRegion tr(Texture t) { return new TextureRegion(t); }
    private Color c(float r, float g, float b) { return new Color(r, g, b, 1f); }
    private Color c(float r, float g, float b, float a) { return new Color(r, g, b, a); }

    private Pixmap createPixmap(int w, int h, Color color) {
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(color);
        p.fill();
        return p;
    }

    private Texture createRectTexture(int w, int h, Color color) {
        Pixmap p = createPixmap(w, h, color);
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture createCircleTexture(int size, Color color) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0); p.fill();
        p.setColor(color);
        p.fillCircle(size / 2, size / 2, size / 2 - 2);
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture createDetailedTower(int size, Color base, Color top, Color highlight) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0); p.fill();
        int cx = size / 2;
        // Stone base
        p.setColor(darken(base, 0.6f));
        p.fillRectangle(cx - 14, 2, 28, 6);
        // Body
        p.setColor(base);
        p.fillRectangle(cx - 10, 8, 20, size - 22);
        // Highlight strip
        p.setColor(highlight);
        p.fillRectangle(cx - 2, 10, 4, size - 26);
        // Top
        p.setColor(top);
        p.fillCircle(cx, size - 14, 10);
        // Battlement details
        p.setColor(darken(top, 0.7f));
        p.fillRectangle(cx - 12, size - 8, 4, 8);
        p.fillRectangle(cx + 8, size - 8, 4, 8);
        p.fillRectangle(cx - 2, size - 6, 4, 6);
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture createTempleTower(int size) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0); p.fill();
        int cx = size / 2;
        p.setColor(c(0.85f, 0.8f, 0.7f));
        p.fillRectangle(cx - 12, 4, 24, size - 18);
        p.setColor(c(1f, 0.9f, 0.4f));
        p.fillTriangle(cx - 16, size - 14, cx + 16, size - 14, cx, size - 2);
        // Cross/glow
        p.setColor(c(1f, 0.95f, 0.6f));
        p.fillRectangle(cx - 2, size / 2 - 2, 4, 14);
        p.fillRectangle(cx - 6, size / 2 + 4, 12, 4);
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture createDetailedEnemy(int size, Color color) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0); p.fill();
        int r = size / 2 - 2;
        // Shadow
        p.setColor(darken(color, 0.5f));
        p.fillCircle(size / 2 + 1, size / 2 + 1, r);
        // Body
        p.setColor(color);
        p.fillCircle(size / 2, size / 2, r);
        // Highlight
        p.setColor(lighten(color, 0.3f));
        p.fillCircle(size / 2 - r / 3, size / 2 - r / 3, r / 3);
        // Outline
        p.setColor(darken(color, 0.3f));
        for (int a = 0; a < 360; a += 5) {
            int px = (int) (size / 2 + r * Math.cos(Math.toRadians(a)));
            int py = (int) (size / 2 + r * Math.sin(Math.toRadians(a)));
            p.drawPixel(px, py);
        }
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture createGrassTile(int size) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        Color base = c(0.22f, 0.38f, 0.18f);
        p.setColor(base); p.fill();
        // Subtle variation
        java.util.Random rng = new java.util.Random(42);
        for (int i = 0; i < 30; i++) {
            float v = 0.92f + rng.nextFloat() * 0.16f;
            p.setColor(base.r * v, base.g * v, base.b * v, 1f);
            int gx = rng.nextInt(size);
            int gy = rng.nextInt(size);
            p.fillRectangle(gx, gy, 2 + rng.nextInt(3), 1);
        }
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture createPathTile(int size) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        Color base = c(0.55f, 0.42f, 0.28f);
        p.setColor(base); p.fill();
        // Dirt texture
        java.util.Random rng = new java.util.Random(77);
        for (int i = 0; i < 20; i++) {
            float v = 0.9f + rng.nextFloat() * 0.2f;
            p.setColor(base.r * v, base.g * v, base.b * v, 1f);
            int gx = rng.nextInt(size);
            int gy = rng.nextInt(size);
            p.fillCircle(gx, gy, 1 + rng.nextInt(2));
        }
        // Border
        p.setColor(darken(base, 0.7f));
        p.fillRectangle(0, 0, size, 2);
        p.fillRectangle(0, size - 2, size, 2);
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Color darken(Color c, float factor) {
        return new Color(c.r * factor, c.g * factor, c.b * factor, c.a);
    }

    private Color lighten(Color c, float amount) {
        return new Color(
            Math.min(1f, c.r + amount),
            Math.min(1f, c.g + amount),
            Math.min(1f, c.b + amount), c.a);
    }

    public TextureRegion getTowerTexture(com.medievaltd.model.TowerType type) {
        return switch (type) {
            case ARCHER -> towerArcher;
            case ARTILLERY -> towerArtillery;
            case BALLISTA -> towerBallista;
            case MAGIC -> towerMagic;
            case FIRE_MAGIC -> towerFire;
            case ICE_MAGIC -> towerIce;
            case LIGHTNING_MAGIC -> towerLightning;
            case ICE_TOWER -> towerIceTower;
            case BARRACKS -> towerBarracks;
            case TEMPLE -> towerTemple;
        };
    }

    public void dispose() {
        // All textures created procedurally
    }
}
