package com.adambrodin.osrsflipper.models;

import java.time.LocalDateTime;

public class BuyingLimit {
    public int amountUsed;
    public LocalDateTime expiryTime;

    public BuyingLimit(int amountUsed, LocalDateTime expiryTime) {
        this.amountUsed = amountUsed;
        this.expiryTime = expiryTime;
    }
}
