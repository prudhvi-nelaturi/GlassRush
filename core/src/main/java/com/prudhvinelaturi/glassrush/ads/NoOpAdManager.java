package com.prudhvinelaturi.glassrush.ads;

/** Ships in v1: no ad network wired up yet. Never reports a rewarded ad as ready. */
public class NoOpAdManager implements AdManager {
    @Override
    public boolean isRewardedAdReady() {
        return false;
    }

    @Override
    public void showRewardedAd(Runnable onRewardEarned, Runnable onFailedOrCancelled) {
        onFailedOrCancelled.run();
    }
}
