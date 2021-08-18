package com.isport.isportlibrary.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@SuppressLint("SimpleDateFormat")
public class DateUtil {

    public static String getTodayStr(String format) {
        Calendar calendar = Calendar.getInstance();
        return dataToString(calendar.getTime(), format);
    }

    public static String dataToString(Date date, String format) {
        SimpleDateFormat format1 = new SimpleDateFormat(format);
        return format1.format(date);
    }

    public static Date stringToDate(String date, String format) {
        SimpleDateFormat format1 = new SimpleDateFormat(format);
        try {
            return format1.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date longToDate(long time) {
        return new Date(time);
    }

    public static String toString(int number) {
        return number < 10 ? "0" + number : "" + number;
    }

    public static boolean is24Hour(Context context) {
        String timeFormat = android.provider.Settings.System.getString(context.getContentResolver(),
                                                                       android.provider.Settings.System.TIME_12_24);

        return DateFormat.is24HourFormat(context);
/*
        if(timeFormat.equals("24")){
			return true;
		}
		return false;*/
    }

    public static int getTimeZone() {
        TimeZone timezone = TimeZone.getDefault();
        int rawOffSet = timezone.getRawOffset();
        int offset = rawOffSet / 3600000;
        return offset;
    }

    public static int getWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public static int getDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static Calendar getCurrentCalendar() {
        return Calendar.getInstance();
    }

    public static Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    /**
     * 获取前40分钟的时间list
     *
     * @param nowTime
     * @return
     */
    public static List<String> getLast40MintueTime(String nowTime) {
        List histList = new ArrayList();
        int nowM = Integer.parseInt(nowTime.split(":")[0]) * 60 + Integer.parseInt(nowTime.split(":")[1]);
        for (int i = 0; i < 8; i++) {
            int lastM = nowM - (i + 1) * 5;
            histList.add(getFormatTimemmss(lastM));
        }
        return histList;
    }

    /**
     * 得到一个格式化的时间
     *
     * @param time 时间 分钟
     * @return 时：分
     */
    public static String getFormatTimemmss(long time) {
        long minute = time % 60;
        long hour = time / 60;
        String strHour = ("00" + hour).substring(("00" + hour).length() - 2);
        String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
        return strHour + ":" + strMinute;
    }

    /**
     * 判断是否是今天
     *
     * @param date
     * @return
     */
    public static boolean isToday(String date) {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date2 = new Date();
        try {
            date2 = sdFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return DateUtils.isToday(date2.getTime());
    }
}