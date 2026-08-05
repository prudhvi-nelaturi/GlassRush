package com.prudhvinelaturi.glassrush.config;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Guard rails on the balance constants. These do not assert specific tuned values —
 * they assert the relationships the engine's logic depends on, so a rebalance that
 * would break gameplay (an unpassable gap, an unreachable upgrade) fails here first.
 */
public class GameConfigTest {

    @Test
    public void worldIsTallerThanItIsWideForAVerticalRunner() {
        assertTrue(GameConfig.WORLD_HEIGHT > GameConfig.WORLD_WIDTH);
    }

    @Test
    public void playerSitsInsideTheWorldNearTheBottom() {
        assertTrue(GameConfig.PLAYER_RADIUS > 0f);
        assertTrue(GameConfig.PLAYER_Y > GameConfig.PLAYER_RADIUS);
        assertTrue(GameConfig.PLAYER_Y < GameConfig.WORLD_HEIGHT / 2f);
    }

    @Test
    public void playerLerpIsAUsableFraction() {
        assertTrue(GameConfig.PLAYER_MOVE_LERP > 0f);
        assertTrue(GameConfig.PLAYER_MOVE_LERP <= 1f);
    }

    @Test
    public void scrollSpeedRampsUpwardAndIsBounded() {
        assertTrue(GameConfig.BASE_SCROLL_SPEED > 0f);
        assertTrue(GameConfig.MAX_SCROLL_SPEED > GameConfig.BASE_SCROLL_SPEED);
        assertTrue(GameConfig.SPEED_RAMP_PER_METER > 0f);
        assertTrue(GameConfig.DISTANCE_PER_WORLD_UNIT > 0f);
    }

    @Test
    public void gapShrinksWithDifficultyOverAPositiveRamp() {
        assertTrue(GameConfig.GAP_WIDTH_MIN < GameConfig.GAP_WIDTH_BASE);
        assertTrue(GameConfig.GAP_WIDTH_RAMP_METERS > 0f);
    }

    @Test
    public void narrowestGapStillFitsThePlayer() {
        // If the hardest gap were narrower than the marble, the run would become unwinnable.
        assertTrue("GAP_WIDTH_MIN must admit a marble of diameter " + (GameConfig.PLAYER_RADIUS * 2f),
                GameConfig.GAP_WIDTH_MIN > GameConfig.PLAYER_RADIUS * 2f);
    }

    @Test
    public void widestGapStillLeavesGlassToDodge() {
        assertTrue(GameConfig.GAP_WIDTH_BASE < GameConfig.WORLD_WIDTH);
    }

    @Test
    public void spawnableGapAlwaysFitsInsideTheWorld() {
        // Mirrors GameEngine.spawnPanel's gapX range: PLAYER_RADIUS .. WORLD_WIDTH - gapWidth - PLAYER_RADIUS.
        float widestSpan = GameConfig.GAP_WIDTH_BASE + GameConfig.PLAYER_RADIUS * 2f;
        assertTrue(widestSpan <= GameConfig.WORLD_WIDTH);
    }

    @Test
    public void spawnIntervalTightensWithDifficulty() {
        assertTrue(GameConfig.PANEL_SPAWN_INTERVAL_MIN > 0f);
        assertTrue(GameConfig.PANEL_SPAWN_INTERVAL_MIN < GameConfig.PANEL_SPAWN_INTERVAL_BASE);
        assertTrue(GameConfig.PANEL_SPAWN_RAMP_METERS > 0f);
    }

    @Test
    public void panelsTakeAtLeastOneHitToShatter() {
        assertTrue(GameConfig.PANEL_MAX_HP >= 1);
        assertTrue(GameConfig.PANEL_HEIGHT > 0f);
    }

    @Test
    public void projectilesOutrunTheScrollSoTheyCanReachPanels() {
        assertTrue(GameConfig.PROJECTILE_SPEED > GameConfig.MAX_SCROLL_SPEED);
        assertTrue(GameConfig.PROJECTILE_RADIUS > 0f);
        assertTrue(GameConfig.PROJECTILE_RADIUS < GameConfig.PLAYER_RADIUS);
    }

