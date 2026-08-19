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
    private static final float NODE_W = 140, NODE_H = 52;

    private final MedievalTDGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(W, H, camera);
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont titleFont, font, smallFont;
    private final GlyphLayout layout = new GlyphLayout();
    private final ResearchState research;
    private final ResearchTree tree;
    private ResearchNode hovered;
    private ResearchNode selected;

    public ResearchScreen(MedievalTDGame game) {
        this.game = game;
        this.research = game.getResearch();
        this.tree = research.getTree();
        titleFont = game.createFont(32);
        font = game.createFont(15);
        smallFont = game.createFont(12);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(GameColors.BACKGROUND.r, GameColors.BACKGROUND.g, GameColors.BACKGROUND.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply(); camera.update();

        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        hovered = null;
        for (ResearchNode node : tree.all()) {
            if (hitTest(node, mouse)) { hovered = node; break; }
        }

        // Draw connection lines
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (ResearchNode node : tree.all()) {
            for (ResearchId reqId : node.requires) {
                ResearchNode req = tree.get(reqId);
                if (req == null) continue;
                boolean reqDone = research.isUnlocked(reqId);
                boolean nodeDone = research.isUnlocked(node.id);
                if (reqDone && nodeDone) shapes.setColor(GameColors.UI_GOLD);
                else if (reqDone) shapes.setColor(0.5f, 0.5f, 0.5f, 0.8f);
                else shapes.setColor(0.3f, 0.3f, 0.3f, 0.6f);
                shapes.line(
                    req.treeX + NODE_W / 2, req.treeY + NODE_H,
                    node.treeX + NODE_W / 2, node.treeY
                );
            }
        }
        shapes.end();

        // Draw node backgrounds
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (ResearchNode node : tree.all()) {
            boolean unlocked = research.isUnlocked(node.id);
            boolean available = research.canUnlock(node.id);
            boolean isHov = node == hovered;
            boolean isSel = node == selected;

            if (unlocked) {
                shapes.setColor(0.2f, 0.45f, 0.2f, 1f);
            } else if (available) {
                shapes.setColor(isHov ? 0.4f : 0.28f, isHov ? 0.3f : 0.22f, isHov ? 0.12f : 0.08f, 1f);
            } else {
                shapes.setColor(0.15f, 0.12f, 0.1f, 1f);
            }
            shapes.rect(node.treeX, node.treeY, NODE_W, NODE_H);

            // Border
            if (isSel) shapes.setColor(GameColors.UI_GOLD);
            else if (unlocked) shapes.setColor(0.3f, 0.7f, 0.3f, 1f);
            else if (available) shapes.setColor(GameColors.UI_BORDER);
            else shapes.setColor(0.3f, 0.25f, 0.2f, 1f);
            shapes.rect(node.treeX, node.treeY, NODE_W, 2);
            shapes.rect(node.treeX, node.treeY + NODE_H - 2, NODE_W, 2);
            shapes.rect(node.treeX, node.treeY, 2, NODE_H);
            shapes.rect(node.treeX + NODE_W - 2, node.treeY, 2, NODE_H);
        }
        shapes.end();

        // Draw node text
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        titleFont.setColor(GameColors.UI_GOLD);
        layout.setText(titleFont, "Исследования");
        titleFont.draw(batch, "Исследования", W / 2f - layout.width / 2, H - 30);

        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, "Монет науки: " + research.getGold(), 30, H - 35);
        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "ESC / кнопка — в меню", 30, H - 60);

        for (ResearchNode node : tree.all()) {
            boolean unlocked = research.isUnlocked(node.id);
            boolean available = research.canUnlock(node.id);

            font.setColor(unlocked ? new com.badlogic.gdx.graphics.Color(0.5f, 1f, 0.5f, 1f)
                : available ? GameColors.UI_TEXT
                : new com.badlogic.gdx.graphics.Color(0.5f, 0.45f, 0.4f, 1f));
            font.draw(batch, node.name, node.treeX + 8, node.treeY + 36);
            smallFont.setColor(available && !unlocked ? GameColors.UI_GOLD
                : new com.badlogic.gdx.graphics.Color(0.6f, 0.6f, 0.6f, 0.8f));
            String label = unlocked ? "✓" : (node.cost + "M");
            smallFont.draw(batch, label, node.treeX + 8, node.treeY + 18);
        }

        // Sidebar: selected node details
        if (selected != null) {
            renderSidebar(selected);
        }

        // Back button
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(GameColors.UI_PANEL);
        shapes.rect(W - 190, 20, 170, 44);
        shapes.end();

        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "← Меню", W - 175, 50);

        batch.end();

        handleInput(mouse);
    }

    private void renderSidebar(ResearchNode node) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(GameColors.UI_PANEL);
        shapes.rect(W - 290, 80, 280, 280);
        shapes.setColor(GameColors.UI_BORDER);
        shapes.rect(W - 290, 80, 280, 2);
        shapes.rect(W - 290, 80, 2, 280);
        shapes.end();

        boolean unlocked = research.isUnlocked(node.id);
        boolean available = research.canUnlock(node.id);

        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, node.name, W - 278, 346);
        smallFont.setColor(GameColors.UI_TEXT);
        smallFont.draw(batch, node.description, W - 278, 318);
        smallFont.draw(batch, "Стоимость: " + node.cost + " монет", W - 278, 294);

        if (!node.requires.isEmpty()) {
            smallFont.setColor(0.7f, 0.7f, 0.7f, 1f);
            smallFont.draw(batch, "Требует:", W - 278, 270);
            int ry = 250;
            for (ResearchId req : node.requires) {
                ResearchNode rn = tree.get(req);
                boolean done = research.isUnlocked(req);
                smallFont.setColor(done ? new com.badlogic.gdx.graphics.Color(0.4f, 0.9f, 0.4f, 1f)
                    : GameColors.UI_HEALTH);
                if (rn != null) smallFont.draw(batch, (done ? "✓ " : "✗ ") + rn.name, W - 270, ry);
                ry -= 18;
            }
        }

        if (unlocked) {
            font.setColor(new com.badlogic.gdx.graphics.Color(0.4f, 1f, 0.4f, 1f));
            font.draw(batch, "Исследовано", W - 278, 140);
        } else if (available) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0.3f, 0.55f, 0.25f, 1f);
            shapes.rect(W - 278, 95, 140, 36);
            shapes.end();
            font.setColor(GameColors.UI_TEXT);
            font.draw(batch, "Изучить (" + node.cost + "M)", W - 274, 120);
        } else {
            font.setColor(GameColors.UI_HEALTH);
            font.draw(batch, "Недоступно", W - 278, 120);
        }
    }

    private boolean hitTest(ResearchNode node, Vector2 pos) {
        return pos.x >= node.treeX && pos.x <= node.treeX + NODE_W
            && pos.y >= node.treeY && pos.y <= node.treeY + NODE_H;
    }

    private void handleInput(Vector2 mouse) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.returnToMenu(); return;
        }
        if (!Gdx.input.justTouched()) return;

        // Back button
        if (mouse.x >= W - 190 && mouse.x <= W - 20 && mouse.y >= 20 && mouse.y <= 64) {
            game.returnToMenu(); return;
        }

        for (ResearchNode node : tree.all()) {
            if (hitTest(node, mouse)) {
                if (node == selected && research.canUnlock(node.id)) {
                    research.unlock(node.id);
                } else {
                    selected = node;
                }
                return;
            }
        }

        // Click buy button in sidebar
        if (selected != null && research.canUnlock(selected.id)) {
            if (mouse.x >= W - 278 && mouse.x <= W - 138 && mouse.y >= 95 && mouse.y <= 131) {
                research.unlock(selected.id);
            }
        }
    }

    @Override
    public void resize(int w, int h) { viewport.update(w, h); }

    @Override
    public void dispose() {
        batch.dispose(); shapes.dispose(); titleFont.dispose(); font.dispose(); smallFont.dispose();
    }
}
