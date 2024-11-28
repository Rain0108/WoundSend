package com.htzx.oil.algorithm;

import com.alibaba.fastjson.JSONObject;
import com.htzx.oil.IO.Instance;
import com.htzx.oil.IO.InstanceLoader;
import com.htzx.oil.IO.oilInputData;
import com.htzx.oil.IO.oilOutputData;
import javafx.util.Pair;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OPT {
    public static oilOutputData opt(oilInputData inputPath) throws ParseException {

        JSONObject res=new JSONObject();
        JSONObject data=new JSONObject();
//        String path ="data/test.json";
        Instance original_instance= InstanceLoader.loadInstance(inputPath);
        createTimeWindow.createTW(original_instance);
        GreedyAlgorithm.instance=original_instance;
//        GreedyAlgorithm.hopper= RouterClass.initial();
        GreedyAlgorithm.success=true;
        HashMap<Integer, List<Integer>> oilTypeNum = assignVehicles.assign(original_instance);

        List<JSONObject> recordRouteList=new ArrayList<>();
        List<JSONObject> recordDateList=new ArrayList<>();

        for (Map.Entry<Integer, List<Integer>> entry : oilTypeNum.entrySet()) {
            int key = entry.getKey();
            List<Integer> carIndices = entry.getValue();
            Pair<List<JSONObject>, List<JSONObject>> listListPair = GreedyAlgorithm.optInstance(original_instance, key, carIndices);
            recordRouteList.addAll(listListPair.getKey());
            recordDateList.addAll(listListPair.getValue());
        }
        data.put("recordRouteList",recordRouteList);
        data.put("recordDateList",recordDateList);
        System.out.println(recordRouteList);
        System.out.println(recordDateList);

        res.put("success",true);
        res.put("data",data);

        return res.toJavaObject(oilOutputData.class);
    }
}
