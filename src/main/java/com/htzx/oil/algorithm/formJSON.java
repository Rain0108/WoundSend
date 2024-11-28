package com.htzx.oil.algorithm;//package algorithm;
//
//import IO.Instance;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class formJSON {
//    public Instance instance;
//
//    public formJSON(Instance instance) {
//        this.instance = instance;
//    }
//
//    public JSONObject formVRP() {
//        JSONObject dataJson = new JSONObject();
//        JSONObject data = new JSONObject();
//        data.put("NumberOfNodes", instance.nameToPositionMap.size());
//        data.put("NumberOfVehicles", 1);
//        dataJson.put("Data", data);
//
//
//        JSONObject parameter = new JSONObject();
//        parameter.put("MultiTrip",true);
//        dataJson.put("Parameter", parameter);
//
//
//        JSONObject capacityConstraint = formCapacityConstraint(instance);
//        JSONObject inventoryCapacityConstraint=formInventoryCapacityConstraint(instance);
////        JSONObject timeWindowConstraint = formGeneralTimeWindowConstraint(instance);
//        JSONObject minimizeDistance = formMinimizeDistance(instance);
//        JSONArray constraint = new JSONArray();
//        constraint.add(capacityConstraint);
//        constraint.add(inventoryCapacityConstraint);
////        constraint.add(timeWindowConstraint);
//        constraint.add(minimizeDistance);
//        dataJson.put("Constraint", constraint);
//
//
//        JSONObject insertBase = new JSONObject();
//        insertBase.put("Name", "InsertBase");
//        JSONArray constructionOperator = new JSONArray();
//        constructionOperator.add(insertBase);
//        dataJson.put("ConstructionOperator", constructionOperator);
//
//        JSONObject greedy = new JSONObject();
//        JSONArray vehicles = formVehicles(instance);
//        greedy.put("Vehicles", vehicles);
//        dataJson.put("Greedy", greedy);
//
//
//        JSONObject relocateBase = new JSONObject();
//        relocateBase.put("Name", "RelocateBase");
//        JSONArray localSearchOperator = new JSONArray();
//        localSearchOperator.add(relocateBase);
//        dataJson.put("LocalSearchOperator", localSearchOperator);
//
//
//        JSONObject nodes = new JSONObject();
//        nodes.put("Customers", formCustomers(instance));
//        nodes.put("Priorities",formPriorities(instance));
//        dataJson.put("Nodes", nodes);
//
//
//        return dataJson;
//    }
//
//    public static JSONArray formVehicles(Instance instance) {
//        JSONArray vehicles = new JSONArray();
//
//        for(int i=0;i<instance.DepotNum;i++) {
//            JSONObject vehicle = new JSONObject();
//            vehicle.put("Depot", i);
//            vehicle.put("NumberOfTypes", 1);
//            List<Integer> typesList = new ArrayList<>();
//            typesList.add(0);
//            vehicle.put("Types", typesList);
//            List<Integer> numberOfEachTypeList = new ArrayList<>();
//            numberOfEachTypeList.add(instance.VehicleNum);
//            vehicle.put("NumberOfEachType", numberOfEachTypeList);
//            vehicles.add(vehicle);
//        }
//        return vehicles;
//    }
//
//    public static JSONObject formInventoryCapacityConstraint(Instance instance)
//    {
//        JSONObject inventoryCapacityConstraint=new JSONObject();
//        inventoryCapacityConstraint.put("Name","InventoryCapacityConstraint");
//        inventoryCapacityConstraint.put("NumberOfDepots",instance.DepotNum);
//        inventoryCapacityConstraint.put("NumberOfSKUs",1);
//        inventoryCapacityConstraint.put("Map",formInventoryMap(instance));
//        inventoryCapacityConstraint.put("Inventory",formInventory(instance));
//
//        return inventoryCapacityConstraint;
//    }
//
//    public static JSONObject formInventoryMap(Instance instance){
//        JSONObject InventoryMap=new JSONObject();
//        for(int i=0;i<instance.DepotNum;i++)
//        {
//            InventoryMap.put(Integer.toString(i),0);
//        }
//
//        for(int i=instance.DepotNum;i<instance.DepotNum+instance.NodeNum;i++)
//        {
//            InventoryMap.put(Integer.toString(i),0);
//        }
//        return InventoryMap;
//    }
//
//    public static JSONObject formInventory(Instance instance)
//    {
//        JSONObject Inventory=new JSONObject();
//        for(int i=0;i<instance.DepotNum;i++)
//        {
//            JSONObject temp=new JSONObject();
//            temp.put("0",instance.oilDepotList.get(instance.IdToNameMap.get(i)).oilList.get(0));
//            Inventory.put(String.valueOf(i),temp);
//        }
//        return Inventory;
//    }
//
//    public static JSONObject formGeneralTimeWindowConstraint(Instance instance) {
//        JSONObject timeWindowConstraint = new JSONObject();
//        timeWindowConstraint.put("Name", "GeneralTimeWindowConstraint");
//        timeWindowConstraint.put("ServiceTimes", formServiceTimes(instance));
////        timeWindowConstraint.put("NodeTimeWindows", formNodeTimeWindows(instance));
////        timeWindowConstraint.put("VehicleTimeWindows", formVehicleTimeWindows(instance));
//        timeWindowConstraint.put("TimeMatrix", instance.nodeMatrix.distanceMatrix);
//        //todo
//        return timeWindowConstraint;
//    }
//
//    public static JSONObject formServiceTimes(Instance instance)
//    {
//        JSONObject ServiceTimes=new JSONObject();
//        // 0表示在油库取油不需要时间
//        for(int i=0;i<instance.DepotNum;i++)
//        {
//            ServiceTimes.put(Integer.toString(i),0);
//        }
//
//        //0 表示0号油
//        for(int i=instance.DepotNum;i<instance.DepotNum+instance.NodeNum;i++)
//        {
//            ServiceTimes.put(Integer.toString(i),
//                    1.0*instance.fightList.get(instance.IdToNameMap.get(i)).oilList.get(0)/instance.supportCarAddOilSpeed.get(0)
//            );
//        }
//
//        return ServiceTimes;
//    }
//
//
//    public static List<Integer> formCustomers(Instance instance) {
//        List<Integer> customers = new ArrayList<>();
//        for (int i = instance.DepotNum; i < instance.DepotNum + instance.NodeNum; i++) {
//            customers.add(i);
//        }
//        return customers;
//    }
//
//
//    public static JSONObject formPriorities(Instance instance) {
//        JSONObject customers = new JSONObject();
//        for (int i = instance.DepotNum; i < instance.DepotNum + instance.NodeNum; i++) {
//            customers.put(Integer.toString(i),instance.fightList.get(instance.IdToNameMap.get(i)).priority);
//        }
//        return customers;
//    }
//
//    public static JSONObject formCapacityConstraint(Instance instance) {
//        JSONObject capacityConstraint = new JSONObject();
//        capacityConstraint.put("Name", "CapacityConstraint");
//        capacityConstraint.put("Dimension",1);
//        capacityConstraint.put("Numbers",formNumbers(instance));
//        capacityConstraint.put("Units",formUnits(instance));
//        capacityConstraint.put("Capacities",formCapacities(instance));
//        return capacityConstraint;
//    }
//
//
//    //todo
//    public static JSONObject formNumbers(Instance instance) {
//        JSONObject numbers = new JSONObject();
//        for(int i=0;i<instance.DepotNum;i++)
//        {
//            numbers.put(String.valueOf(i),0);
//        }
//
//        for (int i = instance.DepotNum; i < instance.DepotNum+instance.NodeNum ; i++) {
//            numbers.put(String.valueOf(i), instance.fightList.get(instance.IdToNameMap.get(i)).oilList.get(0));
//        }
//        return numbers;
//    }
//
//    public static JSONObject formUnits(Instance instance) {
//        JSONObject units = new JSONObject();
//        for(int i=0;i<instance.IdToNameMap.size();i++)
//        {
//            JSONObject temp=new JSONObject();
//            temp.put("0",1.0);
//            units.put(String.valueOf(i),temp);
//        }
//        return units;
//    }
//
//    public static JSONObject formCapacities(Instance instance) {
//        JSONObject capacities = new JSONObject();
//        JSONObject temp=new JSONObject();
//        temp.put("0",instance.VehicleCapacity);
//        capacities.put("0",temp);
//        return capacities;
//    }
//
//
//    public static JSONObject formDemands(Instance instance) {
//        JSONObject numbers = new JSONObject();
//        for (int i = 0; i < instance.DepotNum; i++) {
//            numbers.put(String.valueOf(i), 0);
//        }
//        for (int i = instance.DepotNum; i < instance.DepotNum + instance.NodeNum; i++) {
//            numbers.put(String.valueOf(i), instance.fightList.get(instance.IdToNameMap.get(i)).oilList.get(0));
//        }
//        return numbers;
//    }
//
//
//
//    public static JSONObject formMinimizeDistance(Instance instance)
//    {
//        JSONObject minimizeDistance=new JSONObject();
//        minimizeDistance.put("Name","MinimizeDistance");
//        JSONObject temp=new JSONObject();
//        temp.put("0",1.0);
//        minimizeDistance.put("UnitCosts",temp);
//        minimizeDistance.put("DistanceMatrix",instance.nodeMatrix.distanceMatrix);
//        return minimizeDistance;
//    }
//}
