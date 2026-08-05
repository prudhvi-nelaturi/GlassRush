package com.prudhvinelaturi.glassrush.entities;

import com.prudhvinelaturi.glassrush.config.GameConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Geometry of a glass wall: a full-width panel with a single gap running from
 * {@code gapX} to {@code gapX + gapWidth}. Solid glass is everything else, so the
 * player only survives a panel when their whole circle fits inside the gap.
 */
public class GlassPanelTest {

    private static final float GAP_X = 200f;
    private static final float GAP_W = 100f;
    private static final float GAP_RIGHT = GAP_X + GAP_W; // 300
    private static final float R = GameConfig.PLAYER_RADIUS; // 18

    private GlassPanel panel() {
        return new GlassPanel(500f, GAP_X, GAP_W);
    }

    @Test
    public void newPanelStartsIntactUnscoredAndAtFullHp() {
        GlassPanel p = panel();
        assertFalse(p.broken);
        assertFalse(p.scored);
        assertEquals(GameConfig.PANEL_MAX_HP, p.hp);
        assertEquals(500f, p.y, 0.0001f);
        assertEquals(GAP_X, p.gapX, 0.0001f);
        assertEquals(GAP_W, p.gapWidth, 0.0001f);
    }

    @Test
    public void gapContainsCircleFullyInsideTheGap() {
        GlassPanel p = panel();
        assertTrue(p.gapContains(250f, R)); // 232..268, comfortably inside 200..300
    }

    @Test
    public void gapContainsIsInclusiveAtTheExactLeftEdge() {
        GlassPanel p = panel();
        // x - radius == gapX exactly: the circle is flush against the left glass but not overlapping it.
        assertTrue(p.gapContains(GAP_X + R, R));
        assertFalse(p.blocksAt(GAP_X + R, R));
    }

    @Test
    public void gapContainsIsInclusiveAtTheExactRightEdge() {
        GlassPanel p = panel();
        // x + radius == gapX + gapWidth exactly.
        assertTrue(p.gapContains(GAP_RIGHT - R, R));
        assertFalse(p.blocksAt(GAP_RIGHT - R, R));
    }

    @Test
    public void oneUnitPastTheLeftEdgeOverlapsGlass() {
        GlassPanel p = panel();
        float x = GAP_X + R - 1f;
        assertFalse(p.gapContains(x, R));
        assertTrue(p.blocksAt(x, R));
    }

    @Test
    public void oneUnitPastTheRightEdgeOverlapsGlass() {
        GlassPanel p = panel();
        float x = GAP_RIGHT - R + 1f;
        assertFalse(p.gapContains(x, R));
        assertTrue(p.blocksAt(x, R));
    }

    @Test
    public void circleEntirelyLeftOfTheGapIsInsideSolidGlass() {
        // Regression guard: "entirely to the left of the gap" means buried in the left glass pane,
        // which is a death, not a safe pass.
        GlassPanel p = panel();
        assertTrue(p.blocksAt(40f, R));
        assertFalse(p.gapContains(40f, R));
    }

    @Test
    public void circleEntirelyRightOfTheGapIsInsideSolidGlass() {
        GlassPanel p = panel();
        assertTrue(p.blocksAt(GameConfig.WORLD_WIDTH - R, R));
        assertFalse(p.gapContains(GameConfig.WORLD_WIDTH - R, R));
    }

    @Test
    public void blocksAtIsExactlyTheNegationOfGapContainsWhileIntact() {
        GlassPanel p = panel();
        for (float x = 0f; x <= GameConfig.WORLD_WIDTH; x += 0.5f) {
            assertEquals("x=" + x, !p.gapContains(x, R), p.blocksAt(x, R));
        }
    }

    @Test
    public void brokenPanelBlocksNothing() {
        GlassPanel p = panel();
        p.broken = true;
        for (float x = 0f; x <= GameConfig.WORLD_WIDTH; x += 10f) {
            assertFalse("x=" + x, p.blocksAt(x, R));
        }
    }

    @Test
    public void brokenFlagDoesNotChangeGapGeometry() {
        GlassPanel p = panel();
        boolean before = p.gapContains(40f, R);
        p.broken = true;
        assertEquals(before, p.gapContains(40f, R));
    }

    @Test
    public void zeroRadiusPointIsInclusiveOnBothGapEdges() {
        GlassPanel p = panel();
        assertTrue(p.gapContains(GAP_X, 0f));
        assertTrue(p.gapContains(GAP_RIGHT, 0f));
        assertFalse(p.gapContains(GAP_X - 0.01f, 0f));
        assertFalse(p.gapContains(GAP_RIGHT + 0.01f, 0f));
    }

    @Test
    public void gapNarrowerThanTheCircleIsImpassable() {
        GlassPanel tight = new GlassPanel(500f, GAP_X, R * 2f - 1f);
        for (float x = 0f; x <= GameConfig.WORLD_WIDTH; x += 1f) {
            assertTrue("x=" + x, tight.blocksAt(x, R));
        }
    }

    @Test
    public void projectileRadiusPassesThroughTheGapButHitsTheGlass() {
        GlassPanel p = panel();
        float pr = GameConfig.PROJECTILE_RADIUS;
        assertFalse(p.blocksAt(250f, pr));         // straight down the middle of the gap
        assertTrue(p.blocksAt(GAP_X - 1f, pr));    // clipping the left pane
        assertTrue(p.blocksAt(GAP_RIGHT + 1f, pr)); // clipping the right pane
    }
}
