package com.prudhvinelaturi.glassrush.logic;

import com.prudhvinelaturi.glassrush.config.GameConfig;
import com.prudhvinelaturi.glassrush.entities.GlassPanel;
import com.prudhvinelaturi.glassrush.entities.Pickup;
import com.prudhvinelaturi.glassrush.entities.Projectile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Pure game logic — no rendering, no LibGDX Graphics/Input dependency, no storage.
 * Takes a {@link RunState} and a delta time, returns what happened this frame as
 * {@link GameEvent}s. Fully unit-testable on a plain JVM.
 */
public class GameEngine {
    private final Random random;

    public GameEngine() {
        this(new Random());
    }

    /** Seeded constructor for deterministic tests. */
    public GameEngine(long seed) {
        this(new Random(seed));
    }

    private GameEngine(Random random) {
        this.random = random;
    }

    public List<GameEvent> update(RunState state, float delta) {
        List<GameEvent> events = new ArrayList<>();
        if (!state.alive) return events;

        state.scrollSpeed = Math.min(GameConfig.MAX_SCROLL_SPEED,
                GameConfig.BASE_SCROLL_SPEED + state.distanceMeters * GameConfig.SPEED_RAMP_PER_METER);

        float scrolled = state.scrollSpeed * delta;
        state.distanceMeters += scrolled * GameConfig.DISTANCE_PER_WORLD_UNIT;
        state.score = (int) (state.distanceMeters * GameConfig.SCORE_PER_METER);

        state.player.update(delta);

        maybeSpawnPanel(state, scrolled);
        updateProjectiles(state, delta, events);
        updatePanels(state, scrolled, events);
        updatePickups(state, scrolled, events);

        if (!state.alive) {
            events.add(GameEvent.GAME_OVER);
        }
        return events;
    }

    public void movePlayer(RunState state, float targetWorldX) {
        float clamped = Math.max(state.player.radius, Math.min(GameConfig.WORLD_WIDTH - state.player.radius, targetWorldX));
        state.player.targetX = clamped;
    }

    public boolean fireProjectile(RunState state) {
        if (state.ammo <= 0 || !state.alive) return false;
        state.ammo--;
        state.projectiles.add(new Projectile(state.player.x, state.player.y + state.player.radius));
        return true;
    }

    /** Spends a continue token (or grants one free continue via rewarded ad) to revive mid-run. */
    public boolean continueRun(RunState state, PlayerProfile profile, boolean viaAd) {
        if (state.alive || state.usedContinue) return false;
        if (!viaAd) {
            if (profile.continueTokens <= 0) return false;
            profile.continueTokens--;
        }
        state.usedContinue = true;
        state.alive = true;
        // Clear any panel sitting right on top of the player so they don't die again instantly.
        state.panels.removeIf(p -> !p.broken && p.blocksAt(state.player.x, state.player.radius)
                && p.y <= state.player.y + GameConfig.PANEL_HEIGHT);
        return true;
    }

    private void maybeSpawnPanel(RunState state, float scrolled) {
        float traveled = state.advanceSpawnTimer(scrolled);
        if (traveled >= state.spawnThreshold()) {
            spawnPanel(state);
            float intervalSeconds = difficultyLerp(state.distanceMeters,
                    GameConfig.PANEL_SPAWN_INTERVAL_BASE, GameConfig.PANEL_SPAWN_INTERVAL_MIN, GameConfig.PANEL_SPAWN_RAMP_METERS);
            state.resetSpawnTimer(intervalSeconds * state.scrollSpeed);
        }
    }

    private void spawnPanel(RunState state) {
        float gapWidth = difficultyLerp(state.distanceMeters,
                GameConfig.GAP_WIDTH_BASE, GameConfig.GAP_WIDTH_MIN, GameConfig.GAP_WIDTH_RAMP_METERS);
        float gapX = GameConfig.PLAYER_RADIUS + random.nextFloat() * (GameConfig.WORLD_WIDTH - gapWidth - GameConfig.PLAYER_RADIUS * 2f);
        GlassPanel panel = new GlassPanel(GameConfig.WORLD_HEIGHT + GameConfig.PANEL_HEIGHT, gapX, gapWidth);
        state.panels.add(panel);

        if (random.nextFloat() < GameConfig.PICKUP_SPAWN_CHANCE) {
            Pickup.Type type = random.nextFloat() < GameConfig.PICKUP_AMMO_WEIGHT ? Pickup.Type.AMMO : Pickup.Type.SHARD;
            float px = gapX + gapWidth / 2f;
            state.pickups.add(new Pickup(px, panel.y, type));
        }
    }

