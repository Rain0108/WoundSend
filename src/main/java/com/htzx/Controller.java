package com.htzx;
import com.alibaba.fastjson.JSON;
import com.htzx.oil.IO.oilInputData;
import com.htzx.oil.IO.oilOutputData;
import com.htzx.oil.OilRouteService;
import com.htzx.wound.IO.woundedInputData;
import com.htzx.wound.IO.woundedOutputData;
import com.htzx.wound.WoundedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.text.ParseException;


@RestController
@RequestMapping()
public class Controller {
    private final OilRouteService oilRouteService;
    private final WoundedService woundedService;

    @Autowired
    public Controller(OilRouteService oilRouteService, WoundedService woundedService) {
        this.oilRouteService = oilRouteService;
        this.woundedService = woundedService;
    }

    @PostMapping("/oilRoute")
    public oilOutputData getOilRoute(@RequestBody oilInputData oilInputData) throws ParseException {
        return oilRouteService.getOilRoute(oilInputData);
    }

    @PostMapping("/wounded")
    public woundedOutputData getWoundedOutputData(@RequestBody woundedInputData woundedInputData) throws ParseException {
        return woundedService.getWoundedOutputData(woundedInputData);
    }
}
