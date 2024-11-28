package com.htzx.wound.wounded;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * 用于合并扫描时间，考虑到一个需求点可能被多个扫描区域限制的情况
 */
public class TimeCoverage {

    public static ArrayList<TimeRange> calculateCoverage(ArrayList<TimeRange> timeRanges) {
        if (timeRanges == null || timeRanges.isEmpty())
            // 如果输入为空，返回一个空的时间范围列表
            return new ArrayList<>();
        // 对时间范围进行排序，按照起始时间升序
        timeRanges.sort(Comparator.comparingLong(tr -> tr.startTime));

        ArrayList<TimeRange> result = new ArrayList<>();
        TimeRange currentRange = timeRanges.get(0);

        // 遍历所有时间范围，合并重叠的范围
        for (int i = 1; i < timeRanges.size(); i++) {
            TimeRange nextRange = timeRanges.get(i);

            if (currentRange.endTime < nextRange.startTime) {
                // 当前范围与下一个范围没有重叠，将当前范围加入结果列表，并更新当前范围为下一个范围
                result.add(currentRange);
                currentRange = nextRange;
            } else {
                // 当前范围与下一个范围有重叠，合并范围
                currentRange.endTime = Math.max(currentRange.endTime, nextRange.endTime);
            }
        }

        // 将最后一个范围加入结果列表
        result.add(currentRange);

        return result;
    }
}
class TimeRange {
    long startTime;
    long endTime;

    public TimeRange(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
