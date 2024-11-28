package com.htzx.oil.algorithm;

public class Depot {
    int storage;
    Location location;

    public Depot(int storage, double lat, double lon) {
        this.storage = storage;
        this.location = new Location(lat, lon);
    }
}
