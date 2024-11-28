package com.htzx.wound.wounded;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.htzx.wound.IO.woundedInputData;

public class Instance {
    Parameter parameters;
    public ArrayList<Hospital> hospitals;  //医院列表
    public ArrayList<ArrayList<Hospital>> hospitals_x;  //分级医院列表
    public int routeType;  //要求的类型：时间最少/路径最短

    public String startTime;  //配送开始时间
    public ArrayList<Demand> DemandLocationList;  //需求列表
    public ArrayList<NovArea> NovAreas;  //避让区域，多边形
    public ArrayList<SotList> SotList;  //卫星扫描区域与时间
    public ArrayList<Node> allNodes;  //包含所有战场和医院的节点集合
    public ArrayList<Node> battleGround;  //战场集合

    public Instance(woundedInputData woundedInputData) throws ParseException {
        CheckData.check(woundedInputData);
        parameters = new Parameter();
        JSONObject jsonObject = (JSONObject) JSON.toJSON(woundedInputData);

        JSONArray hospitalList = jsonObject.getJSONArray("hospitalList");
        hospitals = new ArrayList<>(hospitalList.size());
        hospitals_x = new ArrayList<>();
        //读取医院
        for(int i=0;i<hospitalList.size();i++){
            ArrayList<Car> cars = new ArrayList<>();
            if(hospitalList.getJSONObject(i).getIntValue("type") != 3) {
                for(int j=0;j<hospitalList.getJSONObject(i).getJSONArray("capacity").size();j++){
                    cars.add(new Car(i + "-" + j, hospitalList.getJSONObject(i).getJSONArray("capacity").getIntValue(j), 0L));
                }
            }
            hospitals.add(new Hospital(i, hospitalList.getJSONObject(i).getString("nm"), hospitalList.getJSONObject(i).getIntValue("type"),
                    cars, hospitalList.getJSONObject(i).getBigDecimal("lon"), hospitalList.getJSONObject(i).getBigDecimal("lat"),
                    hospitalList.getJSONObject(i).getIntValue("ability"), hospitalList.getJSONObject(i).getIntValue("bedNums")));
            if(hospitals.get(hospitals.size()-1).ability > hospitals.get(hospitals.size()-1).bedNum){
                int temp = hospitals.get(hospitals.size()-1).ability;
                hospitals.get(hospitals.size()-1).ability = hospitals.get(hospitals.size()-1).bedNum;
                hospitals.get(hospitals.size()-1).bedNum = temp;
            }
        }
        ArrayList<Hospital> temp1 = new ArrayList<>();
        ArrayList<Hospital> temp2 = new ArrayList<>();
        ArrayList<Hospital> temp3 = new ArrayList<>();
        for(int i=0;i<hospitals.size();i++){
            if(hospitals.get(i).type == 1) temp1.add(hospitals.get(i));
            else if (hospitals.get(i).type == 2) temp2.add(hospitals.get(i));
            else temp3.add(hospitals.get(i));
        }
        hospitals_x.add(temp1);
        hospitals_x.add(temp2);
        hospitals_x.add(temp3);

        routeType = jsonObject.getIntValue("type");
        startTime = jsonObject.getString("startTime");

        //读取需求点集合
        JSONArray demandLocationList = jsonObject.getJSONArray("demandLocationList");
        DemandLocationList = new ArrayList<>(demandLocationList.size());
        for(int i=0;i<demandLocationList.size();i++){
            HashMap<Integer, Integer> wounded = new HashMap<>();
            ArrayList<Car> cars = new ArrayList<>();
            for(int j=0;j<demandLocationList.getJSONObject(i).getJSONArray("woundedList").size();j++){
                int curKey = demandLocationList.getJSONObject(i).getJSONArray("woundedList").getJSONObject(j).getIntValue("type");
                int curValue = demandLocationList.getJSONObject(i).getJSONArray("woundedList").getJSONObject(j).getIntValue("num");
                if(!wounded.containsKey(curKey)) wounded.put(curKey, curValue);
                else wounded.put(curKey, wounded.get(curKey) + curValue);
            }
            for(int j=0;j<demandLocationList.getJSONObject(i).getJSONArray("capacity").size();j++){
                cars.add(new Car(i + "-" + j, demandLocationList.getJSONObject(i).getJSONArray("capacity").getIntValue(j), 0L));
            }
            DemandLocationList.add(new Demand(demandLocationList.getJSONObject(i).getString("nm"), demandLocationList.getJSONObject(i).getIntValue("type"),
                    demandLocationList.getJSONObject(i).getBigDecimal("lon"), demandLocationList.getJSONObject(i).getBigDecimal("lat"), cars, wounded, 0));
        }

        //读取避让区域
        JSONArray nov = jsonObject.getJSONArray("novArea");
        NovAreas = new ArrayList<>(nov.size());
        for(int i=0;i<nov.size();i++){
            ArrayList<BigDecimal> lon = new ArrayList<>();
            ArrayList<BigDecimal> lat = new ArrayList<>();
            for(int j=0;j<nov.getJSONArray(i).size();j++){
                lon.add(nov.getJSONArray(i).getJSONObject(j).getBigDecimal("lon"));
                lat.add(nov.getJSONArray(i).getJSONObject(j).getBigDecimal("lat"));

            }
            NovAreas.add(new NovArea(lon, lat));
        }

        //读取扫描区域和扫描时间
        JSONArray sotList = jsonObject.getJSONArray("sotList");
        SotList = new ArrayList<>(sotList.size());
        ArrayList<SotArea> SotAreas = new ArrayList<>();
        ArrayList<SotTime> SotTimes = new ArrayList<>();
        for(int i=0;i<sotList.size();i++){
            ArrayList<BigDecimal> lon = new ArrayList<>();
            ArrayList<BigDecimal> lat = new ArrayList<>();
            ArrayList<Long> st = new ArrayList<>();
            ArrayList<Long> en = new ArrayList<>();
            for(int k=0;k<sotList.getJSONObject(i).getJSONArray("sotPoint").size();k++){
                lon.add(sotList.getJSONObject(i).getJSONArray("sotPoint").getJSONObject(k).getBigDecimal("lon"));
                lat.add(sotList.getJSONObject(i).getJSONArray("sotPoint").getJSONObject(k).getBigDecimal("lat"));
            }
            for(int k=0;k<sotList.getJSONObject(i).getJSONArray("sotDateList").size();k++){
                st.add(getTime(startTime, sotList.getJSONObject(i).getJSONArray("sotDateList").getJSONArray(k).getString(0)));
                en.add(getTime(startTime, sotList.getJSONObject(i).getJSONArray("sotDateList").getJSONArray(k).getString(1)));
            }
            SotAreas.add(new SotArea(lon, lat));
            SotTimes.add(new SotTime(st, en));
            SotList.add(new SotList(SotAreas, SotTimes));
        }

        //战场集合和所有节点集合的初始化
        allNodes = new ArrayList<>();
        battleGround = new ArrayList<>();
        for (Demand demand : DemandLocationList) {
            if (demand.type == 0) {
                allNodes.add(new Node(demand.nm, demand.lon, demand.lat));
                battleGround.add(new Node(demand.nm, demand.lon, demand.lat));
            }
        }
        for (Hospital hospital : hospitals) {
            allNodes.add(new Node(hospital.nm, hospital.lon, hospital.lat));
        }

    }

