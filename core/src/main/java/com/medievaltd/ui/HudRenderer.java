package com.medievaltd.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.medievaltd.model.TowerType;
import com.medievaltd.util.Assets;
import com.medievaltd.util.GameColors;

public class HudRenderer {
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private final Rectangle waveButton = new Rectangle(1050, 20, 200, 50);
    private final Rectangle[] towerButtons = new Rectangle[3];
    private final Rectangle upgradeButton = new Rectangle(1050, 580, 95, 40);
    private final Rectangle sellButton = new Rectangle(1155, 580, 95, 40);
    private final Rectangle menuButton = new Rectangle(20, 20, 120, 40);

    public HudRenderer(BitmapFont font) {
        this.font = font;
        for (int i = 0; i < towerButtons.length; i++) {
            towerButtons[i] = new Rectangle(20, 520 - i * 70, 220, 60);
        }
    }

    public void renderPanelBackgrounds(ShapeRenderer shapes, float width, float height) {
        shapes.setColor(GameColors.UI_PANEL);
        shapes.rect(0, 0, width, 80);
        shapes.rect(0, height - 90, 240, 90);
        shapes.rect(width - 270, 0, 270, 90);
        shapes.rect(width - 270, height - 90, 270, 90);
        shapes.setColor(GameColors.UI_BORDER);
        shapes.rect(0, 78, width, 2);
        shapes.rect(238, height - 90, 2, 90);
        shapes.rect(width - 270, 0, 2, 90);
        shapes.rect(width - 270, height - 90, 2, 90);
    }

    public void renderButtons(ShapeRenderer shapes, Assets assets, TowerType selectedType, boolean canStartWave) {
        shapes.setColor(canStartWave ? new com.badlogic.gdx.graphics.Color(0.3f, 0.5f, 0.25f, 1f) : GameColors.UI_PANEL);
        shapes.rect(waveButton.x, waveButton.y, waveButton.width, waveButton.height);

        TowerType[] types = TowerType.values();
        for (int i = 0; i < types.length; i++) {
            if (types[i] == selectedType) {
                shapes.setColor(GameColors.UI_BORDER);
            } else {
                shapes.setColor(0.18f, 0.14f, 0.1f, 0.9f);
            }
            shapes.rect(towerButtons[i].x, towerButtons[i].y, towerButtons[i].width, towerButtons[i].height);
        }

        shapes.setColor(0.25f, 0.2f, 0.15f, 1f);
        shapes.rect(upgradeButton.x, upgradeButton.y, upgradeButton.width, upgradeButton.height);
        shapes.rect(sellButton.x, sellButton.y, sellButton.width, sellButton.height);
        shapes.rect(menuButton.x, menuButton.y, menuButton.width, menuButton.height);
    }

    public void renderText(SpriteBatch batch, int gold, int lives, int wave, int totalWaves,
                           boolean canStartWave, TowerType selectedType) {
        font.setColor(GameColors.UI_GOLD);
        font.draw(batch, "Золото: " + gold, 20, 55);
        font.setColor(GameColors.UI_HEALTH);
        font.draw(batch, "Жизни: " + lives, 200, 55);
        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "Волна: " + Math.max(wave, 0) + " / " + totalWaves, 420, 55);

        font.setColor(canStartWave ? GameColors.UI_GOLD : GameColors.UI_TEXT);
        String waveLabel = canStartWave ? "След. волна" : "Бой...";
        layout.setText(font, waveLabel);
        font.draw(batch, waveLabel, waveButton.x + (waveButton.width - layout.width) / 2, waveButton.y + 32);

        TowerType[] types = TowerType.values();
        for (int i = 0; i < types.length; i++) {
            TowerType type = types[i];
            Rectangle btn = towerButtons[i];
            font.setColor(gold >= type.baseCost ? GameColors.UI_TEXT : GameColors.UI_HEALTH);
            font.draw(batch, type.displayName + " (" + type.baseCost + ")", btn.x + 10, btn.y + 42);
            font.getData().setScale(0.65f);
            font.setColor(GameColors.UI_TEXT);
            font.draw(batch, type.description, btn.x + 10, btn.y + 18);
            font.getData().setScale(1f);
        }

        font.setColor(GameColors.UI_TEXT);
        font.draw(batch, "Меню", menuButton.x + 28, menuButton.y + 28);
        font.draw(batch, "Улучш.", upgradeButton.x + 10, upgradeButton.y + 28);
        font.draw(batch, "Продать", sellButton.x + 8, sellButton.y + 28);
    }

    public Rectangle getWaveButton() { return waveButton; }
    public Rectangle getTowerButton(int index) { return towerButtons[index]; }
    public Rectangle getUpgradeButton() { return upgradeButton; }
    public Rectangle getSellButton() { return sellButton; }
    public Rectangle getMenuButton() { return menuButton; }
}
