package com.prudhvinelaturi.glassrush.entities;

import com.prudhvinelaturi.glassrush.config.GameConfig;

/** A ball fired upward by the player to shatter a glass panel blocking the way. */
public class Projectile {
    public float x;
    public float y;
    public boolean alive = true;

    public Projectile(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(float delta) {
        y += GameConfig.PROJECTILE_SPEED * delta;
    }
}
