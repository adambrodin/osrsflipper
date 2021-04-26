package com.adambrodin.osrsflipper.models;

import java.util.HashMap;
import java.util.List;

public class TradingInfo {
    public List<ActiveFlip> activeFlips;
    public HashMap<FlipItem, BuyingLimit> usedBuyingLimits;

    public TradingInfo(List<ActiveFlip> activeFlips, HashMap<FlipItem, BuyingLimit> usedBuyingLimits) {
        this.activeFlips = activeFlips;
        this.usedBuyingLimits = usedBuyingLimits;
    }
}
