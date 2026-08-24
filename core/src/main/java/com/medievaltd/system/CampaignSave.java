package com.medievaltd.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.medievaltd.entity.Tower;
import com.medievaltd.model.Difficulty;
import com.medievaltd.model.GameLevel;
import com.medievaltd.model.SurvivalMap;
import com.medievaltd.model.TempleUpgradeChoice;
import com.medievaltd.model.TowerType;
import com.medievaltd.util.Smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Save between waves: towers, gold, lives. Mid-wave enemies are not kept. */
public class CampaignSave {
    private static final String PREFS = "medievaltd-campaign";

    public static class Run {
        public int levelIndex;
        public Difficulty difficulty;
        public int gold;
        public int lives;
        public int completedWave;
        public final List<TowerRec> towers = new ArrayList<>();
    }

    public static class TowerRec {
        public TowerType type;
        public int level;
        public float x;
        public float y;
        public TempleUpgradeChoice temple;
    }

    public static boolean hasSave() {
        return load() != null;
    }

    public static Run load() {
        if (Gdx.app == null) return null;
        Preferences prefs = Gdx.app.getPreferences(PREFS);
        if (!prefs.getBoolean("active", false)) return null;
        try {
            Run run = new Run();
            run.levelIndex = prefs.getInteger("levelIndex", 0);
            int diff = prefs.getInteger("difficulty", 1);
            if (diff < 0 || diff >= Difficulty.values().length) return null;
            run.difficulty = Difficulty.values()[diff];
            run.gold = prefs.getInteger("gold", 0);
            run.lives = prefs.getInteger("lives", 1);
            run.completedWave = prefs.getInteger("completedWave", -1);
            if (run.lives <= 0 || run.gold < 0) return null;
            int totalWaves = run.levelIndex < 0 ? SurvivalMap.TOTAL_WAVES
                : GameLevel.createLevels().get(Math.max(0, Math.min(3, run.levelIndex))).totalWaves;
            if (run.completedWave + 1 >= totalWaves) {
                clear();
                return null;
            }
            parseTowers(prefs.getString("towers", ""), run);
            return run;
        } catch (Exception e) {
            Gdx.app.error("CampaignSave", "Corrupt save", e);
            clear();
            return null;
        }
    }

    public static void write(GameSession session) {
        if (Gdx.app == null || Smoke.on()) return;
        if (session.getResult() != GameSession.Result.PLAYING) {
            clear();
            return;
        }
        Preferences prefs = Gdx.app.getPreferences(PREFS);
        prefs.putBoolean("active", true);
        prefs.putInteger("levelIndex", session.getLevelIndex());
        prefs.putInteger("difficulty", session.getDifficulty().ordinal());
        prefs.putInteger("gold", session.getGold());
        prefs.putInteger("lives", session.getLives());
        prefs.putInteger("completedWave", session.getWaveManager().getCompletedWaveIndex());
        prefs.putString("towers", encodeTowers(session.getTowers()));
        prefs.flush();
    }

    public static void clear() {
        if (Gdx.app == null || Smoke.on()) return;
        Preferences prefs = Gdx.app.getPreferences(PREFS);
        prefs.clear();
        prefs.flush();
    }

    public static String label(Run run) {
        String map = run.levelIndex < 0 ? "Выживание" : switch (run.levelIndex) {
            case 0 -> "Застава Лесного Короля";
            case 1 -> "Мост через пропасть";
            case 2 -> "Замёрзшее ущелье";
            case 3 -> "Врата Тьмы";
            default -> "Карта " + (run.levelIndex + 1);
        };
        int nextWave = Math.max(1, run.completedWave + 2);
        return "Продолжить: " + map + " · " + run.difficulty.displayName + " · волна " + nextWave;
    }

    private static String encodeTowers(List<Tower> towers) {
        StringBuilder sb = new StringBuilder();
        for (Tower t : towers) {
            if (sb.length() > 0) sb.append('|');
            String choice = t.getTempleChoice() == null ? "" : t.getTempleChoice().name();
            sb.append(t.getType().name()).append(',')
                .append(t.getLevel()).append(',')
                .append(String.format(Locale.US, "%.1f", t.getPosition().x)).append(',')
                .append(String.format(Locale.US, "%.1f", t.getPosition().y)).append(',')
                .append(choice);
        }
        return sb.toString();
    }

    private static void parseTowers(String raw, Run run) {
        if (raw == null || raw.isEmpty()) return;
        for (String part : raw.split("\\|")) {
            if (part.isBlank()) continue;
            String[] f = part.split(",", -1);
            if (f.length < 4) continue;
            TowerRec rec = new TowerRec();
            rec.type = TowerType.valueOf(f[0]);
            rec.level = Math.max(1, Math.min(3, Integer.parseInt(f[1])));
            rec.x = Float.parseFloat(f[2]);
            rec.y = Float.parseFloat(f[3]);
            if (f.length >= 5 && !f[4].isEmpty()) {
                rec.temple = TempleUpgradeChoice.valueOf(f[4]);
            }
            run.towers.add(rec);
        }
    }
}
