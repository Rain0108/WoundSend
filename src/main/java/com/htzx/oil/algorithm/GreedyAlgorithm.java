package com.htzx.oil.algorithm;

import com.alibaba.fastjson.JSONObject;
import com.graphhopper.ResponsePath;
import com.htzx.oil.IO.Instance;
import com.htzx.oil.common.fightList;
import com.htzx.oil.common.oilDepotList;
import com.htzx.oil.util.dateTrans;
import javafx.util.Pair;

import java.util.*;

import static com.htzx.oil.algorithm.RouterClass.*;
import static com.htzx.oil.util.TimeWindowCalculator.getEndTime;


public class GreedyAlgorithm {
    public static List<List<Map<String, Double>>> novAreas=new ArrayList<>();

    public static Instance instance;
//    public static GraphHopper hopper;

    public static boolean success;

    GreedyAlgorithm()
    {
        for(com.htzx.oil.common.novArea novArea:instance.novAreas)
        {
            novAreas.add(novArea.area);
        }
    }


    public static List<Pair<List<Location>, List<Pair<Double,Double>>>> serviceNodesPriority(List<Vehicle> vehicles, List<Depot> depots, List<Node> nodes,int oilType) {
        List<Pair<List<Location>, List<Pair<Double,Double>>>> routes = new ArrayList<>();
        //按 load 从大到小排序
        Collections.sort(vehicles, new Comparator<Vehicle>() {
            @Override
            public int compare(Vehicle vehicle1, Vehicle vehicle2) {
                return Integer.compare(vehicle2.load, vehicle1.load);
            }
        });
        outerLoop: while (!allNodesServiced(nodes)) {
            HashMap<Vehicle, Boolean> vehicleOccupyMap = new HashMap<>();
            for (Vehicle vehicle : vehicles) {
                vehicleOccupyMap.put(vehicle, false);
            }
            while (!allNodesServiced(nodes) && vehicleAvailable(vehicleOccupyMap)) {
                List<Integer> oil=new ArrayList<>();
                List<Pair<Double,Double>> oilTW=new ArrayList<>();
                List<Location> route = new ArrayList<>();
                Node nextNode = getHighestPriorityNode(nodes);
                Depot nearestDepot = getNearestDepot(nextNode, depots);
                if(nearestDepot==null) {
                    success=false;
                    break outerLoop;
                }
                Vehicle nearestVehicle = getNearestVehicle(nearestDepot, vehicles, vehicleOccupyMap);
                vehicleOccupyMap.put(nearestVehicle, true);
                route.add(nearestVehicle.location.location);
                oil.add(nearestVehicle.id);
                oilTW.add(new Pair<>((double)nearestVehicle.id,0.0));


                if(nearestVehicle.load< nearestVehicle.capacity)
                {
                    int temp = Math.min(Math.min(nearestVehicle.capacity - nearestVehicle.load, nearestDepot.storage), nextNode.demand);
                    nearestVehicle.load += temp;
                    nearestDepot.storage -= temp;
                    route.add(nearestDepot.location);
                    nearestVehicle.cur_time+=getGHTime_millisecond(getResponsePath(nearestVehicle.location.location, nearestDepot.location));
                    double endTime=getEndTime(nearestVehicle.cur_time,
                            1.0*temp/instance.oilDepotList.get(new Pair<>(nearestDepot.location.lat,nearestDepot.location.lon)).oilSpeedList.get(oilType)*60*60*1000,
                            instance.oilDepotList.get(new Pair<>(nearestDepot.location.lat,nearestDepot.location.lon)).inverseTimeWindow
                            );


                    oil.add(temp);
                    oilTW.add(new Pair<>(nearestVehicle.cur_time,endTime));
                    nearestVehicle.cur_time=endTime;
                }

                nearestVehicle.cur_time+=getGHTime_millisecond(getResponsePath(nearestDepot.location,nextNode.location));
                nearestVehicle.location = nextNode;
                int temp1 = Math.min(nearestVehicle.load, nextNode.demand);
                nextNode.demand -= temp1;
                nearestVehicle.load -= temp1;
                route.add(nextNode.location);
                double endTime=getEndTime(nearestVehicle.cur_time,
                        1.0*temp1/instance.supportCarAddOilSpeed.get(oilType)*60*60*1000,
                        instance.fightList.get(new Pair<>(nextNode.location.lat,nextNode.location.lon)).inverseTimeWindow
                );
                oil.add(-1*temp1);
                oilTW.add(new Pair<>(nearestVehicle.cur_time,endTime));
                nearestVehicle.cur_time=endTime;

                nearestVehicle.location = nextNode;
                System.out.print("vehicle "+oil.get(0)+": ");
                System.out.println(route);
                System.out.println(oil);
                routes.add(new Pair<>(route, oilTW));
            }
        }
        return routes;
    }

