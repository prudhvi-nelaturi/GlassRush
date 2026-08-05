package com.prudhvinelaturi.glassrush.logic;

import com.prudhvinelaturi.glassrush.config.GameConfig;
import com.prudhvinelaturi.glassrush.entities.GlassPanel;
import com.prudhvinelaturi.glassrush.entities.Pickup;
import com.prudhvinelaturi.glassrush.entities.Player;
import com.prudhvinelaturi.glassrush.entities.Projectile;

import java.util.ArrayList;
import java.util.List;

/** Everything that exists for the lifetime of a single run. Thrown away on game-over (after continues, if any). */
public class RunState {
    public final Player player;
    public final List<GlassPanel> panels = new ArrayList<>();
    public final List<Projectile> projectiles = new ArrayList<>();
    public final List<Pickup> pickups = new ArrayList<>();

    public float distanceMeters = 0f;
    public float scrollSpeed = GameConfig.BASE_SCROLL_SPEED;
    public int ammo;
    public int score = 0;
    public int shardsThisRun = 0;
    public float comboMultiplier = 1f;
    public boolean alive = true;
    public boolean usedContinue = false;

    private float distanceSinceLastSpawn = 0f;
    private float spawnIntervalDistance;

    public RunState(PlayerProfile profile) {
        this.player = new Player(GameConfig.WORLD_WIDTH / 2f);
        this.player.skinId = profile.selectedSkin;
        this.ammo = GameConfig.START_AMMO + profile.ammoUpgradeLevel;
        if (this.ammo > GameConfig.MAX_AMMO) this.ammo = GameConfig.MAX_AMMO;
        this.spawnIntervalDistance = GameConfig.PANEL_SPAWN_INTERVAL_BASE * scrollSpeed;
    }

    float advanceSpawnTimer(float scrolled) {
        distanceSinceLastSpawn += scrolled;
        return distanceSinceLastSpawn;
    }

    void resetSpawnTimer(float intervalWorldUnits) {
        distanceSinceLastSpawn = 0f;
        spawnIntervalDistance = intervalWorldUnits;
    }

    float spawnThreshold() {
        return spawnIntervalDistance;
    }
}
