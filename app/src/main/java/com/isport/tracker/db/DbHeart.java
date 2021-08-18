package com.isport.tracker.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.isport.tracker.entity.HeartHistory;
import com.isport.isportlibrary.entry.HeartData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/1.
 */

public class DbHeart {
    public static String COLUMN_TYPE = "column_type";///保存类型
    public static String COLUMN_MAC = "column_mac";///mac 地址
    public static String COLUMN_DATE = "column_date";///日期
    public static String COLUMN_LIST = "column_list";///数据列表
    public static String COLUMN_AVG = "column_avg";////平均值
    public static String COLUMN_MAX = "column_max";///最大值
    public static String COLUMN_MIN = "column_min";////最小值
    public static String COLUMN_TOTAL = "column_total";///心率总数
    public static String COLUMN_SIZE = "column_size";
    public static String COLUMN_CAL = "column_cal";////热量消耗，仅适用心率带
    public static String COLUMN_ISHISTORY = "column_ishistory";//是否是历史同步的数据
    public static String TABLE_NAME = "dbHeart";

    public static String TABLE_SQL = "create table "+TABLE_NAME+"("
            +COLUMN_TYPE+" integer,"
            +COLUMN_DATE+" varchar(20) not null,"
            +COLUMN_MAC+" varchar(20) not null,"
            +COLUMN_AVG+" integer,"
            +COLUMN_MAX+" integer,"
            +COLUMN_MIN+" integer,"
            +COLUMN_LIST+" blod,"
            +COLUMN_TOTAL+" long,"
            +COLUMN_SIZE+" integer,"
            +COLUMN_CAL+" long,"
            +COLUMN_ISHISTORY+" integer,"
            +"PRIMARY KEY("+COLUMN_DATE+","+COLUMN_MAC+","+COLUMN_TYPE+")"
            +");";

    private static volatile DbHeart sInstance;

    public static DbHeart getIntance(){
        if(sInstance == null){
            synchronized (DbHeart.class){
                if(sInstance == null){
                    sInstance = new DbHeart();
                }
            }
        }
        return sInstance;
    }

    private DbHeart(){

    }

    public void saveOrUpdate(List<HeartHistory> list) {
        if (list != null) {
            DataHeartHelper db = DataHeartHelper.getInstance();
            db.beginTransaction();
            try {
                for (int i = 0; i < list.size(); i++) {
                    HeartHistory heartRecord = list.get(i);
                    update(heartRecord);
                }
                db.setTransactionSuccessful();
            }finally {
                db.endTransaction();
            }

        }
    }

    public void update(HeartHistory history){
        ArrayList<HeartData> list = new ArrayList<>();
        list.addAll(history.getHeartDataList());
        DataHeartHelper db = DataHeartHelper.getInstance();

        ContentValues values = new ContentValues();
        values.put(COLUMN_AVG,history.getAvg());
        values.put(COLUMN_DATE,history.getStartDate());
        values.put(COLUMN_LIST,toByteArray(list));
        values.put(COLUMN_MAC,history.getMac());
        values.put(COLUMN_MAX,history.getMax());
        values.put(COLUMN_MIN,history.getMin());
        values.put(COLUMN_TYPE,history.getType());
        values.put(COLUMN_TOTAL,history.getTotal());
        values.put(COLUMN_SIZE,history.getSize());
        values.put(COLUMN_CAL,history.getTotalCal());
        values.put(COLUMN_ISHISTORY,history.getIsHistory());
        db.replace(TABLE_NAME,null,values);
    }

    public List<HeartHistory> getListHistory(String selection, String[] selectionArgs, String groudBy, String having, String orderby){
        DataHeartHelper helper = DataHeartHelper.getInstance();
        Cursor cursor = helper.query(TABLE_NAME,null,selection,selectionArgs,groudBy,having,orderby);
        List<HeartHistory> historyList = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int avg = cursor.getInt(cursor.getColumnIndex(COLUMN_AVG));
                    int min = cursor.getInt(cursor.getColumnIndex(COLUMN_MIN));
                    int max = cursor.getInt(cursor.getColumnIndex(COLUMN_MAX));
                    int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                    String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    String mac = cursor.getString(cursor.getColumnIndex(COLUMN_MAC));
                    byte[] tb = cursor.getBlob(cursor.getColumnIndex(COLUMN_LIST));
                    long total = cursor.getLong(cursor.getColumnIndex(COLUMN_TOTAL));
                    long cal = cursor.getLong(cursor.getColumnIndex(COLUMN_CAL));
                    int isHistory = cursor.getInt(cursor.getColumnIndex(COLUMN_ISHISTORY));
                    Object to = getObjectFromBytes(tb);
                    ArrayList<HeartData> list = null;
                    if (to == null) {
                        list = new ArrayList<>();
                    } else {
                        list = (ArrayList<HeartData>) to;
                    }
                    historyList.add(new HeartHistory(type, mac, date, list, avg, max, min, total, cal,isHistory));
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

    public void delete(List<HeartHistory> list){
        if(list == null || list.size() == 0){
            return;
        }
        DataHeartHelper helper = DataHeartHelper.getInstance();
        helper.beginTransaction();
        for (int i=0;i<list.size();i++){
            HeartHistory history = list.get(i);
            helper.delete(TABLE_NAME,COLUMN_MAC+"=? and "+COLUMN_DATE+"=? and "+COLUMN_TYPE+"=?",new String[]{history.getMac(),history.getStartDate(),history.getType()+""});
        }
        helper.setTransactionSuccessful();
        helper.endTransaction();
    }

    /**
     * 统计
     * @param where
     * @param whereArgs
     * @return
     */
    public long[] query(String where,String[] whereArgs){

        DataHeartHelper helper = DataHeartHelper.getInstance();
        String[] select = new String[]{"sum("+COLUMN_SIZE+") as ssize","sum("+COLUMN_TOTAL+") as stotal","max("+COLUMN_MAX+") as mmax","min("+COLUMN_MIN+") as mmin"};
        Cursor cursor = helper.query(TABLE_NAME,select,where,whereArgs,null,null,null);
        long[] values = new long[4];
        if(cursor != null){
            if(cursor.moveToNext()){
                int size = cursor.getInt(cursor.getColumnIndex("ssize"));
                long total = cursor.getLong(cursor.getColumnIndex("stotal"));
                int max = cursor.getInt(cursor.getColumnIndex("mmax"));
                int min = cursor.getInt(cursor.getColumnIndex("mmin"));
                values = new long[]{size,total,max,min};
                cursor.close();
                return values;
            }
            cursor.close();
        }

        return null;
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
