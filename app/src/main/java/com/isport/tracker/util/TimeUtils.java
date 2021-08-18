package com.isport.tracker.util;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import com.isport.isportlibrary.tools.DateUtil;
import com.isport.tracker.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Administrator on 2016/10/21 0021.
 */

public class TimeUtils {
    private static final String TAG = TimeUtils.class.getSimpleName();
    static SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static long toSeconds(long date) {
        return date / 1000L;
    }

    private static long toMinutes(long date) {
        return toSeconds(date) / 60L;
    }

    private static long toHours(long date) {
        return toMinutes(date) / 60L;
    }

    private static long toDays(long date) {
        return toHours(date) / 24L;
    }

    private static long toMonths(long date) {
        return toDays(date) / 30L;
    }

    private static long toYears(long date) {
        return toMonths(date) / 365L;
    }

    public static boolean is24Hour(Context context) {
        return DateFormat.is24HourFormat(context);
    }

    /**
     * hh:mm
     *
     * @param data
     * @return
     */
    public static String getMinTime(List<String> data) {
        int temp = 2400;
        int index = 0;
//        null, 07:55, null, null, null, null, null
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) != null) {
                int i1 = Integer.parseInt(data.get(i).replace(":", ""));
                if (i1 <= temp) {
                    temp = i1;
                    index = i;
                }
            }
        }
        return data.get(index);
    }
    /**
     * hh:mm
     *
     * @param data
     * @return
     */
    public static String getAVgTime(List<String> data) {
        int totalNum = 0;//有几天有数据
        int totalTime = 0;//分钟
//        null, 07:55, null, null, null, null, null
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) != null) {
                totalNum++;
                int hour = Integer.parseInt(data.get(i).split(":")[0]);
                int min = Integer.parseInt(data.get(i).split(":")[1]);
                totalTime+= hour*60+min;
            }
        }
            int totlamin = totalTime / totalNum;
            int hour = totlamin / 60;
            int min = totlamin % 60;
        if ((min+"").length()==1){
            return hour+":0"+min;
        }
        return hour+":"+min;
    }

    public static int getSleepDays(List<String> data) {
        int totalNum = 0;//有几天有数据
//        null, 07:55, null, null, null, null, null
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) != null) {
                totalNum++;
            }
        }
        return totalNum;
    }

    /**
     * hh:mm
     *
     * @param data
     * @return
     */
    public static String getMaxTime(List<String> data) {
        String result = "00:00";
        int temp = 0;
        int index = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) != null) {
                int i1 = Integer.parseInt(data.get(i).replace(":", ""));
                if (i1 >= temp) {
                    temp = i1;
                    index = i;
                }
            }
        }
        return data.get(index);
    }

    /**
     * 得到一个格式化的时间
     *
     * @param time 时间 毫秒
     * @return 时：分：秒：毫秒
     */
    public static String getFormatTime(long time) {
        time = time / 1000;
        long second = time % 60;
        long minute = (time % 3600) / 60;
        long hour = time / 3600;
        // 毫秒秒显示两位
        // String strMillisecond = "" + (millisecond / 10);
        // 秒显示两位
        String strSecond = ("00" + second).substring(("00" + second).length() - 2);
        // 分显示两位
        String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
        // 时显示两位
        String strHour = ("00" + hour).substring(("00" + hour).length() - 2);
        return strHour + ":" + strMinute + ":" + strSecond;
    }

    /**
     * @param time 秒
     * @return
     */
    public static String getFormatTimeHHMMSS(long time) {
        long second = time % 60;
        long minute = (time % 3600) / 60;
        long hour = time / 3600;
        // 秒显示两位
        String strSecond = ("00" + second).substring(("00" + second).length() - 2);
        // 分显示两位
        String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
        // 时显示两位
        String strHour = ("00" + hour).substring(("00" + hour).length() - 2);
        return strHour + ":" + strMinute + ":" + strSecond;
    }

    /**
     * 得到一个格式化的时间
     *
     * @param time 时间 秒
     * @return 分：秒：秒
     */
    public static String getFormatTimemmss(long time) {
        long second = time % 60;
        long minute = (time % 3600) / 60;
        // 毫秒秒显示两位
        // String strMillisecond = "" + (millisecond / 10);
        // 秒显示两位
        String strSecond = ("00" + second).substring(("00" + second).length() - 2);
        // 分显示两位
        String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
        return strMinute + ":" + strSecond;
    }

    /**
     * 得到一个格式化的时间
     *
     * @param time 时间 秒
     * @return 分：秒：秒
     */
    public static String getFormatTimemm(long time) {
        long minute = (time % 3600) / 60;
        // 毫秒秒显示两位
        // 分显示两位
        String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
        return strMinute;
    }

    /**
     * 得到一个格式化的时间
     * 配速的显示格式
     *
     * @param time 时间 秒
     * @return 时：分：秒：秒
     */
    public static String getFormatTimems(long time) {
        long second = time % 60;
        long minute = time - second * 60;
        // 秒显示两位
        String strSecond = ("00" + second).substring(("00" + second).length() - 2);
        // 分显示两位
        String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
        return strMinute + "'" + strSecond + "''";
    }

    public static long getDaysBetween(Date date1,Date date2){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.HOUR,0);
        calendar.set(Calendar.MILLISECOND,0);
        long time1 = calendar.getTimeInMillis();
        calendar.setTime(date2);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.HOUR,0);
        calendar.set(Calendar.MILLISECOND,0);
        long time2 = calendar.getTimeInMillis();
        long dv = time2 - time1;
        return dv/(3600*1000*24L)+1;
    }

    public static Date string2Date(String strDate,String fmt){
        SimpleDateFormat format = new SimpleDateFormat(fmt);
        try {
            return format.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static String getFormatTimemsTemp(long time) {
        long second = time % 60;
        long minute = time - second * 60;
        // 秒显示两位
//        String strSecond = ("00" + second).substring(("00" + second).length() - 2);
        String strSecond = String.format("%02d", time%60);
        // 分显示两位
//        String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
        String strMinute = String.format("%02d", (time%3600)/60);
        return strMinute + "'" + strSecond + "''";
    }

    public static String getFormatTimemsF(long time) {
        long second = time % 60;
        long minute = time - second * 60;
        // 秒显示两位
//        String strSecond = ("00" + second).substring(("00" + second).length() - 2);
        String strSecond = String.format("%02d", time%60);
        // 分显示两位
//        String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
        String strMinute = String.format("%02d", (time%3600)/60);
        return strMinute + ":" + strSecond;
    }

    public static String getTodayMMDDHHMM() {
        SimpleDateFormat sdFormat = new SimpleDateFormat("MM-dd HH:mm");
        Date date2 = new Date(System.currentTimeMillis());
        return sdFormat.format(date2);
    }

    public static String getTodayHHMM() {
        SimpleDateFormat sdFormat = new SimpleDateFormat("HH:mm");
        Date date2 = new Date(System.currentTimeMillis());
        return sdFormat.format(date2);
    }

    /**
     * 获取昨天的日期格式为yyyy-MM-ddstring
     *
     * @return
     */
    public static String getYestodayYYYYMMDD() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    /**
     * 获取今天的yyyy-MM-dd
     *
     * @return
     */
    public static String getTodayYYYYMMDD() {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date2 = new Date(System.currentTimeMillis());
        return sdFormat.format(date2);
    }

    /**
     * @param time 毫秒
     * @return
     */
    public static String getTimeByMMDDHHMM(long time) {
        SimpleDateFormat sdFormat = new SimpleDateFormat("MM-dd HH:mm");
        Date date2 = new Date(time);
        return sdFormat.format(date2);
    }

    public static String getTimeByYYYYMMDD(long time) {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date2 = new Date(time);
        return sdFormat.format(date2);
    }

    public static String getTodayddMMyyyy() {
        Calendar calendar = Calendar.getInstance();
        String s = DateUtil.dataToString(calendar.getTime(), "dd/MM/yyyy");
        return s;
    }

    public static String getTodayddMMyyyy(Date date) {
        String s = DateUtil.dataToString(date, "dd/MM/yyyy");
        return s;
    }

    /**
     * @param time 毫秒
     * @return
     */
    public static String getTimeByHHmm(long time) {
        SimpleDateFormat sdFormat = new SimpleDateFormat(" HH:mm");
        Date date2 = new Date(time);
        return sdFormat.format(date2);
    }

    /**
     * 获取当前的时间（UNIX时间戳形式）
     *
     * @return long
     */
    public static long getCurrentTimeUnixLong() {
        return System.currentTimeMillis() / 1000;
    }

    public static String getCurrentTimeUnixString() {
        return System.currentTimeMillis() / 1000 + "";
    }

    /**
     * 将 yyyy-MM-dd HH:mm:ss 时间转化成 long 时间
     */
    public static long changeStrDateToLongDate(String strDate) {
        Date date = null;
        try {
            date = f.parse(strDate);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public static long changeStrDateToLong(String strDate) {
        Date date = null;
        try {
            date = f1.parse(strDate);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * 根据日期获得当前日期是周几
     *
     * @param SVNyyyy-MM-dd HH:mm:ss
     * @return 1, 2, 3, 4, 5, 6, 7 周一，周二，周三，周四，周五，周六，周日
     */
    public static int getWeekByDateReturnInt(String date) {
        int year = Integer.valueOf(date.substring(0, 4));
        int month = Integer.valueOf(date.substring(5, 7));
        int day = Integer.valueOf(date.substring(8, 10));
        return getWeek(year, month, day);
    }

    /**
     * 获取周几
     *
     * @param year
     * @param month
     * @param day
     * @return int
     */
    public static int getWeek(int year, int month, int day) {
        if (month == 1 || month == 2) {
            month += 12;
            year--;
        }
        int w = (day + 2 * month + 3 * (month + 1) / 5 + year + year / 4 - year / 100 + year / 400) % 7;
        return w + 1;
    }

    /**
     * 获取年
     *
     * @param date 格式2016-03-28
     * @return
     */
    public static String getYear(String date) {
        return date.split("-")[0];
    }

    /**
     * 获取月
     *
     * @param date 格式2016-03-28
     * @return
     */
    public static String getMonth(String date) {
        String result;
        if (date.split("-")[1].startsWith("0")) {
            result = date.split("-")[1].split("0")[1];
        } else {
            result = date.split("-")[1];
        }
        return result;
    }

    /**
     * 获取日
     *
     * @param date 格式2016-03-28
     * @return
     */
    public static String getDay(String date) {
        String result;
        if (date.split("-")[2].startsWith("0")) {
            result = date.split("-")[2].split("0")[1];
        } else {
            result = date.split("-")[2];
        }
        return result;
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

    /**
     * 获取N天后的日期
     *
     * @param time
     * @param n
     * @return
     */
    public static String getNDayAfter(String time, int n, String myfm) {
        SimpleDateFormat formatDate = new SimpleDateFormat(myfm); // 字符串转换
        Date date = null;
        try {
            date = formatDate.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return "";
        } else {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(date.getTime());
            c.add(Calendar.DATE, n);// 天后的日期
            return formatDate.format(c.getTime());
        }
    }

    /**
     * 获取N天前
     *
     * @param time
     * @param n
     * @return
     */
    public static String getNDayBefore(String time, int n, String myfm) {
        SimpleDateFormat formatDate = new SimpleDateFormat(myfm); // 字符串转换
        Date date = null;
        try {
            date = formatDate.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return "";
        } else {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(date.getTime());
            c.add(Calendar.DATE, -n);// 天前的日期
            return formatDate.format(c.getTime());
        }
    }

    /**
     * 返回8/29格式
     *
     * @param date
     * @return
     */
    public static String getDateString(String date) {
        return getMonth(date) + "/" + getDay(date);
    }


    /**
     * unix时间转换为北京时间
     *
     * @param secondTime
     * @return "yyyy-MM-dd"
     */
    public static String unixTimeToBeijingDate(long secondTime) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        return f.format(new Date(secondTime * 1000L));
    }

    /**
     * unix时间戳 转化成 北京时间
     *
     * @param secondTime long
     * @return String "yyyy-MM-dd HH:mm:ss"
     */
    public static String unixTimeToBeijingTime(long secondTime) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return f.format(new Date(secondTime * 1000L));
    }

    /**
     * 时间戳转换为指定格式的北京时区字符串
     *
     * @param secondTime
     * @param f
     * @return
     */
    public static String unixTimeToBeijingTime(long secondTime, SimpleDateFormat f) {
        f.setTimeZone(TimeZone.getTimeZone("GTM"));
        return f.format(new Date(secondTime * 1000l));
    }

    /**
     * 将Unix时间转化为yyyyMMdd，以便于和MyDetailTable中的sign2作比较
     *
     * @param secondTime 秒
     * @return
     */
    public static String getStrDateFormLong(long secondTime) {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
        return f.format(new Date(secondTime * 1000L));
    }

    /**
     * 获取两个日期之间的间隔天数
     *
     * @return
     */
    public static int getGapCount(String start, String end, String myFm) {
        SimpleDateFormat sf = new SimpleDateFormat(myFm);
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = sf.parse(start);
            endDate = sf.parse(end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (startDate == null || endDate == null) {
            return 0;
        } else {
            Calendar fromCalendar = Calendar.getInstance();
            fromCalendar.setTime(startDate);
            fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            fromCalendar.set(Calendar.MILLISECOND, 0);

            Calendar toCalendar = Calendar.getInstance();
            toCalendar.setTime(endDate);
            toCalendar.set(Calendar.HOUR_OF_DAY, 0);
            toCalendar.set(Calendar.MINUTE, 0);
            toCalendar.set(Calendar.SECOND, 0);
            toCalendar.set(Calendar.MILLISECOND, 0);
            return (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
        }
    }

    /**
     * 获取传入的指定日期当周的  "8/10-8/17"  字符串
     *
     * @param dateStr
     * @return
     */
    public static String getWhichWeekByFormatdate(String dateStr) {
        SimpleDateFormat myFm = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = myFm.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        String monday = "";
        String sunday = "";
        if (dayOfWeek == 1) { // 周日
            monday = unixTimeToBeijingTime(c.getTimeInMillis() / 1000 - 5 * 24 * 3600, new SimpleDateFormat("yyyy-MM-dd"));
            sunday = unixTimeToBeijingTime(c.getTimeInMillis() / 1000 + 24 * 3600, new SimpleDateFormat("yyyy-MM-dd"));
        } else {
            monday = unixTimeToBeijingTime(c.getTimeInMillis() / 1000 + (3 - dayOfWeek) * 24 * 3600, new SimpleDateFormat("yyyy-MM-dd"));
            sunday = unixTimeToBeijingTime(c.getTimeInMillis() / 1000 + (9 - dayOfWeek) * 24 * 3600, new SimpleDateFormat("yyyy-MM-dd"));
        }
        return getMonth(monday) + "/" + getDay(monday) + "-" + getMonth(sunday) + "/" + getDay(sunday);
    }

    /**
     * 一周的周几
     *
     * @param date
     * @param myFm
     * @return
     * @throws ParseException
     */
    public static int getDayOfWeekByDate(String date, String myFm) throws ParseException {
        SimpleDateFormat fm = new SimpleDateFormat(myFm);
        Date mdate = fm.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mdate);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 比较两个时间是否是同一周 在一周
     *
     * @param firDay
     * @param secDay
     * @param format
     * @return
     */
    //
    public static boolean isInSameWeek(String firDay, String secDay, String format) {//
        if (getWhichWeek(firDay, format, null).equalsIgnoreCase(getWhichWeek(secDay, format, null))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 返回两个时间是否是同一月
     *
     * @param first
     * @param second
     * @return
     */
    public static boolean isSameMonth(String first, String second) {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date one = sdFormat.parse(first);
            Date two = sdFormat.parse(second);
            Calendar cd = Calendar.getInstance();
            cd.setTime(one);
            int firstYear = cd.get(Calendar.YEAR);
            int firstMonth = cd.get(Calendar.MONTH);
            cd.setTime(two);
            int secondYear = cd.get(Calendar.YEAR);
            int secondMonth = cd.get(Calendar.MONTH);
            return firstYear == secondYear && firstMonth == secondMonth;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * 传入指定格式时间字符串返回所在周的周一到周日
     *
     * @param dateStr
     * @param mFormat
     * @param tarFormat
     * @return
     */
    public static String getWhichWeek(String dateStr, String mFormat, String tarFormat) {
        SimpleDateFormat myFm = new SimpleDateFormat(mFormat);
        if (tarFormat == null) {
            tarFormat = mFormat;
        }
        Date date = null;
        try {
            date = myFm.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        String monday = "";
        String sunday = "";
        if (dayOfWeek == 1) { // 周日
            monday = unixTimeToBeijingTime(c.getTimeInMillis() / 1000 - 5 * 24 * 3600, new SimpleDateFormat(tarFormat));
            sunday = unixTimeToBeijingTime(c.getTimeInMillis() / 1000 + 24 * 3600, new SimpleDateFormat(tarFormat));
        } else {
            monday = unixTimeToBeijingTime(c.getTimeInMillis() / 1000 + (3 - dayOfWeek) * 24 * 3600, new SimpleDateFormat(tarFormat));
            sunday = unixTimeToBeijingTime(c.getTimeInMillis() / 1000 + (9 - dayOfWeek) * 24 * 3600, new SimpleDateFormat(tarFormat));
        }
        return monday + "-" + sunday;
    }

    /**
     * 传入指定格式时间字符串返回所在周的周一
     */
    public static String getWhichWeekMonday(String dateStr, String mFormat, String tarFormat) {
        SimpleDateFormat myFm = new SimpleDateFormat(mFormat);
        if (tarFormat == null) {
            tarFormat = mFormat;
        }
        Date date = null;
        try {
            date = myFm.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        String monday = "";
        String sunday = "";
        if (dayOfWeek == 1) { // 周日
            monday = unixTimeToBeijingTime(c.getTimeInMillis() / 1000 - 5 * 24 * 3600, new SimpleDateFormat(tarFormat));
        } else {
            monday = unixTimeToBeijingTime(c.getTimeInMillis() / 1000 + (3 - dayOfWeek) * 24 * 3600, new SimpleDateFormat(tarFormat));
        }
        return monday;
    }

    /**
     * 传入时间是多久前
     *
     * @param strDate
     * @return
     */
    public static String getTimeBefore(String strDate) {
        long m = getCurrentTimeUnixLong() - changeStrDateToLongDate(strDate);
        if (m < 60) {
            return "1分钟前";
        } else if (60 <= m && m < 3600) {
            return (int) (m / 60) + "分钟前";
        } else if (3600 <= m && m < 86400) {
            return (int) (m / 3600) + "小时前";
        } else if (86400 <= m) {
            return (int) (m / 86400) + "天前";
        }
        return null;
    }

    /**
     * hh：mm：ss
     *
     * @param time
     * @return long型 秒值
     */
    public static long getSeconds(String time) {
        if (time != null) {
            if (time.contains(":")) {
                String[] my = time.split(":");
                int hour = Integer.parseInt(my[0]);
                int min = Integer.parseInt(my[1]);
                int sec = Integer.parseInt(my[2]);
                long totalSec = (hour * 3600 + min * 60 + sec);
                return totalSec;
            } else {
                return Long.parseLong(time);
            }
        } else {
            return 0;
        }
    }

    /**
     * 判断某个日期是否是上周的
     *
     * @param lastTime
     * @return
     */
    public static boolean isLastWeek(long lastTime) {

        Calendar cal = Calendar.getInstance();
        int day_of_week = cal.get(Calendar.DAY_OF_WEEK) - 2;
        cal.add(Calendar.DATE, -day_of_week);

        // 如果是本周周一保存的直接返回false
        if (areSameDay(lastTime, cal.getTimeInMillis())) {
            return false;
        }
        if (cal.getTimeInMillis() > lastTime) {
            // 上周
            return true;
        } else {
            // 本周
            return false;
        }
    }

    /**
     * 判断两天是否是同一天
     *
     * @param time1
     * @param time2
     * @return
     */
    public static boolean areSameDay(long time1, long time2) {
        Calendar calDateA = Calendar.getInstance();
        calDateA.setTimeInMillis(time1);

        Calendar calDateB = Calendar.getInstance();
        calDateB.setTimeInMillis(time2);

        return calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR) && calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH)
                && calDateA.get(Calendar.DAY_OF_MONTH) == calDateB.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 本年第几周
     */
    public static int getWeekInYear(int year, String month, String day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Date date = null;
        try {
            date = sdf.parse(year + "-" + month + "-" + day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.WEEK_OF_YEAR);
        } else {
            return 0;
        }
    }

    /**
     * 获取当前日期为当年第几周
     *
     * @param date 格式2016-03-28
     * @return 返回周数
     */
    public static int getWeekOfYear(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateaa = null;
        try {
            dateaa = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(dateaa);
        int weekOfMonth = calendar.get(Calendar.WEEK_OF_YEAR);
        return weekOfMonth;
    }

    /**
     * 获取当前日期为当年第几月
     *
     * @param date 格式2016-03-28
     * @return 返回周数
     */
    public static int getMonthOfYear(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateaa = null;
        try {
            dateaa = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateaa);
        int monthofYear = calendar.get(Calendar.MONTH) + 1;
        return monthofYear;
    }

    /**
     * 获取当前日期为当年第几周
     *
     * @param date 格式2016-03-28
     * @return 返回格式 2016-8
     */
    public static String getWeekOfYearFormat(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateaa = null;
        try {
            dateaa = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(dateaa);
        int weekOfMonth = calendar.get(Calendar.WEEK_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        return year + "-" + weekOfMonth;
    }

    /**
     * 获取本周是第几周 2016-12
     */
    public static String getCurrentWeekOfYear() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        try {
            calendar.setTime(format.parse(format.format(new Date())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int weekOfMonth = calendar.get(Calendar.WEEK_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        return year + "-" + weekOfMonth;
    }

    //                    2017-09-08
    public static String getShareTimeStr(String dateStr){
        String[] stringArray = UIUtils.getContext().getResources().getStringArray(R.array.month_small);
        String result=dateStr;
        String[] split = dateStr.split("-");
        String month;
        if (split[1].startsWith("0")){
            month=split[1].replace("0","");
        }else {
            month=split[1];
        }
        String monthStr=stringArray[Integer.parseInt(month)-1];
        if (split[2].startsWith("0")){
            result=monthStr+" "+split[2].replace("0","")+", "+split[0];
        }else {
            result=monthStr+" "+split[2]+", "+split[0];
        }
        return result;
    }


    //TODO
    public static String getTimeFromSec(String second) {
        if (second == null || "".equals(second)) {
            return "00:00:00";
        }
        int time = Integer.parseInt(second);
        int hour = time / 3600;
        int min = (time - 3600 * hour) / 60;
        int sec = time % 60;
        return String.format("%02d:%02d:%02d", hour, min, sec);
    }

    /**
     * 由于时间取点为05 10，故start取点应该向前推5分钟
     * @param start
     * @return
     */
    public static StringBuilder  getStartStr(String start) {
        int temp = (Integer.parseInt(start.split(":")[0]) * 60 +
                Integer.parseInt(start.split(":")[1]) - 5) / 60;
        int temp1 = (Integer.parseInt(start.split(":")[0]) * 60 +
                Integer.parseInt(start.split(":")[1]) - 5) % 60;
        StringBuilder text = new StringBuilder();
        if (temp < 10) {
            text.append("0" + temp + ":");
        } else {
            text.append(temp + ":");
        }
        if (temp1 < 10) {
            text.append("0" + temp1);
        } else {
            text.append(temp1);
        }
        return text;
    }

    /**
     * 由于时间取点为05 10，故start取点应该向前推5分钟
     * @param start
     * @return
     */
    public static StringBuilder  getEndStr(String start) {
        int temp = (Integer.parseInt(start.split(":")[0]) * 60 +
                Integer.parseInt(start.split(":")[1]) +5) / 60;
        int temp1 = (Integer.parseInt(start.split(":")[0]) * 60 +
                Integer.parseInt(start.split(":")[1]) + 5) % 60;
        StringBuilder text = new StringBuilder();
        if (temp < 10) {
            text.append("0" + temp + ":");
        } else {
            text.append(temp + ":");
        }
        if (temp1 < 10) {
            text.append("0" + temp1);
        } else {
            text.append(temp1);
        }
        return text;
    }
}
