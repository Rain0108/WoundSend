package com.htzx.oil.algorithm;

import com.htzx.oil.IO.Instance;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.htzx.oil.util.PointInPolygon.isPointInPolygon;

public class createTimeWindow {

    public static void createTW(Instance instance) {
        for (int i = 0; i < instance.sotLists.size(); i++) {
            for (Pair<Double,Double> name : instance.oilDepotList.keySet()) {
                double testx = instance.oilDepotList.get(name).lon;
                double testy = instance.oilDepotList.get(name).lat;
                if (isPointInPolygon(testx, testy, instance.sotLists.get(i).polygon)) {
                    for(int j=0;j<instance.sotLists.get(i).earlyTW.size();j++)
                    {
                        List<Double> inverseTW = new ArrayList<>();
                        inverseTW.add(instance.sotLists.get(i).earlyTW.get(j));
                        inverseTW.add(instance.sotLists.get(i).lateTW.get(j));
                        instance.oilDepotList.get(name).inverseTimeWindow.add(inverseTW);
                    }
                }
            }

            for (Pair<Double,Double> name : instance.fightList.keySet()) {
                double testx = instance.fightList.get(name).lon;
                double testy = instance.fightList.get(name).lat;
                if (isPointInPolygon(testx, testy, instance.sotLists.get(i).polygon)) {
                    for(int j=0;j<instance.sotLists.get(i).earlyTW.size();j++) {
                        List<Double> inverseTW = new ArrayList<>();
                        inverseTW.add(instance.sotLists.get(i).earlyTW.get(j));
                        inverseTW.add(instance.sotLists.get(i).lateTW.get(j));
                        instance.fightList.get(name).inverseTimeWindow.add(inverseTW);
                    }
                }


            }
        }
    }
}
