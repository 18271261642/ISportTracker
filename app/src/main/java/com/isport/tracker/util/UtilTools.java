package com.isport.tracker.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/10/13.
 */

public class UtilTools {


    public static long daysBetween(Date startdate,Date enddate){
        Calendar cal = Calendar.getInstance();
        cal.setTime(startdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(enddate);
        long time2 = cal.getTimeInMillis();
        long dt = (time2 - time1)/(1000*3600*24);
        return  dt;
    }

    public static int weeksBetween(Date startdate,Date enddate){
        Calendar cal = Calendar.getInstance();
        cal.setTime(startdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(enddate);
        long time2 = cal.getTimeInMillis();
        long dt = (time2 - time1)/(1000*3600*24);
        int count = (int)(dt%7>0?dt/7+1:dt/7);
        return count;
    }

    public static int weeksBetween(String startdate,String endDate){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return weeksBetween(sdf.parse(startdate),sdf.parse(endDate));
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public static void showToast(Context context,String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context,int msg){
        Toast.makeText(context, context.getString(msg), Toast.LENGTH_SHORT).show();
    }

    public static SimpleDateFormat getDateFormat(){
        SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getDateInstance(3);
        String pattern = format.toPattern();
        //pattern = pattern.replace("M","MM").replace("d","dd").replace("yy","yyyy");
        format = new SimpleDateFormat(pattern);
        return format;
    }

    public static float pixelToDp(Context context, float val) {
        float density = context.getResources().getDisplayMetrics().density;
        return val * density;
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

    public static Object[] listToObj(List list){
        if(list != null && list.size()>0) {
            Object[] objs = new Object[list.size()];
            for (int i=0;i<list.size();i++){
                objs[i] = list.get(i);
            }
            return objs;
        }
        return null;
    }

    public static int byteToInt(byte data) {
        return (data & 0x00ff);
    }

    public static Bitmap getBitmap(String path,BitmapFactory.Options options) {
        File file = new File(path);
        if(!file.exists())
            return null;
        return BitmapFactory.decodeFile(path,options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options op, int reqWidth,
                                     int reqheight) {
        int originalWidth = op.outWidth;
        int originalHeight = op.outHeight;
        int inSampleSize = 1;
        if (originalWidth > reqWidth || originalHeight > reqheight) {
            int halfWidth = originalWidth / 2;
            int halfHeight = originalHeight / 2;
            while ((halfWidth / inSampleSize > reqWidth)
                    &&(halfHeight / inSampleSize > reqheight)) {
                inSampleSize *= 2;

            }
        }
        return inSampleSize;
    }


    public static String deviceSpiltString(String deviceName){
        return deviceName == null?"":deviceName.split("_")[0];
    }

    public static String replaceWith(String obj,String src,String des){
        return obj.replace(src,des);
    }

    /**
     * 判断是否存在SDCard
     *
     * @return
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public static int roundHalfUp(double value) {
        return new BigDecimal(value).setScale(0, BigDecimal.ROUND_HALF_UP)
                .intValue();
    }

    public static String format00(int value){
        return String.format("%02d",value);
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

   /* public static Date string2Date(String date,String fmt){
        SimpleDateFormat format = new SimpleDateFormat(fmt);
        try {
            Date dt = format.parse(date);
            return dt;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public static String date2String(Date date,String fmt){
        SimpleDateFormat format = new SimpleDateFormat(fmt);
        return format.format(date);
    }

    public static int getYearBetween(Date date1,Date date2){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        int year1 = calendar.get(Calendar.YEAR);
        calendar.setTime(date2);
        int year2 = calendar.get(Calendar.YEAR);
        return year2 - year1+1;
    }

    /**
     * 获取两个日期之间的星期数
     * @param date1
     * @param date2
     * @return
     */
    public static int getWeekBetween(Date date1,Date date2){
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        int dayOfWeek1 = calendar1.get(Calendar.DAY_OF_WEEK);
        calendar1.add(Calendar.DAY_OF_MONTH,-dayOfWeek1);
        calendar1.set(Calendar.MINUTE,0);
        calendar1.set(Calendar.HOUR_OF_DAY,0);
        calendar1.set(Calendar.MILLISECOND,0);
        calendar1.set(Calendar.SECOND,0);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        int dayOfWeek2 = calendar2.get(Calendar.DAY_OF_WEEK);
        calendar2.add(Calendar.DAY_OF_MONTH,7- dayOfWeek2);
        calendar2.set(Calendar.MINUTE,0);
        calendar2.set(Calendar.HOUR_OF_DAY,0);
        calendar2.set(Calendar.MILLISECOND,0);
        calendar2.set(Calendar.SECOND,0);

        long dt1 = calendar1.getTimeInMillis();
        long dt2 = calendar2.getTimeInMillis();
        return (int) ((dt2- dt1)/(3600*24*1000*7L));
    }

    public static int getMonthBetween(Date date1,Date date2){
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        int y1 = calendar1.get(Calendar.YEAR);
        int m1 = calendar1.get(Calendar.MONTH);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        int y2 = calendar2.get(Calendar.YEAR);
        int m2 = calendar2.get(Calendar.MONTH);

        return (y1 == y2?(m2 - m1+1):(y2 - y1-1)*12+(11-m1)+m2);
    }

    public static Date long2Date(long time){
        Date date = new Date(time);
        return date;
    }

    /**
     * 对象转数组
     *
     * @param obj
     * @return
     */
    public static byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

}
