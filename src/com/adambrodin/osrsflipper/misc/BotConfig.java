package com.adambrodin.osrsflipper.misc;

public class BotConfig {
    // API
    public static final String RUNELITE_API_URL = "https://prices.runescape.wiki/api/v1/osrs";
    public static final String API_USER_AGENT = "Trading Analyzer - worecraft@gmail.com";
    public static final String ITEM_INFORMATION_ENDPOINT = "/mapping";

    // USER CONFIGURATION
    public static final String PRICE_DATA_ENDPOINT = "/latest"; // The endpoint to gather data from
    public static final String MARKET_HOUR_DATA_ENDPOINT = "/1h"; // The endpoint to gather volume data
    public static final boolean onlyF2pItems = false;
    public static final int greatItemVolume = 1000; // Determine what min volume that gets extra score when considered
    public static final float maxItemMarginPerc = 25; // Max percentage margin for an item to even be considered at all
    public static final float maxValidMarginPerc = 10; // Maximum percentage for an item to considered normally (this is to minimize items such as runes with 25% margin)
    public static final float minItemMarginPerc = 1f; // Min margin for an item to be considered
    public static final float minItemMarginGp = 1;
    public static final float minItemVolume = 200; // Min volume for an item in the endpoint timespan
}
