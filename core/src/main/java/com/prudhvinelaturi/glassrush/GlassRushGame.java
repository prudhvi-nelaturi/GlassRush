package com.prudhvinelaturi.glassrush;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.prudhvinelaturi.glassrush.ads.AdManager;
import com.prudhvinelaturi.glassrush.audio.SfxManager;
import com.prudhvinelaturi.glassrush.billing.BillingManager;
import com.prudhvinelaturi.glassrush.logic.PlayerProfile;
import com.prudhvinelaturi.glassrush.save.SaveManager;
import com.prudhvinelaturi.glassrush.screens.SplashScreen;
import com.prudhvinelaturi.glassrush.ui.UiFactory;

/**
 * Entry point shared by every platform launcher. Holds everything screens need to
 * navigate and share state: {@code game.setScreen(new XScreen(game))} is the navigation
 * convention used throughout {@code screens/}.
 */
public class GlassRushGame extends Game {
    public final AdManager adManager;
    public final BillingManager billingManager;

    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public BitmapFont font;
    public SaveManager saveManager;
    public SfxManager sfxManager;
    public PlayerProfile profile;
    public Skin skin;

    public GlassRushGame(AdManager adManager, BillingManager billingManager) {
        this.adManager = adManager;
        this.billingManager = billingManager;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        saveManager = new SaveManager();
        sfxManager = new SfxManager();
        profile = saveManager.load();
        skin = UiFactory.createSkin(font);

        setScreen(new SplashScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        sfxManager.dispose();
        skin.dispose();
    }
}
