package com.adambrodin.osrsflipper.misc;

import org.dreambot.api.methods.map.Area;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class BotConfig {
    // API
    public static final String RUNELITE_API_URL = "https://prices.runescape.wiki/api/v1/osrs";
    public static final String API_USER_AGENT = "Trading Analyzer - worecraft@gmail.com";
    public static final String ITEM_INFORMATION_ENDPOINT = "/mapping";
    public static final String PRICE_DATA_ENDPOINT = "/latest"; // The endpoint to gather data from
    public static final String MARKET_HOUR_DATA_ENDPOINT = "/1h"; // The endpoint to gather volume data

    // FLIP CONFIGURATIONS
    public static final boolean ONLY_F2P_ITEMS = false;
    public static final int ITEM_VOLUME_GREAT = 100000; // Determine what min volume that gets extra score when considered
    public static final float MAX_CASHSTACK_PERCENTAGE_PER_FLIP = 0.6f; // The max percentage of the current money to use in a single flip (values less than 100 provide a broader range of items instead of spending everything on one item)
    public static final float MIN_PROFIT_FOR_FLIP = 5000;
    public static final boolean INCLUDE_RISKY_FLIP = true;
    public static final float MAX_NO_RESTRICTIONS_MARGIN_PERC = 35;
    public static final float MAX_NO_RESTRICTIONS_CASHSTACK_PERC = 0.2f; // The max amount of % money to use for the "risky" flip (without requirements)
    public static final int MIN_GOLD_FOR_FLIP = 50000; // Minimum amount of gp in inventory to start new flips
    public static final int MIN_CASHSTACK_FOR_PERCENTAGE_FLIP = 2000000; // Min amount of gp to only use MAX_CASHSTACK_PERCENTAGE_PER_FLIP instead of 100%
    public static final int MAX_FLIP_ACTIVE_TIME_MINUTES = 50;
    public static final int MIN_FLIP_ITEMS_FORCE_SELL = 20; // The minimum amount of items to force-sell if not all items were bought
    public static final float MAX_FLIP_COMPLETED_PERC_EXIT = 90; // The maximum amount of % completed before force-exiting it
    public static final float MIN_FLIP_NORMAL_SELL_PERC = 25f; // The minimum amount of % sell to sell it normally (otherwise force-sell)
    public static final float MAX_ITEM_MARGIN_PERCENTAGE = 20; // Max percentage margin for an item to even be considered at all
    public static final float MAX_VALID_MARGIN_PERCENTAGE = 10; // Maximum percentage for an item to considered normally (this is to minimize items such as runes with 25% margin)
    public static final float MIN_ITEM_MARGIN_PERCENTAGE = 0.5f; // Min margin for an item to be considered
    public static final float MIN_ITEM_MARGIN_GP = 2;
    public static final float MIN_ITEM_VOLUME = 40000; // Min volume for an item in the endpoint timespan
    public static final int MIN_ITEM_PRICE_FOR_CUT = 100; // Min item price for it to be cut (to prevent cutting very cheap items)

    public static final List<String> BLOCKED_ITEMS = Arrays.asList("Swamp tar", "Swamp paste", "Thread", "Bucket of water", "Bucket", "Jug", "Jug of water", "Fishing bait", "Trading sticks", "Old school bond");
    // LOCATIONS
    public static final Area GRANDEXCHANGE_AREA = new Area(3155, 3480, 3174, 3499);
    // TIMEOUTS
    public static final int MAX_ACTION_TIMEOUT_MS = 3000; // Max time before automatically moving on from a sleepUntil (in case something went wrong, to prevent program getting stuck)
    public static final int LOGOUT_SLEEP_DURATION_MINUTES = 45; // The amount of time to sleep when logged out before logging back in
    // MISC
    public static final boolean DISABLE_AUTOLOGIN = false;
    public static final int BUYING_LIMIT_HOURS = 4; // The amount of hours to add when saving the used buying limits internally (through io)
    // TRAVELLING
    public static final int MIN_RUNENERGY_ACTIVATE = 3; // The minimum amount of run energy to randomly activate run when walking
    public static final int MAX_RUNENERGY_ACTIVATE = 10; // The maximum amount of run energy to randomly activate run when walking
    public static final int MIN_WALK_CLICK_DELAY_MS = 500; // The minimum amount of delay before clicking the next tile while walking/running
    public static final int MAX_WALK_CLICK_DELAY_MS = 2000; // The maximum amount of delay before clicking the next tile while walking/running
    // GUI
    public static final int OVERLAY_TEXT_X_OFFSET = 3;
    public static final int OVERLAY_TEXT_Y_OFFSET = 25;
    public static final Font OVERLAY_FONT = new Font("Open Sans", Font.BOLD, 20);
    public static final Font SLOT_OVERLAY_FONT = new Font("Open Sans", Font.BOLD, 14);
    // IO
    public static final String SAVED_DATA_PATH = System.getProperty("user.dir");
    public static final String SAVED_DATA_FILE_NAME = SAVED_DATA_PATH + "OSRS_FLIPPER_DATA.json";
    public static final boolean CUT_PRICES = true; // If the bot should under/overcut prices when trading (may increase profits by making trades execute faster)
}
