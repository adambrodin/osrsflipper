package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.misc.BotConfig;
import com.adambrodin.osrsflipper.models.ActiveFlip;
import com.adambrodin.osrsflipper.models.FlipItem;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;

import static org.dreambot.api.methods.MethodProvider.*;

public class GEController {
    // Buy/sell in the GE
    public static void TransactItem(FlipItem item, boolean buy, int amount) {
        boolean tradeCreated;

        if (buy) {
            int price = item.avgLowPrice;
            if (BotConfig.CUT_PRICES && price >= BotConfig.MIN_ITEM_PRICE_FOR_CUT) {
                // Increase the price slightly to overcut
                price += Math.round(item.marginGp / 5);
            }

            tradeCreated = GrandExchange.buyItem(item.item.itemName, amount, price);
        } else {
            int price = item.avgLowPrice + item.marginGp;
            if (BotConfig.CUT_PRICES && price >= BotConfig.MIN_ITEM_PRICE_FOR_CUT) {
                // Decrease the price slightly to undercut
                price -= Math.round(item.marginGp / 5);
            }

            tradeCreated = GrandExchange.sellItem(item.item.itemName, amount, price);
        }

        if (tradeCreated) {
            sleepUntil(() -> GEController.ItemInSlot(item), 5000);
            if (GEController.ItemInSlot(item)) {
                ActiveFlip flip = new ActiveFlip(buy, amount, item);
                log("Added new active flip (BUY: " + buy + "): " + flip.amount + "x " + flip.item.item.itemName + " - potential profit: " + flip.item.potentialProfitGp + "gp - " + flip.item.marginPerc + "% margin (" + flip.item.marginGp + "gp)");
                Flipper.activeFlips.add(flip);
            }
            sleep(1000);
        }
    }

    public static boolean ItemInSlot(FlipItem item) {
        for (GrandExchangeItem geItem : GrandExchange.getItems()) {
            try {
                if (geItem != null && geItem.getItem().getName().equals(item.item.itemName)) {
                    return true;
                }
            } catch (Exception e) {
                log(e.getMessage());
            }
        }
        return false;
    }

    // Returns the amount of items from slot
    public static void CollectItem(FlipItem item, boolean isBuy) {
        try {
            if (GrandExchange.isOpen()) {
                for (GrandExchangeItem geItem : GrandExchange.getItems()) {
                    if (geItem != null && geItem.getItem().getName().equals(item.item.itemName) && geItem.isBuyOffer() == isBuy) {
                        // If its fully completed
                        if (GetCompletedPercentage(item) < 100) {
                            GrandExchange.cancelOffer(geItem.getSlot());
                            sleep(1000);
                            GrandExchange.goBack();
                            sleep(1000);
                        }
                        sleepUntil(() -> GrandExchange.isReadyToCollect(), BotConfig.MAX_ACTION_TIMEOUT_MS);
                        GrandExchange.collect();
                        sleep(1000);
                        sleepUntil(() -> !GrandExchange.isReadyToCollect(geItem.getSlot()) && Inventory.contains(item.item.itemName), BotConfig.MAX_ACTION_TIMEOUT_MS);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            log(e.getMessage());
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
                if (geItem != null && geItem.getItem().getName().equalsIgnoreCase(item.item.itemName)) {
                    return ((float) geItem.getTransferredAmount() / (float) geItem.getAmount()) * 100;
                }
            }
        } catch (Exception e) {
            log(e.getMessage());
        }

        log("Couldn't get completed percentage for item " + item.item.itemName + " - not found!");
        sleep(2000);

        // Item not found
        return -1;
    }

    public static int GetSlotGoldAmount(FlipItem item) {
        try {
            for (GrandExchangeItem geItem : GrandExchange.getItems()) {
                if (geItem != null && geItem.getItem().getName().equals(item.item.itemName)) {
                    return geItem.getTransferredValue();
                }
            }
        } catch (Exception e) {
            log(e.getMessage());
        }

        // Item not found
        return 0;
    }

    public static int GetAvailableSlotsAmount() {
        int availableSlots = 8;
        try {
            for (GrandExchangeItem geItem : GrandExchange.getItems()) {
                if (geItem.getItem() != null) {
                    availableSlots--;
                }
            }
        } catch (Exception e) {
            log(e.getMessage());
        }

        return availableSlots;
    }

    public static int GetSlotFromItem(FlipItem item) {
        try {
            for (GrandExchangeItem geItem : GrandExchange.getItems()) {
                if (geItem != null && geItem.getItem().getName().equals(item.item.itemName) && item.maxAmountAvailable == geItem.getAmount()) {
                    return geItem.getSlot();
                }
            }
        } catch (Exception e) {
            log(e.getMessage());
        }

        log(item.item.itemName + " not found in any slot!");

        // Item not found
        return -1;
    }
}
