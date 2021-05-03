package com.adambrodin.osrsflipper.models;

public class CompletedFlip {
    public FlipItem item;
    public long startEpochs, endEpochs;
    public int totalProfitGp;

    public CompletedFlip(FlipItem item, long startEpochs, long endEpochs, int totalProfitGp) {
        this.item = item;
        this.startEpochs = startEpochs;
        this.endEpochs = endEpochs;
        this.totalProfitGp = totalProfitGp;
    }
}
