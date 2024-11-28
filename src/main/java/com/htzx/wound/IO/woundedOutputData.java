package com.htzx.wound.IO;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class woundedOutputData {

    public Boolean success;  //伤员后送是否成功

    //public String message;
    //public Double timeOut;  //超时秒数

    public List<dataArray> dataArray;

    //public String code;
    @Data
    public static class dataArray{
        public int sequence;
        public List<matchResult> matchResult;
    }

    @Data
    public static class matchResult{
        //战场-医院匹配结果
        public String nm;  //内码

        public List<matchHospitals> matchHospitalList;  //战场-多医院
    }
    @Data
    public static class matchHospitals{
        private String nm;  //医院内码

        private double timeOut;  //超时秒数

        private double startTime;  //起送时间

        private List<woundedClass> wounded;  //伤员

        private List<route> recordRouteList;  //路径

        private List<carClass> carList;  //配送车辆集合
    }

    @Data
    public static class woundedClass{
        //伤员类
        private int num;    //数量

        private int type;    //伤员类型;1:轻伤;2:中伤;3:重伤


    }
    @Data
    public static class route{
        //路径类
        private double time;  //时间

        private double distance;  //距离

        //private List<roadPoint> pointList;  //路径经过点的列表

        //private List<pathInfo> pathInfoList;  //道路信息列表
    }
    @Data
    public static class roadPoint{
        //路径经过的点
        private BigDecimal lon;

        private BigDecimal lat;
    }
    @Data
    public static class pathInfo{
        private String name;  //道路名

        private roadPoint startPoint;  //起始点

        private roadPoint endPoint;  //终点

        private String roadType;  //道路类型

        private String averageSpeed;  //平均速度
    }

    @Data
    public static class carClass{
        //配送车辆类
        private String carID;    //车辆编号

        private Integer woundedNum;  //该车辆运送的伤员数量

        private Integer woundedType;  //该批伤员的类型

    }
}


