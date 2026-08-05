package com.prudhvinelaturi.glassrush.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.prudhvinelaturi.glassrush.GlassRushGame;
import com.prudhvinelaturi.glassrush.ads.NoOpAdManager;
import com.prudhvinelaturi.glassrush.billing.NoOpBillingManager;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("GlassRush");
        config.setWindowedMode(480, 800);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new GlassRushGame(new NoOpAdManager(), new NoOpBillingManager()), config);
    }
}
