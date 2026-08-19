package com.medievaltd.research;

import com.medievaltd.model.DamageType;

import java.util.EnumSet;
import java.util.Set;

public class ResearchState {
    private int accumulatedGold;
    private final Set<ResearchId> unlocked = EnumSet.noneOf(ResearchId.class);
    private final ResearchTree tree = new ResearchTree();

    public void addLevelReward(int gold) {
        accumulatedGold += gold;
    }

    public boolean canUnlock(ResearchId id) {
        ResearchNode node = tree.get(id);
        if (node == null || unlocked.contains(id)) return false;
        if (accumulatedGold < node.cost) return false;
        return unlocked.containsAll(node.requires);
    }

    public boolean unlock(ResearchId id) {
        if (!canUnlock(id)) return false;
        accumulatedGold -= tree.get(id).cost;
        unlocked.add(id);
        return true;
    }

    public boolean isUnlocked(ResearchId id) {
        return unlocked.contains(id);
    }

    public int getGold() {
        return accumulatedGold;
    }

    public ResearchTree getTree() {
        return tree;
    }

    // ---- Computed bonuses ----

    public float getDamageMultiplier() {
        float m = 1f;
        if (isUnlocked(ResearchId.DAMAGE_BOOST)) m += 0.10f;
        if (isUnlocked(ResearchId.DAMAGE_BOOST_2)) m += 0.15f;
        return m;
    }

    public float getSpeedMultiplier() {
        float m = 1f;
        if (isUnlocked(ResearchId.SPEED_BOOST)) m -= 0.08f; // lower cooldown = faster
        return m;
    }

    public float getRangeMultiplier() {
        float m = 1f;
        if (isUnlocked(ResearchId.RANGE_BOOST)) m += 0.10f;
        return m;
    }

    public float getGoldMultiplier() {
        float m = 1f;
        if (isUnlocked(ResearchId.GOLD_BONUS)) m += 0.15f;
        if (isUnlocked(ResearchId.GOLD_BONUS_2)) m += 0.20f;
        return m;
    }

    /** How much to subtract from enemy resistance for a given type (0..1) */
    public float getPenetration(DamageType dt) {
        float pen = 0f;
        if (isUnlocked(ResearchId.ALL_PEN)) pen += 0.10f;
        pen += switch (dt) {
            case PHYSICAL -> isUnlocked(ResearchId.PHYSICAL_PEN) ? 0.15f : 0f;
            case FIRE -> isUnlocked(ResearchId.FIRE_PEN) ? 0.15f : 0f;
            case ICE -> isUnlocked(ResearchId.ICE_PEN) ? 0.15f : 0f;
            case LIGHTNING -> isUnlocked(ResearchId.LIGHTNING_PEN) ? 0.15f : 0f;
            case MAGIC -> isUnlocked(ResearchId.MAGIC_PEN) ? 0.15f : 0f;
        };
        return pen;
    }

    public float getBonusVsBosses() {
        return isUnlocked(ResearchId.BONUS_VS_BOSSES) ? 1.20f : 1f;
    }

    public float getBonusVsSlowed() {
        return isUnlocked(ResearchId.BONUS_VS_SLOWED) ? 1.15f : 1f;
    }

    public float getBonusVsBlocked() {
        return isUnlocked(ResearchId.BONUS_VS_BLOCKED) ? 1.15f : 1f;
    }
}
