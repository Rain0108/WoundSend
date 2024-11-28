package com.htzx.wound.wounded;

public class SA {
    Solution solution;
    API api;
    Parameter parameters;
    public Solution SA_run(){
        return null;
    }
    public double solution_cost(Solution solution){
        for(int i=0;i<solution.assignResults.size();i++){
            //当前批次，计算负载方差
            for(int j=0;j<solution.assignResults.get(i).size();j++){
                for(int k=0;k<solution.assignResults.get(i).get(j).size();k++){

                }
            }
        }
        return 0;
    }
    public SA(Solution solution, API api){
        this.api = api;
        this.solution = solution;
        this.parameters = solution.parameters;
    }
}
