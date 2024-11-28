package com.htzx.wound.wounded;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

public class API {
    //调用API获得任意两点间的距离，时间以及道路信息
    Instance instance;
    Parameter parameters;

    //RouterClass routerClass = new RouterClass();
    double[][] distance;
    double[][] time;
    ArrayList<ArrayList<Route>> routes;
    public Route getCurRoute(int curI, int curJ){
        if(curI < curJ) return routes.get(curI).get(curJ - curI - 1);
        else return routes.get(curJ).get(curI - curJ - 1);
    }
    public API(Instance instance){
        this.instance = instance;
        distance = new double[instance.allNodes.size()][instance.allNodes.size()];
        time = new double[instance.allNodes.size()][instance.allNodes.size()];
        parameters = new Parameter();
        routes = new ArrayList<>();
        //按照最短距离/最短时间用API数据填充这两个矩阵
        ArrayList<RoadPoint> st = new ArrayList<>();
        ArrayList<RoadPoint> en = new ArrayList<>();
        for(int i=0;i<instance.allNodes.size()-1;i++){
            for(int j=i+1;j<instance.allNodes.size();j++){
                st.add(new RoadPoint(instance.allNodes.get(i).lon, instance.allNodes.get(i).lat));
                en.add(new RoadPoint(instance.allNodes.get(j).lon, instance.allNodes.get(j).lat));
            }
        }
        //ArrayList<Route> result = routerClass.getRouteInfo(st, en, instance.NovAreas);
        //不用GraphHopper的临时方法：
        ArrayList<Route> result = getRouteInfo_temp(st, en);
        int flag = 0;
        for(int i=0;i<instance.allNodes.size()-1;i++){
            ArrayList<Route> arr = new ArrayList<>();
            for(int j=i+1;j<instance.allNodes.size();j++){
                distance[i][j] = result.get(flag).distance;
                time[i][j] = result.get(flag).time;
                distance[j][i] = distance[i][j];
                time[j][i] = time[i][j];
                arr.add(result.get(flag));
                flag++;
            }
            routes.add(arr);
        }
    }
    public ArrayList<Route> getRouteInfo_temp(ArrayList<RoadPoint> st, ArrayList<RoadPoint> en){
        ArrayList<Route> res = new ArrayList<>();
        for(int i=0;i<st.size();i++){
            double st_x = st.get(i).lat.doubleValue();
            double st_y = st.get(i).lon.doubleValue();
            double en_x = en.get(i).lat.doubleValue();
            double en_y = en.get(i).lon.doubleValue();
            double dis = CalculateDistance.GetDistance(st_x, st_y, en_x, en_y);
            res.add(new Route(dis, dis/14, null, null));
        }
        return res;
    }
}
class Route{
    double distance;
    double time;
    ArrayList<RoadPoint> pointList;
    ArrayList<RoadInfo> pathInfoList;
    public Route(double distance, double time, ArrayList<RoadPoint> pointList, ArrayList<RoadInfo> pathInfoList){
        this.distance = distance;
        this.time = time;
        this.pointList = pointList;
        this.pathInfoList = pathInfoList;
    }
}
class RoadPoint{
    BigDecimal lon;
    BigDecimal lat;
    public RoadPoint(BigDecimal lon, BigDecimal lat){
        this.lon = lon;
        this.lat = lat;
    }
}
class RoadInfo{
    String name;
    RoadPoint start;
    RoadPoint end;
    String roadType;
    String averageSpeed;
    public RoadInfo(String name, RoadPoint start, RoadPoint end, String roadType, String averageSpeed){
        this.name = name;
        this.start = start;
        this.end = end;
        this.roadType = roadType;
        this.averageSpeed = averageSpeed;
    }
}
