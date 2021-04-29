package com.adambrodin.osrsflipper.models;

import com.adambrodin.osrsflipper.core.GEController;
import com.adambrodin.osrsflipper.misc.BotConfig;

public class FlipItem {
    public boolean skippedRequirements;
    public ApiItem item;
    public int avgLowPrice;
    public float marginPerc;
    public int marginGp;
    public float averagedVolume;

    public int potentialProfitGp;
    public int maxAmountAvailable;

    public FlipItem(ApiItem item, int avgLowPrice, float marginPerc, int marginGp, float averagedVolume) {
        this.item = item;
        this.avgLowPrice = avgLowPrice;
        this.marginPerc = marginPerc;
        this.marginGp = marginGp;
        this.averagedVolume = averagedVolume;
    }

    public int GetPerformanceScore(int availableGp, int remainingBuyingLimit) {
        float score = 0;
        // Determines how many items that can be bought based on player gold & current market
        int purchasePrice = BotConfig.CUT_PRICES ? avgLowPrice + Math.round((float) marginGp / 5) : avgLowPrice;
        maxAmountAvailable = (int) Math.min(Math.min(remainingBuyingLimit, averagedVolume), (float) (availableGp / purchasePrice));
        potentialProfitGp = maxAmountAvailable * marginGp;

        // Increases score, the more the profit is
        score += potentialProfitGp * 10;

        // Increases score if its below a certain percentage margin threshold (to minimize 25% margins such as runes which are typically slower)
        if (marginPerc > BotConfig.MAX_VALID_MARGIN_PERCENTAGE) {
            score -= potentialProfitGp * 3;
        }

        // Increases score if the item has had more trading volume (more likely to successfully flip)
        if (averagedVolume >= BotConfig.ITEM_VOLUME_GREAT) {
            score += potentialProfitGp * 3;
        }

        // If the item has great volume margins (more likely to flip faster)
        if (averagedVolume >= remainingBuyingLimit * 3) {
            score += potentialProfitGp * 25;
        }

        // If there is only one slot left and the last item doesn't use the whole cash stack, lower its score
        if (GEController.AmountOfSlotsAvailable() == 1 && (maxAmountAvailable * avgLowPrice) <= (float) (availableGp * 0.75)) {
            score -= potentialProfitGp * 30;
        }

        return (int) score;
    }
}
