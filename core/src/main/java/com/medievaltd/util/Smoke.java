package com.medievaltd.util;

/** One-shot in-game playtest. Does not write saves or research. */
public final class Smoke {
    private Smoke() {}

    public static boolean on() {
        return "1".equals(System.getProperty("medievaltd.smoke"));
    }
}
