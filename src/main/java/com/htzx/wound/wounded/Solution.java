package com.htzx.wound.wounded;

import java.math.BigDecimal;
import java.util.*;

/**
 * 处理伤员后送问题的解决方案类
 */
public class Solution {
    public Instance instance;
    Parameter parameters;
    API api;

    /**
     * assignResults为最终的匹配结果，从外到内分别是配送批次，需求点以及对应的匹配结果
     */
    public ArrayList<ArrayList<ArrayList<AssignPair>>> assignResults;
    /**
     * carResHashMap为最终的车辆分配结果，几个维度分别代表配送批次，需求点和对应的车辆分配结果
     */
    public ArrayList<HashMap<Integer, ArrayList<FinalCarRes>>> carResHashMap;
    public ArrayList<Demand> allDemands; //可能的需求点集合
    public ArrayList<ArrayList<Demand>> demandsInFact;  //三轮运送过程中实际产生的需求点
    public double timeCost;
    public boolean success;

    /**
     * 构造函数，初始化解决方案类
     *
     * @param instance 伤员后送问题实例
     * @param api      与GraphHopper API交互的类，由于时间限制，该类不再调用API，
     *                 而是根据经纬度估算直线距离和时间，输出结果时直接调用API获取起点-终点之间的道路信息
     */
    public Solution(Instance instance, API api){
        parameters = new Parameter();  //参数列表
        this.instance = instance;
        this.api = api;
        this.success = false;  //运送是否成功
        this.assignResults = new ArrayList<>();
        this.carResHashMap = new ArrayList<>();
        this.allDemands = new ArrayList<>();
        this.demandsInFact = new ArrayList<>();
        this.timeCost = 0;
    }

    /**
     * 初始化伤员后送解决方案
     *
     * @param isMinDis 是否选择最短路径
     * @return 初始化是否成功
     */
    public boolean ini_Solution(boolean isMinDis){
        //isMinDis为true：则要求最短总距离，否则最短总时间

        //首先进行allDemands和demandsInFact的初始化，前者一开始就建好，后者每批次配送完毕后将新生成的需求添加进来
        allDemands.addAll(instance.DemandLocationList);
        demandsInFact.add(new ArrayList<>(instance.DemandLocationList));
        for(int i=0;i<2;i++){
            for(int j=0;j<instance.hospitals_x.get(i).size();j++){
                allDemands.add(new Demand(instance.hospitals_x.get(i).get(j).nm, instance.hospitals_x.get(i).get(j).type,
                        instance.hospitals_x.get(i).get(j).lon, instance.hospitals_x.get(i).get(j).lat, instance.hospitals_x.get(i).get(j).availableCars, null, 0));
            }
        }
        ArrayList<Demand> curDemands = instance.DemandLocationList;  //当前需要满足的配送需求，每批次配送完后更新
        ArrayList<Demand> newDemands = new ArrayList<>(); //满足当前需求后会生成的新需求，每批次配送完后更新
        for(int i=0;i<3;i++){
            //尝试进行医院匹配和车辆分配
            HashMap<Integer, ArrayList<FinalCarRes>> carRes = new HashMap<>();
            ArrayList<ArrayList<AssignPair>> cur = assignWounded(curDemands, carRes, newDemands, isMinDis);
            if(cur == null) return false;
            /*
            ArrayList<ArrayList<AssignPair>> new_cur = new ArrayList<>();
            for(int j=0;j<cur.size();j++){
                for(int k=0;k<cur.get(j).size();k++){
                    if(new_cur.size() == 0){
                        ArrayList<AssignPair> temp = new ArrayList<>();
                        temp.add(cur.get(j).get(k));
                        new_cur.add(temp);
                    }
                    for(int l=0;l<new_cur.size();l++){
                        boolean canBeMerged = false;
                        int mergeID = -1;
                        for(int m=0;m< new_cur.get(l).size();m++){
                            if(new_cur.get(l).get(m).demandLocation.nm.equals(cur.get(j).get(k).demandLocation.nm)){
                                canBeMerged = true;
                                mergeID = l;
                            }
                        }
                        if(canBeMerged) {
                            new_cur.get(mergeID).add(cur.get(j).get(k));
                        }
                        else {
                            ArrayList<AssignPair> temp = new ArrayList<>();
                            temp.add(cur.get(j).get(k));
                            new_cur.add(temp);
                        }
                    }
                }
            }

             */
            assignResults.add(cur);
            carResHashMap.add(carRes);
            //成功后如果当前不是第一批，或者说当前需求点不是战场，则需要释放床位
            if(i != 0){
                for (Demand curDemand : curDemands) {
                    int sendNum = 0;//当前需求点送出的伤员总数
                    for (int k = 1; k <= 3; k++) {
                        if (curDemand.woundedList.containsKey(k)) {
                            sendNum += curDemand.woundedList.get(k);
                        }
                    }
                    for (int k = 0; k < instance.hospitals.size(); k++) {
                        if (instance.hospitals.get(k).nm.equals(curDemand.nm)) {
                            //释放需求点的床位和救治能力
                            instance.hospitals.get(k).ability += sendNum;
                            instance.hospitals.get(k).bedNum += sendNum;
                            break;
                        }
                    }
                }
            }
            //ArrayList<Demand> cur = new ArrayList<>();
            //如果当前不是最后一批，或者说目标医院不是3级医院，则在目标医院生成新的配送需求
            //因为目标医院可能已经有配送需求，所以这里将新需求累加进去
            if(i != 2) demandsInFact.add(new ArrayList<>(newDemands));

            //更新allDemands的伤员信息以及下一批需要解决的伤员运送需求
            curDemands.clear();
            curDemands.addAll(newDemands);
            for (Demand allDemand : allDemands) {
                for (Demand curDemand : curDemands) {
                    if (allDemand.nm.equals(curDemand.nm)) {
                        allDemand.woundedList = curDemand.woundedList;
                    }
                }
            }
            newDemands.clear();
        }
        return true;
    }

