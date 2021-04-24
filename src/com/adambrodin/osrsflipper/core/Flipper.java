package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.gui.IngameGUI;
import com.adambrodin.osrsflipper.logic.FlipFinder;
import com.adambrodin.osrsflipper.misc.AccountSetup;
import com.adambrodin.osrsflipper.misc.BotConfig;
import com.adambrodin.osrsflipper.models.ActiveFlip;
import com.adambrodin.osrsflipper.models.FlipItem;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.grandexchange.GrandExchange;

import java.util.ArrayList;
import java.util.List;

import static org.dreambot.api.methods.MethodProvider.*;

public class Flipper {
    public static List<ActiveFlip> activeFlips = new ArrayList<>();
    private static FlipFinder flipFinder = new FlipFinder();

    public static void ExecuteFlips() {
        if (AccountSetup.IsReadyToTrade()) {
            IngameGUI.currentAction = "Waiting for flips to update";
            CheckFlips();

            int cashInInventory = 0;
            if (Inventory.contains("Coins")) {
                cashInInventory = Inventory.count("Coins");
            }

            if (!Inventory.contains("Coins") || cashInInventory < BotConfig.MIN_GOLD_FOR_FLIP) {
                IngameGUI.currentAction = "Too little cash to flip!";
            } else if (GEController.AmountOfSlotsAvailable() > 0) {
                int availableGp = cashInInventory;
                if (availableGp >= BotConfig.MIN_CASHSTACK_FOR_PERCENTAGE_FLIP && GEController.GetAvailableSlotsAmount() > 1) {
                    availableGp = (int) (cashInInventory * 0.7);
                }

                FlipItem bestItem = flipFinder.GetBestMarginItem(availableGp);
                log("Best item: " + bestItem.item.itemName + " at " + bestItem.marginPerc + "% margin - " + bestItem.marginGp + "gp - potential profit: " + bestItem.potentialProfitGp + "gp"
                        + " - avgLowPrice: " + bestItem.avgLowPrice + " - averaged volume: " + bestItem.averagedVolume);
                GEController.TransactItem(bestItem, true, bestItem.maxAmountAvailable);
            }
        }

        sleep(5000);
    }

    private static void CheckFlips() {
        if (!activeFlips.isEmpty()) {
            for (int i = 0; i < activeFlips.size(); i++) {
                ActiveFlip flip = activeFlips.get(i);
                float activeTimeMinutes = (float) ((System.currentTimeMillis() - flip.startedTimeEpochsMs) / 1000) / 60;
                float completedPercentage = GEController.GetCompletedPercentage(flip.item);

                if (activeTimeMinutes >= BotConfig.MAX_FLIP_ACTIVE_TIME_MINUTES || completedPercentage >= 95) {
                    IngameGUI.currentAction = "Cancelling - " + flip.item.item.itemName;
                    int profit = 0;

                    // Collect all items
                    GEController.CollectItem(flip.item, flip.buy);
                    sleep(2500);

                    boolean tradeCreated = false;

                    // If the flip is a buy
                    if (flip.buy) {
                        if (Inventory.contains(flip.item.item.itemName)) {
                            int amount = Inventory.get(flip.item.item.itemName).getAmount();
                            int sellPrice = flip.item.avgLowPrice + flip.item.marginGp;
                            log("Selling " + amount + "x " + flip.item.item.itemName);
                            tradeCreated = GrandExchange.sellItem(flip.item.item.itemName, amount, sellPrice);
                            if (tradeCreated) {
                                sleepUntil(() -> GEController.ItemInSlot(flip.item), BotConfig.MAX_ACTION_TIMEOUT_MS);
                                ActiveFlip sellFlip = new ActiveFlip(false, amount, flip.item);
                                sellFlip.item.potentialProfitGp = amount * sellPrice;
                                activeFlips.add(sellFlip);
                                log("Added new active flip (SELL): " + amount + "x " + flip.item.item.itemName + " for " + sellPrice + " each");
                            }
                        } else { // All items were fully bought, simply remove flip
                            tradeCreated = true;
                        }
                        // If the flip is a sell and the inventory still contains the item
                    } else if (Inventory.contains(flip.item.item.itemName)) {
                        // Force sell the rest of the items that weren't sold
                        int amount = Inventory.get(flip.item.item.itemName).getAmount();
                        profit += (flip.amount - amount) * flip.item.marginGp;
                        log("Force-selling " + amount + "x " + flip.item.item.itemName);
                        tradeCreated = GrandExchange.sellItem(flip.item.item.itemName, amount, 1);
                        boolean finalTradeCreated = tradeCreated;
                        sleepUntil(() -> finalTradeCreated && GEController.GetCompletedPercentage(flip.item) >= 100, BotConfig.MAX_ACTION_TIMEOUT_MS);
                        sleep(2000);
                        GrandExchange.collect();
                        sleep(1000);
                        sleepUntil(() -> !GrandExchange.isReadyToCollect(), BotConfig.MAX_ACTION_TIMEOUT_MS);
                    } else if (completedPercentage >= 100) { // All items were fully sold, simply remove flip
                        profit += flip.amount * flip.item.marginGp;
                        log("Flip (" + flip.item.item.itemName + ") was fully sold, removing!");
                        tradeCreated = true;
                    }

                    if (tradeCreated) {
                        if (!flip.buy) {
                            // Display the profit
                            IngameGUI.sessionProfit += profit;
                            log("Flip [" + (flip.buy ? "BUY" : "SELL") + "]" + "(" + flip.amount + "x " + flip.item.item.itemName + " ended with a profit of: " + profit + " gp");
                        }
                        activeFlips.remove(flip);
                    }
                }
            }
            sleep(2000);
        }
    }
}
