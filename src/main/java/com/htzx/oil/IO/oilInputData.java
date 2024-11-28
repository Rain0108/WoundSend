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
public class oilInputData implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 1：对象优先；2：时间最短；3：路径最短
     */
    private Integer type;
    /**
     * 开始时间
     */
    private String time;
    /**
     * 作战部队
     */
    private List<FightUnit> fightList;

    /**
     * 油库列表
     */
    private List<OilDepot> oilDepotList;
    /**
     * 卫星避让区域列表
     */
    private List<Sot> sotList;
    /**
     * 避让区域
     */
    private List<List<oilMyPoint>> novArea;
    /**
     * 保障部队列表
     */
    private List<SupportUnit> supportList;

    @Data
    public static class FightUnit {
        /**
         * 作战部队内码
         */
        private String fightNM;
        /**
         * 纬度
         */
        private double lat;
        /**
         * 经度
         */
        private double lon;
        /**
         * 需要的油料列表
         */
        private List<Oil> oilList;
        /**
         * 优先级一共10级，1-10表示，1级最高，10级最低
         */
        private Integer priority;
    }

    @Data
    public static class OilDepot {
        /**
         * 油库内码
         */
        private String oilDepotNM;
        /**
         * 纬度
         */
        private double lat;
        /**
         * 经度
         */
        private double lon;
        /**
         * 油料列表
         */
        private List<OilSpeed> oilList;
    }

    @Data
    public static class Sot {
        /**
         * 卫星临空区域
         */
        private List<oilMyPoint> sotPoint;
        /**
         * 卫星临空时间段
         */
        private List<String[]> sotDateList;
    }

    @Data
    public static class SupportUnit {
        /**
         * 保障部队内码
         */
        private String supportNM;
        /**
         * 纬度
         */
        private double lat;
        /**
         * 经度
         */
        private double lon;
        /**
         * 拥有油料列表
         */
        private List<OilSupport> oilList;
    }

    @Data
    public static class Oil {
        /**
         * 油料种类
         */
        private Integer type;
        /**
         * 需求数量
         */
        private Integer num;
    }

    @Data
    public static class OilSpeed extends Oil {
        /**
         * 加油每吨时间单位L/h
         */
        private Integer speed;
    }

    @Data
    public static class OilSupport extends OilSpeed {
        /**
         * 车辆数
         */
        private Integer vehicleNum;
        /**
         * 车辆容量
         */
        private Integer vehicleCapacity;
    }

    // Assuming MyPoint class already defined elsewhere similar to your earlier class.
    @Data
    public static class oilMyPoint {
        /**
         * 经度
         */
        private double lon;
        /**
         * 纬度
         */
        private double lat;
    }


    public static void main(String[] args) {
        JSONObject data = jsonReader.readJsonFile("data/oilRoute.json");
        oilInputData oilInputData = data.toJavaObject(oilInputData.class);
//        System.out.println(oilInputData);
        JSONObject jsonObject = (JSONObject) JSON.toJSON(oilInputData);
        System.out.println(jsonObject);

        try (FileWriter writer = new FileWriter("data/input1.json")) {
            // 将JSON字符串写入文件
            writer.write(String.valueOf(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
