package com.medievaltd.util;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    private final List<Texture> owned = new ArrayList<>();
    private File dumpDir;

    public void setDumpDir(File dir) {
        this.dumpDir = dir;
        if (dir != null) dir.mkdirs();
    }

    public void load() {
        whitePixel = finish(createPixmap(1, 1, Color.WHITE));
        white = new TextureRegion(whitePixel);
        circle = tr(createCircleTexture(64, Color.WHITE));

        towerArcher = tr(drawArcher());
        towerArtillery = tr(drawArtillery());
        towerBallista = tr(drawBallista());
        towerMagic = tr(drawMagic());
        towerFire = tr(drawFireMagic());
        towerIce = tr(drawIceMagic());
        towerLightning = tr(drawLightning());
        towerIceTower = tr(drawIceTower());
        towerBarracks = tr(drawBarracks());
        towerTemple = tr(drawTemple());

        enemyGoblin = tr(drawGoblin());
        enemyWolf = tr(drawWolf());
        enemyOrc = tr(drawOrc());
        enemyKnight = tr(drawKnight());
        enemyDragon = tr(drawDragon());
        enemyFrostElemental = tr(drawFrostElemental());
        enemyFireImp = tr(drawFireImp());
        enemyGolem = tr(drawGolem());
        enemyDarkMage = tr(drawDarkMage());
        enemyWyvern = tr(drawWyvern());

        projectileArrow = tr(drawArrow());
        projectileCannonball = tr(sprite("proj-cannon", circlePixmap(12, c(0.18f, 0.18f, 0.2f))));
        projectileMagic = tr(drawDiamond("proj-magic", 16, c(0.7f, 0.35f, 1f), c(0.95f, 0.8f, 1f)));
        projectileFire = tr(drawFlame(16));
        projectileIce = tr(drawDiamond("proj-ice", 16, c(0.45f, 0.8f, 1f), c(0.9f, 0.98f, 1f)));
        projectileLightning = tr(drawBoltSpark(16));
        projectileBolt = tr(drawSpear());

        tileGrass = tr(createGrassTile(64));
        tilePath = tr(createPathTile(64));
        tileBuildSpot = tr(createRectTexture(64, 64, c(0.28f, 0.45f, 0.22f, 0.6f)));

        soldierIcon = tr(drawSoldier());
    }

    private TextureRegion tr(Texture t) { return new TextureRegion(t); }
    private Color c(float r, float g, float b) { return new Color(r, g, b, 1f); }
    private Color c(float r, float g, float b, float a) { return new Color(r, g, b, a); }

    private Pixmap blank(int size) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        p.setBlending(Pixmap.Blending.SourceOver);
        p.setColor(0, 0, 0, 0);
        p.fill();
        return p;
    }

    private Texture finish(Pixmap p) {
        Texture t = tex(p);
        p.dispose();
        return t;
    }

    private Texture sprite(String name, Pixmap p) {
        ink(p);
        if (dumpDir != null) {
            PixmapIO.writePNG(new FileHandle(new File(dumpDir, name + ".png")), p);
        }
        return finish(p);
    }

    /** 1px dark edge so shapes read on grass at ~48px. */
    private void ink(Pixmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] a = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                a[y * w + x] = src.getPixel(x, y) & 0xff;
            }
        }
        int edge = Color.rgba8888(0.05f, 0.04f, 0.03f, 1f);
        src.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (a[y * w + x] < 20) continue;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = x + dx, ny = y + dy;
                        if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                        if (a[ny * w + nx] < 20) src.drawPixel(nx, ny, edge);
                    }
                }
            }
        }
        src.setBlending(Pixmap.Blending.SourceOver);
    }

    private Texture tex(Pixmap p) {
        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        owned.add(t);
        return t;
    }

    private Pixmap circlePixmap(int size, Color color) {
        Pixmap p = blank(size);
        p.setColor(color);
        p.fillCircle(size / 2, size / 2, size / 2 - 1);
        return p;
    }

    private Pixmap createPixmap(int w, int h, Color color) {
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(color);
        p.fill();
        return p;
    }

    private Texture createRectTexture(int w, int h, Color color) {
        return finish(createPixmap(w, h, color));
    }

    private Texture createCircleTexture(int size, Color color) {
        return finish(circlePixmap(size, color));
    }

    private void ring(Pixmap p, int cx, int cy, int outer, int inner, Color color) {
        p.setColor(color);
        p.fillCircle(cx, cy, outer);
        p.setBlending(Pixmap.Blending.None);
        p.setColor(0, 0, 0, 0);
        p.fillCircle(cx, cy, inner);
        p.setBlending(Pixmap.Blending.SourceOver);
    }

    // Pixmap y=0 is the top of the sprite on screen.
    private Texture drawArcher() {
        int s = 48;
        Pixmap p = blank(s);
        Color wood = c(0.45f, 0.3f, 0.14f);
        Color leaf = c(0.28f, 0.48f, 0.18f);
        p.setColor(darken(wood, 0.65f));
        p.fillRectangle(14, 38, 20, 8);
        p.setColor(wood);
        p.fillRectangle(18, 14, 10, 26);
        p.setColor(leaf);
        p.fillCircle(24, 12, 12);
        p.fillCircle(12, 16, 8);
        p.fillCircle(36, 16, 8);
        ring(p, 38, 28, 11, 7, c(0.72f, 0.55f, 0.28f));
        p.setColor(c(0.85f, 0.75f, 0.5f));
        p.fillRectangle(26, 26, 18, 4);
        p.fillTriangle(42, 22, 42, 34, 47, 28);
        return sprite("tower-archer", p);
    }

    private Texture drawArtillery() {
        int s = 48;
        Pixmap p = blank(s);
        Color stone = c(0.42f, 0.38f, 0.34f);
        Color iron = c(0.22f, 0.22f, 0.24f);
        p.setColor(darken(stone, 0.7f));
        p.fillRectangle(8, 34, 32, 10);
        p.setColor(stone);
        p.fillRectangle(12, 22, 24, 14);
        p.setColor(iron);
        p.fillRectangle(22, 10, 22, 10);
        p.fillCircle(22, 15, 6);
        p.setColor(c(0.12f, 0.12f, 0.12f));
        p.fillCircle(42, 15, 3);
        p.setColor(c(0.55f, 0.35f, 0.15f));
        p.fillRectangle(14, 18, 4, 8);
        p.fillRectangle(30, 18, 4, 8);
        return sprite("tower-artillery", p);
    }

    private Texture drawBallista() {
        int s = 48;
        Pixmap p = blank(s);
        Color wood = c(0.5f, 0.38f, 0.2f);
        Color dark = darken(wood, 0.6f);
        p.setColor(dark);
        p.fillRectangle(10, 36, 28, 6);
        p.setColor(wood);
        p.fillRectangle(8, 14, 6, 24);
        p.fillRectangle(34, 14, 6, 24);
        p.fillRectangle(8, 18, 32, 5);
        p.fillRectangle(22, 10, 5, 28);
        p.setColor(c(0.7f, 0.55f, 0.3f));
        p.fillTriangle(6, 12, 14, 12, 10, 4);
        p.fillTriangle(34, 12, 42, 12, 38, 4);
        p.setColor(c(0.35f, 0.28f, 0.18f));
        p.fillRectangle(24, 8, 20, 4);
        p.fillTriangle(44, 6, 44, 14, 47, 10);
        return sprite("tower-ballista", p);
    }

    private Texture drawMagic() {
        int s = 48;
        Pixmap p = blank(s);
        Color staff = c(0.42f, 0.28f, 0.5f);
        Color orb = c(0.72f, 0.45f, 1f);
        p.setColor(c(0.25f, 0.18f, 0.32f));
        p.fillRectangle(18, 36, 12, 8);
        p.setColor(staff);
        p.fillRectangle(21, 16, 6, 22);
        p.setColor(orb);
        p.fillCircle(24, 12, 10);
        p.setColor(c(0.95f, 0.85f, 1f));
        p.fillCircle(21, 9, 4);
        p.setColor(c(0.55f, 0.3f, 0.8f));
        p.drawCircle(24, 12, 10);
        return sprite("tower-magic", p);
    }

    private Texture drawFireMagic() {
        int s = 48;
        Pixmap p = blank(s);
        Color brick = c(0.5f, 0.18f, 0.1f);
        p.setColor(darken(brick, 0.7f));
        p.fillRectangle(14, 36, 20, 8);
        p.setColor(brick);
        p.fillRectangle(16, 20, 16, 18);
        p.setColor(c(1f, 0.45f, 0.08f));
        p.fillTriangle(12, 22, 36, 22, 24, 2);
        p.setColor(c(1f, 0.8f, 0.2f));
        p.fillTriangle(18, 20, 30, 20, 24, 6);
        p.setColor(c(1f, 0.95f, 0.7f));
        p.fillTriangle(21, 18, 27, 18, 24, 10);
        return sprite("tower-fire", p);
    }

    private Texture drawIceMagic() {
        int s = 48;
        Pixmap p = blank(s);
        Color ice = c(0.55f, 0.82f, 1f);
        p.setColor(c(0.2f, 0.35f, 0.5f));
        p.fillRectangle(16, 36, 16, 8);
        p.setColor(ice);
        p.fillTriangle(24, 2, 10, 28, 38, 28);
        p.setColor(c(0.85f, 0.95f, 1f));
        p.fillTriangle(24, 8, 16, 26, 32, 26);
        p.setColor(c(0.35f, 0.6f, 0.85f));
        p.fillRectangle(22, 28, 4, 10);
        return sprite("tower-ice-magic", p);
    }

    private Texture drawLightning() {
        int s = 48;
        Pixmap p = blank(s);
        Color stone = c(0.28f, 0.22f, 0.48f);
        p.setColor(darken(stone, 0.7f));
        p.fillRectangle(14, 38, 20, 6);
        p.setColor(stone);
        p.fillRectangle(18, 16, 12, 24);
        p.setColor(c(1f, 0.92f, 0.28f));
        p.fillTriangle(24, 2, 14, 18, 22, 18);
        p.fillTriangle(20, 18, 34, 18, 18, 32);
        p.fillTriangle(22, 28, 32, 28, 26, 42);
        return sprite("tower-lightning", p);
    }

    private Texture drawIceTower() {
        int s = 48;
        Pixmap p = blank(s);
        Color ice = c(0.4f, 0.7f, 0.9f);
        p.setColor(darken(ice, 0.6f));
        p.fillRectangle(10, 36, 28, 8);
        p.setColor(ice);
        p.fillRectangle(14, 14, 20, 24);
        p.setColor(c(0.75f, 0.92f, 1f));
        p.fillRectangle(16, 16, 6, 20);
        p.fillTriangle(10, 16, 24, 2, 38, 16);
        p.setColor(c(0.9f, 0.97f, 1f));
        p.fillTriangle(16, 16, 24, 6, 32, 16);
        return sprite("tower-ice", p);
    }

    private Texture drawBarracks() {
        int s = 48;
        Pixmap p = blank(s);
        Color wall = c(0.55f, 0.42f, 0.22f);
        p.setColor(darken(wall, 0.65f));
        p.fillRectangle(6, 38, 36, 6);
        p.setColor(wall);
        p.fillRectangle(8, 16, 32, 24);
        p.setColor(c(0.4f, 0.28f, 0.12f));
        for (int i = 0; i < 5; i++) {
            p.fillRectangle(8 + i * 7, 10, 5, 8);
        }
        p.setColor(c(0.22f, 0.14f, 0.08f));
        p.fillRectangle(20, 26, 10, 14);
        p.setColor(c(0.7f, 0.55f, 0.25f));
        p.fillRectangle(12, 20, 6, 6);
        p.fillRectangle(30, 20, 6, 6);
        p.setColor(c(0.85f, 0.7f, 0.35f));
        p.fillRectangle(22, 8, 4, 8);
        return sprite("tower-barracks", p);
    }

    private Texture drawTemple() {
        int s = 48;
        Pixmap p = blank(s);
        Color marble = c(0.88f, 0.84f, 0.74f);
        p.setColor(darken(marble, 0.7f));
        p.fillRectangle(10, 38, 28, 6);
        p.setColor(marble);
        p.fillRectangle(8, 22, 6, 16);
        p.fillRectangle(34, 22, 6, 16);
        p.fillRectangle(16, 20, 16, 18);
        p.setColor(c(1f, 0.88f, 0.35f));
        p.fillTriangle(6, 22, 42, 22, 24, 4);
        p.setColor(c(1f, 0.96f, 0.7f));
        p.fillRectangle(22, 24, 4, 14);
        p.fillRectangle(16, 28, 16, 4);
        return sprite("tower-temple", p);
    }

    private Texture drawGoblin() {
        int s = 32;
        Pixmap p = blank(s);
        Color skin = c(0.32f, 0.62f, 0.22f);
        p.setColor(darken(skin, 0.55f));
        p.fillRectangle(10, 26, 5, 6);
        p.fillRectangle(18, 26, 5, 6);
        p.setColor(skin);
        p.fillCircle(16, 20, 8);
        p.fillCircle(16, 12, 7);
        p.setColor(c(0.25f, 0.45f, 0.15f));
        p.fillTriangle(2, 16, 14, 10, 8, 1);
        p.fillTriangle(30, 16, 18, 10, 24, 1);
        p.setColor(c(0.9f, 0.9f, 0.4f));
        p.fillCircle(13, 11, 2);
        p.fillCircle(19, 11, 2);
        p.setColor(c(0.15f, 0.15f, 0.1f));
        p.fillCircle(13, 11, 1);
        p.fillCircle(19, 11, 1);
        return sprite("enemy-goblin", p);
    }

    private Texture drawWolf() {
        int s = 40;
        Pixmap p = blank(s);
        Color fur = c(0.58f, 0.58f, 0.64f);
        p.setColor(darken(fur, 0.55f));
        p.fillRectangle(10, 30, 4, 8);
        p.fillRectangle(18, 32, 4, 7);
        p.fillRectangle(26, 30, 4, 8);
        p.setColor(fur);
        p.fillCircle(18, 22, 9);
        p.fillCircle(28, 20, 8);
        p.setColor(c(0.45f, 0.45f, 0.5f));
        p.fillTriangle(1, 22, 12, 18, 8, 8);
        p.fillTriangle(30, 10, 34, 12, 32, 2);
        p.fillTriangle(36, 12, 40, 14, 38, 2);
        p.setColor(fur);
        p.fillCircle(34, 18, 6);
        p.setColor(c(0.85f, 0.85f, 0.88f));
        p.fillTriangle(38, 16, 40, 22, 40, 14);
        p.setColor(c(0.9f, 0.3f, 0.22f));
        p.fillCircle(36, 17, 2);
        return sprite("enemy-wolf", p);
    }

    private Texture drawOrc() {
        int s = 36;
        Pixmap p = blank(s);
        Color skin = c(0.42f, 0.52f, 0.22f);
        p.setColor(darken(skin, 0.55f));
        p.fillRectangle(10, 28, 6, 8);
        p.fillRectangle(20, 28, 6, 8);
        p.setColor(skin);
        p.fillCircle(18, 22, 11);
        p.fillCircle(18, 12, 8);
        p.setColor(c(0.95f, 0.95f, 0.9f));
        p.fillTriangle(8, 14, 15, 16, 10, 24);
        p.fillTriangle(28, 14, 21, 16, 26, 24);
        p.setColor(c(0.25f, 0.2f, 0.12f));
        p.fillRectangle(8, 4, 20, 6);
        p.setColor(c(0.15f, 0.15f, 0.1f));
        p.fillCircle(15, 11, 2);
        p.fillCircle(21, 11, 2);
        return sprite("enemy-orc", p);
    }

    private Texture drawKnight() {
        int s = 40;
        Pixmap p = blank(s);
        Color steel = c(0.62f, 0.64f, 0.7f);
        p.setColor(c(0.3f, 0.28f, 0.25f));
        p.fillRectangle(12, 32, 6, 8);
        p.fillRectangle(22, 32, 6, 8);
        p.setColor(steel);
        p.fillRectangle(12, 16, 16, 18);
        p.fillCircle(20, 10, 8);
        p.setColor(darken(steel, 0.45f));
        p.fillRectangle(16, 8, 8, 4);
        p.setColor(c(0.75f, 0.18f, 0.12f));
        p.fillTriangle(2, 14, 14, 14, 8, 32);
        p.setColor(c(0.75f, 0.6f, 0.2f));
        p.fillRectangle(28, 10, 5, 22);
        p.fillTriangle(26, 8, 36, 8, 31, 2);
        return sprite("enemy-knight", p);
    }

    private Texture drawDragon() {
        int s = 52;
        Pixmap p = blank(s);
        Color scale = c(0.72f, 0.16f, 0.12f);
        p.setColor(c(0.9f, 0.28f, 0.08f));
        p.fillTriangle(2, 24, 24, 20, 14, 2);
        p.fillTriangle(28, 20, 50, 24, 38, 2);
        p.setColor(scale);
        p.fillCircle(26, 28, 13);
        p.fillCircle(40, 20, 8);
        p.fillTriangle(1, 32, 16, 26, 8, 42);
        p.setColor(c(1f, 0.7f, 0.15f));
        p.fillTriangle(44, 16, 51, 14, 48, 26);
        p.setColor(c(0.15f, 0.1f, 0.1f));
        p.fillCircle(42, 18, 2);
        p.setColor(darken(scale, 0.6f));
        p.fillRectangle(18, 38, 6, 12);
        p.fillRectangle(28, 38, 6, 12);
        return sprite("enemy-dragon", p);
    }

    private Texture drawFrostElemental() {
        int s = 38;
        Pixmap p = blank(s);
        Color ice = c(0.45f, 0.75f, 0.95f);
        p.setColor(ice);
        p.fillTriangle(19, 2, 4, 20, 34, 20);
        p.fillTriangle(19, 36, 4, 18, 34, 18);
        p.setColor(c(0.85f, 0.95f, 1f));
        p.fillTriangle(19, 8, 12, 19, 26, 19);
        p.fillTriangle(19, 30, 12, 19, 26, 19);
        p.setColor(c(0.7f, 0.9f, 1f));
        p.fillCircle(19, 19, 4);
        return sprite("enemy-frost", p);
    }

    private Texture drawFireImp() {
        int s = 28;
        Pixmap p = blank(s);
        Color fire = c(0.92f, 0.32f, 0.08f);
        p.setColor(fire);
        p.fillCircle(13, 18, 8);
        p.fillTriangle(5, 12, 12, 12, 8, 1);
        p.fillTriangle(16, 12, 23, 12, 20, 1);
        p.fillTriangle(18, 20, 27, 14, 27, 24);
        p.setColor(c(1f, 0.8f, 0.2f));
        p.fillCircle(13, 17, 4);
        p.setColor(c(0.1f, 0.05f, 0.05f));
        p.fillCircle(11, 16, 1);
        p.fillCircle(15, 16, 1);
        return sprite("enemy-imp", p);
    }

    private Texture drawGolem() {
        int s = 46;
        Pixmap p = blank(s);
        Color rock = c(0.48f, 0.44f, 0.38f);
        p.setColor(darken(rock, 0.7f));
        p.fillRectangle(8, 34, 12, 10);
        p.fillRectangle(26, 34, 12, 10);
        p.setColor(rock);
        p.fillRectangle(6, 16, 34, 20);
        p.fillRectangle(10, 2, 26, 16);
        p.setColor(c(0.65f, 0.6f, 0.5f));
        p.fillRectangle(8, 18, 10, 8);
        p.fillRectangle(28, 24, 10, 8);
        p.setColor(c(0.9f, 0.55f, 0.15f));
        p.fillRectangle(14, 6, 6, 6);
        p.fillRectangle(26, 6, 6, 6);
        return sprite("enemy-golem", p);
    }

    private Texture drawDarkMage() {
        int s = 34;
        Pixmap p = blank(s);
        Color robe = c(0.32f, 0.1f, 0.42f);
        p.setColor(robe);
        p.fillTriangle(17, 10, 4, 32, 30, 32);
        p.setColor(c(0.18f, 0.06f, 0.25f));
        p.fillTriangle(4, 14, 30, 14, 17, 1);
        p.fillRectangle(12, 10, 10, 4);
        p.setColor(c(0.75f, 0.55f, 0.7f));
        p.fillCircle(17, 16, 4);
        p.setColor(c(0.85f, 0.4f, 1f));
        p.fillCircle(26, 20, 4);
        return sprite("enemy-mage", p);
    }

    private Texture drawWyvern() {
        int s = 48;
        Pixmap p = blank(s);
        Color hide = c(0.32f, 0.58f, 0.28f);
        p.setColor(c(0.22f, 0.42f, 0.16f));
        p.fillTriangle(6, 24, 22, 26, 4, 2);
        p.fillTriangle(18, 22, 42, 16, 28, 1);
        p.setColor(hide);
        p.fillCircle(18, 30, 8);
        p.fillRectangle(22, 20, 14, 7);
        p.fillCircle(38, 18, 6);
        p.setColor(c(0.8f, 0.82f, 0.35f));
        p.fillTriangle(42, 14, 47, 18, 42, 22);
        p.setColor(hide);
        p.fillTriangle(1, 30, 14, 26, 2, 44);
        p.setColor(darken(hide, 0.55f));
        p.fillRectangle(14, 36, 5, 10);
        p.setColor(c(0.08f, 0.1f, 0.05f));
        p.fillCircle(40, 16, 2);
        return sprite("enemy-wyvern", p);
    }

    private Texture drawSoldier() {
        int s = 32;
        Pixmap p = blank(s);
        Color steel = c(0.55f, 0.58f, 0.65f);
        Color cloth = c(0.22f, 0.38f, 0.62f);
        p.setColor(c(0.2f, 0.18f, 0.15f));
        p.fillRectangle(10, 26, 4, 6);
        p.fillRectangle(18, 26, 4, 6);
        p.setColor(cloth);
        p.fillRectangle(10, 14, 12, 14);
        p.setColor(steel);
        p.fillCircle(16, 9, 7);
        p.setColor(c(0.12f, 0.1f, 0.08f));
        p.fillTriangle(1, 10, 14, 13, 3, 30);
        p.setColor(c(0.62f, 0.7f, 0.85f));
        p.fillTriangle(3, 12, 12, 14, 5, 26);
        p.setColor(c(0.78f, 0.62f, 0.22f));
        p.fillRectangle(22, 8, 4, 16);
        p.fillTriangle(21, 6, 27, 6, 24, 2);
        p.setColor(darken(steel, 0.5f));
        p.fillRectangle(12, 8, 8, 3);
        p.setColor(c(0.85f, 0.2f, 0.18f));
        p.fillRectangle(14, 16, 6, 8);
        return sprite("soldier", p);
    }

    private Texture drawArrow() {
        Pixmap p = blank(18);
        p.setColor(c(0.55f, 0.38f, 0.18f));
        p.fillRectangle(2, 7, 12, 4);
        p.setColor(c(0.75f, 0.75f, 0.7f));
        p.fillTriangle(12, 4, 12, 14, 17, 9);
        p.setColor(c(0.7f, 0.2f, 0.15f));
        p.fillTriangle(0, 5, 0, 13, 4, 9);
        return sprite("proj-arrow", p);
    }

    private Texture drawSpear() {
        Pixmap p = blank(22);
        p.setColor(c(0.5f, 0.36f, 0.18f));
        p.fillRectangle(0, 8, 14, 5);
        p.setColor(c(0.7f, 0.72f, 0.75f));
        p.fillTriangle(12, 4, 12, 18, 21, 11);
        return sprite("proj-bolt", p);
    }

    private Texture drawDiamond(String name, int size, Color fill, Color hi) {
        Pixmap p = blank(size);
        int m = size / 2;
        p.setColor(fill);
        p.fillTriangle(m, 1, 1, m, size - 2, m);
        p.fillTriangle(m, size - 2, 1, m, size - 2, m);
        p.setColor(hi);
        p.fillTriangle(m, 4, 6, m, m, m);
        return sprite(name, p);
    }

    private Texture drawFlame(int size) {
        Pixmap p = blank(size);
        p.setColor(c(1f, 0.4f, 0.05f));
        p.fillTriangle(size / 2, 1, 2, size - 2, size - 3, size - 2);
        p.setColor(c(1f, 0.85f, 0.25f));
        p.fillTriangle(size / 2, 5, 5, size - 3, size - 6, size - 3);
        return sprite("proj-fire", p);
    }

    private Texture drawBoltSpark(int size) {
        Pixmap p = blank(size);
        p.setColor(c(1f, 0.95f, 0.35f));
        p.fillTriangle(size / 2, 1, 4, size / 2, size / 2 + 2, size / 2);
        p.fillTriangle(size / 2 - 2, size / 2, size - 3, size / 2, size / 2, size - 2);
        return sprite("proj-spark", p);
    }

    private Texture createGrassTile(int size) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        Color base = c(0.22f, 0.38f, 0.18f);
        p.setColor(base); p.fill();
        java.util.Random rng = new java.util.Random(42);
        for (int i = 0; i < 30; i++) {
            float v = 0.92f + rng.nextFloat() * 0.16f;
            p.setColor(base.r * v, base.g * v, base.b * v, 1f);
            int gx = rng.nextInt(size);
            int gy = rng.nextInt(size);
            p.fillRectangle(gx, gy, 2 + rng.nextInt(3), 1);
        }
        return finish(p);
    }

    private Texture createPathTile(int size) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        Color base = c(0.55f, 0.42f, 0.28f);
        p.setColor(base); p.fill();
        java.util.Random rng = new java.util.Random(77);
        for (int i = 0; i < 20; i++) {
            float v = 0.9f + rng.nextFloat() * 0.2f;
            p.setColor(base.r * v, base.g * v, base.b * v, 1f);
            int gx = rng.nextInt(size);
            int gy = rng.nextInt(size);
            p.fillCircle(gx, gy, 1 + rng.nextInt(2));
        }
        p.setColor(darken(base, 0.7f));
        p.fillRectangle(0, 0, size, 2);
        p.fillRectangle(0, size - 2, size, 2);
        return finish(p);
    }

    private Color darken(Color col, float factor) {
        return new Color(col.r * factor, col.g * factor, col.b * factor, col.a);
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
        for (Texture t : owned) t.dispose();
        owned.clear();
    }
}
