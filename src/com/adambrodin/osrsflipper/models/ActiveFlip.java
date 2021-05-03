package com.adambrodin.osrsflipper.models;

import com.adambrodin.osrsflipper.core.GEController;
import com.adambrodin.osrsflipper.gui.IngameGUI;
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
        this.slot = GEController.GetSlotFromItem(item, buy);

        if (buy) {
            // Adds & saves the used limit to prevent double purchasing when it isn't possible
            SaveManager.AddUsedLimit(item, amount);
        }

        SaveManager.tradingInfo.totalFlipsInitiated++;
    }

    @Override
    public String toString() {
        return "Flip [" + (buy ? "BUY" : "SELL") + "] - (" + amount + "x " + item.item.itemName + ") - Potential Profit: [" + IngameGUI.GetFormattedNumbers(item.potentialProfitGp, true, false)
                + "] - Averaged Volume: " + IngameGUI.GetFormattedNumbers((int) item.averagedVolume, true, true) + "/HR - Margin (" + item.marginGp + "gp - " + String.format("%.2f", item.marginPerc) + "%) - Average Low Price: " +
                IngameGUI.GetFormattedNumbers(item.avgLowPrice, false, false);
    }
}