    /**
     * 进行指定类型需求点集合的分配工作，并生成下一批需求集合。
     *
     * @param DemandLocationList 当前的需求集合
     * @param carRes 保存车辆分配结果的映射
     * @param newDemandLocations 生成的下一批需求
     * @param isMinDis 是否要求最短总距离，true表示最短总距离，false表示最短总时间
     * @return 配送结果的集合，如果配送失败返回null
     */
    public ArrayList<ArrayList<AssignPair>> assignWounded(ArrayList<Demand> DemandLocationList, HashMap<Integer, ArrayList<FinalCarRes>> carRes, ArrayList<Demand> newDemandLocations, boolean isMinDis){
        //进行指定类型需求点集合的分配工作，并生成下一批需求集合
        //st_type是起点类型，DemandLocationList是当前的需求集合，newDemandLocations是生成的下一批需求
        ArrayList<ArrayList<AssignPair>> res = new ArrayList<>();  //当前批次的匹配结果
        for (int i = 0; i < DemandLocationList.size(); i++) {
            ArrayList<AssignPair> curRes = new ArrayList<>();
            int curID = -1;  //找到当前需求点在allNodes中的序号
            for(int t=0;t<instance.allNodes.size();t++){
                if(instance.allNodes.get(t).nm.equals(DemandLocationList.get(i).nm)) {
                    curID = t;
                    break;
                }
            }
            //wT是当前要处理的伤员类型
            for(int wT=1;wT<=3;wT++) {
                int curWounded = 0;  //当前需求点中当前类型伤员的数量
                if(DemandLocationList.get(i).woundedList.containsKey(wT)) curWounded = DemandLocationList.get(i).woundedList.get(wT);
                double waitTime;
                int curIter = 0;  //当前迭代轮次，如果在最大迭代轮次之前满足不了配送需求说明没有可行解，最大迭代轮次在Parameter类中，可调整
                while (curWounded > 0) {
                    if (curIter >= parameters.maxIter) {
                        throw new RuntimeException("在最大迭代轮次之前无法满足配送需求，没有可行解");
                    }
                    waitTime = isInPolygon_inTime(DemandLocationList.get(i).lon, DemandLocationList.get(i).lat, DemandLocationList.get(i).startTime);
                    if (waitTime != 0) {
                        //起送点在扫描区域内且此时在扫描时间内，则需要等到扫描结束
                        DemandLocationList.get(i).waitTime = waitTime;
                    }
                    Hospital bestHospital;
                    //按照要求的不同，选择不同方案来匹配最佳医院
                    if (isMinDis)
                        bestHospital = assign_to_Hospital_dis(DemandLocationList.get(i).lon, DemandLocationList.get(i).lat,
                                wT);
                    else
                        bestHospital = assign_to_Hospital_time(DemandLocationList.get(i).lon, DemandLocationList.get(i).lat,
                                wT);
                    if (bestHospital != null) {
                        if (curWounded <= bestHospital.ability) {
                            //当前目标医院救治能力充足，则全部送过去（这里救治能力和床位数量在instance中就做过比较和交换，保证前者一定不大于后者）
                            curRes.add(new AssignPair(DemandLocationList.get(i), bestHospital, wT, curWounded, DemandLocationList.get(i).startTime, 0));
                            bestHospital.ability -= curWounded;
                            bestHospital.bedNum -= curWounded;
                            curWounded = 0;

                        } else {
                            //大于救治能力，选择全部运送，剩余部分伤员
                            curRes.add(new AssignPair(DemandLocationList.get(i), bestHospital, wT, bestHospital.ability, DemandLocationList.get(i).startTime, 0));
                            bestHospital.bedNum -= bestHospital.ability;
                            curWounded -= bestHospital.ability;
                            bestHospital.ability = 0;
                        }
                        curIter++;
                    } else {
                        //找不到可用医院，配送失败
                        throw new RuntimeException("无法为当前需求点找到可用医院，匹配失败");
                    }
                }

            }
            res.add(curRes);
            //进入车辆分配环节，每个需求点完成匹配之后进行一次车辆分配
            carRes.put(i, assignCarMain(DemandLocationList, newDemandLocations, i, curID, curRes));
        }
        success = true;
        return res;
    }

