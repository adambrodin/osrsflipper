package com.adambrodin.osrsflipper.models;

public class ActiveFlip {
    public boolean buy;
    public int amount;
    public FlipItem item;
    public long startedTimeEpochsMs;

    public ActiveFlip(boolean buy, int amount, FlipItem item) {
        this.buy = buy;
        this.amount = amount;
        this.item = item;
        this.startedTimeEpochsMs = System.currentTimeMillis();
    }
}
