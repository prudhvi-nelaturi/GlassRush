package com.prudhvinelaturi.glassrush.logic;

import com.prudhvinelaturi.glassrush.config.GameConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Per-run state construction: what the profile carries into a fresh run. */
public class RunStateTest {

    @Test
    public void defaultProfileStartsWithBaseAmmo() {
        RunState state = new RunState(new PlayerProfile());
        assertEquals(GameConfig.START_AMMO, state.ammo);
    }

    @Test
    public void ammoUpgradeLevelAddsOneStartingRoundPerLevel() {
        for (int level = 0; level <= GameConfig.AMMO_UPGRADE_MAX_LEVEL; level++) {
            PlayerProfile profile = new PlayerProfile();
            profile.ammoUpgradeLevel = level;
            RunState state = new RunState(profile);
            int expected = Math.min(GameConfig.MAX_AMMO, GameConfig.START_AMMO + level);
            assertEquals("ammoUpgradeLevel=" + level, expected, state.ammo);
        }
    }

    @Test
    public void startingAmmoIsCappedAtMaxAmmo() {
        PlayerProfile profile = new PlayerProfile();
        profile.ammoUpgradeLevel = 99; // corrupt/hacked save
        RunState state = new RunState(profile);
        assertEquals(GameConfig.MAX_AMMO, state.ammo);
    }

    @Test
    public void fullyUpgradedProfileReachesMaxAmmoWithoutOverflow() {
        PlayerProfile profile = new PlayerProfile();
        profile.ammoUpgradeLevel = GameConfig.AMMO_UPGRADE_MAX_LEVEL;
        RunState state = new RunState(profile);
        assertEquals(GameConfig.MAX_AMMO, state.ammo);
    }

    @Test
    public void playerWearsTheProfilesSelectedSkin() {
        PlayerProfile profile = new PlayerProfile();
        profile.ownedSkins.add("emerald");
        profile.selectedSkin = "emerald";
        assertEquals("emerald", new RunState(profile).player.skinId);
    }

    @Test
    public void defaultProfileWearsTheDefaultSkin() {
        assertEquals("default", new RunState(new PlayerProfile()).player.skinId);
    }

    @Test
    public void playerStartsCentredAndStationary() {
        RunState state = new RunState(new PlayerProfile());
        assertEquals(GameConfig.WORLD_WIDTH / 2f, state.player.x, 0.0001f);
        assertEquals(GameConfig.WORLD_WIDTH / 2f, state.player.targetX, 0.0001f);
        assertEquals(GameConfig.PLAYER_Y, state.player.y, 0.0001f);
        assertEquals(GameConfig.PLAYER_RADIUS, state.player.radius, 0.0001f);
    }

    @Test
    public void freshRunHasZeroedProgressAndIsAlive() {
        RunState state = new RunState(new PlayerProfile());
        assertEquals(0f, state.distanceMeters, 0.0001f);
        assertEquals(0, state.score);
        assertEquals(0, state.shardsThisRun);
        assertEquals(1f, state.comboMultiplier, 0.0001f);
        assertTrue(state.alive);
        assertFalse(state.usedContinue);
        assertEquals(GameConfig.BASE_SCROLL_SPEED, state.scrollSpeed, 0.0001f);
    }

    @Test
    public void freshRunHasNoEntitiesOnScreen() {
        RunState state = new RunState(new PlayerProfile());
        assertTrue(state.panels.isEmpty());
        assertTrue(state.projectiles.isEmpty());
        assertTrue(state.pickups.isEmpty());
    }

    @Test
    public void firstSpawnThresholdIsTheBaseIntervalAtBaseSpeed() {
        RunState state = new RunState(new PlayerProfile());
        assertEquals(GameConfig.PANEL_SPAWN_INTERVAL_BASE * GameConfig.BASE_SCROLL_SPEED,
                state.spawnThreshold(), 0.001f);
    }

    @Test
    public void spawnTimerAccumulatesScrolledDistance() {
        RunState state = new RunState(new PlayerProfile());
        assertEquals(10f, state.advanceSpawnTimer(10f), 0.0001f);
        assertEquals(25f, state.advanceSpawnTimer(15f), 0.0001f);
    }

    @Test
    public void resettingTheSpawnTimerZeroesProgressAndSetsANewThreshold() {
        RunState state = new RunState(new PlayerProfile());
        state.advanceSpawnTimer(500f);
        state.resetSpawnTimer(123f);

        assertEquals(123f, state.spawnThreshold(), 0.0001f);
        assertEquals(7f, state.advanceSpawnTimer(7f), 0.0001f);
    }
}
