package com.htzx.oil.IO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.htzx.oil.util.jsonReader;
import lombok.Data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Data
public class oilOutputData implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 车辆路线数据
     */
    private DataField data;
    /**
     * 是否成功
     */
    private boolean success;

    @Data
    public static class DataField {
        /**
         * 最优路径
         */
        private List<RecordRoute> recordRouteList;
        /**
         *补油/保障时间
         */
        private List<RecordDate> recordDateList;
    }

    @Data
    public static class RecordRoute {
        /**
         * 规划线路
         */
        private List<oilMyPoint> pointList;
        /**
         * 里程，米
         */
        private double distance;
        /**
         * 油的种类
         */
        private int oilType;
        /**
         * 道路名称
         */
        private List<PathInfo> pathInfoList;
        /**
         * 线路时间，毫秒
         */
        private double time;
        /**
         * 车辆编号
         */
        private int vehicle;
    }

    @Data
    public static class oilMyPoint {
        /**
         * 经度
         */
        private double lon;
        /**
         *维度
         */
        private double lat;
    }

    @Data
    public static class PathInfo {
        /**
         * 道路起点
         */
        private oilMyPoint startPoint;
        /**
         * 平均速度
         */
        private String averageSpeed;
        /**
         * 道路终点
         */
        private oilMyPoint endPoint;
        /**
         * 道路类型：road，bridge，tunnel
         */
        private String roadType;
        /**
         * 道路名称
         */
        private String name;
    }

    @Data
    public static class RecordDate {
        /**
         * 访问部队或油库时间
         */
        private List<TimeWindow> timeWindow;
        /**
         * 油的种类
         */
        private int oilType;
        /**
         * 车辆编号
         */
        private int vehicle;
    }

    @Data
    public static class TimeWindow {
        /**
         * 开始时间
         */
        private String startTime;
        /**
         * 结束时间
         */
        private String endTime;
        /**
         * 内码
         */
        private String NM;
    }

    public static void main(String[] args) {
        JSONObject data = jsonReader.readJsonFile("data/output.json");
        oilOutputData oilInputData = data.toJavaObject(oilOutputData.class);
//        System.out.println(oilInputData);
        JSONObject jsonObject = (JSONObject) JSON.toJSON(oilInputData);
        System.out.println(jsonObject);

        try (FileWriter writer = new FileWriter("data/output1.json")) {
            // 将JSON字符串写入文件
            writer.write(String.valueOf(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}