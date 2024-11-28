package com.htzx.oil.util;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class jsonReader {
    public static JSONObject readJsonFile(String path) {
        File file = new File(path);
        BufferedReader reader;
        String laststr = "";
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            //read the file line by line
            while ((tempString = reader.readLine()) != null) {
                laststr = laststr + tempString;
            }
            reader.close();
            //convert the string to JSONObject
            JSONObject jsonObject = JSONObject.parseObject(laststr);
            return jsonObject;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
