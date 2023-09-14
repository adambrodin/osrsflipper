package com.adambrodin.osrsflipper.network;

import com.adambrodin.osrsflipper.misc.BotConfig;
import com.adambrodin.osrsflipper.models.*;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RuneLiteApi {
    public static String GetJsonString(String endPoint) {
        URL url = null;
        try {
            url = new URL(BotConfig.RUNELITE_API_URL + endPoint);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) (url != null ? url.openConnection() : null);
            assert connection != null;
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", BotConfig.API_USER_AGENT);

            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("HttpResponse error: " + connection.getResponseCode() + " - " + connection.getResponseMessage());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }

            reader.close();
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IO Exception when fetching API data: " + e.getMessage());
        }
    }

    public static HashMap<Integer, PriceData> GetItemData() {
        // Fetch live-data from API
        String priceDataJson = GetJsonString(BotConfig.PRICE_DATA_ENDPOINT);
        String marketDataJson = GetJsonString(BotConfig.MARKET_HOUR_DATA_ENDPOINT);

        ApiPriceDataResponse priceResponse = new Gson().fromJson(priceDataJson, ApiPriceDataResponse.class);
        ApiMarketDataResponse marketResponse = new Gson().fromJson(marketDataJson, ApiMarketDataResponse.class);

        HashMap<Integer, PriceData> filteredItems = new HashMap<>();
        for (Map.Entry<Integer, MarketData> marketData : marketResponse.data.entrySet()) {
            for (Map.Entry<Integer, PriceData> priceData : priceResponse.data.entrySet()) {
                if (marketData.getKey().equals(priceData.getKey())) {
                    PriceData pd = priceData.getValue();
                    MarketData md = marketData.getValue();

                    if (priceData.getKey() == 0 || pd.highPrice == 0 || pd.lowPrice == 0 || pd.highPriceTime == 0 || pd.lowPriceTime == 0 || md.highPriceVolume == 0 || md.lowPriceVolume == 0 || pd.highPrice == pd.lowPrice) {
                        continue;
                    }
                    pd.highPriceVolume = md.highPriceVolume;
                    pd.lowPriceVolume = md.lowPriceVolume;

                    filteredItems.put(priceData.getKey(), pd);
                }
            }
        }

        return filteredItems;
    }

    public static PriceData GetSingleItemData(int itemId)
    {
        // Fetch live-data from API
        String priceDataJson = GetJsonString(BotConfig.PRICE_DATA_ENDPOINT + "?id=" + itemId);
        ApiPriceDataResponse priceResponse = new Gson().fromJson(priceDataJson, ApiPriceDataResponse.class);

        return priceResponse.data.get(itemId);
    }

    public static ArrayList<ApiItem> GetAllItems() {
        // Fetch all available OSRS items
        String jsonData = GetJsonString(BotConfig.ITEM_INFORMATION_ENDPOINT);
        ApiItem[] apiItems = new Gson().fromJson(jsonData, ApiItem[].class);
        ArrayList<ApiItem> apiItemsList = new ArrayList<>(Arrays.asList(apiItems));

        for (int i = 0; i < apiItemsList.size(); i++) {
            if (apiItemsList.get(i) == null || apiItemsList.get(i).itemID == 0 || apiItemsList.get(i).buyingLimit == 0 || apiItemsList.get(i).itemName.equalsIgnoreCase("null") || apiItemsList.get(i).itemValue == 0) {
                apiItemsList.remove(i);
            }
        }

        return apiItemsList;
    }
}
