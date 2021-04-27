package com.adambrodin.osrsflipper.models;

import com.adambrodin.osrsflipper.core.GEController;
import com.adambrodin.osrsflipper.io.SaveManager;

public class ActiveFlip {
    public int slot;
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

        if (buy) {
            // Adds & saves the used limit to prevent double purchasing when it isn't possible
            SaveManager.AddUsedLimit(item, amount);
        }

        SaveManager.tradingInfo.totalFlipsInitiated++;
    }
}
