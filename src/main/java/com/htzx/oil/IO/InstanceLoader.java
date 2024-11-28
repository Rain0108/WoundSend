package com.htzx.oil.IO;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.htzx.oil.common.fightList;
import com.htzx.oil.common.novArea;
import com.htzx.oil.common.oilDepotList;
import com.htzx.oil.common.sotList;
import com.htzx.oil.util.calDist;
import javafx.util.Pair;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.htzx.oil.util.dateTrans.string2Date;

public class InstanceLoader {

    public static Instance loadInstance(oilInputData path) throws ParseException {
        JSONObject data = (JSONObject) JSON.toJSON(path);
        Instance instance = new Instance();
        JSONArray oilDepotListData = data.getJSONArray("oilDepotList");
        for (int i = 0; i < oilDepotListData.size(); i++) {
            JSONArray oilListData = oilDepotListData.getJSONObject(i).getJSONArray("oilList");

            HashMap<Integer, Integer> oilList = new HashMap<>();
            for (int j = 0; j < oilListData.size(); j++) {
                oilList.put(oilListData.getJSONObject(j).getInteger("type"), oilListData.getJSONObject(j).getInteger("num"));
            }
            HashMap<Integer, Integer> oilSpeedList = new HashMap<>();
            for (int j = 0; j < oilListData.size(); j++) {
                oilSpeedList.put(oilListData.getJSONObject(j).getInteger("type"), oilListData.getJSONObject(j).getInteger("speed"));
            }

            instance.oilDepotList.put(
//                    oilDepotListData.getJSONObject(i).getString("oilDepotNM"),
                    new Pair<>(oilDepotListData.getJSONObject(i).getDouble("lat"),
                            oilDepotListData.getJSONObject(i).getDouble("lon")),
                    new oilDepotList(
                            oilDepotListData.getJSONObject(i).getString("oilDepotNM"),
                            oilDepotListData.getJSONObject(i).getDouble("lat"),
                            oilDepotListData.getJSONObject(i).getDouble("lon"),
                            oilList,
                            oilSpeedList
                    ));

            instance.IdToNameMap.put(i + 1, oilDepotListData.getJSONObject(i).getString("oilDepotNM"));
            instance.nameToPositionMap.put(oilDepotListData.getJSONObject(i).getString("oilDepotNM"), new Pair<>(oilDepotListData.getJSONObject(i).getDouble("lat"), oilDepotListData.getJSONObject(i).getDouble("lon")));
        }

        JSONArray supportListData = data.getJSONArray("supportList");
        for (int i = 0; i < supportListData.size(); i++) {
            JSONArray oilListData = supportListData.getJSONObject(i).getJSONArray("oilList");

            HashMap<Integer, Integer> oilList = new HashMap<>();
            for (int j = 0; j < oilListData.size(); j++) {
                oilList.put(oilListData.getJSONObject(j).getInteger("type"), oilListData.getJSONObject(j).getInteger("num"));
            }
            HashMap<Integer, Integer> oilSpeedList = new HashMap<>();
            for (int j = 0; j < oilListData.size(); j++) {
                oilSpeedList.put(oilListData.getJSONObject(j).getInteger("type"), oilListData.getJSONObject(j).getInteger("speed"));
            }

            HashMap<Integer, Integer> numList = new HashMap<>();
            for (int j = 0; j < oilListData.size(); j++) {
                numList.put(oilListData.getJSONObject(j).getInteger("type"), oilListData.getJSONObject(j).getInteger("vehicleNum"));
            }

            HashMap<Integer, Integer> capacityList = new HashMap<>();
            for (int j = 0; j < oilListData.size(); j++) {
                capacityList.put(oilListData.getJSONObject(j).getInteger("type"), oilListData.getJSONObject(j).getInteger("vehicleCapacity"));
            }

            instance.supportOilDepot=new oilDepotList(
                            supportListData.getJSONObject(i).getString("supportNM"),
                            supportListData.getJSONObject(i).getDouble("lat"),
                            supportListData.getJSONObject(i).getDouble("lon"),
                            oilList,
                            oilSpeedList
                    );
//            instance.VehicleNum=supportListData.getJSONObject(i).getInteger("vehicleNum");
//            instance.VehicleCapacity=supportListData.getJSONObject(i).getInteger("vehicleCapacity");
            instance.supportCarNum = numList;
            instance.supportCarCapacity = capacityList;
            instance.supportCarAddOilSpeed = oilSpeedList;
            instance.IdToNameMap.put(0, supportListData.getJSONObject(i).getString("supportNM"));
            instance.nameToPositionMap.put(supportListData.getJSONObject(i).getString("supportNM"), new Pair<>(supportListData.getJSONObject(i).getDouble("lat"), supportListData.getJSONObject(i).getDouble("lon")));
        }

        JSONArray fightListData = data.getJSONArray("fightList");
        for (int i = 0; i < fightListData.size(); i++) {
            HashMap<Integer, Integer> oilList = new HashMap<>();
            JSONArray oilListData = fightListData.getJSONObject(i).getJSONArray("oilList");
            for (int j = 0; j < oilListData.size(); j++) {
                oilList.put(oilListData.getJSONObject(j).getInteger("type"), oilListData.getJSONObject(j).getInteger("num"));
            }

            instance.fightList.put(
//                    fightListData.getJSONObject(i).getString("FightNM"),
                    new Pair<>(fightListData.getJSONObject(i).getDouble("lat"),
                            fightListData.getJSONObject(i).getDouble("lon")),
                    new fightList(
                            fightListData.getJSONObject(i).getString("fightNM"),
                            fightListData.getJSONObject(i).getDouble("lat"),
                            fightListData.getJSONObject(i).getDouble("lon"),
                            oilList,
                            fightListData.getJSONObject(i).getInteger("priority")
                    ));
            instance.IdToNameMap.put(i + instance.oilDepotList.size(), fightListData.getJSONObject(i).getString("fightNM"));
            instance.nameToPositionMap.put(fightListData.getJSONObject(i).getString("fightNM"), new Pair<>(fightListData.getJSONObject(i).getDouble("lat"), fightListData.getJSONObject(i).getDouble("lon")));

        }

        instance.initialTime = string2Date(data.getString("time"));
        instance.type = data.getInteger("type");

        JSONArray sotListData = data.getJSONArray("sotList");
        for (int i = 0; i < sotListData.size(); i++) {
            List<Map<String, Double>> polygon = new ArrayList<>();

            List<Double> earlyTW = new ArrayList<>();
            List<Double> lateTW = new ArrayList<>();

            JSONArray sotPoint = sotListData.getJSONObject(i).getJSONArray("sotPoint");
            JSONArray sotDateList = sotListData.getJSONObject(i).getJSONArray("sotDateList");

            for (int j = 0; j < sotPoint.size(); j++) {
                Map<String, Double> point = new HashMap<>();
                point.put("lat", sotPoint.getJSONObject(j).getDouble("lat"));
                point.put("lon", sotPoint.getJSONObject(j).getDouble("lon"));
                polygon.add(point);
            }

            for (int j = 0; j < sotDateList.size(); j++) {
                earlyTW.add(string2Date(sotDateList.getJSONArray(j).getString(0)) - instance.initialTime);
                lateTW.add(string2Date(sotDateList.getJSONArray(j).getString(1)) - instance.initialTime);
            }

            instance.sotLists.add(
                    new sotList(
                            polygon,
                            earlyTW,
                            lateTW
                    )
            );
        }

        JSONArray novAreaData = data.getJSONArray("novArea");
        for (int i = 0; i < sotListData.size(); i++) {
            List<Map<String, Double>> polygon = new ArrayList<>();


            JSONArray area = novAreaData.getJSONArray(i);

            for (int j = 0; j < area.size(); j++) {
                Map<String, Double> point = new HashMap<>();
                point.put("lat", area.getJSONObject(j).getDouble("lat"));
                point.put("lon", area.getJSONObject(j).getDouble("lon"));
                polygon.add(point);
            }


            instance.novAreas.add(
                    new novArea(
                            polygon
                    )
            );
        }

        JSONObject distanceMatrix = new JSONObject();
        for (int i = 0; i < instance.IdToNameMap.size(); i++) {
            JSONObject distanceLineMatrix = new JSONObject();
            for (int j = 0; j < instance.IdToNameMap.size(); j++) {
                distanceLineMatrix.put(String.valueOf(j),
                        calDist.cal2dDis(
                                instance.nameToPositionMap.get(instance.IdToNameMap.get(i)).getKey(),
                                instance.nameToPositionMap.get(instance.IdToNameMap.get(i)).getValue(),
                                instance.nameToPositionMap.get(instance.IdToNameMap.get(j)).getKey(),
                                instance.nameToPositionMap.get(instance.IdToNameMap.get(j)).getValue()
                        )
                );
            }
            distanceMatrix.put(String.valueOf(i), distanceLineMatrix);

        }
        instance.nodeMatrix.distanceMatrix = distanceMatrix;

        instance.NodeNum=instance.fightList.size();
        instance.DepotNum=instance.oilDepotList.size();

        return instance;
    }
}
