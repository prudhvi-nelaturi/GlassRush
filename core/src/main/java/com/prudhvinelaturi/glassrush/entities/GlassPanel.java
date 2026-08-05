package com.prudhvinelaturi.glassrush.entities;

import com.prudhvinelaturi.glassrush.config.GameConfig;

/**
 * A horizontal glass wall segment scrolling down the screen with one gap the player
 * must pass through — either by steering into the gap, or shattering the glass with
 * a projectile if the gap is blocked/too narrow to reach in time.
 */
public class GlassPanel {
    public float y;
    public final float gapX;      // left edge of the gap
    public final float gapWidth;
    public int hp;
    public boolean broken = false;
    public boolean scored = false; // true once this panel has been counted for score/pass-through

    public GlassPanel(float y, float gapX, float gapWidth) {
        this.y = y;
        this.gapX = gapX;
        this.gapWidth = gapWidth;
        this.hp = GameConfig.PANEL_MAX_HP;
    }

    public boolean gapContains(float x, float radius) {
        return x - radius >= gapX && x + radius <= gapX + gapWidth;
    }

    /** True if the given x overlaps solid glass (i.e. NOT within the gap), accounting for radius. */
    public boolean blocksAt(float x, float radius) {
        return !broken && !gapContains(x, radius);
    }
}
