package com.medievaltd.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.medievaltd.MedievalTDGame;
import com.medievaltd.research.ResearchId;
import com.medievaltd.research.ResearchNode;
import com.medievaltd.research.ResearchState;
import com.medievaltd.research.ResearchTree;
import com.medievaltd.util.GameColors;

public class ResearchScreen extends ScreenAdapter {
    private static final float W = 1280, H = 720;
    private static final float NW = 140, NH = 56;

    private final MedievalTDGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(W, H, camera);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont titleFont, font, small;
    private final GlyphLayout layout = new GlyphLayout();
    private final ResearchState research;
    private final ResearchTree tree;
    private ResearchNode selected;

    public ResearchScreen(MedievalTDGame game) {
        this.game = game;
        this.research = game.getResearch();
        this.tree = research.getTree();
        titleFont = game.createFont(30);
        font = game.createFont(14);
        small = game.createFont(11);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(GameColors.BACKGROUND.r, GameColors.BACKGROUND.g, GameColors.BACKGROUND.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        camera.update();

        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        // Connection lines
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (ResearchNode node : tree.all()) {
            for (ResearchId reqId : node.requires) {
                ResearchNode req = tree.get(reqId);
                if (req == null) continue;
                boolean rd = research.isUnlocked(reqId);
                boolean nd = research.isUnlocked(node.id);
                shapes.setColor(rd && nd ? GameColors.UI_GOLD :
                    rd ? new Color(0.5f, 0.5f, 0.5f, 0.8f) :
                        new Color(0.25f, 0.25f, 0.25f, 0.5f));
                shapes.line(req.treeX + NW / 2, req.treeY + NH,
                    node.treeX + NW / 2, node.treeY);
            }
        }
        shapes.end();

        // Node boxes
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (ResearchNode node : tree.all()) {
            int lv = research.getLevel(node.id);
            boolean max = research.isMaxed(node.id);
            boolean canUp = research.canUpgrade(node.id);
            boolean hov = hitTest(node, mouse);
            boolean sel = node == selected;

            if (max) shapes.setColor(0.18f, 0.42f, 0.18f, 1f);
            else if (canUp) shapes.setColor(hov ? 0.38f : 0.26f, hov ? 0.28f : 0.20f, 0.1f, 1f);
            else if (lv > 0) shapes.setColor(0.22f, 0.30f, 0.18f, 1f);
            else shapes.setColor(0.14f, 0.12f, 0.1f, 1f);
            shapes.rect(node.treeX, node.treeY, NW, NH);

            // Border
            Color bc = sel ? GameColors.UI_GOLD :
                max ? new Color(0.3f, 0.7f, 0.3f, 1f) :
                    canUp ? GameColors.UI_BORDER : new Color(0.25f, 0.22f, 0.18f, 1f);
            shapes.setColor(bc);
            shapes.rect(node.treeX, node.treeY, NW, 2);
            shapes.rect(node.treeX, node.treeY + NH - 2, NW, 2);
            shapes.rect(node.treeX, node.treeY, 2, NH);
            shapes.rect(node.treeX + NW - 2, node.treeY, 2, NH);

            // Level bar (10 segments)
            if (lv > 0) {
                float barY = node.treeY + 4;
                float barX = node.treeX + 6;
                float segW = (NW - 12) / 10f;
                for (int i = 0; i < 10; i++) {
                    shapes.setColor(i < lv ? new Color(0.4f, 0.8f, 0.3f, 0.9f) :
                        new Color(0.2f, 0.2f, 0.2f, 0.5f));
                    shapes.rect(barX + i * segW, barY, segW - 1, 4);
                }
            }
        }
        shapes.end();

        // Text
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        titleFont.setColor(GameColors.UI_GOLD);
        layout.setText(titleFont, "Древо исследований");
        titleFont.draw(batch, "Древо исследований", W / 2f - layout.width / 2, H - 25);

        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, "Монеты науки: " + research.getGold(), 30, H - 30);
        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "ESC — в меню", 30, H - 52);

        for (ResearchNode node : tree.all()) {
            int lv = research.getLevel(node.id);
            boolean canUp = research.canUpgrade(node.id);
            font.setColor(research.isMaxed(node.id) ? new Color(0.5f, 1f, 0.5f, 1f) :
                canUp ? GameColors.UI_TEXT : new Color(0.5f, 0.45f, 0.4f, 1f));
            font.draw(batch, node.name, node.treeX + 6, node.treeY + NH - 8);

            small.setColor(lv > 0 ? GameColors.UI_GOLD : new Color(0.5f, 0.5f, 0.5f, 0.7f));
            String lvText = lv >= ResearchState.MAX_LEVEL ? "MAX" : "ур." + lv + "/10";
            small.draw(batch, lvText, node.treeX + 6, node.treeY + 24);

            if (!research.isMaxed(node.id) && canUp) {
                small.setColor(GameColors.UI_TEXT);
                small.draw(batch, research.getUpgradeCost(node.id) + "M", node.treeX + NW - 40, node.treeY + 24);
            }
        }

