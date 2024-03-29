package com.adambrodin.osrsflipper.logic;

import com.adambrodin.osrsflipper.core.Flipper;
import com.adambrodin.osrsflipper.io.SaveManager;
import com.adambrodin.osrsflipper.misc.BotConfig;
import com.adambrodin.osrsflipper.models.ActiveFlip;
import com.adambrodin.osrsflipper.models.ApiItem;
import com.adambrodin.osrsflipper.models.FlipItem;
import com.adambrodin.osrsflipper.models.PriceData;
import com.adambrodin.osrsflipper.network.RuneLiteApi;
import org.dreambot.api.utilities.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.adambrodin.osrsflipper.misc.BotConfig.MIN_ITEM_PRICE_FOR_CUT;


public class FlipFinder {
    private final List<ApiItem> availableItems;

    public FlipFinder() {
        ArrayList<ApiItem> allItems = RuneLiteApi.GetAllItems();
        if (BotConfig.ONLY_F2P_ITEMS) {
            // Remove all member items
            allItems.removeIf(item -> item.isMembers);
        }

        availableItems = allItems;
    }

    public FlipItem GetBestItem(int availableGp, boolean skipRestrictions) {
        HashMap<Integer, PriceData> apiData = RuneLiteApi.GetItemData();
        ArrayList<FlipItem> bestItems = new ArrayList<>();

        for (Map.Entry<Integer, PriceData> entry : apiData.entrySet()) {
            PriceData entryItem = entry.getValue();
            ApiItem apiItem = null;

            boolean isMember = false;
            for (ApiItem item : availableItems) {
                if (item.itemID == entry.getKey()) {
                    if (BotConfig.ONLY_F2P_ITEMS && item.isMembers) {
                        isMember = true;
                    }

                    apiItem = item;
                    break;
                }
            }

            // Check if the item reaches the set requirements
            if (!isMember && apiItem != null) {
                int avgHighPrice = entryItem.highPrice;
                int avgLowPrice = entryItem.lowPrice;

                if(BotConfig.CUT_PRICES && avgLowPrice >= MIN_ITEM_PRICE_FOR_CUT)
                {
                    avgHighPrice = (int) (avgHighPrice - (avgHighPrice *  BotConfig.CUT_PRICES_PERC));
                    avgLowPrice = (int) (avgLowPrice + (avgLowPrice *  BotConfig.CUT_PRICES_PERC));
                }

                float marginPerc = (((float) avgHighPrice / (float) avgLowPrice) * 100) - 100;
                int marginGp = avgHighPrice - avgLowPrice;

                if(avgHighPrice >= BotConfig.ITEM_VALUE_TAX_THRESHOLD)
                {
                    marginPerc = marginPerc - (marginPerc * BotConfig.ITEM_VALUE_TAX_PERC);
                    marginGp = (int) (marginGp - (marginGp * BotConfig.ITEM_VALUE_TAX_PERC));
                }

                double averagedVolume = ((double) entryItem.highPriceVolume + (double) entryItem.lowPriceVolume) / 2;

                boolean itemAlreadyFlipping = false;
                if ((skipRestrictions && marginPerc <= BotConfig.MAX_NO_RESTRICTIONS_MARGIN_PERC || (marginPerc >= BotConfig.MIN_ITEM_MARGIN_PERCENTAGE && marginPerc <= BotConfig.MAX_ITEM_MARGIN_PERCENTAGE && averagedVolume >= BotConfig.MIN_ITEM_VOLUME && marginGp >= BotConfig.MIN_ITEM_MARGIN_GP))
                        && !BotConfig.BLOCKED_ITEMS.contains(apiItem.itemName)) {
                    for (ActiveFlip flip : Flipper.activeFlips) {
                        // Prevent the same item being flipped multiple times
                        if (flip.item.item.itemName.toLowerCase().contains(apiItem.itemName.toLowerCase())) {
                            itemAlreadyFlipping = true;
                            break;
                        }
                    }

                    if (!itemAlreadyFlipping) {
                        bestItems.add(new FlipItem(apiItem, avgLowPrice, marginPerc, marginGp, (int) averagedVolume));
                    }
                }
            }
        }

        FlipItem bestItem = null;
        int bestItemPerfScore = 0;

        for (FlipItem item : bestItems) {
            if (bestItem == null) {
                bestItem = item;
                continue;
            }

            try {
                if (item.GetPerformanceScore(availableGp, SaveManager.GetRemainingLimit(item)) > bestItemPerfScore && item.potentialProfitGp >= BotConfig.MIN_PROFIT_FOR_FLIP) {
                    bestItem = item;
                    bestItemPerfScore = bestItem.GetPerformanceScore(availableGp, SaveManager.GetRemainingLimit(bestItem));
                }
            } catch (Exception e) {
                Logger.log("Exception for item " + item.item.itemName + " - " + e.getMessage());
            }
        }

        Logger.log("Considered items: " + (long) bestItems.size());

        // Return the best item
        return bestItem;
    }
}
