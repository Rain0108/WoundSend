package com.htzx.oil.algorithm;

class Location {
    double lat, lon;

    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "(" + lat + ", " + lon + ")";
    }
}
