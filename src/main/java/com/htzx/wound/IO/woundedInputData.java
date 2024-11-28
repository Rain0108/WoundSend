package com.htzx.wound.IO;

import lombok.Data;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.util.List;
import com.htzx.wound.util.jsonReader;

@Data
public class woundedInputData {

    private Integer type;  //1：时间最少  2：路径最短

    private String startTime;  //起送时间

    private List<Demand> demandLocationList;  //需求列表

    private List<Hospital> hospitalList;  //医院列表

    private List<Sot> sotList;  //卫星临空区域

    private List<List<woundedPoint>> novArea;  //避让区域

    @Data
    public static class Demand{
        //需求列表
        private String nm;    //战场/医院内码

        private Integer type;    //战场/医院类型  0：战场  1:救护所  2:野营/基地

        private BigDecimal lon;    //经度

        private BigDecimal lat;    //纬度

        private List<wounded> woundedList;    //伤员

        private Integer availableCarNum;  //初始车辆数

        private List<Integer> capacity;  //车辆容量
    }

    @Data
    public static class wounded{
        //伤员类
        private Integer type;    //伤员类型;1:轻伤;2:中伤;3:重伤

        private Integer num;    //数量
    }

    @Data
    public static class Hospital{
        private String nm;    //医院内码

        private Integer type;   //医院类型  0：战场  1:救护所  2:野营/基地

        private Integer availableCarNum;  //初始车辆数

        private List<Integer> capacity;  //车辆容量

        private BigDecimal lon;  //经度

        private BigDecimal lat;  //纬度

        private Integer ability;  //救治能力/人

        private Integer bedNums;   //救治床位/人
    }

    @Data
    public static class Sot {

        private List<woundedPoint> sotPoint;  //多边形

        private List<String[]> sotDateList;  //时间窗

    }
    @Data
    public static class woundedPoint{

        private BigDecimal lon;

        private BigDecimal lat;
    }

    public static void main(String[] args) {
        JSONObject data = jsonReader.readJsonFile("data\\input\\wounded.json");
        woundedInputData woundedInputData = data.toJavaObject(woundedInputData.class);
        JSONObject jsonObject = (JSONObject) JSON.toJSON(woundedInputData);
        System.out.println(jsonObject);
    }
}