    public static List<Pair<List<Location>, List<Pair<Double,Double>>>> serviceNodesDistance(List<Vehicle> vehicles, List<Depot> depots, List<Node> nodes,int oilType) {
        List<Pair<List<Location>, List<Pair<Double,Double>>>> routes = new ArrayList<>();
        //按 load 从大到小排序
        Collections.sort(vehicles, new Comparator<Vehicle>() {
            @Override
            public int compare(Vehicle vehicle1, Vehicle vehicle2) {
                return Integer.compare(vehicle2.load, vehicle1.load);
            }
        });
        while (!allNodesServiced(nodes)&&!allDepotsEmpty(depots)) {
            HashMap<Vehicle, Boolean> vehicleOccupyMap = new HashMap<>();
            for (Vehicle vehicle : vehicles) {
                vehicleOccupyMap.put(vehicle, false);
            }
            while (!allNodesServiced(nodes) && vehicleAvailable(vehicleOccupyMap)&&!allDepotsEmpty(depots)) {
                List<Integer> oil=new ArrayList<>();
                List<Pair<Double,Double>> oilTW=new ArrayList<>();
                List<Location> route = new ArrayList<>();
                Node nextNode = getHighestPriorityNode(nodes);
                DepotAndVehicle result = getNNearestDepot(nextNode, depots, vehicles, vehicleOccupyMap);
                List<Depot> NnearestDepot = result.depots;
                Vehicle nearestVehicle = result.vehicle;
                vehicleOccupyMap.put(nearestVehicle, true);
                route.add(nearestVehicle.location.location);
//                int serviceAmount = 0;
                oil.add(nearestVehicle.id);
                oilTW.add(new Pair<>((double)nearestVehicle.id,0.0));

                Location from=nearestVehicle.location.location;
                Location to_=null;
                if(nearestVehicle.load< nearestVehicle.capacity)
                {
                    for (Depot depot : NnearestDepot) {
                        if (nearestVehicle.load <= nearestVehicle.capacity) {
                            to_=depot.location;
                            int temp = Math.min(nearestVehicle.capacity - nearestVehicle.load, depot.storage);
                            nearestVehicle.load += temp;
                            depot.storage -= temp;
                            oil.add(temp);
                            nearestVehicle.cur_time+=getGHTime_millisecond(getResponsePath(from, to_));
                            double endTime=getEndTime(nearestVehicle.cur_time,
                                    1.0*temp/instance.oilDepotList.get(new Pair<>(to_.lat,to_.lon)).oilSpeedList.get(oilType)*60*60*1000,
                                    instance.oilDepotList.get(new Pair<>(depot.location.lat,depot.location.lon)).inverseTimeWindow
                            );
                            oilTW.add(new Pair<>(nearestVehicle.cur_time,endTime));
                            nearestVehicle.cur_time=endTime;
                            from=depot.location;

                            route.add(depot.location);
                        } else {
                            break;
                        }
                    }
                }


                int temp1 = Math.min(nearestVehicle.load, nextNode.demand);
                nextNode.demand -= temp1;
                to_=nextNode.location;
                nearestVehicle.load -= temp1;
                oil.add(-1*temp1);
                nearestVehicle.cur_time+=getGHTime_millisecond(getResponsePath(from, to_));
                double endTime=getEndTime(nearestVehicle.cur_time,
                         1.0*temp1/instance.supportCarAddOilSpeed.get(oilType)*60*60*1000,
                        instance.fightList.get(new Pair<>(nextNode.location.lat,nextNode.location.lon)).inverseTimeWindow
                );
                oilTW.add(new Pair<>(nearestVehicle.cur_time,endTime));
                nearestVehicle.cur_time=endTime;
                from=nextNode.location;
                nearestVehicle.location = nextNode;
                route.add(nextNode.location);


                // todo//
                while (nearestVehicle.load > 0 && !allNodesServiced(nodes)) {
                    Node nearestNode = getNearestNode(nextNode, nodes);
                    int temp2 = Math.min(nearestVehicle.load, nearestNode.demand);
                    nearestNode.demand -= temp2;
                    nearestVehicle.load -= temp2;
                    oil.add(-1*temp2);
                    to_=nextNode.location;

                    nearestVehicle.cur_time+=getGHTime_millisecond(getResponsePath(from, to_));
                    endTime=getEndTime(nearestVehicle.cur_time,
                            1.0* temp2/instance.supportCarAddOilSpeed.get(oilType)*60*60*1000,
                            instance.fightList.get(new Pair<>(nextNode.location.lat,nextNode.location.lon)).inverseTimeWindow
                    );
                    oilTW.add(new Pair<>(nearestVehicle.cur_time,endTime));
                    nearestVehicle.cur_time=endTime;
                    from=nextNode.location;
                    route.add(nearestNode.location);
                    nearestVehicle.location = nearestNode;
                }
                System.out.print("vehicle "+oil.get(0)+": ");
                System.out.println(route);
                System.out.println(oil);
                routes.add(new Pair<>(route, oilTW));
            }
        }
        return routes;
    }

