package com.prudhvinelaturi.glassrush.ads;

/**
 * Rewarded-ad contract. {@link NoOpAdManager} is what ships in v1 (matches ChaiTapriTycoon's
 * launch precedent — ads land after retention data justifies wiring a real network).
 * TODO(launch-blocker): implement an AdMob-backed AndroidAdManager once Prudhvi has created
 * an AdMob account + app entry for GlassRush and can supply real ad unit IDs.
 */
public interface AdManager {
    boolean isRewardedAdReady();

    /** Shows a rewarded ad if available. Exactly one of the callbacks fires. */
    void showRewardedAd(Runnable onRewardEarned, Runnable onFailedOrCancelled);
}
