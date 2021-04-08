package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.logic.FlipFinder;
import com.adambrodin.osrsflipper.misc.AccountSetup;
import com.adambrodin.osrsflipper.models.ActiveFlip;
import com.adambrodin.osrsflipper.models.FlipItem;
import org.dreambot.api.methods.container.impl.Inventory;

import java.util.List;

import static org.dreambot.api.methods.MethodProvider.log;

public class Flipper {
    public static List<ActiveFlip> activeFlips;
    private static FlipFinder flipFinder = new FlipFinder();

    public static void ExecuteFlips() {
        if (AccountSetup.IsReadyToTrade()) {
            if (GEController.AmountOfSlotsAvailable() > 0 && Inventory.contains("Coins")) {
                int availableGp = Inventory.count("Coins");
                log("Available cash: " + availableGp + "gp");
                FlipItem bestItem = flipFinder.GetBestMarginItem(availableGp);
                log("Best item: " + bestItem.item.itemName + " at " + bestItem.marginPerc + "% margin - " + bestItem.marginGp + "gp - potential profit: " + bestItem.potentialProfitGp + "gp"
                        + " - avgLowPrice: " + bestItem.avgLowPrice + " - averaged volume: " + bestItem.averagedVolume);
                GEController.TransactItem(bestItem, true, bestItem.maxAmountAvailable);
            }
        }
    }
}
