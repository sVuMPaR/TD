package com.medievaltd.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.medievaltd.model.TowerType;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/** Tiny procedural WAV bank. No external audio files. */
public class Sfx {
    private Sound shoot;
    private Sound hit;
    private Sound miss;
    private Sound place;
    private Sound upgrade;
    private Sound sell;
    private Sound wave;
    private Sound leak;
    private Sound win;
    private Sound lose;
    private Sound click;
    private Sound pause;
    private boolean ready;

    public void load() {
        try {
            shoot = fromWav("shoot", punch(520, 0.055f, 0.4f));
            hit = fromWav("hit", noiseBurst(0.05f, 0.32f));
            miss = fromWav("miss", chirp(280, 140, 0.08f, 0.22f));
            place = fromWav("place", twoTone(320, 480, 0.07f, 0.3f));
            upgrade = fromWav("upgrade", twoTone(520, 780, 0.08f, 0.28f));
            sell = fromWav("sell", chirp(360, 160, 0.1f, 0.28f));
            wave = fromWav("wave", twoTone(220, 330, 0.12f, 0.34f));
            leak = fromWav("leak", chirp(180, 70, 0.18f, 0.42f));
            win = fromWav("win", twoTone(523, 784, 0.16f, 0.3f));
            lose = fromWav("lose", chirp(160, 70, 0.32f, 0.4f));
            click = fromWav("click", squareBurst(900, 0.03f, 0.18f));
            pause = fromWav("pause", twoTone(300, 220, 0.07f, 0.26f));
            ready = true;
        } catch (Exception e) {
            Gdx.app.error("Sfx", "Audio unavailable", e);
            ready = false;
        }
    }

    public void shoot(TowerType type) {
        float pitch = switch (type) {
            case ARCHER -> 1.25f;
            case ARTILLERY -> 0.65f;
            case BALLISTA -> 0.85f;
            case MAGIC, LIGHTNING_MAGIC -> 1.35f;
            case FIRE_MAGIC -> 1.05f;
            case ICE_MAGIC, ICE_TOWER -> 1.15f;
            default -> 1f;
        };
        play(shoot, 0.28f, pitch);
    }

    public void hit() { play(hit, 0.22f, MathUtils.random(0.92f, 1.08f)); }
    public void miss() { play(miss, 0.2f, 0.9f); }
    public void place() { play(place, 0.35f, 1f); }
    public void upgrade() { play(upgrade, 0.35f, 1.1f); }
    public void sell() { play(sell, 0.3f, 0.85f); }
    public void wave() { play(wave, 0.4f, 1f); }
    public void leak() { play(leak, 0.45f, 0.8f); }
    public void win() { play(win, 0.45f, 1.2f); }
    public void lose() { play(lose, 0.45f, 0.7f); }
    public void click() { play(click, 0.18f, 1f); }
    public void pause() { play(pause, 0.25f, 1f); }

    private void play(Sound sound, float volume, float pitch) {
        if (!ready || sound == null || !Settings.sound()) return;
        sound.play(volume, pitch, 0f);
    }

    public void dispose() {
        dispose(shoot, hit, miss, place, upgrade, sell, wave, leak, win, lose, click, pause);
    }

    private static void dispose(Sound... sounds) {
        for (Sound s : sounds) if (s != null) s.dispose();
    }

    private Sound fromWav(String name, byte[] wav) {
        FileHandle dir = Gdx.files.local(".sfx");
        dir.mkdirs();
        FileHandle file = dir.child(name + ".wav");
        file.writeBytes(wav, false);
        return Gdx.audio.newSound(file);
    }

    private static byte[] punch(float hz, float seconds, float volume) {
        int rate = 22050;
        int n = samples(rate, seconds);
        short[] pcm = new short[n];
        for (int i = 0; i < n; i++) {
            float t = i / (float) rate;
            float env = envelope(i, n);
            float sq = Math.sin(2 * Math.PI * hz * t) >= 0 ? 1f : -1f;
            float click = (float) Math.sin(2 * Math.PI * hz * 2.4f * t);
            pcm[i] = sample(env * volume * (0.55f * sq + 0.45f * click));
        }
        return wrapWav(pcm, rate);
    }

