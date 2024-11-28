package com.htzx.ghdemo;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.reader.osm.conditional.DateRangeParser;
import com.graphhopper.routing.ev.Roundabout;
import com.graphhopper.routing.ev.VehicleAccess;
import com.graphhopper.routing.ev.VehicleSpeed;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.util.parsers.CarAccessParser;
import com.graphhopper.routing.util.parsers.CarAverageSpeedParser;
import com.graphhopper.routing.weighting.custom.CustomProfile;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.PMap;

public class route {

    static GraphHopper hopper=initial();

    public static GraphHopper initial() {
        String relDir = "C:\\Users\\Rain\\IdeaProjects\\GraphHopper\\mapData\\china-latest.osm.pbf";
        GraphHopper hopper = new GraphHopper();
        hopper.setVehicleEncodedValuesFactory((name, config) -> {
            if (name.equals("truck")) {
                return VehicleEncodedValues.car(new PMap(config).putObject("name", "truck"));
            } else {
                return new DefaultVehicleEncodedValuesFactory().createVehicleEncodedValues(name, config);
            }
        });
        hopper.setVehicleTagParserFactory((lookup, name, config) -> {
            if (name.equals("truck")) {
                return new VehicleTagParsers(
                        new CarAccessParser(lookup.getBooleanEncodedValue(VehicleAccess.key("truck")), lookup.getBooleanEncodedValue(Roundabout.KEY), config, TransportationMode.HGV)
                                .init(config.getObject("date_range_parser", new DateRangeParser())),
                        new CarAverageSpeedParser(lookup.getDecimalEncodedValue(VehicleSpeed.key("truck")), 80),
                        null
                );
            }
            return new DefaultVehicleTagParserFactory().createParsers(lookup, name, config);
        });
        hopper.setOSMFile(relDir).
                setGraphHopperLocation("truckdef/routing-graph-cache").
                setProfiles(
                        new CustomProfile("truck_fastest").setCustomModel(new CustomModel().setDistanceInfluence(50.0)).setVehicle("truck"),
                        new CustomProfile("truck_shortest").setCustomModel(new CustomModel().setDistanceInfluence(120.0)).setVehicle("truck")
//        new CustomProfile("truck_fastest").setVehicle("truck").setWeighting("fastest").setTurnCosts(false),
//                        new CustomProfile("truck_shortest").setVehicle("truck").setWeighting("shortest").setTurnCosts(false)
                );
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("truck_fastest"), new CHProfile("truck_shortest"));
        hopper.importOrLoad();
        return hopper;
    }
    public static GHResponse getGHResponse(GHRequest reqShortest)
    {
        return hopper.route(reqShortest);
    }
}
