package com.htzx.oil.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class oilDepotList {
    public String oilDepotNM;
    public double lat;
    public double lon;
    public HashMap<Integer, Integer> oilList; //type:num
    public HashMap<Integer, Integer> oilSpeedList;  //type:speed
    public List<List<Double>> inverseTimeWindow;


    // Constructor
    public oilDepotList(String oilDepotNM, double lat, double lon, HashMap<Integer, Integer> oilList , HashMap<Integer, Integer> oilSpeedList) {
        this.oilDepotNM = oilDepotNM;
        this.lat = lat;
        this.lon = lon;
        this.oilList = oilList;
        this.oilSpeedList=oilSpeedList;
        inverseTimeWindow=new ArrayList<>();
    }
}