package com.adambrodin.osrsflipper.models;

import java.util.List;

public class TradingInfo {
    public int totalUptimeSeconds, totalProfitGp, totalFlipsInitiated;
    public List<ActiveFlip> activeFlips;
    public List<BuyingLimit> usedBuyingLimits;
    public List<CompletedFlip> completedFlips;

    public TradingInfo(List<ActiveFlip> activeFlips, List<BuyingLimit> usedBuyingLimits, List<CompletedFlip> completedFlips, int totalUptimeSeconds, int totalProfitGp, int totalFlipsInitiated) {
        this.activeFlips = activeFlips;
        this.usedBuyingLimits = usedBuyingLimits;
        this.completedFlips = completedFlips;
        this.totalUptimeSeconds = totalUptimeSeconds;
        this.totalProfitGp = totalProfitGp;
        this.totalFlipsInitiated = totalFlipsInitiated;
    }
}
