package com.htzx.oil.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


// todo: need to minus a min date
public class dateTrans {
    public static double string2Date(String dateTimeString) throws ParseException {
        // 时间为空则设置为double_max
        if (dateTimeString == null || dateTimeString.equals("")) {
            return Double.MAX_VALUE;
        } else {
            String pattern = "yyyy-MM-dd HH:mm";

            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Date date = sdf.parse(dateTimeString);
            return (double) date.getTime() ;

        }

    }

    public static String date2String(double timestamp) {
        if (timestamp == Double.MAX_VALUE) {
            return ""; // 或者可以根据需求返回特定的字符串，如 "N/A" 或 "Invalid Date"
        } else {
            long timestampMillis = (long) timestamp;
            Date date = new Date(timestampMillis);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(date);
        }
    }

    public static void main(String[] args) {
        System.out.println(date2String(1.6970037E12));
    }

}


