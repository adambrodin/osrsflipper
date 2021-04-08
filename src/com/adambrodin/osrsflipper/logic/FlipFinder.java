package com.adambrodin.osrsflipper.logic;

import com.adambrodin.osrsflipper.misc.BotConfig;
import com.adambrodin.osrsflipper.models.ApiItem;
import com.adambrodin.osrsflipper.models.FlipItem;
import com.adambrodin.osrsflipper.models.PriceData;
import com.adambrodin.osrsflipper.network.RuneLiteApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dreambot.api.methods.MethodProvider.log;

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

    public FlipItem GetBestMarginItem(int availableGp) {
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
                float marginPerc = (((float) avgHighPrice / (float) avgLowPrice) * 100) - 100;
                int marginGp = avgHighPrice - avgLowPrice;

                double averagedVolume = ((double) entryItem.highPriceVolume + (double) entryItem.lowPriceVolume) / 2;
                log(apiItem.itemName + " - averaged hour volume: " + averagedVolume);

                if (marginPerc >= BotConfig.MIN_ITEM_MARGIN_PERCENTAGE && marginPerc <= BotConfig.MAX_ITEM_MARGIN_PERCENTAGE && averagedVolume >= BotConfig.MIN_ITEM_VOLUME && marginGp >= BotConfig.MIN_ITEM_MARGIN_GP) {
                    bestItems.add(new FlipItem(apiItem, avgLowPrice, marginPerc, marginGp, (int) averagedVolume));
                }
            }
        }

        FlipItem bestItem = null;
        int bestItemPerfScore = 0;

        int consideredItems = 0;
        for (FlipItem item : bestItems) {
            if (bestItem == null) {
                bestItem = item;
                continue;
            }

            try {
                if (item.GetPerformanceScore(availableGp, item.item.buyingLimit) > bestItemPerfScore) {
                    bestItem = item;
                    bestItemPerfScore = bestItem.GetPerformanceScore(availableGp, bestItem.item.buyingLimit);
                    consideredItems++;
                }
            } catch (Exception e) {
                log("Exception for item " + item.item.itemName + " - " + e.getMessage());
            }
        }

        log("Considered items: " + consideredItems);

        // Return the best item
        return bestItem;
    }
}
