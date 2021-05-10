package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.gui.IngameGUI;
import com.adambrodin.osrsflipper.io.SaveManager;
import com.adambrodin.osrsflipper.logic.FlipFinder;
import com.adambrodin.osrsflipper.misc.AccountSetup;
import com.adambrodin.osrsflipper.misc.BotConfig;
import com.adambrodin.osrsflipper.models.ActiveFlip;
import com.adambrodin.osrsflipper.models.CompletedFlip;
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
        for (int i = 0; i < Flipper.activeFlips.size(); i++) {
            if (!Inventory.contains(Flipper.activeFlips.get(i).item.item.itemName) && !GEController.ItemInSlot(Flipper.activeFlips.get(i).item)) {
                log("Flip no longer active, removing! - " + Flipper.activeFlips.get(i).toString());
                Flipper.activeFlips.remove(Flipper.activeFlips.get(i));
                SaveManager.SaveActiveFlips(Flipper.activeFlips);
                continue;
            }
        }

        Main.currentAction = "Waiting for flips to update";
        CheckFlips();

        // Checks if trading is possible and then prioritises sell flips if needed
        if (AccountSetup.IsReadyToTrade() && !activeFlips.stream().anyMatch(flip -> !GEController.ItemInSlot(flip.item) && Inventory.contains(flip.item.item.itemName))) {
            if (GrandExchange.isReadyToCollect()) {
                GrandExchange.collect();
                sleepUntil(() -> !GrandExchange.isReadyToCollect(), BotConfig.MAX_ACTION_TIMEOUT_MS);
            }

            int cashInInventory = Inventory.contains("Coins") ? Inventory.count("Coins") : 0;
            if (!Inventory.contains("Coins") || cashInInventory < BotConfig.MIN_GOLD_FOR_FLIP) {
                if (Main.currentAction != "Too little cash to flip!" && Main.currentAction != "Waiting for flips to update") {
                    Main.currentAction = "Too little cash to flip!";
                }
            } else if (GEController.AmountOfSlotsAvailable() > 0) {
                int availableGp = cashInInventory;
                if (availableGp >= BotConfig.MIN_CASHSTACK_FOR_PERCENTAGE_FLIP && GEController.GetAvailableSlotsAmount() > 1) {
                    availableGp = (int) (cashInInventory * BotConfig.MAX_CASHSTACK_PERCENTAGE_PER_FLIP);
                }

                FlipItem bestItem;
                if (activeFlips.stream().anyMatch(flip -> flip.item.skippedRequirements)) {
                    bestItem = flipFinder.GetBestItem(availableGp, false);
                } else {
                    availableGp = GEController.AmountOfSlotsAvailable() > 1 && activeFlips.size() <= 5 ? (int) (cashInInventory * BotConfig.MAX_CASHSTACK_PERCENTAGE_FOR_RISKY_FLIP) : cashInInventory;
                    log("The next flip will be fetched without using volume/margin rules!");
                    bestItem = flipFinder.GetBestItem(availableGp, true);
                    bestItem.skippedRequirements = true;
                }

                GEController.TransactItem(bestItem, true, bestItem.maxAmountAvailable);
                SaveManager.SaveActiveFlips(activeFlips);
                sleep(500);
            }
        }
    }

    private static void CheckFlips() {
        if (!SaveManager.tradingInfo.usedBuyingLimits.isEmpty()) {
            for (int i = 0; i < SaveManager.tradingInfo.usedBuyingLimits.size() - 1; i++) {
                if (Duration.between(LocalDateTime.now(), SaveManager.tradingInfo.usedBuyingLimits.get(i).expiryTime).toHours() >= BotConfig.BUYING_LIMIT_HOURS) {
                    SaveManager.tradingInfo.usedBuyingLimits.remove(i);
                    logInfo("Removed " + SaveManager.tradingInfo.usedBuyingLimits.get(i).amountUsed + "x used limit from " + SaveManager.tradingInfo.usedBuyingLimits.get(i).item.item.itemName + "! - Surpassed time limit.");
                }
            }
        }

        if (activeFlips.isEmpty()) {
            log("No active flips were found!");
            Main.currentAction = "No active flips found";
            return;
        }

        for (int i = 0; i < activeFlips.size(); i++) {
            ActiveFlip flip = activeFlips.get(i);
            float activeTimeMinutes = (float) ((System.currentTimeMillis() - flip.startedTimeEpochsMs) / 1000) / 60;
            float completedPercentage = GEController.GetCompletedPercentage(flip.item, flip.buy);
            boolean shouldExitFlip = activeTimeMinutes >= BotConfig.MAX_FLIP_ACTIVE_TIME_MINUTES || completedPercentage >= BotConfig.MAX_FLIP_COMPLETED_PERC_EXIT || (!GEController.ItemInSlot(flip.item) && Inventory.contains(flip.item.item.itemName)) && GEController.GetAvailableSlotsAmount() > 0;
            if (shouldExitFlip) {
                int profit = 0;

                // Collect the items/end the active transaction
                GEController.CollectItem(flip.item, flip.buy);
                sleepUntil(() -> (!GEController.ItemInSlot(flip.item) && Inventory.contains(flip.item.item.itemName)) || completedPercentage < 0, BotConfig.MAX_ACTION_TIMEOUT_MS);
                if (GEController.ItemInSlot(flip.item)) {
                    log("Something went wrong when collecting " + flip.item.item.itemName + "!");
                    continue;
                }
                sleep(3000);
                int amountInInv = Inventory.contains(flip.item.item.itemName) ? Inventory.count(flip.item.item.itemName) : 0;

                ActiveFlip newFlip = null;

                if (flip.buy) {
                    log("Completed percentage for " + flip.item.item.itemName + " is: " + String.format("%.2f",completedPercentage) + "% - " + amountInInv + "x in inventory");

                    // Bought over the % limit
                    if ((completedPercentage >= BotConfig.MIN_FLIP_NORMAL_SELL_PERC || completedPercentage == -1) && Inventory.contains(flip.item.item.itemName)) {
                        log("Selling " + flip.item.item.itemName + " normally!");

                        newFlip = GEController.TransactItem(flip.item, false, amountInInv);
                        if (newFlip == null) { // If something goes wrong when selling (items remain in inv)
                            return;
                        }
                        newFlip.item.potentialProfitGp = amountInInv * newFlip.item.marginGp;

                        // Remove the unused buying limit (if < 100% was bought)
                        SaveManager.ModifyLimit(flip.item, flip.amount, flip.amount - amountInInv);

                        if (flip.item.skippedRequirements) {
                            newFlip.item.skippedRequirements = true;
                        }

                    } else if (Inventory.contains(flip.item.item.itemName)) { // Limit not reached - force-sell items
                        int forceProfit = ForceSell(flip);
                        profit += forceProfit;
                    } else {  // No items were bought
                        SaveManager.RemoveLimit(flip.item, flip.amount);
                    }
                } else {
                    // If it's a sell flip
                    if (completedPercentage >= 100) {
                        profit = flip.amount * (flip.item.marginGp - Math.round((float) flip.item.avgLowPrice / 100));
                    } else {
                        int forceProfit = ForceSell(flip);
                        profit += forceProfit;
                    }
                }

                if (!Inventory.contains(flip.item.item.itemName) && GEController.GetSlotFromItem(flip.item, flip.buy) == -1) {
                    if (profit >= flip.item.potentialProfitGp * 1.5) {
                        log("Profit for item: " + flip.item.item.itemName + " is suspiciously high, using potential profit instead. Original value: " + profit + " gp");
                        profit = flip.item.potentialProfitGp;
                    }

                    if(profit != 0 && profit != -1) {
                        Main.sessionProfit += profit;
                        CompletedFlip completedFlip = new CompletedFlip(flip.item, flip.startedTimeEpochsMs, System.currentTimeMillis(), profit);
                        SaveManager.AddCompletedFlip(completedFlip);
                    }

                    if (!flip.buy) {
                        logInfo(flip.toString() + " ENDED with a profit of: " + IngameGUI.GetFormattedNumbers(profit, true, false));
                    }
                    activeFlips.remove(activeFlips.get(i));

                    if (newFlip != null) {
                        activeFlips.add(newFlip);
                    }
                }
            }
        }
    }

    private static int ForceSell(ActiveFlip flip) {
        int amount = Inventory.count(flip.item.item.itemName);
        logInfo("Force-selling " + amount + "x " + flip.item.item.itemName);
        GrandExchange.sellItem(flip.item.item.itemName, amount, 1);
        sleepUntil(() -> GEController.ItemInSlot(flip.item) && GEController.GetCompletedPercentage(flip.item, false) >= 100, BotConfig.MAX_ACTION_TIMEOUT_MS);
        int receivedGold = GEController.GetTransferredValue(flip.item);
        GrandExchange.collect();
        sleepUntil(() -> !GrandExchange.isReadyToCollect(), BotConfig.MAX_ACTION_TIMEOUT_MS);
        return receivedGold - (amount * (flip.item.avgLowPrice + (flip.item.marginGp - Math.round((float) flip.item.avgLowPrice / 100))));
    }
}
