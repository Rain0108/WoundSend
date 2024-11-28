package com.htzx.oil.algorithm;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.json.Statement;
import com.graphhopper.util.*;
import com.graphhopper.util.details.PathDetail;
import com.graphhopper.util.shapes.GHPoint3D;
import com.htzx.ghdemo.route;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RouterClass {

    public static ResponsePath getGHResponse(int type, double fromLat, double fromLon, double toLat, double toLon, List<List<Map<String, Double>>> novAreas) {
        //type=1最短距离  type=2 最短时间
        String profile = type == 1 ? "truck_shortest" : "truck_fastest";
        //请求
        GHRequest reqShortest = new GHRequest(fromLat, fromLon, toLat, toLon).
                // note that we have to specify which profile we are using even when there is only one like here
                        setProfile(profile).
//                        setProfile("truck_fastest").

                // define the language for the turn instructions
                        setLocale(Locale.SIMPLIFIED_CHINESE).
                setPathDetails(Arrays.asList(Parameters.Details.AVERAGE_SPEED, Parameters.Details.STREET_NAME, "road_environment"));

        CustomModel customModel = new CustomModel();
        customModel.setAreas(generateNovAreas(novAreas, customModel));
        reqShortest.setCustomModel(customModel);

        reqShortest.putHint(Parameters.CH.DISABLE, true);
        // rsp 结果 解析
        GHResponse rsp = route.getGHResponse(reqShortest);
//        GHResponse rsp = hopper.route(reqShortest);

//        getPathInfos(rsp.getBest());
        return rsp.getBest();

    }

    public static double getGHDistance_meter(ResponsePath responsePath)
    {

        return responsePath.getDistance();
    }

    public static double getGHTime_millisecond(ResponsePath responsePath)
    {

        return responsePath.getTime();
    }

    public static List<JSONObject> getPointList(ResponsePath responsePath)
    {
        List<JSONObject> pointList = new ArrayList<>();
        PointList pointsList = responsePath.getPoints();
        for (GHPoint3D point : pointsList) {
            JSONObject pointJson=new JSONObject();
            pointJson.putByPath("lat",point.lat);
            pointJson.putByPath("lon",point.lon);
//            System.out.println(point);
            pointList.add(pointJson);
        }
        return pointList;
    }

    public static List<JSONObject> getPathInfos(ResponsePath responsePath) {
        List<JSONObject> pathInfoList = new ArrayList<>();
        Map<String, List<PathDetail>> pathDetailMap = responsePath.getPathDetails();
        List<PathDetail> roadEnvironment = pathDetailMap.get("road_environment");
        List<PathDetail> averageSpeed = pathDetailMap.get("average_speed");
        for (PathDetail streetName : pathDetailMap.get("street_name")) {
//            if (ObjectUtil.isEmpty(streetName.getValue())) {
//                continue;
//            }
//            System.out.println(streetName);
            JSONObject pathInfo = new JSONObject();
            pathInfo.put("name",String.valueOf(streetName.getValue()));
            Set<String> roadEnvironmentValues = getOtherInfo(streetName, roadEnvironment);
            pathInfo.put("roadType",roadEnvironmentValues.stream().collect(Collectors.joining(",")));
            Set<String> averageSpeedValues = getOtherInfo(streetName, averageSpeed);
            pathInfo.put("averageSpeed",averageSpeedValues.stream().collect(Collectors.joining(",")));
            JSONObject startPoint=new JSONObject();
            startPoint.putByPath("lat",responsePath.getPoints().getLat(streetName.getFirst()));
            startPoint.putByPath("lon",responsePath.getPoints().getLon(streetName.getFirst()));
            pathInfo.put("startPoint",startPoint);
            JSONObject endPoint=new JSONObject();
            endPoint.putByPath("lon",responsePath.getPoints().getLon(streetName.getLast()));
            endPoint.putByPath("lat",responsePath.getPoints().getLat(streetName.getLast()));
            pathInfo.put("endPoint",endPoint);
            pathInfoList.add(pathInfo);
        }
        return pathInfoList;
    }

    private static Set<String> getOtherInfo(PathDetail mainInfo, List<PathDetail> values) {
        Set<String> uniqueValues = new HashSet<>();

        int start = findStartIndex(mainInfo.getFirst(), values);
        for (int i = start; i < values.size(); i++) {
            PathDetail info = values.get(i);
            // Check for intersection
            if (info.getFirst() == mainInfo.getFirst() || (info.getLast() > mainInfo.getFirst() && info.getFirst() < mainInfo.getLast())) {
                uniqueValues.add(String.valueOf(values.get(i).getValue()));
            } else if ((mainInfo.getFirst() >= mainInfo.getLast())) {
                break;
            }
        }

        return uniqueValues;
    }


    private static int findStartIndex(int first, List<PathDetail> values) {
        int left = 0, right = values.size() - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (values.get(mid).getLast() >= first) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        return left;
    }


    /**
     * 生产避让区域
     *
     * @param novAreas
     * @return
     */
    public static JsonFeatureCollection generateNovAreas(List<List<Map<String, Double>>> novAreas, CustomModel model) {
        JsonFeatureCollection areas = new JsonFeatureCollection();
        for (int i = 0; i < novAreas.size(); i++) {
            List<Map<String, Double>> myPointList = novAreas.get(i);
            //构建一个闭合的Coordinate
            Coordinate[] area_coordinates = new Coordinate[myPointList.size()];
            Assert.isTrue(myPointList.size() > 3, "避让区域少于3个坐标数");
            Assert.isFalse(!myPointList.get(0).get("lat").equals(myPointList.get(myPointList.size() - 1).get("lat")) || !myPointList.get(0).get("lon").equals(myPointList.get(myPointList.size() - 1).get("lon")), "首尾坐标未相连");
            for (int j = 0; j < myPointList.size(); j++) {
                Map<String, Double> point = myPointList.get(j);
                area_coordinates[j] = new Coordinate(point.get("lon"), point.get("lat"));
            }
            areas.getFeatures().add(new JsonFeature("area_" + i,
                    "Feature",
                    null,
                    new GeometryFactory().createPolygon(area_coordinates),
                    new HashMap<>()));
            model.addToPriority(Statement.If("in_area_" + i, Statement.Op.MULTIPLY, "0"));
        }
        return areas;
    }


    public static String convertMillisToHoursAndMinutes(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long remainingSeconds = totalSeconds % 3600;
        long minutes = remainingSeconds / 60;

        return hours + "小时" + minutes + "分";
    }

    public static String convertMetersToKilometers(double meters) {
        double kilometers = meters / 1000.0;
        return Math.round(kilometers * 100.0) / 100.0 + "公里";
    }
//
//    public static void main(String[] args) {
//
//        GraphHopper hopper=initial();
//        List<List<Map<String, Double>>> novAreas=new ArrayList<>();
//        List<Map<String, Double>> novarea=new ArrayList<>();
//
//        Map<String,Double> m=new HashMap<>();
//        m.put("lat",30.280629);
//        m.put("lon",118.1492);
//        novarea.add(m);
//        m=new HashMap<>();
//        m.put("lat",30.27182);
//        m.put("lon", 118.126285);
//        novarea.add(m);
//
//        m=new HashMap<>();
//        m.put("lat",30.264208);
//        m.put("lon",118.141998);
//        novarea.add(m);
//
//        m=new HashMap<>();
//        m.put("lat",30.280629);
//        m.put("lon",118.1492);
//        novarea.add(m);
//        novAreas.add(novarea);
//
//        novarea=new ArrayList<>();
//        m=new HashMap<>();
//        m.put("lat",31.280629);
//        m.put("lon",128.1492);
//        novarea.add(m);
//        m=new HashMap<>();
//        m.put("lat",31.27182);
//        m.put("lon", 128.126285);
//        novarea.add(m);
//
//        m=new HashMap<>();
//        m.put("lat",31.264208);
//        m.put("lon",128.141998);
//        novarea.add(m);
//
//        m=new HashMap<>();
//        m.put("lat",31.280629);
//        m.put("lon",128.1492);
//        novarea.add(m);
//
//        novAreas.add(novarea);
//
//        System.out.println(getGHDistance_meter(getGHResponse(1, 35.7596, 114.5986, 35.7576, 114.5986, novAreas)));
//        System.out.println(getGHTime_millisecond(getGHResponse(1, 35.7596, 114.5986, 35.7576, 114.5986, novAreas)));
//        System.out.println(getPointList(getGHResponse(1, 35.7596, 114.5986, 35.7576, 114.5986, novAreas)));
//        System.out.println(getPathInfos(getGHResponse(1, 35.7596, 114.5986, 35.7576, 114.5986, novAreas)));
//
//    }
}
