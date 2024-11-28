package com.htzx.oil.util;

public class calDist {
    public static double cal2dDis(double x1, double y1, double x2, double y2) {
        x1 *= 1000;
        x2 *= 1000;
        y1 *= 1000;
        y2 *= 1000;

        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}
