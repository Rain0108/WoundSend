package com.htzx.oil.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class fightList {
    public String FightNM;
    public double lat;
    public double lon;
    public HashMap<Integer, Integer> oilList;  // Each HashMap represents type as key and num as value.
    public int priority;

    public List<List<Double>> inverseTimeWindow;

    // Constructor
    public fightList(String FightNM, double lat, double lon, HashMap<Integer, Integer> oilList , int priority) {
        this.FightNM = FightNM;
        this.lat = lat;
        this.lon = lon;
        this.oilList = oilList;
        this.priority = priority;
        inverseTimeWindow=new ArrayList<>();

    }



}