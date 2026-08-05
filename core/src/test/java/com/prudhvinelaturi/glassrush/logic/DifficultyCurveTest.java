package com.prudhvinelaturi.glassrush.logic;

import com.prudhvinelaturi.glassrush.config.GameConfig;
import com.prudhvinelaturi.glassrush.entities.GlassPanel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The difficulty ramp: scroll speed climbs, gaps narrow, panels arrive more often —
 * all as a function of distance travelled, all clamped at their configured extremes.
 */
public class DifficultyCurveTest {

    private static final float DT = 1f / 60f;
    private static final float EPS = 0.001f;

    // --- difficultyLerp itself -------------------------------------------------

    @Test
    public void lerpStartsAtTheBaseValue() {
        assertEquals(200f, GameEngine.difficultyLerp(0f, 200f, 90f, 700f), EPS);
    }

    @Test
    public void lerpReachesTheMinimumExactlyAtTheRampDistance() {
        assertEquals(90f, GameEngine.difficultyLerp(700f, 200f, 90f, 700f), EPS);
    }

    @Test
    public void lerpHoldsAtTheMinimumBeyondTheRamp() {
        assertEquals(90f, GameEngine.difficultyLerp(700.1f, 200f, 90f, 700f), EPS);
        assertEquals(90f, GameEngine.difficultyLerp(5_000f, 200f, 90f, 700f), EPS);
        assertEquals(90f, GameEngine.difficultyLerp(Float.MAX_VALUE, 200f, 90f, 700f), EPS);
    }

    @Test
    public void lerpClampsNegativeDistanceToTheBaseValue() {
        assertEquals(200f, GameEngine.difficultyLerp(-50f, 200f, 90f, 700f), EPS);
    }

    @Test
    public void lerpIsLinearAcrossTheRamp() {
        assertEquals(145f, GameEngine.difficultyLerp(350f, 200f, 90f, 700f), EPS); // halfway
        assertEquals(172.5f, GameEngine.difficultyLerp(175f, 200f, 90f, 700f), EPS); // quarter
        assertEquals(117.5f, GameEngine.difficultyLerp(525f, 200f, 90f, 700f), EPS); // three quarters
    }

    @Test
    public void lerpNeverIncreasesAsDistanceGrows() {
        float previous = Float.MAX_VALUE;
        for (float d = 0f; d <= 1_600f; d += 25f) {
            float value = GameEngine.difficultyLerp(d, 200f, 90f, 700f);
            assertTrue("distance " + d, value <= previous + EPS);
            assertTrue("distance " + d, value >= 90f - EPS && value <= 200f + EPS);
            previous = value;
        }
    }

    // --- gap width -------------------------------------------------------------

    @Test
    public void gapWidthStartsWideAndBottomsOutAtTheMinimum() {
        assertEquals(GameConfig.GAP_WIDTH_BASE, gapWidthAt(0f), EPS);
        assertEquals(GameConfig.GAP_WIDTH_MIN, gapWidthAt(GameConfig.GAP_WIDTH_RAMP_METERS), EPS);
        assertEquals(GameConfig.GAP_WIDTH_MIN, gapWidthAt(GameConfig.GAP_WIDTH_RAMP_METERS * 10f), EPS);
    }

    @Test
    public void gapWidthShrinksMonotonically() {
        float previous = gapWidthAt(0f);
        for (float d = 50f; d <= GameConfig.GAP_WIDTH_RAMP_METERS * 2f; d += 50f) {
            float current = gapWidthAt(d);
            assertTrue("distance " + d, current <= previous + EPS);
            previous = current;
        }
    }

    // --- spawn interval --------------------------------------------------------

    @Test
    public void spawnIntervalStartsSlowAndBottomsOutAtTheMinimum() {
        assertEquals(GameConfig.PANEL_SPAWN_INTERVAL_BASE, spawnIntervalAt(0f), EPS);
        assertEquals(GameConfig.PANEL_SPAWN_INTERVAL_MIN, spawnIntervalAt(GameConfig.PANEL_SPAWN_RAMP_METERS), EPS);
        assertEquals(GameConfig.PANEL_SPAWN_INTERVAL_MIN, spawnIntervalAt(GameConfig.PANEL_SPAWN_RAMP_METERS * 3f), EPS);
    }

    @Test
    public void spawnIntervalShrinksMonotonically() {
        float previous = spawnIntervalAt(0f);
        for (float d = 50f; d <= GameConfig.PANEL_SPAWN_RAMP_METERS * 2f; d += 50f) {
            float current = spawnIntervalAt(d);
            assertTrue("distance " + d, current <= previous + EPS);
            previous = current;
        }
    }

    // --- scroll speed, as applied by update() ----------------------------------

    @Test
    public void scrollSpeedStartsAtTheBaseSpeed() {
        RunState state = new RunState(new PlayerProfile());
        new GameEngine(1L).update(state, DT);
        assertEquals(GameConfig.BASE_SCROLL_SPEED, state.scrollSpeed, 0.5f);
    }

    @Test
    public void scrollSpeedRampsLinearlyWithDistance() {
        RunState state = new RunState(new PlayerProfile());
        state.distanceMeters = 50f;
        new GameEngine(1L).update(state, DT);
        assertEquals(GameConfig.BASE_SCROLL_SPEED + 50f * GameConfig.SPEED_RAMP_PER_METER, state.scrollSpeed, EPS);
    }

    @Test
    public void scrollSpeedIsCappedAtMaxScrollSpeed() {
        RunState state = new RunState(new PlayerProfile());
        state.distanceMeters = 100_000f;
        new GameEngine(1L).update(state, DT);
        assertEquals(GameConfig.MAX_SCROLL_SPEED, state.scrollSpeed, EPS);
    }

