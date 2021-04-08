package com.adambrodin.osrsflipper.models;

import com.google.gson.annotations.SerializedName;

public class ApiItem {
    @SerializedName("id")
    public int itemID;

    @SerializedName("members")
    public boolean isMembers;

    @SerializedName("limit")
    public int buyingLimit;

    @SerializedName("value")
    public int itemValue;

    @SerializedName("name")
    public String itemName;
}
