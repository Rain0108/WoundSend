package com.htzx.wound.wounded;

import cn.hutool.core.lang.Assert;
import com.htzx.wound.IO.woundedInputData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CheckData {
    public static boolean check(woundedInputData woundedInputData) throws ParseException {
        //每个必要字段的缺省检查
        Assert.notNull(woundedInputData.getType(), "错误：Type缺省");
        if(woundedInputData.getType() != 1 && woundedInputData.getType() != 2){
            throw new RuntimeException("Type值不合法");
        }
        Assert.notNull(woundedInputData.getStartTime(), "错误：startTime缺省");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            Date date = df.parse(woundedInputData.getStartTime());
        }catch(Exception e){
            throw new RuntimeException("错误：起始时间格式不合法");
        }
        Assert.notNull(woundedInputData.getDemandLocationList(), "错误：demandLocationList缺省");
        Assert.notNull(woundedInputData.getHospitalList(), "错误：hospitalList缺省");
        ArrayList<Integer> allWoundedTypes = new ArrayList<>();
        ArrayList<Integer> allHosTypes = new ArrayList<>();
        for(int i=0;i<woundedInputData.getDemandLocationList().size();i++){
            Assert.notNull(woundedInputData.getDemandLocationList().get(i).getNm(), "错误：初始需求点内码缺省");
            Assert.notNull(woundedInputData.getDemandLocationList().get(i).getType(), "错误：初始需求点类型缺省");
            if(woundedInputData.getDemandLocationList().get(i).getType() != 0) {
                throw new RuntimeException("错误：初始需求点类型不为战场");
            }
            Assert.notNull(woundedInputData.getDemandLocationList().get(i).getLat(), "错误：初始需求点纬度缺省");
            Assert.notNull(woundedInputData.getDemandLocationList().get(i).getLon(), "错误：初始需求点经度缺省");
            if(woundedInputData.getDemandLocationList().get(i).getLat().doubleValue() < 0 || woundedInputData.getDemandLocationList().get(i).getLon().doubleValue() < 0){
                throw new RuntimeException("错误：初始需求点坐标不合法");
            }
            Assert.notNull(woundedInputData.getDemandLocationList().get(i).getCapacity(), "错误：初始需求点车辆列表缺省");
            for(int j=0;j<woundedInputData.getDemandLocationList().get(i).getCapacity().size();j++){
                if(woundedInputData.getDemandLocationList().get(i).getCapacity().get(j) < 0){
                    throw new RuntimeException("错误：车辆容量不合法");
                }
            }
            if(woundedInputData.getDemandLocationList().get(i).getAvailableCarNum() != null &&
                    woundedInputData.getDemandLocationList().get(i).getAvailableCarNum() != woundedInputData.getDemandLocationList().get(i).getCapacity().size()){
                throw new RuntimeException("错误：给定的AvailableCarNum与Capacity长度不符");
            }
            //如果AvailableCarNum缺省，则用capacity的长度填充
            if(woundedInputData.getDemandLocationList().get(i).getAvailableCarNum() == null)
                woundedInputData.getDemandLocationList().get(i).setAvailableCarNum(woundedInputData.getDemandLocationList().get(i).getCapacity().size());

            Assert.notNull(woundedInputData.getDemandLocationList().get(i).getWoundedList(), "错误：初始需求点伤员列表缺省");
            ArrayList<Integer> wTs = new ArrayList<>();
            wTs.add(1);
            wTs.add(2);
            wTs.add(3);
            for(int j=0;j<woundedInputData.getDemandLocationList().get(i).getWoundedList().size();j++){
                Assert.notNull(woundedInputData.getDemandLocationList().get(i).getWoundedList().get(j).getType(), "错误：初始需求点伤员类型缺省");
                if(!wTs.contains(woundedInputData.getDemandLocationList().get(i).getWoundedList().get(j).getType())){
                    throw new RuntimeException("伤员类型不合法");
                }
                if(!allWoundedTypes.contains(woundedInputData.getDemandLocationList().get(i).getWoundedList().get(j).getType()))
                    allWoundedTypes.add(woundedInputData.getDemandLocationList().get(i).getWoundedList().get(j).getType());
                Assert.notNull(woundedInputData.getDemandLocationList().get(i).getWoundedList().get(j).getNum(), "错误：初始需求点伤员数量缺省");
                if(!woundedInputData.getDemandLocationList().get(i).getWoundedList().get(j).getNum().getClass().toString().equals("class java.lang.Integer") ||
                        woundedInputData.getDemandLocationList().get(i).getWoundedList().get(j).getNum() < 0){
                    throw new RuntimeException("伤员数量不合法");
                }
            }
        }
        for(int i=0;i<woundedInputData.getHospitalList().size();i++){
            Assert.notNull(woundedInputData.getHospitalList().get(i).getNm(), "错误：医院内码缺省");
            Assert.notNull(woundedInputData.getHospitalList().get(i).getType(), "错误：医院类型缺省");
            ArrayList<Integer> hTs = new ArrayList<>();
            hTs.add(1);
            hTs.add(2);
            hTs.add(3);
            if(!hTs.contains(woundedInputData.getHospitalList().get(i).getType())) {
                throw new RuntimeException("错误：医院类型不合法");
            }
            if(!allHosTypes.contains(woundedInputData.getHospitalList().get(i).getType()))
                allHosTypes.add(woundedInputData.getHospitalList().get(i).getType());
            Assert.notNull(woundedInputData.getHospitalList().get(i).getLat(), "错误：医院纬度缺省");
            Assert.notNull(woundedInputData.getHospitalList().get(i).getLon(), "错误：医院经度缺省");
            if(woundedInputData.getHospitalList().get(i).getLat().doubleValue() < 0 || woundedInputData.getHospitalList().get(i).getLon().doubleValue() < 0){
                throw new RuntimeException("错误：医院坐标不合法");
            }
            //如果医院类型不为3，则必须有车辆列表
            if(woundedInputData.getHospitalList().get(i).getType() != 3) {
                Assert.notNull(woundedInputData.getHospitalList().get(i).getCapacity(), "错误：医院车辆列表缺省");
                for (int j = 0; j < woundedInputData.getHospitalList().get(i).getCapacity().size(); j++) {
                    if (woundedInputData.getHospitalList().get(i).getCapacity().get(j) < 0) {
                        throw new RuntimeException("错误：车辆容量不合法");
                    }
                }
                if (woundedInputData.getHospitalList().get(i).getAvailableCarNum() != null &&
                        woundedInputData.getHospitalList().get(i).getAvailableCarNum() != woundedInputData.getHospitalList().get(i).getCapacity().size()) {
                    throw new RuntimeException("错误：给定的AvailableCarNum与Capacity长度不符");
                }
                //如果AvailableCarNum缺省，则用capacity的长度填充
                if (woundedInputData.getHospitalList().get(i).getAvailableCarNum() == null)
                    woundedInputData.getHospitalList().get(i).setAvailableCarNum(woundedInputData.getHospitalList().get(i).getCapacity().size());
            }
            Assert.notNull(woundedInputData.getHospitalList().get(i).getAbility(), "医院救治能力字段缺省");
            if(!woundedInputData.getHospitalList().get(i).getAbility().getClass().toString().equals("class java.lang.Integer") || woundedInputData.getHospitalList().get(i).getAbility() < 0){
                throw new RuntimeException("错误：医院救治能力数量不合法");
            }
            Assert.notNull(woundedInputData.getHospitalList().get(i).getBedNums(), "医院床位数量字段缺省");
            if(!woundedInputData.getHospitalList().get(i).getBedNums().getClass().toString().equals("class java.lang.Integer") || woundedInputData.getHospitalList().get(i).getBedNums() < 0){
                throw new RuntimeException("错误：医院床位数量不合法");
            }
        }
        //如果存在某一类伤员，却没有对应类型的医院，则有问题
        if(allWoundedTypes.contains(1)){
            //如果初始有重伤伤员，所有类型的医院都要有
            if(!allHosTypes.contains(1) || !allHosTypes.contains(2) || !allHosTypes.contains(3)) {
                throw new RuntimeException("错误：医院类型不足");
            }
        }
        if(allWoundedTypes.contains(2)){
            //如果初始有中伤员，则至少需要2,3级医院
            if(!allHosTypes.contains(2) || !allHosTypes.contains(3)) {
                throw new RuntimeException("错误：医院类型不足");
            }
        }
        if(allWoundedTypes.contains(3)){
            //如果初始有轻伤员，则至少需要3级医院
            if(!allHosTypes.contains(3)) {
                throw new RuntimeException("错误：医院类型不足");
            }
        }
        //如果存在避让区域，则检查合法性:数据不缺省，首尾相连的多边形
        if(woundedInputData.getNovArea() != null){
            for(int i=0;i<woundedInputData.getNovArea().size();i++){
                if(!isPolygon(woundedInputData.getNovArea().get(i))){
                    throw new RuntimeException("错误：避让区域不合法");
                }
            }
        }
        //如果存在扫描区域，检查扫描区域和扫描时间的合法性，并判断是否一一对应
        if(woundedInputData.getSotList() != null){
            for(int i=0;i<woundedInputData.getSotList().size();i++){
                Assert.notNull(woundedInputData.getSotList().get(i).getSotDateList(), "扫描时间缺失");
                Assert.notNull(woundedInputData.getSotList().get(i).getSotPoint(), "扫描区域缺失");
                if(!isPolygon(woundedInputData.getSotList().get(i).getSotPoint())){
                    throw new RuntimeException("错误：扫描区域不合法");
                }
                for(int j=0;j<woundedInputData.getSotList().get(i).getSotDateList().size();j++){
                    if(woundedInputData.getSotList().get(i).getSotDateList().get(j).length != 2){
                        throw new RuntimeException("错误：扫描时间格式不合法");
                    }
                    SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try{
                        Date date = df1.parse(woundedInputData.getSotList().get(i).getSotDateList().get(j)[0]);
                        Date date1 = df1.parse(woundedInputData.getSotList().get(i).getSotDateList().get(j)[1]);
                    }catch(Exception e){
                        throw new RuntimeException("错误：扫描时间不合法");
                    }
                    if(Instance.getTime(woundedInputData.getSotList().get(i).getSotDateList().get(j)[0], woundedInputData.getSotList().get(i).getSotDateList().get(j)[1]) < 0){
                        throw new RuntimeException("错误：扫描时间不合法");
                    }
                }
            }
        }

        return true;
    }

    //判断一组数据是否能够构成一个合法的扫描区域或者避让区域
    public static boolean isPolygon(List<woundedInputData.woundedPoint> points) {
        if (points.size() < 3) {
            // 至少需要三个点
            return false;
        }

        for (int i = 0; i < points.size()-1; i++) {
            woundedInputData.woundedPoint p1 = points.get(i);
            woundedInputData.woundedPoint p2 = points.get((i + 1) % points.size());
            woundedInputData.woundedPoint p3 = points.get((i + 2) % points.size());

            // 检查三个点是否共线
            if (!arePointsCollinear(p1, p2, p3)) {
                return true;  // 如果有三个点不共线，返回true，表示能构成多边形
            }
        }

        return false;  // 所有的三点都共线，不能构成多边形
    }

    private static boolean arePointsCollinear(woundedInputData.woundedPoint p1, woundedInputData.woundedPoint p2, woundedInputData.woundedPoint p3) {
        // 检查三个点是否共线，即斜率相等
        return (p1.getLon().doubleValue() * (p2.getLat().doubleValue() - p3.getLat().doubleValue()) +
                p2.getLon().doubleValue() * (p3.getLat().doubleValue() - p1.getLat().doubleValue()) +
                p3.getLon().doubleValue() * (p1.getLat().doubleValue() - p2.getLat().doubleValue())) == 0;
    }

}