    @Test
    public void scrollSpeedNeverDropsAsARunProgresses() {
        RunState state = new RunState(new PlayerProfile());
        GameEngine engine = new GameEngine(7L);
        float previous = 0f;
        for (int i = 0; i < 400; i++) {
            state.panels.clear(); // keep the marble alive; we only care about the speed curve here
            engine.update(state, DT);
            assertTrue(state.scrollSpeed >= previous - EPS);
            assertTrue(state.scrollSpeed <= GameConfig.MAX_SCROLL_SPEED + EPS);
            previous = state.scrollSpeed;
        }
        assertTrue("speed should have grown over 400 frames", previous > GameConfig.BASE_SCROLL_SPEED);
    }

    // --- the curve as the engine actually spawns panels -------------------------

    @Test
    public void panelsSpawnedEarlyUseNearlyTheWidestGap() {
        GlassPanel first = firstSpawnedPanel(0f, 400);
        assertTrue("expected a near-base gap but got " + first.gapWidth,
                first.gapWidth > GameConfig.GAP_WIDTH_BASE - 5f);
        assertTrue(first.gapWidth <= GameConfig.GAP_WIDTH_BASE + EPS);
    }

    @Test
    public void panelsSpawnedPastTheRampUseTheMinimumGap() {
        GlassPanel late = firstSpawnedPanel(GameConfig.GAP_WIDTH_RAMP_METERS * 3f, 400);
        assertEquals(GameConfig.GAP_WIDTH_MIN, late.gapWidth, EPS);
    }

    @Test
    public void gapsNarrowAsTheRunGetsLonger() {
        assertTrue(firstSpawnedPanel(GameConfig.GAP_WIDTH_RAMP_METERS / 2f, 400).gapWidth
                < firstSpawnedPanel(0f, 400).gapWidth);
    }

    @Test
    public void spawnedPanelsAlwaysStartAboveTheScreen() {
        GlassPanel panel = firstSpawnedPanel(0f, 400);
        assertTrue(panel.y >= GameConfig.WORLD_HEIGHT - EPS);
        assertFalse(panel.broken);
        assertFalse(panel.scored);
    }

    @Test
    public void everySpawnedGapStaysReachableInsideTheWorld() {
        RunState state = new RunState(new PlayerProfile());
        GameEngine engine = new GameEngine(99L);
        int spawns = 0;
        for (int frame = 0; frame < 4_000; frame++) {
            int before = state.panels.size();
            engine.update(state, DT);
            state.alive = true; // ignore deaths; we are only sampling spawn geometry
            if (state.panels.size() > before) spawns++;

            for (GlassPanel p : state.panels) {
                assertTrue("gap starts off-world: " + p.gapX, p.gapX >= GameConfig.PLAYER_RADIUS - EPS);
                assertTrue("gap ends off-world: " + (p.gapX + p.gapWidth),
                        p.gapX + p.gapWidth <= GameConfig.WORLD_WIDTH - GameConfig.PLAYER_RADIUS + EPS);
                assertTrue("gap too narrow: " + p.gapWidth, p.gapWidth >= GameConfig.GAP_WIDTH_MIN - EPS);
                assertTrue("gap too wide: " + p.gapWidth, p.gapWidth <= GameConfig.GAP_WIDTH_BASE + EPS);
            }
        }
        assertTrue("expected a meaningful sample of spawns, got " + spawns, spawns >= 20);
    }

    @Test
    public void panelsArriveMoreFrequentlyLaterInTheRun() {
        int earlyFrames = framesBetweenSpawns(0f);
        int lateFrames = framesBetweenSpawns(GameConfig.PANEL_SPAWN_RAMP_METERS * 2f);
        assertTrue("early=" + earlyFrames + " late=" + lateFrames, lateFrames < earlyFrames);
    }

    // --- helpers ---------------------------------------------------------------

    private static float gapWidthAt(float distanceMeters) {
        return GameEngine.difficultyLerp(distanceMeters,
                GameConfig.GAP_WIDTH_BASE, GameConfig.GAP_WIDTH_MIN, GameConfig.GAP_WIDTH_RAMP_METERS);
    }

    private static float spawnIntervalAt(float distanceMeters) {
        return GameEngine.difficultyLerp(distanceMeters,
                GameConfig.PANEL_SPAWN_INTERVAL_BASE, GameConfig.PANEL_SPAWN_INTERVAL_MIN, GameConfig.PANEL_SPAWN_RAMP_METERS);
    }

    /** Runs the engine from the given starting distance until the first panel spawns. */
    private static GlassPanel firstSpawnedPanel(float startDistance, int maxFrames) {
        RunState state = new RunState(new PlayerProfile());
        state.distanceMeters = startDistance;
        GameEngine engine = new GameEngine(42L);
        for (int i = 0; i < maxFrames; i++) {
            engine.update(state, DT);
            if (!state.panels.isEmpty()) return state.panels.get(0);
        }
        throw new AssertionError("no panel spawned within " + maxFrames + " frames");
    }

    /** Frames elapsed between the first and second spawn at a given difficulty. */
    private static int framesBetweenSpawns(float startDistance) {
        RunState state = new RunState(new PlayerProfile());
        state.distanceMeters = startDistance;
        GameEngine engine = new GameEngine(42L);
        int firstSpawnFrame = -1;
        for (int i = 0; i < 2_000; i++) {
            int before = state.panels.size();
            engine.update(state, DT);
            state.alive = true; // spawn cadence only; deaths are irrelevant here
            if (state.panels.size() > before) {
                if (firstSpawnFrame < 0) {
                    firstSpawnFrame = i;
                } else {
                    return i - firstSpawnFrame;
                }
            }
        }
        throw new AssertionError("fewer than two spawns at distance " + startDistance);
    }
}
