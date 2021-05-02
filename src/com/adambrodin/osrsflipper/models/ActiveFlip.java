package com.adambrodin.osrsflipper.models;

import com.adambrodin.osrsflipper.core.GEController;
import com.adambrodin.osrsflipper.gui.IngameGUI;
import com.adambrodin.osrsflipper.io.SaveManager;

public class ActiveFlip {
    public int slot;
    public boolean buy;
    public int amount;
    public FlipItem item;
    public long startedTimeEpochsMs, completedEpochsMs;

    public ActiveFlip(boolean buy, int amount, FlipItem item) {
        this.buy = buy;
        this.amount = amount;
        this.item = item;
        this.startedTimeEpochsMs = System.currentTimeMillis();
        this.slot = GEController.GetSlotFromItem(item, amount);

        if (buy) {
            // Adds & saves the used limit to prevent double purchasing when it isn't possible
            SaveManager.AddUsedLimit(item, amount);
        }

        SaveManager.tradingInfo.totalFlipsInitiated++;
    }

    @Override
    public String toString() {
        return "Flip [" + (buy ? "BUY" : "SELL") + "] - (" + amount + "x " + item.item.itemName + ") - Potential Profit: [" + IngameGUI.GetFormattedGold(item.potentialProfitGp, true)
                + "] - Averaged Volume: " + IngameGUI.GetFormattedGold((int) item.averagedVolume, true) + "/HR - Margin (" + item.marginGp + "gp - " + String.format("%.2f", item.marginPerc) + "%) - Average Low Price: " +
                item.avgLowPrice + "gp";
    }
}
