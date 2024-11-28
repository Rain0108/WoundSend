package com.htzx.oil.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointInPolygon {

    public static boolean isPointInPolygon(double testx, double testy, List<Map<String, Double>> polygon) {
        int i, j;
        boolean result = false;
        for (i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            if ((polygon.get(i).get("lat") > testy) != (polygon.get(j).get("lat") > testy) &&
                    (testx < (polygon.get(j).get("lon") - polygon.get(i).get("lon")) * (testy - polygon.get(i).get("lat")) / (polygon.get(j).get("lat")-polygon.get(i).get("lat")) + polygon.get(i).get("lon"))) {
                result = !result;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        // 输入的多边形坐标列表
        List<Map<String, Double>> polygon = new ArrayList<>();

        Map<String, Double> point1 = new HashMap<>();
        point1.put("lon", 118.1492);
        point1.put("lat", 30.280629);
        polygon.add(point1);

        Map<String, Double> point2 = new HashMap<>();
        point2.put("lon", 118.126285);
        point2.put("lat", 30.27182);
        polygon.add(point2);

        Map<String, Double> point3 = new HashMap<>();
        point3.put("lon", 118.141998);
        point3.put("lat", 30.264208);
        polygon.add(point3);

        Map<String, Double> point4 = new HashMap<>();
        point4.put("lon", 118.1492);
        point4.put("lat", 30.280629);
        polygon.add(point4);

        // 要测试的点
        double testx = 118.130;
        double testy = 30.2675;

        // 判断点是否在多边形内部
        boolean inside = isPointInPolygon(testx, testy, polygon);
        System.out.println(inside ? "点在多边形内部" : "点在多边形外部");
    }
}