    public static Depot getNearestDepot(Node node, List<Depot> depots) {
        Depot nearestDepot = null;
        double minDistance = Double.MAX_VALUE;
        for (Depot depot : depots) {
            double distance = getDistance(node.location, depot.location);
            if (distance < minDistance && depot.storage > 0) {
                minDistance = distance;
                nearestDepot = depot;
            }
        }
        return nearestDepot;
    }


    public static boolean vehicleAvailable(HashMap<Vehicle, Boolean> vehicleOccupyMap) {
        for (Vehicle vehicle : vehicleOccupyMap.keySet()) {
            if (!vehicleOccupyMap.get(vehicle)) return true;
        }
        return false;
    }

    public static boolean allDepotsEmpty(List<Depot> depots)
    {
        for (Depot depot:depots)
        {
            if( depot.storage>0) return false;
        }
        return true;
    }

    public static boolean allNodesServiced(List<Node> nodes) {
        for (Node node : nodes) {
            if (node.demand > 0) return false;
        }
        return true;
    }

    public static Node getHighestPriorityNode(List<Node> nodes) {
        nodes.sort(Comparator.comparingInt(a -> a.priority));
        for (Node node : nodes) {
            if (node.demand > 0) return node;
        }
        return null;
    }


    public static Node getNearestNode(Node currentNode, List<Node> nodes) {
        Node nearestNode = null;
        double minDistance = Double.MAX_VALUE;
        for (Node node : nodes) {
            if (node.demand == 0) continue;
            double distance = getDistance(currentNode.location, node.location);
            if (distance < minDistance && node.demand > 0) {
                minDistance = distance;
                nearestNode = node;
            }
        }
        return nearestNode;
    }


