package com.adambrodin.osrsflipper.misc;

import org.dreambot.api.methods.map.Area;

public class BotConfig {
    // API
    public static final String RUNELITE_API_URL = "https://prices.runescape.wiki/api/v1/osrs";
    public static final String API_USER_AGENT = "Trading Analyzer - worecraft@gmail.com";
    public static final String ITEM_INFORMATION_ENDPOINT = "/mapping";
    public static final String PRICE_DATA_ENDPOINT = "/latest"; // The endpoint to gather data from
    public static final String MARKET_HOUR_DATA_ENDPOINT = "/1h"; // The endpoint to gather volume data

    // FLIP CONFIGURATIONS
    public static final boolean ONLY_F2P_ITEMS = false;
    public static final int ITEM_VOLUME_GREAT = 1000; // Determine what min volume that gets extra score when considered
    public static final float MAX_ITEM_MARGIN_PERCENTAGE = 25; // Max percentage margin for an item to even be considered at all
    public static final float MAX_VALID_MARGIN_PERCENTAGE = 10; // Maximum percentage for an item to considered normally (this is to minimize items such as runes with 25% margin)
    public static final float MIN_ITEM_MARGIN_PERCENTAGE = 1f; // Min margin for an item to be considered
    public static final float MIN_ITEM_MARGIN_GP = 1;
    public static final float MIN_ITEM_VOLUME = 200; // Min volume for an item in the endpoint timespan

    // LOCATIONS
    public static final Area GRANDEXCHANGE_AREA = new Area(3155, 3480, 3174, 3499);

    // TIMEOUTS
    public static final int MAX_ACTION_TIMEOUT_MS = 5000; // Max time before automatically moving on from a sleepUntil (in case something went wrong, to prevent program getting stuck)

    // MISC
    public static final boolean DISABLE_AUTOLOGIN = true;
}
