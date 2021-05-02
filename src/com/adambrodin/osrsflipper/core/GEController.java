package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.gui.IngameGUI;
import com.adambrodin.osrsflipper.misc.BotConfig;
import com.adambrodin.osrsflipper.models.ActiveFlip;
import com.adambrodin.osrsflipper.models.FlipItem;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;

import static org.dreambot.api.methods.MethodProvider.*;

public class GEController {
    // Buy/sell in the GE
    public static ActiveFlip TransactItem(FlipItem item, boolean buy, int amount) {
        boolean tradeCreated;

        int price;
        if (buy) {
            price = item.avgLowPrice;
            if (BotConfig.CUT_PRICES && price >= BotConfig.MIN_ITEM_PRICE_FOR_CUT) {
                // Increase the price slightly to overcut
                price += Math.round((float) item.avgLowPrice / 100);
            }

            tradeCreated = GrandExchange.buyItem(item.item.itemName, amount, price);
        } else {
            price = item.avgLowPrice + item.marginGp;
            if (BotConfig.CUT_PRICES && price >= BotConfig.MIN_ITEM_PRICE_FOR_CUT) {
                // Decrease the price slightly to undercut
                price -= Math.round((float) item.avgLowPrice / 100);
            }

            tradeCreated = GrandExchange.sellItem(item.item.itemName, amount, price);
        }

        if (tradeCreated) {
            sleepUntil(() -> GEController.ItemInSlot(item), 5000);
            if (GEController.ItemInSlot(item)) {
                ActiveFlip flip = new ActiveFlip(buy, amount, item);
                logInfo(flip.toString());
                Flipper.activeFlips.add(flip);
                return flip;
            }
            sleep(1000);
        }

        return null;
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
    public static void CollectItem(FlipItem item, boolean isBuy, int amount) {
        try {
            if (GrandExchange.isOpen()) {
                for (GrandExchangeItem geItem : GrandExchange.getItems()) {
                    if (geItem != null && geItem.getItem().getName().equals(item.item.itemName) && geItem.isBuyOffer() == isBuy) {
                        // If its fully completed
                        if (GetCompletedPercentage(item, amount) < 100) {
                            GrandExchange.cancelOffer(geItem.getSlot());
                            sleep(1000);
                            GrandExchange.goBack();
                            sleep(1000);
                        }
                        sleepUntil(GrandExchange::isReadyToCollect, BotConfig.MAX_ACTION_TIMEOUT_MS);
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

    public static int GetTransferredValue(FlipItem item) {
        try {
            for (GrandExchangeItem geItem : GrandExchange.getItems()) {
                if (geItem.getItem().getName().equalsIgnoreCase(item.item.itemName)) {
                    return geItem.getTransferredValue();
                }
            }
        } catch (Exception e) {
            log(e.getMessage());
        }

        return -1;
    }

    public static float GetCompletedPercentage(FlipItem item, int amount) {
        try {
            int slot = GetSlotFromItem(item, amount);
            for (GrandExchangeItem geItem : GrandExchange.getItems()) {
                if (geItem != null && geItem.getSlot() == slot) {
                    return ((float) geItem.getTransferredAmount() / (float) geItem.getAmount()) * 100;
                }
            }

            if (slot != -1 && !IngameGUI.currentAction.equals("Can't get completed percentage for: " + item.item.itemName + "!")) {
                IngameGUI.currentAction = "Can't get completed percentage for: " + item.item.itemName + "!";
            }
        } catch (Exception e) {
            log(e.getMessage());
        }

        // Item not found
        return -1;
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

    public static int GetSlotFromItem(FlipItem item, int amount) {
        try {
            for (GrandExchangeItem geItem : GrandExchange.getItems()) {
                if (geItem != null && geItem.getItem().getName().equalsIgnoreCase(item.item.itemName) && geItem.getAmount() == amount) {
                    return geItem.getSlot();
                }
            }

            if (!Inventory.contains(item.item.itemName) && !IngameGUI.currentAction.equals(item.item.itemName + " not found in any slot!")) {
                IngameGUI.currentAction = item.item.itemName + " not found in any slot!";
            }
        } catch (Exception e) {
            log(e.getMessage());
        }

        // Item not found
        return -1;
    }
}
