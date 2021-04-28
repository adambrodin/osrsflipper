package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.gui.IngameGUI;
import com.adambrodin.osrsflipper.io.SaveManager;
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
    private static final FlipFinder flipFinder = new FlipFinder();
    public static List<ActiveFlip> activeFlips = new ArrayList<>();

    public static void ExecuteFlips() {
        if (AccountSetup.IsReadyToTrade()) {
            IngameGUI.currentAction = "Waiting for flips to update";
            CheckFlips();

            if (GrandExchange.isReadyToCollect()) {
                GrandExchange.collect();
                sleepUntil(() -> !GrandExchange.isReadyToCollect(), BotConfig.MAX_ACTION_TIMEOUT_MS);
            }

            int cashInInventory = 0;
            if (Inventory.contains("Coins")) {
                cashInInventory = Inventory.count("Coins");
            }

            if (!Inventory.contains("Coins") || cashInInventory < BotConfig.MIN_GOLD_FOR_FLIP) {
                IngameGUI.currentAction = "Too little cash to flip!";
            } else if (GEController.AmountOfSlotsAvailable() > 0) {
                int availableGp = cashInInventory;
                if (availableGp >= BotConfig.MIN_CASHSTACK_FOR_PERCENTAGE_FLIP && GEController.GetAvailableSlotsAmount() > 1) {
                    availableGp = (int) (cashInInventory * 0.65);
                }

                FlipItem bestItem = flipFinder.GetBestItem(availableGp);
                GEController.TransactItem(bestItem, true, bestItem.maxAmountAvailable);
                SaveManager.SaveActiveFlips(activeFlips);
            }
        }
    }

    private static void CheckFlips() {
        if (!activeFlips.isEmpty()) {
            for (int i = 0; i < activeFlips.size(); i++) {
                ActiveFlip flip = activeFlips.get(i);

                float activeTimeMinutes = (float) ((System.currentTimeMillis() - flip.startedTimeEpochsMs) / 1000) / 60;
                float completedPercentage = GEController.GetCompletedPercentage(flip.item, flip.amount);

                if (activeTimeMinutes >= BotConfig.MAX_FLIP_ACTIVE_TIME_MINUTES || completedPercentage >= 95 || (!GEController.ItemInSlot(flip.item) && Inventory.contains(flip.item.item.itemName))) {
                    IngameGUI.currentAction = "Cancelling - " + flip.item.item.itemName;
                    int profit = 0;

                    // Collect all items
                    GEController.CollectItem(flip.item, flip.buy, flip.amount);
                    sleep(2500);

                    boolean tradeCreated = false;

                    if (GrandExchange.getFirstOpenSlot() != -1) {
                        // If the flip is a buy
                        if (flip.buy) {
                            if (Inventory.contains(flip.item.item.itemName)) {
                                logInfo("Flip [BUY] - (" + flip.amount + "x " + flip.item.item.itemName + ") has finished, selling!");
                                int amount = Inventory.get(flip.item.item.itemName).getAmount();
                                int sellPrice = flip.item.avgLowPrice + flip.item.marginGp;

                                // Subtracts the used limit by the ones that were not bought (eg 500 out of 1000 bought, remove 500 from used limit)
                                SaveManager.ModifyLimit(flip.item, flip.amount, flip.amount - amount);
                                tradeCreated = GrandExchange.sellItem(flip.item.item.itemName, amount, sellPrice);
                                if (tradeCreated) {
                                    sleepUntil(() -> GEController.ItemInSlot(flip.item), BotConfig.MAX_ACTION_TIMEOUT_MS);
                                    ActiveFlip sellFlip = new ActiveFlip(false, amount, flip.item);
                                    sellFlip.amount = amount;
                                    sellFlip.item.potentialProfitGp = amount * flip.item.marginGp;
                                    activeFlips.add(sellFlip);
                                    logInfo("Added new active flip [SELL]: " + amount + "x " + flip.item.item.itemName + " for " + sellPrice + "gp each - potential profit: " + IngameGUI.GetFormattedGold(sellFlip.item.potentialProfitGp, true));
                                }
                            } else if (!GEController.ItemInSlot(flip.item)) { // No items were bought, simply remove flip
                                // Remove used limit (no limit was used)
                                SaveManager.RemoveLimit(flip.item, flip.amount);
                                tradeCreated = true;
                            }
                            // If the flip is a sell and the inventory still contains the item
                        } else if (Inventory.contains(flip.item.item.itemName)) {
                            // Force sell the rest of the items that weren't sold
                            int amount = Inventory.get(flip.item.item.itemName).getAmount();
                            profit += (flip.amount - amount) * flip.item.marginGp;
                            logInfo("Force-selling " + amount + "x " + flip.item.item.itemName);
                            tradeCreated = GrandExchange.sellItem(flip.item.item.itemName, amount, 1);
                            boolean finalTradeCreated = tradeCreated;
                            sleepUntil(() -> finalTradeCreated && GEController.GetCompletedPercentage(flip.item, flip.amount) >= 100, BotConfig.MAX_ACTION_TIMEOUT_MS);
                            sleep(2000);
                            GrandExchange.collect();
                            sleep(1000);
                            sleepUntil(() -> !GrandExchange.isReadyToCollect(), BotConfig.MAX_ACTION_TIMEOUT_MS);
                        } else if (completedPercentage >= 100 && !GEController.ItemInSlot(flip.item)) { // All items were fully sold, simply remove flip
                            profit += flip.amount * flip.item.marginGp;
                            logInfo("Flip [SELL] - (" + flip.amount + "x " + flip.item.item.itemName + ") is fully sold, removing!");
                            tradeCreated = true;
                        }

                        if (tradeCreated) {
                            if (!flip.buy) {
                                // Display the profit
                                IngameGUI.sessionProfit += profit;
                                logInfo("Flip [SELL]" + " - (" + flip.amount + "x " + flip.item.item.itemName + ") ended with a profit of: " + IngameGUI.GetFormattedGold(profit, true));
                            }
                            activeFlips.remove(flip);
                        }

                        SaveManager.SaveActiveFlips(activeFlips);
                    }
                }
            }
            sleep(1000);
        }
    }
}