    /**
     * 进行车辆分配主要逻辑，生成当前批次、当前需求点的车辆分配结果。
     *
     * @param DemandLocationList 当前的需求集合
     * @param newDemands 生成的下一批需求
     * @param iInDemand 当前需求点在需求集合中的索引
     * @param iInAllNodes 当前需求点在allNodes中的索引
     * @param curAssigns 当前匹配对列表
     * @return 当前批次、当前需求点的车辆分配结果
     */
    public ArrayList<FinalCarRes> assignCarMain(ArrayList<Demand> DemandLocationList, ArrayList<Demand> newDemands, int iInDemand, int iInAllNodes, ArrayList<AssignPair> curAssigns){
        ArrayList<FinalCarRes> curFinalRes = new ArrayList<>();  //curFinalRes中储存当前批次，当前需求点的车辆分配结果
        //curTime是当前时间，整体时间轴推进的逻辑如下：
        //匹配过程不推进时间，只是当需要等待扫描结束时将等待时间存入需求点类的waitTime参数中
        //车辆分配过程中每个需求点的时间轴独立推进，会先将当前需求点的需求按照紧急程度降序排列，在车辆分配过程中推进时间轴

        //当前时间=当前需求点的起送时间+因为扫描区域的存在所以可能存在的等待时间
        double curTime = DemandLocationList.get(iInDemand).startTime + DemandLocationList.get(iInDemand).waitTime;
        //按照紧急程度降序排列后的匹配对集合
        ArrayList<AssignPair> orderedPairs = new ArrayList<>(curAssigns.size());
        //所有匹配对的剩余时间，用于表示紧急程度
        double[] leftTime = new double[curAssigns.size()];
        int[] order = new int[curAssigns.size()];
        for(int j=0;j<order.length;j++) order[j] = j;
        //首先按照紧急程度对匹配对进行降序排列
        for(int j=0;j<curAssigns.size();j++) leftTime[j] = parameters.maxWaitTime[curAssigns.get(j).woundType-1] - curTime;
        for (int x=0;x<leftTime.length-1; x++) {
            for (int y=0;y< leftTime.length-1-x; y++) {
                //冒泡排序，对匹配对的剩余时间进行降序排列
                if (leftTime[y] > leftTime[y+1]) {
                    double temp = leftTime[y+1];
                    leftTime[y+1] = leftTime[y];
                    leftTime[y] = temp;
                    int temp1 = order[y+1];
                    order[y+1] = order[y];
                    order[y] = temp1;
                }
            }
        }
        for(int j=0;j<leftTime.length;j++) orderedPairs.add(curAssigns.get(order[j]));
        //此时orderedPairs中存放了降序排列后的当前节点需求列表，每个元素的格式是（目标医院，伤员类型，伤员数量）
        for (AssignPair orderedPair : orderedPairs) {
            //对于当前的匹配对
            double deadLine = DemandLocationList.get(iInDemand).startTime + parameters.maxWaitTime[orderedPair.woundType - 1];//当前配送过程的最后期限
            //分别找到当前需求点和目标医院在allNodes中的序号
            int curJ = -1;
            for (int f = 0; f < instance.allNodes.size(); f++) {
                if (instance.allNodes.get(f).nm.equals(orderedPair.hospital.nm)) curJ = f;
                if (curJ != -1) break;
            }
            int curPairWoundedNum = orderedPair.num;
            while (curPairWoundedNum > 0) {
                //如果当前需求点在扫描区域内且时间刚好进入扫描时间段，则等到扫描结束，同时推进时间轴
                double curWaitTime = isInPolygon_inTime(DemandLocationList.get(iInDemand).lon, DemandLocationList.get(iInDemand).lat,
                        curTime);
                if (curWaitTime != 0)
                    curTime += curWaitTime;

                //开始分配车辆，如果没有立即可用的车，则只会返回最快可用的那辆车，否则会返回立即可用车辆的集合，在其中选取一个空座率最低的方案
                ArrayList<CarAssignRes> carAssignRes = assignCar(DemandLocationList, iInAllNodes, orderedPair, curTime);
                if (carAssignRes == null) throw new RuntimeException("无法为当前需求找到可用车辆");
                if (carAssignRes.get(0).waitTime == 0) {
                    //如果有立即可用的车，则遍历出空座率最低的方案
                    int totalCapacity = 0;  //所有车的总容量
                    for (CarAssignRes carAssignRe : carAssignRes) {
                        totalCapacity += carAssignRe.res_car.capacity;
                    }
                    //如果所有车加起来都不够用，则直接全部用掉，无需等待，所以不推进时间轴
                    if (totalCapacity < curPairWoundedNum) {
                        for (CarAssignRes carAssignRe : carAssignRes) {
                            //将当前选定车辆加入分配结果
                            curFinalRes.add(new FinalCarRes(carAssignRe.res_car.id, carAssignRe.sendNum, orderedPair.woundType,
                                    orderedPair.hospital));
                            //更新当前剩余伤员人数
                            curPairWoundedNum -= carAssignRe.sendNum;

                            //如果车辆到达目的地时，目的地位于扫描区域内且刚好遇到扫描时间，则等待到扫描结束再下车
                            double newCurTime = curTime + api.time[iInAllNodes][curJ];
                            double newWaitTime = isInPolygon_inTime(orderedPair.hospital.lon, orderedPair.hospital.lat, newCurTime);
                            newCurTime += newWaitTime;
                            //如果因等待导致超时，则更新当前匹配对的超时
                            if (newCurTime > deadLine) orderedPair.timeOut += (newCurTime - deadLine);
                            //更新当前选中车辆的下次可用时间，计算公式是：当前时间+往返时间+在目的地可能的等待时间
                            carAssignRe.res_car.nextAvailableTime = curTime + 2 * api.time[iInAllNodes][curJ] + newWaitTime;
                            //生成对应的新需求
                            if (orderedPair.woundType != 3) {
                                //如果伤员不是轻伤，则送到之后伤势降级并生成新的需求
                                //判断当前新需求是否可以合并到已经添加的某个需求中（条件：起点相同，起始时间相同，伤员类型相同）
                                boolean canBeMerged = false;
                                int mergeID = -1; //如果可以合并，则该项就等于合并对象所在位置
                                for (int x = 0; x < newDemands.size(); x++) {
                                    if (newDemands.get(x).nm.equals(orderedPair.hospital.nm) && newCurTime == newDemands.get(x).startTime) {
                                        mergeID = x;
                                        canBeMerged = true;
                                    }
                                }
                                if (canBeMerged) newDemands.get(mergeID).woundedList.put(orderedPair.woundType + 1,
                                        newDemands.get(mergeID).woundedList.get(orderedPair.woundType + 1) + carAssignRe.sendNum);
                                else {
                                    HashMap<Integer, Integer> newWounded = new HashMap<>();
                                    newWounded.put(orderedPair.woundType + 1, carAssignRe.sendNum);
                                    newDemands.add(new Demand(orderedPair.hospital.nm, orderedPair.hospital.type, orderedPair.hospital.lon,
                                            orderedPair.hospital.lat, orderedPair.hospital.availableCars, newWounded, newCurTime));
                                }
                            }
                        }

                    }
                    //否则遍历出所有方案
                    else {
                        //用于存储所有车辆分配方案，包含可行与不可行的
                        ArrayList<ArrayList<Integer>> assign_methods = new ArrayList<>();
                        ArrayList<Integer> flags = new ArrayList<>();
                        for (int x = 0; x < carAssignRes.size(); x++) {
                            flags.add(x);
                        }
                        for (int k = 1; k <= carAssignRes.size(); k++) {
                            ChooseMFromN.getResult(k, flags);  //最少选取一辆车，最多全部选取，按升序得到车辆的全排列
                            ArrayList<ArrayList<Integer>> cur_methods = ChooseMFromN.result;
                            assign_methods.addAll(cur_methods);
                        }
                        //此时assign_methods中包含车辆的所有排列方式，包括不可行的
                        double minUnSeatRate = Double.MAX_VALUE;  //代表最低空座率
                        int curMethodID = -1;  //最低空座率对应的方案编号
                        for (int x = 0; x < assign_methods.size(); x++) {
                            int curTotalCapacity = 0;
                            for (int y = 0; y < assign_methods.get(x).size(); y++) {
                                curTotalCapacity += carAssignRes.get(assign_methods.get(x).get(y)).res_car.capacity;
                            }
                            if (curTotalCapacity < curPairWoundedNum) continue;  //排除总容量不足的组合
                            double unSeatRate = 0;
                            //计算每个组合的空座率
                            int curTotalNum = curPairWoundedNum;
                            int curCarID = 0;
                            while (curTotalNum > 0) {
                                //当前车辆位置过剩，完成分配，否则占满所有位置并转到下一辆车，计算出当前组合的平均空座率
                                if (carAssignRes.get(assign_methods.get(x).get(curCarID)).res_car.capacity > curTotalNum) {
                                    unSeatRate += (1 - (double) curTotalNum / carAssignRes.get(assign_methods.get(x).get(curCarID)).res_car.capacity);
                                    curTotalNum = 0;
                                } else
                                    curTotalNum -= carAssignRes.get(assign_methods.get(x).get(curCarID)).res_car.capacity;
                                curCarID++;
                            }
                            if (unSeatRate < minUnSeatRate) {
                                minUnSeatRate = unSeatRate;
                                curMethodID = x;
                            }
                        }
                        //使用最佳组合进行分配
                        int curTotalNum = curPairWoundedNum;
                        double newCurTime = curTime + api.time[iInAllNodes][curJ];
                        double newWaitTime = isInPolygon_inTime(orderedPair.hospital.lon, orderedPair.hospital.lat, newCurTime);
                        newCurTime += newWaitTime;
                        for (int c = 0; c < assign_methods.get(curMethodID).size(); c++) {
                            if (carAssignRes.get(assign_methods.get(curMethodID).get(c)).res_car.capacity > curTotalNum) {
                                curFinalRes.add(new FinalCarRes(carAssignRes.get(assign_methods.get(curMethodID).get(c)).res_car.id,
                                        curTotalNum, orderedPair.woundType, orderedPair.hospital));
                                carAssignRes.get(assign_methods.get(curMethodID).get(c)).res_car.nextAvailableTime = curTime + 2 * api.time[iInAllNodes][curJ] + newWaitTime;
                                //生成对应的新需求
                                if (orderedPair.woundType != 3) {
                                    //如果伤员不是轻伤，则送到之后伤势降级并生成新的需求
                                    //判断当前新需求是否可以合并到已经添加的某个需求中（条件：起点相同，起始时间相同，伤员类型相同）
                                    boolean canBeMerged = false;
                                    int mergeID = -1; //如果可以合并，则该项就等于合并对象所在位置
                                    for (int x = 0; x < newDemands.size(); x++) {
                                        if (newDemands.get(x).nm.equals(orderedPair.hospital.nm) && newCurTime == newDemands.get(x).startTime) {
                                            mergeID = x;
                                            canBeMerged = true;
                                        }
                                    }
                                    if (canBeMerged) newDemands.get(mergeID).woundedList.put(orderedPair.woundType + 1,
                                            newDemands.get(mergeID).woundedList.get(orderedPair.woundType + 1) + curTotalNum);
                                    else {
                                        HashMap<Integer, Integer> newWounded = new HashMap<>();
                                        newWounded.put(orderedPair.woundType + 1, curTotalNum);
                                        newDemands.add(new Demand(orderedPair.hospital.nm, orderedPair.hospital.type, orderedPair.hospital.lon,
                                                orderedPair.hospital.lat, orderedPair.hospital.availableCars, newWounded, newCurTime));
                                    }
                                }
                                curPairWoundedNum = 0;
                                curTotalNum = 0;
                            } else {
                                curFinalRes.add(new FinalCarRes(carAssignRes.get(assign_methods.get(curMethodID).get(c)).res_car.id,
                                        carAssignRes.get(assign_methods.get(curMethodID).get(c)).res_car.capacity, orderedPair.woundType, orderedPair.hospital));
                                carAssignRes.get(assign_methods.get(curMethodID).get(c)).res_car.nextAvailableTime = curTime + 2 * api.time[iInAllNodes][curJ] + newWaitTime;
                                //生成对应的新需求
                                if (orderedPair.woundType != 3) {
                                    //如果伤员不是轻伤，则送到之后伤势降级并生成新的需求
                                    //判断当前新需求是否可以合并到已经添加的某个需求中（条件：起点相同，起始时间相同，伤员类型相同）
                                    boolean canBeMerged = false;
                                    int mergeID = -1; //如果可以合并，则该项就等于合并对象所在位置
                                    for (int x = 0; x < newDemands.size(); x++) {
                                        if (newDemands.get(x).nm.equals(orderedPair.hospital.nm) && newCurTime == newDemands.get(x).startTime) {
                                            mergeID = x;
                                            canBeMerged = true;
                                        }
                                    }
                                    if (canBeMerged) newDemands.get(mergeID).woundedList.put(orderedPair.woundType + 1,
                                            newDemands.get(mergeID).woundedList.get(orderedPair.woundType + 1) + carAssignRes.get(assign_methods.get(curMethodID).get(c)).res_car.capacity);
                                    else {
                                        HashMap<Integer, Integer> newWounded = new HashMap<>();
                                        newWounded.put(orderedPair.woundType + 1, carAssignRes.get(assign_methods.get(curMethodID).get(c)).res_car.capacity);
                                        newDemands.add(new Demand(orderedPair.hospital.nm, orderedPair.hospital.type, orderedPair.hospital.lon,
                                                orderedPair.hospital.lat, orderedPair.hospital.availableCars, newWounded, newCurTime));
                                    }
                                }
                                curPairWoundedNum -= carAssignRes.get(assign_methods.get(curMethodID).get(c)).res_car.capacity;
                                curTotalNum -= carAssignRes.get(assign_methods.get(curMethodID).get(c)).res_car.capacity;

                            }
                            if (newCurTime > timeCost) timeCost = newCurTime;
                            if (newCurTime > deadLine) orderedPair.timeOut += (newCurTime - deadLine);
                        }
                    }

                } else {
                    //如果没有立即可用的车，则只会返回最快可用的那一辆，需要等待，推进时间轴
                    curFinalRes.add(new FinalCarRes(carAssignRes.get(0).res_car.id, carAssignRes.get(0).sendNum, orderedPair.woundType,
                            orderedPair.hospital));
                    double newCurTime = curTime + api.time[iInAllNodes][curJ];
                    double newWaitTime = isInPolygon_inTime(orderedPair.hospital.lon, orderedPair.hospital.lat, newCurTime);
                    newCurTime += newWaitTime;
                    curTime += carAssignRes.get(0).waitTime;
                    carAssignRes.get(0).res_car.nextAvailableTime = curTime + 2 * api.time[iInAllNodes][curJ] + newWaitTime;
                    //生成对应的新需求
                    if (orderedPair.woundType != 3) {
                        //如果伤员不是轻伤，则送到之后伤势降级并生成新的需求
                        //判断当前新需求是否可以合并到已经添加的某个需求中（条件：起点相同，起始时间相同，伤员类型相同）
                        boolean canBeMerged = false;
                        int mergeID = -1; //如果可以合并，则该项就等于合并对象所在位置
                        for (int x = 0; x < newDemands.size(); x++) {
                            if (newDemands.get(x).nm.equals(orderedPair.hospital.nm) && newCurTime == newDemands.get(x).startTime) {
                                mergeID = x;
                                canBeMerged = true;
                            }
                        }
                        if (canBeMerged) newDemands.get(mergeID).woundedList.put(orderedPair.woundType + 1,
                                newDemands.get(mergeID).woundedList.get(orderedPair.woundType + 1) + carAssignRes.get(0).sendNum);
                        else {
                            HashMap<Integer, Integer> newWounded = new HashMap<>();
                            newWounded.put(orderedPair.woundType + 1, carAssignRes.get(0).sendNum);
                            newDemands.add(new Demand(orderedPair.hospital.nm, orderedPair.hospital.type, orderedPair.hospital.lon,
                                    orderedPair.hospital.lat, orderedPair.hospital.availableCars, newWounded, newCurTime));
                        }
                    }
                    curPairWoundedNum -= carAssignRes.get(0).sendNum;
                    if (newCurTime > timeCost) timeCost = newCurTime;
                    if (newCurTime > deadLine) orderedPair.timeOut += (newCurTime - deadLine);
                }
            }
        }
        return curFinalRes;
    }
    public ArrayList<CarAssignRes> assignCar(ArrayList<Demand> DemandLocationList, int demandID, AssignPair assignPair, double curTime){
        int demandInAllDemandID = -1;
        //因为这里读入的是当前需求点在allNodes中的序号，所以需要转换一下
        for(int i=0;i<DemandLocationList.size();i++){
            if(DemandLocationList.get(i).nm.equals(instance.allNodes.get(demandID).nm)) demandInAllDemandID = i;
        }
        //将当前需求点的立即可用车辆存入cur_availableCars
        ArrayList<Car> cur_availableCars = new ArrayList<>();
        //将其他车辆存入cur_availableCars
        ArrayList<Car> cur_unavailableCars = new ArrayList<>();
        for (int i = 0; i < DemandLocationList.get(demandInAllDemandID).availableCars.size(); i++) {
            if (DemandLocationList.get(demandInAllDemandID).availableCars.get(i).nextAvailableTime <= curTime)
                cur_availableCars.add(DemandLocationList.get(demandInAllDemandID).availableCars.get(i));
            else {
                cur_unavailableCars.add(DemandLocationList.get(demandInAllDemandID).availableCars.get(i));
            }
        }
        ArrayList<CarAssignRes> result = new ArrayList<>();
        if(cur_availableCars.size() != 0) {
            //这种情况下返回所有立即可用车辆，在主程序中用遍历的方式找到空座率最低的方案
            for (Car curAvailableCar : cur_availableCars) {
                result.add(new CarAssignRes(curAvailableCar, 0, Math.min(curAvailableCar.capacity, assignPair.num)));
            }
            return result;
        }
        if(cur_unavailableCars.size() == 0) throw new RuntimeException("无法找到可用车辆");
        else{
            //没有立即可用的车，需要等待
            double minWaitTime = Double.MAX_VALUE;
            int minID = -1;
            for(int i=0;i<cur_unavailableCars.size();i++){
                if(cur_unavailableCars.get(i).nextAvailableTime < minWaitTime) {
                    minWaitTime = cur_unavailableCars.get(i).nextAvailableTime;
                    minID = i;
                }
            }
            result.add(new CarAssignRes(cur_unavailableCars.get(minID), cur_unavailableCars.get(minID).nextAvailableTime - curTime,
                    (Math.min(cur_unavailableCars.get(minID).capacity, assignPair.num))));
            return result;
        }
    }
    public double isInPolygon_inTime(BigDecimal lon, BigDecimal lat, double curTime){
        //如果当前点在扫描区域内且时间在扫描时间段内则返回等待时间，否则返回0，支持同时被多个扫描区域限制的情况
        double waitTime = 0;
        ArrayList<TimeRange> timeRanges = new ArrayList<>();
        for(int j=0;j<instance.SotList.size();j++){
            //对于当前的扫描集合
            for(int k=0;k<instance.SotList.get(j).sotArea.size();k++){
                //对于当前的扫描区域
                if(isPointInPolygon(lon, lat, instance.SotList.get(j).sotArea.get(k).lon, instance.SotList.get(j).sotArea.get(k).lat)) {
                    for(int m=0;m<instance.SotList.get(j).sotTime.size();m++){
                        //对于当前的扫描时间
                        for(int n=0;n<instance.SotList.get(j).sotTime.get(m).st.size();n++){
                            timeRanges.add(new TimeRange(instance.SotList.get(j).sotTime.get(m).st.get(n), instance.SotList.get(j).sotTime.get(m).en.get(n)));
                        }
                    }
                }
            }
        }
        //调用函数合并扫描时间，给出需要等待的总时间
        ArrayList<TimeRange> arr = TimeCoverage.calculateCoverage(timeRanges);
        for (TimeRange timeRange : arr) {
            if (curTime >= timeRange.startTime && curTime <= timeRange.endTime) {
                // 返回当前扫描时间段的结束时间与当前时间的差值
                return timeRange.endTime - curTime;
            }
        }
        // 如果没有符合条件的扫描时间段，返回0
        return waitTime;
    }
    public static boolean isPointInPolygon(BigDecimal testx, BigDecimal testy, ArrayList<BigDecimal> lon, ArrayList<BigDecimal> lat) {
        //true则点在多边形内部
        List<Map<String, Double>> polygon = new ArrayList<>();
        for(int x=0;x<lon.size();x++){
            Map<String, Double> map = new HashMap<>();
            map.put("lon", lon.get(x).doubleValue());
            map.put("lat", lat.get(x).doubleValue());
            polygon.add(map);
        }
        int i, j;
        boolean result = false;
        double x = testx.doubleValue();
        double y = testy.doubleValue();
        for (i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            if ((polygon.get(i).get("lat") > y) != (polygon.get(j).get("lat") > y) &&
                    (x < (polygon.get(j).get("lon") - polygon.get(i).get("lon")) * (y - polygon.get(i).get("lat")) / (polygon.get(j).get("lat")-polygon.get(i).get("lat")) + polygon.get(i).get("lon"))) {
                result = !result;
            }
        }
        return result;
    }
    public Hospital assign_to_Hospital_dis(BigDecimal lon, BigDecimal lat, int woundedType){
        //战场会产出三种类型伤员，重伤只能送到1级，中伤只能2级，轻伤只能3级，送到之后降级
        int curID = -1;
        for(int i=0;i<instance.allNodes.size();i++){
            if(instance.allNodes.get(i).lon.equals(lon) && instance.allNodes.get(i).lat.equals(lat)){
                curID = i;
            }
        }
        int minNum = -1;
        double minDis = Double.MAX_VALUE;
        //ArrayList<Integer> unableHospitals = new ArrayList<>();
        int maxIter = api.distance.length + 1;
        int curIter = 0;
        boolean isSuccess = false;
        ArrayList<Hospital> curDesHospitals = instance.hospitals_x.get(woundedType-1);

        while(curIter < maxIter) {
            int startID = instance.battleGround.size();  //当前医院在allNode列表中的序号
            for(int j=0;j<woundedType-1;j++) startID += instance.hospitals_x.get(j).size();
            for(int i=0;i<curDesHospitals.size();i++){
                if(curDesHospitals.get(i).ability > 0){
                    if(api.time[curID][startID] < minDis){
                        minDis = api.distance[curID][startID];
                        minNum = i;
                    }
                }
                startID++;
            }
            if(minNum != -1){
                isSuccess = true;
                break;
            }
            curIter ++;
        }
        if(isSuccess) return curDesHospitals.get(minNum);
        else return null;
    }
    public Hospital assign_to_Hospital_time(BigDecimal lon, BigDecimal lat, int woundedType){
        //以最小化总时间为目标
        int curID = -1;
        for(int i=0;i<instance.allNodes.size();i++){
            if(instance.allNodes.get(i).lon.equals(lon) && instance.allNodes.get(i).lat.equals(lat)){
                curID = i;
            }
        }
        int minNum = -1;
        double minTime = Double.MAX_VALUE;
        //ArrayList<Integer> unableHospitals = new ArrayList<>();
        int maxIter = api.distance.length + 1;
        int curIter = 0;
        boolean isSuccess = false;
        ArrayList<Hospital> curDesHospitals = instance.hospitals_x.get(woundedType-1);

        while(curIter < maxIter) {
            int startID = instance.battleGround.size();  //当前医院在allNode列表中的序号
            for(int j=0;j<woundedType-1;j++) startID += instance.hospitals_x.get(j).size();
            for(int i=0;i<curDesHospitals.size();i++){
                if(curDesHospitals.get(i).ability > 0){
                    if(api.time[curID][startID] < minTime){
                        minTime = api.time[curID][startID];
                        minNum = i;
                    }
                }
                startID++;
            }
            if(minNum != -1){
                isSuccess = true;
                break;
            }
            curIter ++;
        }
        if(isSuccess) {
            return curDesHospitals.get(minNum);
        }
        else return null;
    }
}
class AssignPair{
    Demand demandLocation;
    Hospital hospital;
    int woundType;
    int num;
    double startTime;
    double timeOut;
    public AssignPair(Demand demandLocation, Hospital hospital, int woundType, int num, double startTime, double timeOut){
        this.demandLocation = demandLocation;
        this.hospital = hospital;
        this.woundType = woundType;
        this.num = num;
        this.startTime = startTime;
        this.timeOut = timeOut;
    }
}
class CarAssignRes{
    Car res_car;
    double waitTime;
    int sendNum;
    public CarAssignRes(Car res_car, double waitTime, int sendNum){
        this.res_car = res_car;
        this.waitTime = waitTime;
        this.sendNum = sendNum;
    }
}
class FinalCarRes{
    //最终返回值中每个需求点使用的车辆详情
    String id;
    int sendNum;  //运送数量
    int sendType;  //运送类型
    Hospital hospital;  //目的地医院
    public FinalCarRes(String id, int sendNum, int sendType, Hospital hospital){
        this.id = id;
        this.sendNum = sendNum;
        this.sendType = sendType;
        this.hospital = hospital;
    }
}

