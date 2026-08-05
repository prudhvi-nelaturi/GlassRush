package com.prudhvinelaturi.glassrush.logic;

import com.prudhvinelaturi.glassrush.config.GameConfig;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The persistent cross-run profile. {@code SaveManager} needs a live LibGDX
 * {@code Gdx.app} for its Preferences backing store, so these tests cover the
 * plain-data object it serialises rather than the storage layer itself.
 */
public class PlayerProfileTest {

    @Test
    public void freshProfileOwnsOnlyTheDefaultSkin() {
        PlayerProfile profile = new PlayerProfile();
        assertEquals(1, profile.ownedSkins.size());
        assertTrue(profile.ownedSkins.contains("default"));
    }

    @Test
    public void freshProfileSelectsTheDefaultSkin() {
        PlayerProfile profile = new PlayerProfile();
        assertEquals("default", profile.selectedSkin);
    }

    @Test
    public void theDefaultSkinIsAlwaysOwnedAndFree() {
        // The profile hands out "default" for free, so the shop must agree it costs nothing.
        assertEquals("default", GameConfig.SKIN_IDS[0]);
        assertEquals(0, GameConfig.SKIN_COST[0]);
        assertTrue(new PlayerProfile().ownedSkins.contains(GameConfig.SKIN_IDS[0]));
    }

    @Test
    public void freshProfileHasZeroedCounters() {
        PlayerProfile profile = new PlayerProfile();
        assertEquals(0, profile.totalShards);
        assertEquals(0, profile.bestScore);
        assertEquals(0f, profile.bestDistanceMeters, 0.0001f);
        assertEquals(0, profile.ammoUpgradeLevel);
        assertEquals(0, profile.continueTokens);
    }

    @Test
    public void purchasingASkinAddsItWithoutDroppingTheDefault() {
        PlayerProfile profile = new PlayerProfile();
        profile.ownedSkins.add("ruby");
        profile.selectedSkin = "ruby";

        assertEquals(2, profile.ownedSkins.size());
        assertTrue(profile.ownedSkins.contains("default"));
        assertTrue(profile.ownedSkins.contains("ruby"));
        assertEquals("ruby", profile.selectedSkin);
    }

    @Test
    public void ownedSkinsIsADeduplicatingSetThatPreservesInsertionOrder() {
        // A fresh profile already owns "default" (see PlayerProfile's constructor / SaveManager.load) —
        // the fallback skin is always available, never something the player has to unlock.
        PlayerProfile profile = new PlayerProfile();
        profile.ownedSkins.add("gold");
        profile.ownedSkins.add("ruby");
        profile.ownedSkins.add("gold"); // re-purchase / double-load of the same save

        assertEquals(3, profile.ownedSkins.size());
        List<String> order = new ArrayList<>(profile.ownedSkins);
        assertEquals("default", order.get(0));
        assertEquals("gold", order.get(1));
        assertEquals("ruby", order.get(2));
    }

    @Test
    public void everyShopSkinIdIsPurchasableIntoTheProfile() {
        PlayerProfile profile = new PlayerProfile();
        for (String id : GameConfig.SKIN_IDS) {
            profile.ownedSkins.add(id);
        }
        assertEquals(GameConfig.SKIN_IDS.length, profile.ownedSkins.size());
    }

    @Test
    public void profileFieldsAreIndependentlyMutable() {
        PlayerProfile profile = new PlayerProfile();
        profile.totalShards = 250;
        profile.bestScore = 1200;
        profile.bestDistanceMeters = 1200.5f;
        profile.ammoUpgradeLevel = GameConfig.AMMO_UPGRADE_MAX_LEVEL;
        profile.continueTokens = 3;

        assertEquals(250, profile.totalShards);
        assertEquals(1200, profile.bestScore);
        assertEquals(1200.5f, profile.bestDistanceMeters, 0.0001f);
        assertEquals(GameConfig.AMMO_UPGRADE_MAX_LEVEL, profile.ammoUpgradeLevel);
        assertEquals(3, profile.continueTokens);
        assertFalse(profile.ownedSkins.isEmpty());
    }

    @Test
    public void twoProfilesDoNotShareOwnedSkins() {
        PlayerProfile a = new PlayerProfile();
        PlayerProfile b = new PlayerProfile();
        a.ownedSkins.add("prism");
        assertFalse(b.ownedSkins.contains("prism"));
    }
}