    private void updateProjectiles(RunState state, float delta, List<GameEvent> events) {
        Iterator<Projectile> it = state.projectiles.iterator();
        while (it.hasNext()) {
            Projectile proj = it.next();
            proj.update(delta);
            boolean consumed = false;
            for (GlassPanel p : state.panels) {
                if (!p.broken
                        && Math.abs(proj.y - p.y) < GameConfig.PANEL_HEIGHT / 2f + GameConfig.PROJECTILE_RADIUS
                        && p.blocksAt(proj.x, GameConfig.PROJECTILE_RADIUS)) {
                    p.hp--;
                    if (p.hp <= 0) {
                        p.broken = true;
                        state.comboMultiplier = Math.min(GameConfig.COMBO_MAX_MULTIPLIER,
                                state.comboMultiplier + GameConfig.COMBO_MULTIPLIER_STEP);
                        state.shardsThisRun += Math.round(GameConfig.SHARD_PER_PANEL_BROKEN * state.comboMultiplier);
                        events.add(GameEvent.PANEL_SHATTERED);
                    }
                    consumed = true;
                    break;
                }
            }
            if (consumed || proj.y > GameConfig.WORLD_HEIGHT + 50f) {
                it.remove();
            }
        }
    }

    private void updatePanels(RunState state, float scrolled, List<GameEvent> events) {
        Iterator<GlassPanel> it = state.panels.iterator();
        while (it.hasNext()) {
            GlassPanel p = it.next();
            p.y -= scrolled;

            if (!p.scored && p.y <= state.player.y) {
                p.scored = true;
                if (p.blocksAt(state.player.x, state.player.radius)) {
                    state.alive = false;
                    events.add(GameEvent.PLAYER_HIT);
                } else if (!p.broken) {
                    // Dodged through the gap without shooting — resets the combo (safe but no style points).
                    state.comboMultiplier = 1f;
                    events.add(GameEvent.PANEL_DODGED);
                }
            }

            if (p.y < -GameConfig.PANEL_HEIGHT * 2f) {
                it.remove();
            }
        }
    }

    private void updatePickups(RunState state, float scrolled, List<GameEvent> events) {
        Iterator<Pickup> it = state.pickups.iterator();
        while (it.hasNext()) {
            Pickup pk = it.next();
            pk.y -= scrolled;
            if (!pk.collected) {
                float dx = pk.x - state.player.x;
                float dy = pk.y - state.player.y;
                float distSq = dx * dx + dy * dy;
                float rSum = GameConfig.PICKUP_RADIUS + state.player.radius;
                if (distSq < rSum * rSum) {
                    pk.collected = true;
                    if (pk.type == Pickup.Type.AMMO) {
                        state.ammo = Math.min(GameConfig.MAX_AMMO, state.ammo + GameConfig.PICKUP_AMMO_VALUE);
                        events.add(GameEvent.AMMO_PICKUP);
                    } else {
                        state.shardsThisRun += GameConfig.PICKUP_SHARD_VALUE;
                        events.add(GameEvent.SHARD_PICKUP);
                    }
                }
            }
            if (pk.y < -GameConfig.PANEL_HEIGHT * 2f) {
                it.remove();
            }
        }
    }

    /** Linearly interpolates from {@code base} to {@code min} as distance goes from 0 to {@code rampMeters}, then holds at {@code min}. */
    static float difficultyLerp(float distanceMeters, float base, float min, float rampMeters) {
        float t = Math.max(0f, Math.min(1f, distanceMeters / rampMeters));
        return base + (min - base) * t;
    }
}
