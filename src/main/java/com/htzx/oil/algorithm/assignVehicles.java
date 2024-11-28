package com.htzx.oil.algorithm;

import com.htzx.oil.IO.Instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class assignVehicles {
    public static HashMap<Integer, List<Integer>> assign(Instance instance)
    {
        HashMap<Integer, Integer> oilType = new HashMap<>();
        for (Integer oil : instance.supportCarNum.keySet()) {
            // 获取当前fightNode的oilList
            oilType.put(oil, instance.supportCarNum.get(oil));

        }

        HashMap<Integer, List<Integer>> oilTypeNum = new HashMap<>();

        int carIndex = 0;

        for (Map.Entry<Integer, Integer> entry : oilType.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();

            // 使用比例计算车辆数量
            int assignedCars = value;

            // 分配车辆编号
            List<Integer> carIndices = new ArrayList<>();
            for (int i = 0; i < assignedCars; i++) {
                carIndices.add(carIndex++);
            }

            oilTypeNum.put(key, carIndices);
        }

// 输出结果
//        System.out.println(oilTypeNum);

        return oilTypeNum;

    }
}
