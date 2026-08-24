package com.medievaltd.desktop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.medievaltd.util.Assets;

import java.io.File;

/** Writes procedural sprites to build/sprite-preview then exits. */
public class SpriteDump {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("sprite-dump");
        config.setWindowedMode(64, 64);
        config.setInitialVisible(false);
        new Lwjgl3Application(new ApplicationAdapter() {
            @Override
            public void create() {
                File dir = new File("H:/StudioProjects/TD/build/sprite-preview");
                Assets assets = new Assets();
                assets.setDumpDir(dir);
                assets.load();
                assets.dispose();
                Gdx.app.exit();
            }
        }, config);
    }
}
