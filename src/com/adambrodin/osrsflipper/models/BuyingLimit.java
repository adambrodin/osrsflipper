package com.adambrodin.osrsflipper.models;

import java.time.LocalDateTime;

public class BuyingLimit {
    public FlipItem item;
    public int amountUsed;
    public LocalDateTime expiryTime;

    public BuyingLimit(FlipItem item, int amountUsed, LocalDateTime expiryTime) {
        this.item = item;
        this.amountUsed = amountUsed;
        this.expiryTime = expiryTime;
    }
}
