package com.prudhvinelaturi.glassrush.config;

/**
 * The design surface. Balance the game by editing these numbers, not the logic
 * in {@link com.prudhvinelaturi.glassrush.logic.GameEngine}.
 */
public final class GameConfig {
    private GameConfig() {}

    // Virtual viewport (world units), scaled to fit any real screen via LibGDX's FitViewport.
    public static final float WORLD_WIDTH = 480f;
    public static final float WORLD_HEIGHT = 800f;

    // Player
    public static final float PLAYER_RADIUS = 18f;
    public static final float PLAYER_Y = 110f;
    public static final float PLAYER_MOVE_LERP = 0.28f; // how quickly the marble follows the drag target

    // Scroll / difficulty curve — speed and panel density both ramp with distance travelled.
    public static final float BASE_SCROLL_SPEED = 220f;       // world units/sec at distance 0
    public static final float MAX_SCROLL_SPEED = 620f;
    public static final float SPEED_RAMP_PER_METER = 3.2f;    // speed added per meter travelled
    public static final float DISTANCE_PER_WORLD_UNIT = 0.01f; // world units scrolled -> "meters" for display/curve

    // Glass panels (obstacles)
    public static final float PANEL_HEIGHT = 26f;
    public static final float PANEL_SPAWN_INTERVAL_BASE = 1.35f;  // seconds between panel rows at distance 0
    public static final float PANEL_SPAWN_INTERVAL_MIN = 0.62f;   // fastest spawn rate at max difficulty
    public static final float PANEL_SPAWN_RAMP_METERS = 900f;     // meters over which spawn interval reaches its min
    public static final float GAP_WIDTH_BASE = 200f;
    public static final float GAP_WIDTH_MIN = 90f;
    public static final float GAP_WIDTH_RAMP_METERS = 700f;
    public static final int PANEL_MAX_HP = 1; // hits required to shatter (kept at 1 for v1 — easy to raise later for "reinforced" panels)

    // Projectiles
    public static final float PROJECTILE_RADIUS = 8f;
    public static final float PROJECTILE_SPEED = 900f;
    public static final int START_AMMO = 5;
    public static final int MAX_AMMO = 9;

    // Pickups
    public static final float PICKUP_RADIUS = 12f;
    public static final float PICKUP_SPAWN_CHANCE = 0.55f; // chance a pickup spawns in a gap
    public static final float PICKUP_AMMO_WEIGHT = 0.6f;   // vs shard pickup
    public static final int PICKUP_AMMO_VALUE = 2;
    public static final int PICKUP_SHARD_VALUE = 15;

    // Scoring & currency
    public static final int SCORE_PER_METER = 1;
    public static final int SHARD_PER_PANEL_BROKEN = 5;
    public static final float COMBO_MULTIPLIER_STEP = 0.1f;
    public static final float COMBO_MAX_MULTIPLIER = 3.0f;
    public static final int COMBO_BREAK_ON_MISS = 1; // misses before combo resets to 1x

    // Continues
    public static final int CONTINUE_TOKEN_COST = 50; // shards
    public static final int MAX_CONTINUES_PER_RUN = 1;

    // Shop
    public static final int AMMO_UPGRADE_MAX_LEVEL = 4;
    public static final int[] AMMO_UPGRADE_COST = {80, 160, 320, 640}; // cost to reach level 1..4, each +1 starting ammo
    public static final String[] SKIN_IDS = {"default", "ruby", "emerald", "gold", "prism"};
    public static final int[] SKIN_COST = {0, 100, 150, 220, 400};
}
