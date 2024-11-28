package com.htzx.oil;

import com.htzx.oil.IO.oilInputData;
import com.htzx.oil.IO.oilOutputData;
import com.htzx.oil.algorithm.OPT;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public class OilRouteService {

    public oilOutputData getOilRoute(oilInputData oilInputData) throws ParseException {
        oilOutputData res = OPT.opt(oilInputData);
        return res;
    }
}
