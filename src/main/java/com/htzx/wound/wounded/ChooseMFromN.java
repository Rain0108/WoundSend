package com.htzx.wound.wounded;

import java.util.ArrayList;

/**
 * 从数组N中取出M个数的全排列，用于列举出分配车辆的所有可能方案
 */
public class ChooseMFromN {
    public static int all = 0;  // 记录总共的排列数
    public static ArrayList<ArrayList<Integer>> result = new ArrayList<>();  // 存储所有排列的结果

    /**
     * 获取从数组n中取出m个数的全排列
     * @param m 要取出的数的个数
     * @param n 待选择的数组
     */
    public static void getResult(int m, ArrayList<Integer> n) {
        result.clear();  // 清空之前的结果
        take("", m, n);  // 开始递归排列
    }

    /**
     * 递归函数，获取从数组list中取出total个数的全排列
     * @param s 当前已排列的字符串
     * @param total 还需要排列的数的个数
     * @param list 待选择的数组
     */
    public static void take(String s, int total, ArrayList<Integer> list) {
        for (int i = 0; i < list.size(); i++) {
            ArrayList<Integer> tempList = new ArrayList<>(list);
            String n = tempList.remove(i).toString();
            String str = s + "-" + n;
            if (total == 1) {
                String[] strings = str.split("-");
                ArrayList<Integer> curCom = new ArrayList<>();
                for (String string : strings) {
                    if (!string.equals(""))
                        curCom.add(Integer.parseInt(string));
                }
                result.add(curCom);  // 将当前排列添加到结果集中
                all++;  // 记录总排列数

            } else {
                int temp = total - 1;
                take(str, temp, tempList);  // 递归调用
            }
        }
    }

    public static void main(String[] args) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(0);
        arrayList.add(1);
        arrayList.add(2);
        getResult(1, arrayList);
        System.out.println();
    }
}
