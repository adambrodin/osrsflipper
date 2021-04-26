package com.adambrodin.osrsflipper.models;

import java.util.HashMap;
import java.util.List;

public class TradingInfo {
    public List<ActiveFlip> activeFlips;
    public List<BuyingLimit> usedBuyingLimits;

    public TradingInfo(List<ActiveFlip> activeFlips, List<BuyingLimit> usedBuyingLimits) {
        this.activeFlips = activeFlips;
        this.usedBuyingLimits = usedBuyingLimits;
    }
}
