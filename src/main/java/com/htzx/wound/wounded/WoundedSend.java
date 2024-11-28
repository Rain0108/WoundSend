package com.htzx.wound.wounded;

import com.alibaba.fastjson.JSONObject;
import com.htzx.wound.IO.woundedInputData;
import com.htzx.wound.IO.woundedOutputData;

import java.text.ParseException;

import static com.alibaba.fastjson.JSONObject.toJavaObject;

/**
 * 伤员配送类，负责调用算法生成伤员配送结果。
 */
public class WoundedSend {

    /**
     * 执行伤员配送操作，生成配送结果。
     *
     * @param woundedInputData 伤员输入数据对象，包含医院、需求点等信息。
     * @return 伤员配送结果对象，包含配送方案等信息。
     * @throws ParseException 解析异常，处理输入数据的时间格式。
     */
    public static woundedOutputData assign(woundedInputData woundedInputData) throws ParseException {
        // 创建伤员实例，包含医院、需求点等信息。
        Instance instance = new Instance(woundedInputData);

        // 创建API实例，用于调用API获取两点间距离、时间等信息。
        API api = new API(instance);

        // 创建解决方案实例，进行伤员配送规划。
        Solution solution = new Solution(instance, api);

        // 初始化解决方案，根据不同类型选择路径规划算法。
        boolean isSuccess = solution.ini_Solution(instance.routeType == 1);
        SA sa = new SA(solution, api);
        sa.solution_cost(solution);

        // 输出提示信息，若初始化失败。
        if (!isSuccess) {
            System.out.println("伤员后送部分没有可行解");
        }

        // 创建打印结果实例，用于生成输出结果的JSON对象。
        printResult printResult = new printResult(solution, api);

        // 获取生成的JSON结果对象。
        JSONObject res = printResult.getResult();

        // 将JSON对象转换为Java对象，即伤员输出数据对象。
        woundedOutputData resClass = res.toJavaObject(woundedOutputData.class);

        // 返回伤员输出数据对象。
        return resClass;
    }
}
