package com.htzx.wound.wounded;

public class Parameter {
    public int maxIter = 100;  //伤员分配过程的最大迭代次数
    public int[] maxWaitTime = {3600, 10800, 21600};  //重，中，轻伤伤员分别需要在多长时间内送到医院(单位：秒)
    public double alpha = 0.5;  //第一阶段负载平衡的权重
    public double beta = 0.5;  //第二阶段总时间消耗的权重

    //public String api_path = "http://127.0.0.1:8072/xlgh/api/route/plan";

}
