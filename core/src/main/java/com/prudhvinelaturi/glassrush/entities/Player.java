package com.prudhvinelaturi.glassrush.entities;

import com.prudhvinelaturi.glassrush.config.GameConfig;

/** The marble the player controls. Horizontal position only — vertical stays fixed near the bottom of the screen. */
public class Player {
    public float x;
    public float targetX;
    public final float y = GameConfig.PLAYER_Y;
    public final float radius = GameConfig.PLAYER_RADIUS;
    public String skinId = "default";

    public Player(float startX) {
        this.x = startX;
        this.targetX = startX;
    }

    public void update(float delta) {
        x += (targetX - x) * Math.min(1f, GameConfig.PLAYER_MOVE_LERP * (delta * 60f));
    }
}