    public static long getTime(String startTime, String endTime) throws ParseException {
        //获取两个String格式的时间点之间的时间差（单位：秒）
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long eTime = df.parse(endTime).getTime();
        long sTime = df.parse(startTime).getTime();
        return (eTime - sTime) / 1000;
    }
}
class Hospital{
    int ID;
    String nm;
    int type;
    ArrayList<Car> availableCars;
    BigDecimal lon;
    BigDecimal lat;
    int ability;
    int bedNum;
    public Hospital(int ID, String nm, int type, ArrayList<Car> cars, BigDecimal lon, BigDecimal lat, int ability, int bedNum){
        this.ID = ID;
        this.nm = nm;
        this.type = type;
        this.lon = lon;
        this.lat = lat;
        this.ability = ability;
        this.bedNum = bedNum;
        this.availableCars = cars;
    }
}
class Demand{
    String nm;
    int type;
    BigDecimal lon;
    BigDecimal lat;
    ArrayList<Car> availableCars;
    HashMap<Integer, Integer> woundedList;
    double startTime;  //开始配送的时间
    double waitTime;  //需求点如果在扫描区域内又撞上扫描时间则会有waitTime
    public Demand(String nm, int type, BigDecimal lon, BigDecimal lat, ArrayList<Car> cars, HashMap<Integer, Integer> woundedList, double startTime){
        this.nm = nm;
        this.type = type;
        this.lon = lon;
        this.lat = lat;
        this.availableCars = cars;
        this.woundedList = woundedList;
        this.waitTime = 0.0;
        this.startTime = startTime;
    }
}
class NovArea{
    ArrayList<BigDecimal> lon;
    ArrayList<BigDecimal> lat;
    public NovArea(ArrayList<BigDecimal> lon, ArrayList<BigDecimal> lat){
        this.lon = lon;
        this.lat = lat;
    }
}
class SotArea{
    ArrayList<BigDecimal> lon;
    ArrayList<BigDecimal> lat;
    public SotArea(ArrayList<BigDecimal> lon, ArrayList<BigDecimal> lat){
        this.lon = lon;
        this.lat = lat;
    }
}
class SotTime{
    ArrayList<Long> st;
    ArrayList<Long> en;
    public SotTime(ArrayList<Long> st, ArrayList<Long> en){
        this.st = st;
        this.en = en;
    }
}
class SotList{
    ArrayList<SotArea> sotArea;
    ArrayList<SotTime> sotTime;
    public SotList(ArrayList<SotArea> sotArea, ArrayList<SotTime> sotTime){
        this.sotArea = sotArea;
        this.sotTime = sotTime;
    }
}
class Node{
    String nm;
    BigDecimal lon;
    BigDecimal lat;
    public Node(String nm, BigDecimal lon, BigDecimal lat){
        this.nm = nm;
        this.lon = lon;
        this.lat = lat;
    }
}
class Car{
    String id;
    int capacity;
    double nextAvailableTime;  //这辆车出发执行配送任务之后的下次可用时间
    public Car(String id, int capacity, double nextAvailableTime){
        this.id = id;
        this.capacity = capacity;
        this.nextAvailableTime = nextAvailableTime;
    }
}
