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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.dreambot.api.methods.MethodProvider.log;

public class SaveManager {
    private static Gson gson = new Gson();
    private static TradingInfo tradingInfo;

    public static void Save() {
        if (tradingInfo == null) {
            log("Attempted to save null tradingInfo!");
            return;
        }

        SynchronizeFile(BotConfig.SAVED_DATA_FILE_NAME, true, gson.toJson(tradingInfo), SavedType.TradingInfo);
    }

    public static void Load() {
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
                log("Wrote to " + path);
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
            log("Attempted to fetch null tradingInfo!");

            // Return empty list
            return Arrays.asList();
        }
        return tradingInfo.activeFlips;
    }

    public static int GetRemainingLimit(FlipItem item) {
        int maxBuyingLimit = item.item.buyingLimit;
        for (Map.Entry<FlipItem, BuyingLimit> entry : tradingInfo.usedBuyingLimits.entrySet()) {
            if (Duration.between(LocalDateTime.now(), entry.getValue().expiryTime).toHours() >= BotConfig.BUYING_LIMIT_HOURS) {
                tradingInfo.usedBuyingLimits.remove(entry.getKey());
                log("Removed " + entry.getValue().amountUsed + "x used limit from " + entry.getKey().item.itemName + "! - Surpassed time limit.");
                continue;
            }

            if (entry.getKey().item.itemName.equalsIgnoreCase(item.item.itemName)) {
                maxBuyingLimit -= entry.getValue().amountUsed;
            }
        }

        // If the item isn't found in any active buying limits it has 100% of its limit remaining
        return maxBuyingLimit;
    }

    public static void AddUsedLimit(FlipItem item, int amount) {
        tradingInfo.usedBuyingLimits.put(item, new BuyingLimit(amount, LocalDateTime.now().plusHours(BotConfig.BUYING_LIMIT_HOURS)));
        Save();
        log("Added limit - " + amount + "x " + item.item.itemName);
    }

    public static void ModifyLimit(FlipItem item, int amount, int modifyAmount) {
        tradingInfo.usedBuyingLimits.entrySet().stream().filter(
                entry -> entry.getKey().item.itemName.equalsIgnoreCase(item.item.itemName) && entry.getValue().amountUsed == amount).findFirst().get().getValue().amountUsed += modifyAmount;
    }

    public static void RemoveLimit(FlipItem item, int amount) {
        tradingInfo.usedBuyingLimits.entrySet().removeIf(entry -> entry.getValue().amountUsed == amount && entry.getKey().item.itemName.equalsIgnoreCase(item.item.itemName));
        Save();
        log("Removed limit - " + amount + "x " + item.item.itemName);
    }

    private enum SavedType {
        TradingInfo,
    }
}
