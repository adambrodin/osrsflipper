package com.adambrodin.osrsflipper.models;

import com.adambrodin.osrsflipper.core.GEController;

import static org.dreambot.api.methods.MethodProvider.log;

public class ActiveFlip {
    public int slot = -1; // -1 means it hasn't been set yet
    public boolean buy;
    public int amount;
    public FlipItem item;
    public long startedTimeEpochsMs;

    public ActiveFlip(boolean buy, int amount, FlipItem item) {
        this.buy = buy;
        this.amount = amount;
        this.item = item;
        this.startedTimeEpochsMs = System.currentTimeMillis();
        this.slot = GEController.GetSlotFromItem(item);
        log(item.item.itemName + " was found in slot no. " + slot);
    }
}
