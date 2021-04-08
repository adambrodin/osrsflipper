package com.adambrodin.osrsflipper.models;

import com.adambrodin.osrsflipper.misc.BotConfig;

import static org.dreambot.api.methods.MethodProvider.log;

public class FlipItem {
    public ApiItem item;
    public int avgLowPrice;
    public float marginPerc;
    public int marginGp;
    public float averagedVolume;

    public int potentialProfitGp;

    public FlipItem(ApiItem item, int avgLowPrice, float marginPerc, int marginGp, float averagedVolume) {
        this.item = item;
        this.avgLowPrice = avgLowPrice;
        this.marginPerc = marginPerc;
        this.marginGp = marginGp;
        this.averagedVolume = averagedVolume;
    }

    public int GetPerformanceScore(int availableGp, int remainingBuyingLimit) {
        float score = 1000;
        // Determines how many items that can be bought based on player gold & current market
        int maxAmountAvailable = (int) Math.min(Math.min(remainingBuyingLimit, averagedVolume), (availableGp / avgLowPrice));

        int potentialProfit = maxAmountAvailable * marginGp;
        potentialProfitGp = potentialProfit;

        // Increases score, the more the profit is
        score += potentialProfit * 10;

        // Increases score if its below a certain percentage margin threshold (to minimize 25% margins such as runes)
        if (marginPerc <= BotConfig.maxValidMarginPerc) {
            score += potentialProfit * 3;
        }

        // Increases score if the item has had more trading volume (more likely to successfully flip)
        if(averagedVolume >= BotConfig.greatItemVolume)
        {
            score += potentialProfit * 5;
        }

        //log(item.itemName + " - " + "Available gp: " + availableGp + "gp - AvgLowPrice: " + avgLowPrice + "gp - Margin gp:" + marginGp + "gp" + " - remaining limit: "
            //   + remainingBuyingLimit + " potential profit: " + potentialProfitGp +"gp - perf. score: " +score);
        return (int) score;
    }
}
