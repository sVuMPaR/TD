package com.medievaltd.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public final class Settings {
    private static final String PREFS = "medievaltd-settings";
    private static boolean sound = true;
    private static boolean tutorialDone;
    private static int speed = 1;

    private Settings() {}

    public static void load() {
        if (Gdx.app == null) return;
        Preferences prefs = Gdx.app.getPreferences(PREFS);
        sound = prefs.getBoolean("sound", true);
        tutorialDone = prefs.getBoolean("tutorial", false);
        speed = prefs.getInteger("speed", 1);
        if (speed != 1 && speed != 2 && speed != 4) speed = 1;
    }

    public static boolean sound() { return sound; }

    public static void toggleSound() {
        sound = !sound;
        persist();
    }

    public static int speed() { return speed; }

    public static void setSpeed(int value) {
        if (value != 1 && value != 2 && value != 4) return;
        speed = value;
        persist();
    }

    public static void cycleSpeed() {
        setSpeed(speed == 1 ? 2 : speed == 2 ? 4 : 1);
    }

    public static boolean tutorialDone() { return tutorialDone; }

    public static void markTutorialDone() {
        if (tutorialDone) return;
        tutorialDone = true;
        persist();
    }

    private static void persist() {
        if (Gdx.app == null || Smoke.on()) return;
        Preferences prefs = Gdx.app.getPreferences(PREFS);
        prefs.putBoolean("sound", sound);
        prefs.putBoolean("tutorial", tutorialDone);
        prefs.putInteger("speed", speed);
        prefs.flush();
    }
}
