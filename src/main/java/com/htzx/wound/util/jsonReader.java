package com.htzx.wound.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import java.io.*;

public class jsonReader {
    public static JSONObject readJsonFile(String path) {
        File jsonFile = new File(path);
        try {
            FileReader fileReader = new FileReader(jsonFile);
            BufferedReader reader = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            while (true) {
                int ch = reader.read();
                if (ch != -1) {
                    sb.append((char) ch);
                } else {
                    break;
                }
            }
            fileReader.close();
            reader.close();
            return JSON.parseObject(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
