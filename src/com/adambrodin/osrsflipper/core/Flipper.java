package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.gui.IngameGUI;
import com.adambrodin.osrsflipper.io.SaveManager;
import com.adambrodin.osrsflipper.logic.FlipFinder;
import com.adambrodin.osrsflipper.misc.AccountSetup;
import com.adambrodin.osrsflipper.misc.BotConfig;
import com.adambrodin.osrsflipper.models.ActiveFlip;
import com.adambrodin.osrsflipper.models.BuyingLimit;
import com.adambrodin.osrsflipper.models.FlipItem;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.grandexchange.GrandExchange;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.dreambot.api.methods.MethodProvider.*;

public class Flipper {
    private static final FlipFinder flipFinder = new FlipFinder();
    public static List<ActiveFlip> activeFlips = new ArrayList<>();

    public static void ExecuteFlips() {
        // Checks if trading is possible and then prioritises sell flips if needed
        if (AccountSetup.IsReadyToTrade() && !activeFlips.stream().anyMatch(flip -> !GEController.ItemInSlot(flip.item) && Inventory.contains(flip.item.item.itemName))) {
            if (GrandExchange.isReadyToCollect()) {
                GrandExchange.collect();
                sleepUntil(() -> !GrandExchange.isReadyToCollect(), BotConfig.MAX_ACTION_TIMEOUT_MS);
            }

            int cashInInventory = Inventory.contains("Coins") ? Inventory.count("Coins") : 0;
            if (!Inventory.contains("Coins") || cashInInventory < BotConfig.MIN_GOLD_FOR_FLIP) {
                IngameGUI.currentAction = "Too little cash to flip!";
            } else if (GEController.AmountOfSlotsAvailable() > 0) {
                int availableGp = cashInInventory;
                if (availableGp >= BotConfig.MIN_CASHSTACK_FOR_PERCENTAGE_FLIP && GEController.GetAvailableSlotsAmount() > 1) {
                    availableGp = (int) (cashInInventory * BotConfig.MAX_CASHSTACK_PERCENTAGE_PER_FLIP);
                }

                FlipItem bestItem;
                if (activeFlips.stream().anyMatch(flip -> flip.item.skippedRequirements)) {
                    bestItem = flipFinder.GetBestItem(availableGp, false);
                } else {
                    availableGp = (int) (cashInInventory * BotConfig.MAX_CASHSTACK_PERCENTAGE_FOR_RISKY_FLIP);
                    log("The next flip will be fetched without using volume/margin rules!");
                    bestItem = flipFinder.GetBestItem(availableGp, true);
                    bestItem.skippedRequirements = true;
                }

                GEController.TransactItem(bestItem, true, bestItem.maxAmountAvailable);
                SaveManager.SaveActiveFlips(activeFlips);
            }

            IngameGUI.currentAction = "Waiting for flips to update";
            CheckFlips();
        }
    }

    private static void CheckFlips() {
        if (!SaveManager.tradingInfo.usedBuyingLimits.isEmpty()) {
            for (BuyingLimit limit : SaveManager.tradingInfo.usedBuyingLimits) {
                if (Duration.between(LocalDateTime.now(), limit.expiryTime).toHours() >= BotConfig.BUYING_LIMIT_HOURS) {
                    SaveManager.tradingInfo.usedBuyingLimits.remove(limit);
                    logInfo("Removed " + limit.amountUsed + "x used limit from " + limit.item.item.itemName + "! - Surpassed time limit.");
                    continue;
                }
            }
        }

        if (activeFlips.isEmpty()) {
            log("No active flips were found!");
            IngameGUI.currentAction = "No active flips found";
            return;
        }

        for (int i = 0; i < activeFlips.size(); i++) {
            ActiveFlip flip = activeFlips.get(i);
            float activeTimeMinutes = (float) ((System.currentTimeMillis() - flip.startedTimeEpochsMs) / 1000) / 60;
            float completedPercentage = GEController.GetCompletedPercentage(flip.item, flip.amount);
            boolean shouldExitFlip = activeTimeMinutes >= BotConfig.MAX_FLIP_ACTIVE_TIME_MINUTES || completedPercentage >= BotConfig.MAX_FLIP_COMPLETED_PERC_EXIT || (!GEController.ItemInSlot(flip.item) && Inventory.contains(flip.item.item.itemName));
            if (shouldExitFlip) {
                int profit = 0;

                // Collect the items/end the active transaction
                GEController.CollectItem(flip.item, flip.buy, flip.amount);
                sleepUntil(() -> (!GEController.ItemInSlot(flip.item) && Inventory.contains(flip.item)) || completedPercentage < 0, BotConfig.MAX_ACTION_TIMEOUT_MS);
                int amountInInv = Inventory.contains(flip.item.item.itemName) ? Inventory.get(flip.item.item.itemName).getAmount() : 0;

                if (flip.buy) {
                    // Bought over the % limit
                    if (completedPercentage >= BotConfig.MIN_FLIP_NORMAL_SELL_PERC && Inventory.contains(flip.item.item.itemName)) {
                        // Remove the unused buying limit (if < 100% was bought)
                        SaveManager.ModifyLimit(flip.item, flip.amount, flip.amount - amountInInv);

                        ActiveFlip normalSellFlip = GEController.TransactItem(flip.item, false, amountInInv);
                        if (normalSellFlip == null) { // If something goes wrong when selling (items remain in inv)
                            return;
                        }

                        if (flip.item.skippedRequirements) {
                            normalSellFlip.item.skippedRequirements = true;
                        }

                        activeFlips.add(normalSellFlip);
                    }

                    // Limit not reached - force-sell items
                    else if (Inventory.contains(flip.item.item.itemName)) {
                        int forceProfit = ForceSell(flip);
                        profit += forceProfit;
                    }

                    // No items were bought
                    else {
                        SaveManager.RemoveLimit(flip.item, flip.amount);
                    }
                } else {
                    // If it's a sell flip
                    if (completedPercentage >= 100) {
                        profit = flip.amount * flip.item.marginGp;
                    } else {
                        logInfo("Force-selling " + amountInInv + "x " + flip.item.item.itemName);

                        int forceProfit = ForceSell(flip);
                        profit += forceProfit;
                    }
                }

                IngameGUI.sessionProfit += profit;
                flip.completedEpochsMs = System.currentTimeMillis();
                SaveManager.AddCompletedFlip(flip);
                logInfo(flip.toString() + " ENDED with a profit of: " + IngameGUI.GetFormattedGold(profit, true));
                activeFlips.remove(activeFlips.get(i));
            }
        }
    }

    private static int ForceSell(ActiveFlip flip) {
        int amount = Inventory.count(flip.item.item.itemName);
        GrandExchange.sellItem(flip.item.item.itemName, amount, 1);
        sleepUntil(() -> GEController.ItemInSlot(flip.item) && GEController.GetCompletedPercentage(flip.item, amount) >= 100, BotConfig.MAX_ACTION_TIMEOUT_MS);
        int receivedGold = GEController.GetTransferredValue(flip.item);
        GrandExchange.collect();
        sleepUntil(() -> !GrandExchange.isReadyToCollect(), BotConfig.MAX_ACTION_TIMEOUT_MS);
        return receivedGold - (amount * flip.item.avgLowPrice);
    }
}
