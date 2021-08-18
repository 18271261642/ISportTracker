package com.isport.isportlibrary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.isport.isportlibrary.entry.HeartRateData;
import com.isport.isportlibrary.entry.HeartRateHistory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author
 * @Date 2018/11/14
 * @Fuction
 */

public class DbHeartRateHistory extends BaseDb {

    public static String COLUMN_DATE = "column_date";///日期
    public static String COLUMN_MAC = "column_mac";///mac 地址
    public static String COLUMN_LIST = "column_list";///数据列表
    public static String COLUMN_AVG = "column_avg";////平均值
    public static String COLUMN_MAX = "column_max";///最大值
    public static String COLUMN_MIN = "column_min";////最小值
    public static String COLUMN_TOTAL = "column_total";///心率总数
    public static String TABLE_NAME = "dbHeartRate";

    public static String TABLE_SQL = "create table "+TABLE_NAME+"("
            +COLUMN_DATE+" varchar(20) not null,"
            +COLUMN_MAC+" varchar(20) not null,"
            +COLUMN_AVG+" integer,"
            +COLUMN_MAX+" integer,"
            +COLUMN_MIN+" integer,"
            +COLUMN_LIST+" blod,"
            +COLUMN_TOTAL+" integer,"
            +"PRIMARY KEY("+COLUMN_DATE+","+COLUMN_MAC+")"
            +");";

    private static volatile DbHeartRateHistory sInstance;

    public static DbHeartRateHistory getIntance(Context context){
        if(sInstance == null){
            synchronized (DbHeartRateHistory.class){
                if(sInstance == null){
                    sInstance = new DbHeartRateHistory(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private DbHeartRateHistory(Context context) {
        mContext = context;
    }

    /**
     * save data to database, if there is a record, will update it, or insert it
     * @param heartRateHistory the data you want to update or insert
     */
    public void saveOrUpdate(HeartRateHistory heartRateHistory) {
        ArrayList<HeartRateData> list = new ArrayList<>();
        list.addAll(heartRateHistory.getHeartDataList());
        DatabaseHelper db =  DatabaseHelper.getInstance(mContext);
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE,heartRateHistory.getStartDate());
        values.put(COLUMN_MAC,heartRateHistory.getMac());
        values.put(COLUMN_AVG,heartRateHistory.getAvg());
        values.put(COLUMN_MAX,heartRateHistory.getMax());
        values.put(COLUMN_MIN,heartRateHistory.getMin());
        values.put(COLUMN_LIST,toByteArray(list));
        values.put(COLUMN_TOTAL,heartRateHistory.getCount());
        db.replace(TABLE_NAME,null,values);
    }

    public List<HeartRateHistory> getListHistory(String selection, String[] selectionArgs, String groudBy, String having, String orderby){
        DatabaseHelper helper =  DatabaseHelper.getInstance(mContext);
        Cursor cursor = helper.query(TABLE_NAME, null, selection, selectionArgs, groudBy, having, orderby);
        List<HeartRateHistory> historyList = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int avg = cursor.getInt(cursor.getColumnIndex(COLUMN_AVG));
                    int min = cursor.getInt(cursor.getColumnIndex(COLUMN_MIN));
                    int max = cursor.getInt(cursor.getColumnIndex(COLUMN_MAX));
                    String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    String mac = cursor.getString(cursor.getColumnIndex(COLUMN_MAC));
                    byte[] tb = cursor.getBlob(cursor.getColumnIndex(COLUMN_LIST));
                    int total = cursor.getInt(cursor.getColumnIndex(COLUMN_TOTAL));
                    Object to = getObjectFromBytes(tb);
                    ArrayList<HeartRateData> list = null;
                    if (to == null) {
                        list = new ArrayList<>();
                    } else {
                        list = (ArrayList<HeartRateData>) to;
                    }
                    historyList.add(new HeartRateHistory(mac,total, date,  avg, max, min, list));
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return historyList;
    }

    public List<HeartRateHistory> findAll(String selection, String[] selectionArgs, String orderby) {
        Cursor cursor = null;
        List<HeartRateHistory> historyList = new ArrayList<>();
        try {
            cursor = DatabaseHelper.getInstance(this.mContext).query(TABLE_NAME, (String[])null, selection, selectionArgs, orderby);
            if(cursor != null) {
                int var6 = cursor.getCount();
                while(cursor.moveToNext()) {
                    int avg = cursor.getInt(cursor.getColumnIndex(COLUMN_AVG));
                    int min = cursor.getInt(cursor.getColumnIndex(COLUMN_MIN));
                    int max = cursor.getInt(cursor.getColumnIndex(COLUMN_MAX));
                    String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    String mac = cursor.getString(cursor.getColumnIndex(COLUMN_MAC));
                    byte[] tb = cursor.getBlob(cursor.getColumnIndex(COLUMN_LIST));
                    int total = cursor.getInt(cursor.getColumnIndex(COLUMN_TOTAL));
                    Object to = getObjectFromBytes(tb);
                    ArrayList<HeartRateData> list = null;
                    if (to == null) {
                        list = new ArrayList<>();
                    } else {
                        list = (ArrayList<HeartRateData>) to;
                    }
                    historyList.add(new HeartRateHistory(mac,total, date,  avg, max, min, list));
                }
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        } finally {
            if(cursor != null) {
                cursor.close();
            }

        }
        return historyList;
    }

    /**
     * @param obj
     * @return
     */
    public static byte[] toByteArray(Serializable obj) {
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

    /**
     * 从字节数组获取对象
     * @EditTime 2007-8-13 上午11:46:34
     */
    public static Object getObjectFromBytes(byte[] objBytes) {
        if (objBytes == null || objBytes.length == 0) {
            return null;
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(objBytes);
        ObjectInputStream oi = null;
        try {
            oi = new ObjectInputStream(bi);
            return oi.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return oi;
    }

}
