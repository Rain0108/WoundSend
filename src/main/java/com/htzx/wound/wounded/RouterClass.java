package com.htzx.wound.wounded;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
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

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 路由类，处理路线规划和路径信息获取
 */
public class RouterClass {
    /**
     * 获取GraphHopper的响应路径
     *
     * @param type         路线类型，1表示最短距离，2表示最短时间
     * @param fromLat      起始点纬度
     * @param fromLon      起始点经度
     * @param toLat        终点纬度
     * @param toLon        终点经度
     * @param novAreas     避让区域列表
     * @return 响应路径
     */

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
//        GHResponse rsp = hopper.route(reqShortest);
        GHResponse rsp = route.getGHResponse(reqShortest);
//        getPathInfos(rsp.getBest());
        return rsp.getBest();


    }

    public static double getDistance(ResponsePath responsePath)
    {
        return responsePath.getDistance();
    }

    public static double getTime_second(ResponsePath responsePath)
    {
        return (double) responsePath.getTime() / 1000;
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

    private static List<JSONObject> getPathInfos(ResponsePath responsePath) {
        List<JSONObject> pathInfoList = new ArrayList<>();
        Map<String, List<PathDetail>> pathDetailMap = responsePath.getPathDetails();
        List<PathDetail> roadEnvironment = pathDetailMap.get("road_environment");
        List<PathDetail> averageSpeed = pathDetailMap.get("average_speed");
        for (PathDetail streetName : pathDetailMap.get("street_name")) {
            if (ObjectUtil.isEmpty(streetName.getValue())) {
                continue;
            }
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
     * @param novAreas 避让区域列表
     * @param model    自定义模型
     * @return JsonFeatureCollection
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

    public ArrayList<Route> getRouteInfo(ArrayList<RoadPoint> st, ArrayList<RoadPoint> en, ArrayList<NovArea> novArea_list) {

//        GraphHopper hopper=initial();
        List<List<Map<String, Double>>> novAreas=new ArrayList<>();
        //先把novArea转成List-Map形式
        for(int j=0;j<novArea_list.size();j++) {
            List<Map<String, Double>> novarea=new ArrayList<>();
            for (int i = 0; i < novArea_list.get(j).lat.size(); i++) {
                Map<String, Double> m = new HashMap<>();
                m.put("lat", novArea_list.get(j).lat.get(i).doubleValue());
                m.put("lon", novArea_list.get(j).lon.get(i).doubleValue());
                novarea.add(m);
            }
            novAreas.add(novarea);
        }
        ArrayList<Route> routes = new ArrayList<>();
        for(int n=0;n<st.size();n++) {
            ResponsePath responsePath = getGHResponse(1, st.get(n).lat.doubleValue(), st.get(n).lon.doubleValue(), en.get(n).lat.doubleValue(), en.get(n).lon.doubleValue(), novAreas);
            double dis = getDistance(responsePath);
            double time = getTime_second(responsePath);
            List<JSONObject> pointList = getPointList(responsePath);
            List<JSONObject> pathInfos = getPathInfos(responsePath);
            ArrayList<RoadPoint> points = new ArrayList<>();
            ArrayList<RoadInfo> paths = new ArrayList<>();
            for (int i = 0; i < pointList.size(); i++) {
                points.add(new RoadPoint(BigDecimal.valueOf(pointList.get(i).getDouble("lon")), BigDecimal.valueOf(pointList.get(i).getDouble("lat"))));
            }
            for (int i = 0; i < pathInfos.size(); i++) {
                BigDecimal st_lon = BigDecimal.valueOf(pathInfos.get(i).getJSONObject("startPoint").getDouble("lon"));
                BigDecimal st_lat = BigDecimal.valueOf(pathInfos.get(i).getJSONObject("startPoint").getDouble("lat"));
                BigDecimal en_lon = BigDecimal.valueOf(pathInfos.get(i).getJSONObject("endPoint").getDouble("lon"));
                BigDecimal en_lat = BigDecimal.valueOf(pathInfos.get(i).getJSONObject("endPoint").getDouble("lat"));
                paths.add(new RoadInfo(pathInfos.get(i).getStr("name"), new RoadPoint(st_lon, st_lat), new RoadPoint(en_lon, en_lat), pathInfos.get(i).getStr("roadType"), pathInfos.get(i).getStr("averageSpeed")));
            }
            routes.add(new Route(dis, time, points, paths));
        }
        return routes;
    }
    public Route getRouteInfo_single(RoadPoint st, RoadPoint en, ArrayList<NovArea> novArea_list) {
    //获取两点之间的路径信息
//        GraphHopper hopper=initial();
        List<List<Map<String, Double>>> novAreas=new ArrayList<>();
        //先把novArea转成List-Map形式
        for(int j=0;j<novArea_list.size();j++) {
            List<Map<String, Double>> novarea=new ArrayList<>();
            for (int i = 0; i < novArea_list.get(j).lat.size(); i++) {
                Map<String, Double> m = new HashMap<>();
                m.put("lat", novArea_list.get(j).lat.get(i).doubleValue());
                m.put("lon", novArea_list.get(j).lon.get(i).doubleValue());
                novarea.add(m);
            }
            novAreas.add(novarea);
        }
        ResponsePath responsePath = getGHResponse(1, st.lat.doubleValue(), st.lon.doubleValue(), en.lat.doubleValue(), en.lon.doubleValue(), novAreas);
        double dis = getDistance(responsePath);
        double time = getTime_second(responsePath);
        List<JSONObject> pointList = getPointList(responsePath);
        List<JSONObject> pathInfos = getPathInfos(responsePath);
        ArrayList<RoadPoint> points = new ArrayList<>();
        ArrayList<RoadInfo> paths = new ArrayList<>();
        for (int i = 0; i < pointList.size(); i++) {
            points.add(new RoadPoint(BigDecimal.valueOf(pointList.get(i).getDouble("lon")), BigDecimal.valueOf(pointList.get(i).getDouble("lat"))));
        }
        for (int i = 0; i < pathInfos.size(); i++) {
            BigDecimal st_lon = BigDecimal.valueOf(pathInfos.get(i).getJSONObject("startPoint").getDouble("lon"));
            BigDecimal st_lat = BigDecimal.valueOf(pathInfos.get(i).getJSONObject("startPoint").getDouble("lat"));
            BigDecimal en_lon = BigDecimal.valueOf(pathInfos.get(i).getJSONObject("endPoint").getDouble("lon"));
            BigDecimal en_lat = BigDecimal.valueOf(pathInfos.get(i).getJSONObject("endPoint").getDouble("lat"));
            paths.add(new RoadInfo(pathInfos.get(i).getStr("name"), new RoadPoint(st_lon, st_lat), new RoadPoint(en_lon, en_lat), pathInfos.get(i).getStr("roadType"), pathInfos.get(i).getStr("averageSpeed")));
        }
        return new Route(dis, time, points, paths);
    }
}
