package com.adambrodin.osrsflipper.models;

import com.google.gson.annotations.SerializedName;

public class PriceData {
    @SerializedName("high")
    public int highPrice;

    @SerializedName("highTime")
    public int highPriceTime;

    @SerializedName("low")
    public int lowPrice;
    @SerializedName("lowTime")
    public int lowPriceTime;

    public int highPriceVolume;
    public int lowPriceVolume;
}
