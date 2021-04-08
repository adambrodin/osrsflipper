package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.misc.BotConfig;
import com.adambrodin.osrsflipper.models.ActiveFlip;
import com.adambrodin.osrsflipper.models.FlipItem;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;

import static org.dreambot.api.methods.MethodProvider.log;
import static org.dreambot.api.methods.MethodProvider.sleepUntil;

public class GEController {
    // Buy/sell in the GE
    public static void TransactItem(FlipItem item, boolean buy, int amount) {
        if (buy) {
            int price = item.avgLowPrice;
            if (BotConfig.CUT_PRICES) {
                // Increase the price slightly to overcut
                price += Math.round(item.marginGp / 10);
            }

            GrandExchange.buyItem(item.item.itemName, amount, price);
        } else {
            int price = item.avgLowPrice + item.marginGp;
            if (BotConfig.CUT_PRICES) {
                // Decrease the price slightly to undercut
                price -= Math.round(item.marginGp / 10);
            }

            GrandExchange.sellItem(item.item.itemName, amount, price);
        }

        ActiveFlip flip = new ActiveFlip(buy, amount, item);
        log("Added new active flip (BUY)" + ": " + flip.amount + "x " + flip.item.item.itemName + " - potential profit: " + flip.item.potentialProfitGp + "gp - " + flip.item.marginPerc + "% margin (" + flip.item.marginGp + "gp)");
        Flipper.activeFlips.add(flip);
        sleepUntil(() -> ItemInSlot(item), BotConfig.MAX_ACTION_TIMEOUT_MS);
    }

    private static boolean ItemInSlot(FlipItem item) {
        for (GrandExchangeItem geItem : GrandExchange.getItems()) {
            try {
                if (geItem != null && geItem.getItem().getName().equals(item.item.itemName)) {
                    return true;
                }
            } catch (Exception e) {
                //log("ItemInSlot: " + e.getMessage());
            }
        }
        return false;
    }

    // Returns the amount of items from slot
    public static void CollectItem(FlipItem item) {
        if (GrandExchange.isOpen()) {
            for (GrandExchangeItem geItem : GrandExchange.getItems()) {
                if (geItem.getItem().getName().equals(item.item.itemName)) {
                    // If its fully completed
                    if (geItem.getTransferredAmount() != geItem.getAmount()) {
                        GrandExchange.cancelOffer(geItem.getSlot());
                        sleepUntil(() -> geItem.isReadyToCollect(), BotConfig.MAX_ACTION_TIMEOUT_MS);
                    }
                    GrandExchange.collect();
                    sleepUntil(() -> !GrandExchange.isReadyToCollect(geItem.getSlot()), BotConfig.MAX_ACTION_TIMEOUT_MS);
                    return;
                }
            }
        }
    }

    public static int AmountOfSlotsAvailable() {
        if (GrandExchange.getFirstOpenSlot() == -1) {
            return 0;
        }

        int amountOfSlots = 0;

        // 8 = amount of slots in interface
        for (int i = 0; i < 8; i++) {
            if (GrandExchange.isSlotEnabled(i) && !GrandExchange.slotContainsItem(i)) {
                amountOfSlots++;
            }
        }

        return amountOfSlots;
    }

    public static float GetCompletedPercentage(FlipItem item) {
        try {
            for (GrandExchangeItem geItem : GrandExchange.getItems()) {
                if (geItem != null && geItem.getItem().getName().equals(item.item.itemName)) {
                    if (geItem.isReadyToCollect()) {
                        return 100;
                    }

                    if (geItem.getTransferredAmount() <= 0) {
                        return 0;
                    }

                    return (geItem.getTransferredAmount() / geItem.getAmount()) * 100;
                }
            }
        } catch (Exception e) {
        }

        // Item not found
        return -1;
    }
}
