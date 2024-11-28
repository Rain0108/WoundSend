package com.htzx.oil.algorithm;

public class Node {
    int priority;
    int demand;
    Location location;

    public Node(int priority, int demand, double lat, double lon) {
        this.priority = priority;
        this.demand = demand;
        this.location = new Location(lat, lon);
    }
}
