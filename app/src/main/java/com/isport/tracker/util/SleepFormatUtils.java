package com.isport.tracker.util;

import android.util.Log;

import com.isport.tracker.entity.ContinousBarChartEntity;
import com.isport.tracker.entity.sleepResultBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @创建者 bear
 * @创建时间 2019/3/11 11:35
 * @描述
 */
public class SleepFormatUtils {


    public static ArrayList<String> sleepTimeFormatByIndex(int index, ArrayList<sleepResultBean> resultDuration, List<ContinousBarChartEntity> mData, String[] type) {

        if (resultDuration == null) {
            return new ArrayList<>();
        }


        for (int i = 0; i < resultDuration.size(); i++) {
            if (resultDuration.get(i).startIndex <= index && resultDuration.get(i).endIndex >= index) {
                ArrayList<String> result = new ArrayList<>();
                result.add(resultDuration.get(i).startTime + "--" + resultDuration.get(i).endTime);
                result.add(parState(mData, resultDuration.get(i).startIndex, resultDuration.get(i).endIndex, type));
                return result;
            }
        }
        return new ArrayList<>();

       /* int minuteR;
        if (index <= 238) {
            //昨晚的数据 20:00-23:59
            minuteR = 1200 + index;
        } else {
            //今天的数据 0:00-20:00
            minuteR = index - 239;
        }
        long minute = minuteR % 60;
        long hour = minuteR / 60;
        // 分显示两位
        String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
        // 时显示两位
        String strHour = ("00" + hour).substring(("00" + hour).length() - 2);
        return strHour + ":" + strMinute;*/
    }

    public static String parState(List<ContinousBarChartEntity> mData, int startIndex, int endIndex, String[] type) {

        int type0 = 0;
        int type1 = 0;
        int type2 = 0;
        int type3 = 0;
        int type4 = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            if (mData.get(i).type == 0) {
                type0++;
            } else if (mData.get(i).type == 1) {
                type1++;
            } else if (mData.get(i).type == 2) {
                type2++;
            } else if (mData.get(i).type == 3) {
                type3++;
            } else if (mData.get(i).type == 4) {
                type4++;
            }
        }
        return type[4] + ":" + type1 + type[3] + ":" + type2 + type[2] + ":" + type3 + type[1] + ":" + type4;
    }

}