    public static DepotAndVehicle getNNearestDepot(Node node, List<Depot> depots, List<Vehicle> vehicles, HashMap<Vehicle, Boolean> vehicleOccupyMap) {
        // 根据与Node的距离对Depots进行排序
        depots.sort((a, b) -> Double.compare(getDistance(node.location, a.location), getDistance(node.location, b.location)));

        List<Depot> seriesOfDepots = new ArrayList<>();
        int totalStorageFromSeries = 0;
        double totalDistanceFromSeries = 0;

        Vehicle chosenVehicleForSeries = getNearestVehicle(depots.get(0), vehicles, vehicleOccupyMap);
        totalStorageFromSeries += depots.get(0).storage + chosenVehicleForSeries.load;
        totalDistanceFromSeries += getDistance(chosenVehicleForSeries.location.location, depots.get(0).location) + getDistance(node.location, depots.get(0).location);
        seriesOfDepots.add(depots.get(0));

        for (int i = 1; i < depots.size(); i++) {
            if (totalStorageFromSeries >= Math.min(node.demand, chosenVehicleForSeries.capacity)) break;
            totalStorageFromSeries += depots.get(i).storage;
            totalDistanceFromSeries += getDistance(node.location, depots.get(i).location);
            totalDistanceFromSeries += getDistance(depots.get(i - 1).location, depots.get(i).location);
            totalDistanceFromSeries -= getDistance(node.location, depots.get(i - 1).location);
            seriesOfDepots.add(depots.get(i));

        }

        Vehicle chosenVehicleForSingleDepot = null;
        double distanceToFirstSufficientDepot = Double.MAX_VALUE;
        for (Depot depot : depots) {
            chosenVehicleForSingleDepot = getNearestVehicle(depot, vehicles, vehicleOccupyMap);
            if (depot.storage >= Math.min(node.demand, chosenVehicleForSingleDepot.capacity - chosenVehicleForSingleDepot.load)) {
                distanceToFirstSufficientDepot = getDistance(chosenVehicleForSingleDepot.location.location, depot.location) + getDistance(node.location, depot.location);
                break;
            }
        }

        if (totalDistanceFromSeries < distanceToFirstSufficientDepot) {
            return new DepotAndVehicle(seriesOfDepots, chosenVehicleForSeries);
        } else {
            return new DepotAndVehicle(Collections.singletonList(depots.get(seriesOfDepots.size() - 1)), chosenVehicleForSingleDepot);
        }
    }


