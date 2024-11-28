package com.htzx.oil.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimeWindowCalculator {
    public static Double getEndTime(Double startTime, Double serviceTime, List<List<Double>> inverseTimeWindow) {
        if (inverseTimeWindow == null || inverseTimeWindow.isEmpty()) {
            // 如果不能工作时间窗口为空，直接返回开始时间 + 服务时间
            return startTime + serviceTime;
        }

        // 初始化工作完成时间为开始时间 + 服务时间
        Double endTime = startTime + serviceTime;

        for (List<Double> window : inverseTimeWindow) {
            if(window.get(1)<=startTime||window.get(0)>=endTime)
                continue;
            else if(window.get(0)>=startTime&&window.get(1)<=endTime)
                endTime+=(window.get(1)- window.get(0));
            else if(window.get(0)<=startTime&&window.get(1)<=endTime)
                endTime+=(window.get(1)-startTime);
            else if(window.get(0)>=startTime && window.get(1)>=endTime)
                endTime= window.get(1)+(endTime- window.get(0));
            else if(window.get(0)<=startTime && window.get(1)>=endTime)
                endTime= window.get(1)+serviceTime;
        }

        return endTime;
    }


    public static void main(String[] args) {
        Double startTime = 8.0; // 开始时间
        Double serviceTime = 1.5; // 服务时间

        // 创建不能工作时间窗口的列表
        List<List<Double>> inverseTimeWindow = new ArrayList<>();
        inverseTimeWindow.add(Arrays.asList(7.0, 10.0)); // 不能工作时间窗口1
        inverseTimeWindow.add(Arrays.asList(11.0, 12.0)); // 不能工作时间窗口2

        Double endTime = getEndTime(startTime, serviceTime, inverseTimeWindow);
        System.out.println("工作完成时间: " + endTime);
    }
}
