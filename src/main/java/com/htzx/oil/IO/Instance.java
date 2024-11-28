package com.htzx.oil.IO;

import com.htzx.oil.common.matrix;
import com.htzx.oil.common.novArea;
import com.htzx.oil.common.sotList;
import javafx.util.Pair;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Instance {
    public int type;
    public HashMap<Pair<Double, Double>, com.htzx.oil.common.fightList> fightList=new HashMap<>();
    public HashMap<Pair<Double, Double>, com.htzx.oil.common.oilDepotList> oilDepotList=new HashMap<>();
    public com.htzx.oil.common.oilDepotList supportOilDepot;

    public HashMap<Integer, Integer> supportCarAddOilSpeed=new HashMap<>();
    public HashMap<Integer, Integer> supportCarNum=new HashMap<>();
    public HashMap<Integer, Integer> supportCarCapacity=new HashMap<>();


    public double initialTime;
    public List<sotList> sotLists=new ArrayList<>();
    public List<novArea> novAreas=new ArrayList<>();
    public BidiMap<Integer, String> IdToNameMap =new DualHashBidiMap<>(); //<id,name>

    public BidiMap<String, Pair<Double,Double>> nameToPositionMap=new DualHashBidiMap<>(); //<name,position>

    public matrix nodeMatrix=new matrix();

//    public Integer VehicleNum;
//    public Integer VehicleCapacity;
    public Integer NodeNum;
    public Integer DepotNum;

    // lon x lat y



}
