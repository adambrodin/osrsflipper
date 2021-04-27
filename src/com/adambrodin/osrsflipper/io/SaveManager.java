package com.adambrodin.osrsflipper.io;

import com.adambrodin.osrsflipper.misc.BotConfig;
import com.adambrodin.osrsflipper.models.ActiveFlip;
import com.adambrodin.osrsflipper.models.BuyingLimit;
import com.adambrodin.osrsflipper.models.FlipItem;
import com.adambrodin.osrsflipper.models.TradingInfo;
import com.google.gson.Gson;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.dreambot.api.methods.MethodProvider.log;

public class SaveManager {
    private static final Gson gson = new Gson();
    public static TradingInfo tradingInfo;

    public static void Save() {
        if (tradingInfo == null) {
            log("Attempted to save null tradingInfo!");
            return;
        }

        SynchronizeFile(BotConfig.SAVED_DATA_FILE_NAME, true, gson.toJson(tradingInfo), SavedType.TradingInfo);
    }

    public static void Load() {
        if (tradingInfo == null) {
            tradingInfo = new TradingInfo(new ArrayList<>(), new ArrayList<>(), 0, 0, 0);
        }

        // Sets tradingInfo to data from the file
        SynchronizeFile(BotConfig.SAVED_DATA_FILE_NAME, false, "", SavedType.TradingInfo);
    }

    private static void SynchronizeFile(String path, boolean write, String json, SavedType savedType) {
        File file = new File(path);

        if (write) {
            try {
                FileOutputStream outputStream = new FileOutputStream(file);

                outputStream.write(json.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                log("IOException when WRITING: (" + e.getMessage() + ")");
            }
        } else {
            StringBuilder builder = new StringBuilder();

            try {
                InputStream inputStream = new FileInputStream(file);
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    String receivedString;
                    while ((receivedString = bufferedReader.readLine()) != null) {
                        builder.append(receivedString);
                    }

                    inputStream.close();
                    switch (savedType) {
                        case TradingInfo:
                            tradingInfo = gson.fromJson(builder.toString(), TradingInfo.class);
                            break;
                        default:
                            log("Unknown savedType!");
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    public static List<ActiveFlip> GetSavedFlips() {
        if (tradingInfo == null || tradingInfo.activeFlips.isEmpty()) {

            // Return empty list
            return new ArrayList<>();
        }
        return tradingInfo.activeFlips;
    }

    public static void SaveActiveFlips(List<ActiveFlip> activeFlips) {
        tradingInfo.activeFlips = activeFlips;
        Save();
    }

    public static int GetRemainingLimit(FlipItem item) {
        int maxBuyingLimit = item.item.buyingLimit;
        for (BuyingLimit limit : tradingInfo.usedBuyingLimits) {
            if (Duration.between(LocalDateTime.now(), limit.expiryTime).toHours() >= BotConfig.BUYING_LIMIT_HOURS) {
                tradingInfo.usedBuyingLimits.remove(limit);
                log("Removed " + limit.amountUsed + "x used limit from " + limit.item.item.itemName + "! - Surpassed time limit.");
                continue;
            }

            if (limit.item.item.itemName.equalsIgnoreCase(item.item.itemName)) {
                maxBuyingLimit -= limit.amountUsed;
            }
        }

        // If the item isn't found in any active buying limits it has 100% of its limit remaining
        return maxBuyingLimit;
    }

    public static void AddUsedLimit(FlipItem item, int amount) {
        tradingInfo.usedBuyingLimits.add(new BuyingLimit(item, amount, LocalDateTime.now().plusHours(BotConfig.BUYING_LIMIT_HOURS)));
        Save();
    }

    public static void ModifyLimit(FlipItem item, int amount, int modifyAmount) {
        try {
            Objects.requireNonNull(tradingInfo.usedBuyingLimits.stream().filter(
                    entry -> entry.item.item.itemName.equalsIgnoreCase(item.item.itemName) && entry.amountUsed == amount).findFirst().orElse(null)).amountUsed += modifyAmount;
        } catch (Exception e) {
        }
    }

    public static void RemoveLimit(FlipItem item, int amount) {
        tradingInfo.usedBuyingLimits.removeIf(entry -> entry.amountUsed == amount && entry.item.item.itemName.equalsIgnoreCase(item.item.itemName));
        Save();
        log("Removed limit - " + amount + "x " + item.item.itemName);
    }

    private enum SavedType {
        TradingInfo,
    }
}