    @Test
    public void ammoStartsBelowTheCap() {
        assertTrue(GameConfig.START_AMMO > 0);
        assertTrue(GameConfig.START_AMMO <= GameConfig.MAX_AMMO);
    }

    @Test
    public void fullyUpgradedStartingAmmoIsNotWasted() {
        // Every purchased ammo upgrade must translate into a real starting round.
        assertTrue("Top ammo upgrade would be clipped by MAX_AMMO",
                GameConfig.START_AMMO + GameConfig.AMMO_UPGRADE_MAX_LEVEL <= GameConfig.MAX_AMMO);
    }

    @Test
    public void thereIsOneAmmoUpgradePricePerLevel() {
        assertEquals(GameConfig.AMMO_UPGRADE_MAX_LEVEL, GameConfig.AMMO_UPGRADE_COST.length);
    }

    @Test
    public void ammoUpgradeCostsRiseWithEachLevel() {
        for (int i = 1; i < GameConfig.AMMO_UPGRADE_COST.length; i++) {
            assertTrue("level " + i + " must cost more than level " + i,
                    GameConfig.AMMO_UPGRADE_COST[i] > GameConfig.AMMO_UPGRADE_COST[i - 1]);
        }
        assertTrue(GameConfig.AMMO_UPGRADE_COST[0] > 0);
    }

    @Test
    public void pickupValuesAreMeaningful() {
        assertTrue(GameConfig.PICKUP_RADIUS > 0f);
        assertTrue(GameConfig.PICKUP_AMMO_VALUE > 0);
        assertTrue(GameConfig.PICKUP_SHARD_VALUE > 0);
    }

    @Test
    public void pickupProbabilitiesAreValidWeights() {
        assertTrue(GameConfig.PICKUP_SPAWN_CHANCE >= 0f && GameConfig.PICKUP_SPAWN_CHANCE <= 1f);
        assertTrue(GameConfig.PICKUP_AMMO_WEIGHT >= 0f && GameConfig.PICKUP_AMMO_WEIGHT <= 1f);
    }

    @Test
    public void comboRewardsBuildUpToACap() {
        assertTrue(GameConfig.COMBO_MULTIPLIER_STEP > 0f);
        assertTrue(GameConfig.COMBO_MAX_MULTIPLIER > 1f);
        assertTrue(GameConfig.COMBO_BREAK_ON_MISS >= 1);
    }

    @Test
    public void comboCapIsReachableInWholeSteps() {
        float steps = (GameConfig.COMBO_MAX_MULTIPLIER - 1f) / GameConfig.COMBO_MULTIPLIER_STEP;
        assertTrue(steps >= 1f);
    }

    @Test
    public void scoringAndShardRewardsArePositive() {
        assertTrue(GameConfig.SCORE_PER_METER > 0);
        assertTrue(GameConfig.SHARD_PER_PANEL_BROKEN > 0);
    }

    @Test
    public void continueCostsShardsAndIsLimitedToOnePerRun() {
        assertTrue(GameConfig.CONTINUE_TOKEN_COST > 0);
        // GameEngine tracks continues with a single boolean flag, so anything above 1 would be a lie.
        assertEquals(1, GameConfig.MAX_CONTINUES_PER_RUN);
    }

    @Test
    public void thereIsOnePriceForEverySkin() {
        assertEquals(GameConfig.SKIN_IDS.length, GameConfig.SKIN_COST.length);
    }

    @Test
    public void skinIdsAreUniqueAndStartWithTheFreeDefault() {
        Set<String> unique = new HashSet<>();
        for (String id : GameConfig.SKIN_IDS) {
            assertTrue("duplicate skin id: " + id, unique.add(id));
        }
        assertEquals("default", GameConfig.SKIN_IDS[0]);
        assertEquals(0, GameConfig.SKIN_COST[0]);
    }

    @Test
    public void everyNonDefaultSkinCostsSomething() {
        for (int i = 1; i < GameConfig.SKIN_COST.length; i++) {
            assertTrue(GameConfig.SKIN_IDS[i] + " must cost shards", GameConfig.SKIN_COST[i] > 0);
        }
    }
}