        // Sidebar
        if (selected != null) {
            renderSidebar();
        }

        // Back button bg
        batch.end();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(GameColors.UI_PANEL);
        shapes.rect(W - 180, 20, 160, 42);
        shapes.end();
        batch.begin();
        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "← Меню", W - 168, 48);
        batch.end();

        handleInput(mouse);
    }

    private void renderSidebar() {
        ResearchNode n = selected;
        int lv = research.getLevel(n.id);
        boolean canUp = research.canUpgrade(n.id);
        boolean max = research.isMaxed(n.id);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(GameColors.UI_PANEL);
        shapes.rect(W - 300, 80, 290, 310);
        shapes.setColor(GameColors.UI_BORDER);
        shapes.rect(W - 300, 80, 290, 2);
        shapes.rect(W - 300, 388, 290, 2);
        shapes.rect(W - 300, 80, 2, 310);
        shapes.rect(W - 12, 80, 2, 310);
        shapes.end();

        batch.begin();
        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, n.name, W - 288, 376);
        small.setColor(GameColors.UI_TEXT);
        small.draw(batch, n.description, W - 288, 352);
        small.draw(batch, n.maxEffect, W - 288, 334);

        // Current value
        float pct = (float) lv / ResearchState.MAX_LEVEL * 100f;
        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "Уровень: " + lv + " / " + ResearchState.MAX_LEVEL + " (" + (int) pct + "%)", W - 288, 308);

        if (!max) {
            int cost = research.getUpgradeCost(n.id);
            font.draw(batch, "Следующий: " + cost + " монет", W - 288, 284);
        }

        if (!n.requires.isEmpty()) {
            small.setColor(0.65f, 0.65f, 0.65f, 1f);
            small.draw(batch, "Требует:", W - 288, 258);
            int ry = 240;
            for (ResearchId req : n.requires) {
                ResearchNode rn = tree.get(req);
                boolean done = research.isUnlocked(req);
                small.setColor(done ? new Color(0.4f, 0.9f, 0.4f, 1f) : GameColors.UI_HEALTH);
                if (rn != null) small.draw(batch, (done ? "✓ " : "✗ ") + rn.name, W - 280, ry);
                ry -= 16;
            }
        }

        if (max) {
            font.setColor(new Color(0.4f, 1f, 0.4f, 1f));
            font.draw(batch, "Максимум!", W - 288, 140);
        } else if (canUp) {
            batch.end();
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0.28f, 0.52f, 0.22f, 1f);
            shapes.rect(W - 288, 96, 150, 34);
            shapes.end();
            batch.begin();
            font.setColor(GameColors.UI_TEXT);
            font.draw(batch, "Улучшить (" + research.getUpgradeCost(n.id) + "M)", W - 284, 120);
        } else {
            font.setColor(GameColors.UI_HEALTH);
            font.draw(batch, max ? "" : "Недоступно", W - 288, 120);
        }
        batch.end();
    }

    private boolean hitTest(ResearchNode node, Vector2 pos) {
        return pos.x >= node.treeX && pos.x <= node.treeX + NW
            && pos.y >= node.treeY && pos.y <= node.treeY + NH;
    }

    private void handleInput(Vector2 mouse) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.returnToMenu();
            return;
        }
        if (!Gdx.input.justTouched()) return;

        // Back button
        if (mouse.x >= W - 180 && mouse.x <= W - 20 && mouse.y >= 20 && mouse.y <= 62) {
            game.returnToMenu();
            return;
        }

        // Buy button in sidebar
        if (selected != null && research.canUpgrade(selected.id)) {
            if (mouse.x >= W - 288 && mouse.x <= W - 138 && mouse.y >= 96 && mouse.y <= 130) {
                research.upgrade(selected.id);
                return;
            }
        }

        for (ResearchNode node : tree.all()) {
            if (hitTest(node, mouse)) {
                if (node == selected && research.canUpgrade(node.id)) {
                    research.upgrade(node.id);
                } else {
                    selected = node;
                }
                return;
            }
        }
    }

    @Override
    public void resize(int w, int h) { viewport.update(w, h); }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        titleFont.dispose();
        font.dispose();
        small.dispose();
    }
}