    public static Vehicle getNearestVehicle(Depot depot, List<Vehicle> vehicles, HashMap<Vehicle, Boolean> vehicleOccupyMap) {
        Vehicle nearestVehicle = null;
        double minDistance = Double.MAX_VALUE;
        for (Vehicle vehicle : vehicles) {
            if (!vehicleOccupyMap.get(vehicle)) {
                double distance = getDistance(depot.location, vehicle.location.location);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestVehicle = vehicle;
                }
            }
        }
        return nearestVehicle;
    }

    public static void printLocationMap(List<Pair<List<Location>, List<Pair<Double,Double>>>> locationMap) {
        for (Pair<List<Location>, List<Pair<Double,Double>>> pair : locationMap) {
            System.out.print("Vehicle" + pair.getValue().get(0) + ": ");

            List<Location> locations = pair.getKey();
            for (Location location : locations) {
                System.out.print(location + " ");
            }
            System.out.println();
            System.out.println(pair.getValue());
        }
    }



    public static double getDistance(Location a, Location b) {
        if (instance ==null)
            return Math.sqrt(Math.pow(b.lat - a.lat, 2) + Math.pow(b.lon - a.lon, 2));
        return getGHDistance_meter(getResponsePath(a,b));


    }
    public static ResponsePath getResponsePath(Location a, Location b) {
//        return Math.sqrt(Math.pow(b.lat - a.lat, 2) + Math.pow(b.lon - a.lon, 2));

        if(instance==null) return null;
        if(instance.type==1 || instance.type==2)
        {
            ////type=1最短距离  type=2 最短时间
            return getGHResponse(2,a.lat,a.lon,b.lat,b.lon,novAreas);
        }
        if(instance.type==3 )
        {
            return getGHResponse(1,a.lat,a.lon,b.lat,b.lon,novAreas);
            
        }
        return null;
        
    }


    public static Pair<List<JSONObject>,List<JSONObject>> optInstance(Instance instance, int oilType, List<Integer> vehicleIndex)
    {
        List<Node> nodes=new ArrayList<>();
        nodes.add(new Node(0,0,instance.supportOilDepot.lat,instance.supportOilDepot.lon));
        for(fightList fight:instance.fightList.values())
        {
            nodes.add(new Node(fight.priority,fight.oilList.getOrDefault(oilType,0), fight.lat, fight.lon));
        }

        List<Depot> depots = new ArrayList<>();
        for(oilDepotList depot:instance.oilDepotList.values())
        {
            depots.add(new Depot(depot.oilList.getOrDefault(oilType,0), depot.lat, depot.lon));
        }

        List<Vehicle> vehicles=new ArrayList<>();
        for (Integer index:vehicleIndex)
        {
            vehicles.add(new Vehicle(index,
                    instance.supportCarCapacity.get(oilType),
                    nodes.get(0),
                    Math.min(instance.supportOilDepot.oilList.getOrDefault(oilType,0),
                            instance.supportCarCapacity.get(oilType)),
                    0));
            instance.supportOilDepot.oilList.put(oilType,instance.supportOilDepot.oilList.getOrDefault(oilType,0)-Math.min(instance.supportOilDepot.oilList.getOrDefault(oilType,0),instance.supportCarCapacity.get(oilType)));
        }


        List<Pair<List<Location>, List<Pair<Double,Double>>>> res = null;
        if (instance.type==1)
        {
            res=serviceNodesPriority(vehicles, depots, nodes,oilType);
        }
        else if(instance.type==2 ||instance.type==3)
        {
            res=serviceNodesDistance(vehicles, depots, nodes,oilType);
        }

        // print debug info
//        printLocationMap(res);

        List<JSONObject> recordRouteList=new ArrayList<>();
        List<JSONObject> recordDateList=new ArrayList<>();

        for(Pair<List<Location>, List<Pair<Double,Double>>> acar:res)
        {
//            System.out.println(acar);
            JSONObject recordRoute=new JSONObject();
            JSONObject recordDate=new JSONObject();
            recordRoute.put("vehicle", acar.getValue().get(0).getKey().intValue());
            recordRoute.put("oilType",oilType);
            recordDate.put("vehicle",acar.getValue().get(0).getKey().intValue());
            recordDate.put("oilType",oilType);
            List<cn.hutool.json.JSONObject> pointList=new ArrayList<>();
            List<cn.hutool.json.JSONObject> pathInfoList=new ArrayList<>();
            double total_time=0;
            double total_distance=0;
            for(int i=0;i<acar.getKey().size()-1;i++)
            {
                ResponsePath responsePath = getResponsePath(acar.getKey().get(i), acar.getKey().get(i + 1));
                total_time+= getGHTime_millisecond(responsePath);
                total_distance+= getGHDistance_meter(responsePath);
                pointList.addAll(getPointList(responsePath));
                pathInfoList.addAll(getPathInfos(responsePath));
            }
            List<JSONObject> timeWindowList=new ArrayList<>();

            for(int i=1;i<acar.getValue().size();i++)
            {
                JSONObject TWdate=new JSONObject();

                TWdate.put("NM",instance.nameToPositionMap.getKey(new Pair<>(acar.getKey().get(i).lat,acar.getKey().get(i).lon)));
                TWdate.put("startTime", dateTrans.date2String(acar.getValue().get(i).getKey()+instance.initialTime));
                TWdate.put("endTime",dateTrans.date2String(acar.getValue().get(i).getValue()+instance.initialTime));
                timeWindowList.add(TWdate);
            }

            recordRoute.put("time",total_time);
            recordRoute.put("distance",total_distance);
            recordRoute.put("pointList",pointList);
            recordRoute.put("pathInfoList",pathInfoList);


            recordDate.put("timeWindow",timeWindowList);

            recordDateList.add(recordDate);

            recordRouteList.add(recordRoute);


        }

        return new Pair<>(recordRouteList,recordDateList);
    }

    public static void main(String[] args) {

        List<Node> nodes = Arrays.asList(
                new Node(0, 0, 0, 0),
                new Node(1, 90, 10, 4),
                new Node(1, 200, 8, 10),
                new Node(1, 30, 2, 8)
        );

        List<Depot> depots = Arrays.asList(
                new Depot(30, 4, 6),
                new Depot(20, 4, 12),
                new Depot(30, 4, 8),
                new Depot(400, 30, 12)
        );

        List<Vehicle> vehicles = Arrays.asList(
                new Vehicle(0, 100, nodes.get(0),0,0),
                new Vehicle(1, 100, nodes.get(0),0,0),
                new Vehicle(2, 100, nodes.get(0), 0,0)
        );
        printLocationMap(serviceNodesDistance(vehicles, depots, nodes,0));
        printLocationMap(serviceNodesPriority(vehicles, depots, nodes,0));
        printLocationMap(serviceNodesDistance(vehicles, depots, nodes,0));
        printLocationMap(serviceNodesPriority(vehicles, depots, nodes,0));


    }
}
