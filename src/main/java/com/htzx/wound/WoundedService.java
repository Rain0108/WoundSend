package com.htzx.wound;

import com.htzx.wound.IO.woundedInputData;
import com.htzx.wound.IO.woundedOutputData;
import com.htzx.wound.wounded.WoundedSend;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public class WoundedService {

    public woundedOutputData getWoundedOutputData(woundedInputData woundedInputData) throws ParseException {
        woundedOutputData woundedOutputData = WoundedSend.assign(woundedInputData);
        return woundedOutputData;
    }
}
