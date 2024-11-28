package com.htzx.oil.common;

import java.util.List;
import java.util.Map;

public class sotList {
    public List<Map<String, Double>> polygon;
    public List<Double> earlyTW;
    public List<Double> lateTW;


    public sotList(List<Map<String, Double>> polygon, List<Double> earlyTW, List<Double> lateTW) {
        this.polygon = polygon;
        this.earlyTW = earlyTW;
        this.lateTW = lateTW;
    }
}
