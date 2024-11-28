package com.htzx.wound.wounded;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 用于将伤员分配结果以 JSON 格式输出的类。
 */
public class printResult {
    //将结果以json格式输出
    Solution solution;
    API api;
    //RouterClass routerClass = new RouterClass();

    /**
     * 获取伤员分配结果的 JSON 对象。
     *
     * @return 包含伤员分配结果的 JSON 对象
     */
    public JSONObject getResult(){
        JSONObject result = new JSONObject();
        result.put("success", solution.success);  //结果返回是否成功
        if(!solution.success) return result;
        //result.put("timeOut", solution.exceedTime);  //配送过程如有超时，返回超时的秒数
        JSONArray dataArray = new JSONArray();
        Assert.isTrue(solution.assignResults != null && solution.assignResults.size() <= 3, "问题没有可行解");
        for(int o=0;o<3;o++) {
            JSONObject cur = new JSONObject();
            JSONArray curOrder = new JSONArray();
            cur.put("sequence", o);  //配送的批次，共有三批，分别对应战场，1级医院和2级医院
            for (int i = 0; i < solution.assignResults.get(o).size(); i++) {
                JSONObject curDemandLocation = new JSONObject();
                curDemandLocation.put("nm", solution.demandsInFact.get(o).get(i).nm);
                JSONArray matchHospital = new JSONArray();
                for(int j=0;j<solution.assignResults.get(o).get(i).size();j++){
                    JSONObject curHospital = new JSONObject();
                    JSONArray wounded = new JSONArray();
                    JSONArray routeList = new JSONArray();
                    JSONArray carList = new JSONArray();
                    curHospital.put("nm", solution.assignResults.get(o).get(i).get(j).hospital.nm);
                    JSONObject curWounded = new JSONObject();
                    curWounded.put("num", solution.assignResults.get(o).get(i).get(j).num);
                    curWounded.put("type", solution.assignResults.get(o).get(i).get(j).woundType);
                    wounded.add(curWounded);
                    int curI = -1, curJ = -1;
                    for(int f=0;f<solution.instance.allNodes.size();f++){
                        if(solution.instance.allNodes.get(f).nm.equals(solution.demandsInFact.get(o).get(i).nm)) curI = f;
                        if(solution.instance.allNodes.get(f).nm.equals(solution.assignResults.get(o).get(i).get(j).hospital.nm)) curJ = f;
                        if(curI != -1 && curJ != -1) break;
                    }
                    /*
                    Route route = routerClass.getRouteInfo_single(new RoadPoint(solution.demandsInFact.get(o).get(i).lon, solution.demandsInFact.get(o).get(i).lat),
                            new RoadPoint(solution.assignResults.get(o).get(i).get(j).hospital.lon, solution.assignResults.get(o).get(i).get(j).hospital.lat), solution.instance.NovAreas);

                     */
                    Route route = api.getCurRoute(curI, curJ);
                    routeList.add(buildRoute(route, curI, curJ, solution, api));
                    for(int k=0;k<solution.carResHashMap.get(o).get(i).size();k++){
                        JSONObject curCar = new JSONObject();
                        if(solution.carResHashMap.get(o).get(i).get(k).hospital.nm.equals(solution.assignResults.get(o).get(i).get(j).hospital.nm)){
                            curCar.put("carID", solution.carResHashMap.get(o).get(i).get(k).id);
                            curCar.put("woundedNum", solution.carResHashMap.get(o).get(i).get(k).sendNum);
                            curCar.put("woundedType", solution.carResHashMap.get(o).get(i).get(k).sendType);
                            carList.add(curCar);
                        }
                    }
                    if (wounded.size() != 0) curHospital.put("wounded", wounded);
                    if (carList.size() != 0) curHospital.put("carList", carList);
                    if (routeList.size() != 0) curHospital.put("recordRouteList", routeList);
                    curHospital.put("startTime", solution.assignResults.get(o).get(i).get(j).startTime);
                    curHospital.put("timeOut", solution.assignResults.get(o).get(i).get(j).timeOut);
                    if (curHospital.size() != 0) matchHospital.add(curHospital);
                }
                curDemandLocation.put("matchHospitalList", matchHospital);
                curOrder.add(curDemandLocation);
                cur.put("matchResult", curOrder);
            }
            dataArray.add(cur);
        }
        result.put("dataArray", dataArray);
        return result;
    }

    /**
     * 构建 Route 对象的 JSON 数组。
     *
     * @param route Route 对象
     * @param demand_ID 需求点 ID
     * @param hospital_ID 医院 ID
     * @param solution Solution 对象
     * @param api API 对象
     * @return 包含 Route 信息的 JSON 数组
     */
    public JSONArray buildRoute(Route route, int demand_ID, int hospital_ID, Solution solution, API api){
        JSONArray routeList = new JSONArray();
        JSONObject route1 = new JSONObject();
        route1.put("time", route.time);
        route1.put("distance", route.distance);
        /*
        JSONArray pointList = new JSONArray();
        for(int x=0;x<route.pointList.size();x++){
            JSONObject curPoint = new JSONObject();
            curPoint.put("lon", route.pointList.get(x).lon);
            curPoint.put("lat", route.pointList.get(x).lat);
            pointList.add(curPoint);
        }
        JSONArray pathInfoList = new JSONArray();
        for(int x=0;x<route.pathInfoList.size();x++){
            JSONObject path = new JSONObject();
            path.put("name", route.pathInfoList.get(x).name);
            JSONObject startPoint = new JSONObject();
            startPoint.put("lon", route.pathInfoList.get(x).start.lon);
            startPoint.put("lat", route.pathInfoList.get(x).start.lat);
            JSONObject endPoint = new JSONObject();
            endPoint.put("lon", route.pathInfoList.get(x).end.lon);
            endPoint.put("lat", route.pathInfoList.get(x).end.lat);
            path.put("startPoint", startPoint);
            path.put("endPoint", endPoint);
            path.put("roadType", route.pathInfoList.get(x).roadType);
            path.put("averageSpeed", route.pathInfoList.get(x).averageSpeed);
            pathInfoList.add(path);
        }
        route1.put("pointList", pointList);
        route1.put("pathInfoList", pathInfoList);


         */
        routeList.add(route1);
        return routeList;
    }

    /**
     * 构造函数，初始化 printResult 对象。
     *
     * @param solution Solution 对象
     * @param api API 对象
     */
    public printResult(Solution solution, API api){
        this.solution = solution;
        this.api = api;
    }
}
