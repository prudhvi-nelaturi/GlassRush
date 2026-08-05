package com.prudhvinelaturi.glassrush.billing;

/**
 * In-app purchase contract. {@link NoOpBillingManager} is what ships in v1 — no SKUs
 * are purchasable yet (matches ChaiTapriTycoon's launch precedent).
 * TODO(launch-blocker): implement a Play Billing-backed AndroidBillingManager once
 * at least one in-app product is configured in Play Console.
 */
public interface BillingManager {
    boolean isPurchased(String sku);

    void purchase(String sku, Runnable onSuccess, Runnable onFailedOrCancelled);
}
