package com.prudhvinelaturi.glassrush.logic;

/** Emitted by {@link GameEngine#update} so the rendering layer can trigger juice (particles/sfx/screen shake). */
public enum GameEvent {
    PANEL_SHATTERED,
    PANEL_DODGED,
    AMMO_PICKUP,
    SHARD_PICKUP,
    PLAYER_HIT,
    GAME_OVER
}
