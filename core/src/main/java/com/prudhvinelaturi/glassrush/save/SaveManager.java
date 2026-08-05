package com.prudhvinelaturi.glassrush.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.prudhvinelaturi.glassrush.logic.PlayerProfile;

/**
 * Local save/load via LibGDX {@link Preferences} — backed by SharedPreferences on Android,
 * a properties file on desktop. No backend, no accounts; matches the $0-hosting approach
 * used by the rest of the game portfolio.
 */
public class SaveManager {
    private static final String PREFS_NAME = "glassrush-save";

    private static final String KEY_TOTAL_SHARDS = "totalShards";
    private static final String KEY_BEST_SCORE = "bestScore";
    private static final String KEY_BEST_DISTANCE = "bestDistanceMeters";
    private static final String KEY_AMMO_UPGRADE_LEVEL = "ammoUpgradeLevel";
    private static final String KEY_CONTINUE_TOKENS = "continueTokens";
    private static final String KEY_SELECTED_SKIN = "selectedSkin";
    private static final String KEY_OWNED_SKINS = "ownedSkins"; // comma-separated

    private final Preferences prefs;

    public SaveManager() {
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public PlayerProfile load() {
        PlayerProfile profile = new PlayerProfile();
        profile.totalShards = prefs.getInteger(KEY_TOTAL_SHARDS, 0);
        profile.bestScore = prefs.getInteger(KEY_BEST_SCORE, 0);
        profile.bestDistanceMeters = prefs.getFloat(KEY_BEST_DISTANCE, 0f);
        profile.ammoUpgradeLevel = prefs.getInteger(KEY_AMMO_UPGRADE_LEVEL, 0);
        profile.continueTokens = prefs.getInteger(KEY_CONTINUE_TOKENS, 0);
        profile.selectedSkin = prefs.getString(KEY_SELECTED_SKIN, "default");

        String owned = prefs.getString(KEY_OWNED_SKINS, "");
        profile.ownedSkins.clear();
        profile.ownedSkins.add("default");
        if (!owned.isEmpty()) {
            for (String skin : owned.split(",")) {
                if (!skin.isEmpty()) profile.ownedSkins.add(skin);
            }
        }
        return profile;
    }

    public void save(PlayerProfile profile) {
        prefs.putInteger(KEY_TOTAL_SHARDS, profile.totalShards);
        prefs.putInteger(KEY_BEST_SCORE, profile.bestScore);
        prefs.putFloat(KEY_BEST_DISTANCE, profile.bestDistanceMeters);
        prefs.putInteger(KEY_AMMO_UPGRADE_LEVEL, profile.ammoUpgradeLevel);
        prefs.putInteger(KEY_CONTINUE_TOKENS, profile.continueTokens);
        prefs.putString(KEY_SELECTED_SKIN, profile.selectedSkin);
        prefs.putString(KEY_OWNED_SKINS, String.join(",", profile.ownedSkins));
        prefs.flush();
    }

    public void reset() {
        prefs.clear();
        prefs.flush();
    }
}
