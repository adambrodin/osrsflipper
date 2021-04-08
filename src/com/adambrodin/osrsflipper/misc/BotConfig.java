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
    public static final boolean ONLY_F2P_ITEMS = true;
    public static final int ITEM_VOLUME_GREAT = 1000; // Determine what min volume that gets extra score when considered
    public static final int MAX_CASHSTACK_PERCENTAGE_PER_FLIP = 80; // The max percentage of the current money to use in a single flip (values less than 100 provide a broader range of items instead of spending everything on one item)
    public static final float MAX_ITEM_MARGIN_PERCENTAGE = 15; // Max percentage margin for an item to even be considered at all
    public static final float MAX_VALID_MARGIN_PERCENTAGE = 5; // Maximum percentage for an item to considered normally (this is to minimize items such as runes with 25% margin)
    public static final float MIN_ITEM_MARGIN_PERCENTAGE = 1f; // Min margin for an item to be considered
    public static final float MIN_ITEM_MARGIN_GP = 1;
    public static final float MIN_ITEM_VOLUME = 200; // Min volume for an item in the endpoint timespan
    public static boolean CUT_PRICES = true; // If the bot should under/overcut prices when trading (may increase profits by making trades execute faster)

    // LOCATIONS
    public static final Area GRANDEXCHANGE_AREA = new Area(3155, 3480, 3174, 3499);

    // TIMEOUTS
    public static final int MAX_ACTION_TIMEOUT_MS = 5000; // Max time before automatically moving on from a sleepUntil (in case something went wrong, to prevent program getting stuck)

    // MISC
    public static final boolean DISABLE_AUTOLOGIN = true;

    // TRAVELLING
    public static final int MIN_RUNENERGY_ACTIVATE = 3; // The minimum amount of run energy to randomly activate run when walking
    public static final int MAX_RUNENERGY_ACTIVATE = 10; // The maximum amount of run energy to randomly activate run when walking
    public static final int MIN_WALK_CLICK_DELAY_MS = 500; // The minimum amount of delay before clicking the next tile while walking/running
    public static final int MAX_WALK_CLICK_DELAY_MS = 2000; // The maximum amount of delay before clicking the next tile while walking/running
}
