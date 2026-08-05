package com.prudhvinelaturi.glassrush.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

/**
 * Lazily loads short SFX from {@code sfx/<name>.ogg}. Missing files are silently skipped
 * rather than crashing — lets gameplay code call {@link #play} before real audio assets exist.
 */
public class SfxManager {
    private final Map<String, Sound> cache = new HashMap<>();
    private float volume = 1f;
    private boolean muted = false;

    public void play(String name) {
        if (muted) return;
        Sound sound = cache.computeIfAbsent(name, this::loadOrNull);
        if (sound != null) {
            sound.play(volume);
        }
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    public void dispose() {
        for (Sound s : cache.values()) {
            if (s != null) s.dispose();
        }
        cache.clear();
    }

    private Sound loadOrNull(String name) {
        FileHandle handle = Gdx.files.internal("sfx/" + name + ".ogg");
        if (!handle.exists()) return null;
        return Gdx.audio.newSound(handle);
    }
}
