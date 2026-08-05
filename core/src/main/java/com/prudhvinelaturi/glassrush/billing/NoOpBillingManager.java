package com.prudhvinelaturi.glassrush.billing;

/** Ships in v1: no real store connection. Nothing is ever purchased. */
public class NoOpBillingManager implements BillingManager {
    @Override
    public boolean isPurchased(String sku) {
        return false;
    }

    @Override
    public void purchase(String sku, Runnable onSuccess, Runnable onFailedOrCancelled) {
        onFailedOrCancelled.run();
    }
}
