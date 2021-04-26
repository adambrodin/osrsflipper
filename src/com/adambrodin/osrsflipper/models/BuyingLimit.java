package com.adambrodin.osrsflipper.models;

import java.time.LocalTime;

public class BuyingLimit {
    public int amountUsed;
    public LocalTime expiryTime;

    public BuyingLimit(int amountUsed, LocalTime expiryTime) {
        this.amountUsed = amountUsed;
        this.expiryTime = expiryTime;
    }
}
