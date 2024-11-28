package com.htzx.oil.algorithm;

class Vehicle {
    int id;
    int capacity;
    Node location;
    int load;
    double cur_time;

    public Vehicle(int id, int capacity, Node location, int load,double cur_time) {
        this.id = id;
        this.capacity = capacity;
        this.location = location;
        this.load = load;
        this.cur_time=cur_time;
    }
}
