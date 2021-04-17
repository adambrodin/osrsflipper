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
            CheckFlips();

            int cashInInventory = 0;
            if (Inventory.contains("Coins")) {
                cashInInventory = Inventory.count("Coins");
            }

            if (!Inventory.contains("Coins") || cashInInventory < BotConfig.MIN_GOLD_FOR_FLIP) {
                IngameGUI.currentAction = "Too little cash to execute new flips!";
            } else if (GEController.AmountOfSlotsAvailable() > 0) {
                //int availableGp = (int) ((double) cashInInventory * (double) (BotConfig.MAX_CASHSTACK_PERCENTAGE_PER_FLIP/100)); TODO FIX CALCULATION
                int availableGp = cashInInventory;
                if (availableGp >= BotConfig.MIN_CASHSTACK_FOR_PERCENTAGE_FLIP) {
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
                log(flip.item.item.itemName + " - " + "active time minutes: " + activeTimeMinutes + " - (SECONDS: " + (System.currentTimeMillis() - flip.startedTimeEpochsMs) / 1000 + ") - " + GEController.GetCompletedPercentage(flip.item)+"%");

                if (activeTimeMinutes >= BotConfig.MAX_FLIP_ACTIVE_TIME_MINUTES || GEController.GetCompletedPercentage(flip.item) >= 95) {
                    int profit = 0;
                    if (!flip.buy) {
                        profit = GEController.GetSlotGoldAmount(flip.item);
                    }

                    // Collect all items
                    GEController.CollectItem(flip.item);
                    sleep(2500);

                    boolean tradeCreated = false;

                    // If inventory has item (may not have any if flip did not change at all (0% completion))
                    if (flip.buy && Inventory.contains(flip.item.item.itemName)) {
                        int amount = Inventory.get(flip.item.item.itemName).getAmount();
                        int sellPrice = flip.item.avgLowPrice + flip.item.marginGp;
                        tradeCreated = GrandExchange.sellItem(flip.item.item.itemName, amount, sellPrice);
                        if (tradeCreated) {
                            activeFlips.add(new ActiveFlip(false, amount, flip.item));
                            log("Added new active flip (SELL): " + amount + "x " + flip.item.item.itemName + " for " + sellPrice + " each");
                        }
                    } else if (Inventory.contains(flip.item.item.itemName)) {
                        // Force sell the rest of the items that weren't sold
                        int amount = Inventory.get(flip.item.item.itemName).getAmount();
                        log("Force-selling " + amount + "x " + flip.item.item.itemName);
                        tradeCreated = GrandExchange.sellItem(flip.item.item.itemName, amount, 1);
                        boolean finalTradeCreated = tradeCreated;
                        sleepUntil(() -> finalTradeCreated && GEController.GetCompletedPercentage(flip.item) >= 100, BotConfig.MAX_ACTION_TIMEOUT_MS);
                        profit = GEController.GetSlotGoldAmount(flip.item) - (amount * flip.item.avgLowPrice);
                        sleep(2000);
                        GrandExchange.collect();
                        sleep(1000);
                        sleepUntil(() -> !GrandExchange.isReadyToCollect(), BotConfig.MAX_ACTION_TIMEOUT_MS);
                    } else {
                        tradeCreated = true;
                        profit -= (flip.amount * flip.item.avgLowPrice);
                    }

                    if (tradeCreated) {
                        // Display the profit
                        IngameGUI.sessionProfit += profit;
                        log("Flip [BUY: " + flip.buy + "]" + "(" + flip.amount + "x " + flip.item.item.itemName + " ended with a profit of: " + profit + " gp");
                        activeFlips.remove(flip);
                    }
                }
            }
            sleep(2000);
        }
    }
}