    private static byte[] squareBurst(float hz, float seconds, float volume) {
        int rate = 22050;
        int n = samples(rate, seconds);
        short[] pcm = new short[n];
        for (int i = 0; i < n; i++) {
            float t = i / (float) rate;
            float env = envelope(i, n);
            float sq = Math.sin(2 * Math.PI * hz * t) >= 0 ? 1f : -1f;
            pcm[i] = sample(sq * volume * env);
        }
        return wrapWav(pcm, rate);
    }

    private static byte[] noiseBurst(float seconds, float volume) {
        int rate = 22050;
        int n = samples(rate, seconds);
        short[] pcm = new short[n];
        float prev = 0;
        for (int i = 0; i < n; i++) {
            float env = envelope(i, n);
            float raw = MathUtils.random() * 2f - 1f;
            prev = prev * 0.65f + raw * 0.35f;
            pcm[i] = sample(prev * volume * env);
        }
        return wrapWav(pcm, rate);
    }

    private static byte[] chirp(float hz0, float hz1, float seconds, float volume) {
        int rate = 22050;
        int n = samples(rate, seconds);
        short[] pcm = new short[n];
        for (int i = 0; i < n; i++) {
            float u = n <= 1 ? 0f : i / (float) (n - 1);
            float hz = hz0 + (hz1 - hz0) * u;
            float t = i / (float) rate;
            float env = envelope(i, n);
            pcm[i] = sample((float) Math.sin(2 * Math.PI * hz * t) * volume * env);
        }
        return wrapWav(pcm, rate);
    }

    private static byte[] twoTone(float hz1, float hz2, float each, float volume) {
        int rate = 22050;
        int n1 = samples(rate, each);
        int n2 = samples(rate, each);
        short[] pcm = new short[n1 + n2];
        fillSine(pcm, 0, n1, rate, hz1, volume);
        fillSine(pcm, n1, n2, rate, hz2, volume);
        return wrapWav(pcm, rate);
    }

    private static void fillSine(short[] pcm, int offset, int n, int rate, float hz, float volume) {
        for (int i = 0; i < n; i++) {
            float t = i / (float) rate;
            float env = envelope(i, n);
            pcm[offset + i] = sample((float) Math.sin(2 * Math.PI * hz * t) * volume * env);
        }
    }

    private static int samples(int rate, float seconds) {
        return Math.max(1, (int) (rate * seconds));
    }

    private static short sample(float v) {
        return (short) (MathUtils.clamp(v, -1f, 1f) * 32767);
    }

    private static float envelope(int i, int n) {
        float a = Math.min(1f, i / Math.max(1f, n * 0.08f));
        float r = Math.min(1f, (n - 1 - i) / Math.max(1f, n * 0.35f));
        return a * r;
    }

    private static byte[] wrapWav(short[] pcm, int rate) {
        int dataBytes = pcm.length * 2;
        ByteArrayOutputStream out = new ByteArrayOutputStream(44 + dataBytes);
        ByteBuffer hdr = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);
        hdr.put("RIFF".getBytes(StandardCharsets.US_ASCII));
        hdr.putInt(36 + dataBytes);
        hdr.put("WAVE".getBytes(StandardCharsets.US_ASCII));
        hdr.put("fmt ".getBytes(StandardCharsets.US_ASCII));
        hdr.putInt(16);
        hdr.putShort((short) 1);
        hdr.putShort((short) 1);
        hdr.putInt(rate);
        hdr.putInt(rate * 2);
        hdr.putShort((short) 2);
        hdr.putShort((short) 16);
        hdr.put("data".getBytes(StandardCharsets.US_ASCII));
        hdr.putInt(dataBytes);
        out.write(hdr.array(), 0, 44);
        ByteBuffer data = ByteBuffer.allocate(dataBytes).order(ByteOrder.LITTLE_ENDIAN);
        for (short s : pcm) data.putShort(s);
        out.write(data.array(), 0, dataBytes);
        return out.toByteArray();
    }
}
