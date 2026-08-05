package com.prudhvinelaturi.glassrush.logic;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Persistent, cross-run player data. Saved/loaded by
 * {@link com.prudhvinelaturi.glassrush.save.SaveManager}.
 */
public class PlayerProfile {
    public int totalShards = 0;
    public int bestScore = 0;
    public float bestDistanceMeters = 0f;
    public int ammoUpgradeLevel = 0; // 0..GameConfig.AMMO_UPGRADE_MAX_LEVEL
    public int continueTokens = 0;
    public String selectedSkin = "default";
    public final Set<String> ownedSkins = new LinkedHashSet<>();

    public PlayerProfile() {
        ownedSkins.add("default");
    }
}
