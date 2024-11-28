import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.htzx.oil.IO.oilInputData;
import com.htzx.oil.IO.oilOutputData;
import com.htzx.oil.algorithm.OPT;
import com.htzx.wound.IO.woundedOutputData;
import com.htzx.wound.IO.woundedInputData;
import com.htzx.wound.util.jsonReader;
import com.htzx.wound.wounded.WoundedSend;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

public class test {
    public static void main(String[] args) throws ParseException {
        System.out.println("woundered-----------------------------");

        JSONObject data = jsonReader.readJsonFile("data\\input\\wounded.json");
        woundedInputData woundedInputData = data.toJavaObject(woundedInputData.class);
        woundedOutputData woundedOutputData = WoundedSend.assign(woundedInputData);
        System.out.println(JSON.toJSON(woundedOutputData));

        try (FileWriter writer = new FileWriter("data\\result\\output1.json")) {
            writer.write(String.valueOf(JSON.toJSON(woundedOutputData)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("oil-----------------------------");
        JSONObject data1 = jsonReader.readJsonFile("data/oilRoute.json");

        oilInputData oilInputData = data1.toJavaObject(oilInputData.class);
        oilOutputData res= OPT.opt(oilInputData);

        try (FileWriter writer = new FileWriter("data/output.json")) {
            // 将JSON字符串写入文件
            writer.write(String.valueOf(JSON.toJSON((res))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
