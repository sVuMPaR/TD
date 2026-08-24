package com.medievaltd.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.medievaltd.util.Smoke;

/** Sequential campaign unlocks. Map 1 is always open. */
public class CampaignProgress {
    private static final String PREFS = "medievaltd-progress";

    public static boolean isUnlocked(int menuIndex) {
        return menuIndex <= highestBeatenMap();
    }

    public static int highestBeatenMap() {
        if (Gdx.app == null) return 0;
        return Gdx.app.getPreferences(PREFS).getInteger("beaten", 0);
    }

    public static void recordVictory(int mapIndex) {
        if (Gdx.app == null || Smoke.on()) return;
        if (mapIndex < 1 || mapIndex > 4) return;
        Preferences prefs = Gdx.app.getPreferences(PREFS);
        int beaten = Math.max(prefs.getInteger("beaten", 0), mapIndex);
        prefs.putInteger("beaten", beaten);
        prefs.flush();
    }
}
