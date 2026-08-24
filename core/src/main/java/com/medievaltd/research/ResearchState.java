package com.medievaltd.research;

import com.medievaltd.model.DamageType;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.medievaltd.util.Smoke;

import java.util.EnumMap;
import java.util.Map;

public class ResearchState {
    public static final int MAX_LEVEL = 10;
    private static final String PREFS_NAME = "medievaltd-research";

    private int accumulatedGold;
    private final Map<ResearchId, Integer> levels = new EnumMap<>(ResearchId.class);
    private final ResearchTree tree = new ResearchTree();

    public void addLevelReward(int gold) {
        if (Smoke.on()) return;
        accumulatedGold += gold;
        persist();
    }

    public int getLevel(ResearchId id) {
        return levels.getOrDefault(id, 0);
    }

    public boolean isUnlocked(ResearchId id) {
        return getLevel(id) > 0;
    }

    public boolean isMaxed(ResearchId id) {
        return getLevel(id) >= MAX_LEVEL;
    }

    /** Cost to go from current level to next level */
    public int getUpgradeCost(ResearchId id) {
        ResearchNode node = tree.get(id);
        if (node == null) return Integer.MAX_VALUE;
        int currentLevel = getLevel(id);
        if (currentLevel >= MAX_LEVEL) return 0;
        // Each subsequent level costs +40% more than base
        return Math.round(node.baseCost * (1f + currentLevel * 0.4f));
    }

    public boolean canUpgrade(ResearchId id) {
        ResearchNode node = tree.get(id);
        if (node == null || isMaxed(id)) return false;
        if (accumulatedGold < getUpgradeCost(id)) return false;
        // Prerequisites: must be at least level 1
        return node.requires.stream().allMatch(this::isUnlocked);
    }

    public boolean upgrade(ResearchId id) {
        if (!canUpgrade(id)) return false;
        accumulatedGold -= getUpgradeCost(id);
        levels.merge(id, 1, Integer::sum);
        persist();
        return true;
    }

    public void load() {
        if (Gdx.app == null) return;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        accumulatedGold = prefs.getInteger("scienceGold", 0);
        levels.clear();
        for (ResearchId id : ResearchId.values()) {
            int lv = prefs.getInteger("res_" + id.name(), 0);
            if (lv > 0) levels.put(id, Math.min(lv, MAX_LEVEL));
        }
    }

    public void save() {
        persist();
    }

    private void persist() {
        if (Gdx.app == null) return;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putInteger("scienceGold", accumulatedGold);
        for (ResearchId id : ResearchId.values()) {
            prefs.putInteger("res_" + id.name(), getLevel(id));
        }
        prefs.flush();
    }

    public int getGold() {
        return accumulatedGold;
    }

    public ResearchTree getTree() {
        return tree;
    }

    // ---- Computed bonuses (scale linearly to max at level 10) ----

    private float scaled(ResearchId id, float maxValue) {
        return maxValue * getLevel(id) / (float) MAX_LEVEL;
    }

    public float getDamageMultiplier() {
        float m = 1f;
        m += scaled(ResearchId.DAMAGE_BOOST, 0.10f);
        m += scaled(ResearchId.DAMAGE_BOOST_2, 0.15f);
        return m;
    }

    public float getSpeedMultiplier() {
        // Lower = faster fire rate
        return 1f - scaled(ResearchId.SPEED_BOOST, 0.08f);
    }

    public float getRangeMultiplier() {
        return 1f + scaled(ResearchId.RANGE_BOOST, 0.10f);
    }

    /** Hook for later "increased accuracy" research. 1 = no bonus. */
    public float getAccuracyMultiplier() {
        return 1f;
    }

    public float getGoldMultiplier() {
        float m = 1f;
        m += scaled(ResearchId.GOLD_BONUS, 0.15f);
        m += scaled(ResearchId.GOLD_BONUS_2, 0.20f);
        return m;
    }

    public float getPenetration(DamageType dt) {
        float pen = scaled(ResearchId.ALL_PEN, 0.10f);
        pen += switch (dt) {
            case PHYSICAL -> scaled(ResearchId.PHYSICAL_PEN, 0.15f);
            case FIRE -> scaled(ResearchId.FIRE_PEN, 0.15f);
            case ICE -> scaled(ResearchId.ICE_PEN, 0.15f);
            case LIGHTNING -> scaled(ResearchId.LIGHTNING_PEN, 0.15f);
            case MAGIC -> scaled(ResearchId.MAGIC_PEN, 0.15f);
        };
        return pen;
    }

    public float getBonusVsBosses() {
        return 1f + scaled(ResearchId.BONUS_VS_BOSSES, 0.20f);
    }

    public float getBonusVsSlowed() {
        return 1f + scaled(ResearchId.BONUS_VS_SLOWED, 0.15f);
    }

    public float getBonusVsBlocked() {
        return 1f + scaled(ResearchId.BONUS_VS_BLOCKED, 0.15f);
    }
}
