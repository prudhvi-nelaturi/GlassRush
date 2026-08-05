package com.prudhvinelaturi.glassrush.entities;

/** A collectible sitting in a panel's gap — either extra ammo or bonus shards. */
public class Pickup {
    public enum Type { AMMO, SHARD }

    public float x;
    public float y;
    public final Type type;
    public boolean collected = false;

    public Pickup(float x, float y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
