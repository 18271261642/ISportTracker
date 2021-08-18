package com.isport.isportlibrary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.isport.isportlibrary.entry.SportData337B;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by Marcos Cheng on 2016/12/21.
 *
 *  save the sport data for W337B Serial {@link com.isport.isportlibrary.controller.BaseController#CMD_TYPE_W337B}
 *  see {@link SportData337B}
 */

public class DbSportData337B extends BaseDb {

    public static String TABLE_NAME = "dbsportdata";
    /**
     * datetime
     */
    public static String COLUMN_DATE = "sportdate";
    /**
     * mac of device
     */
    public static String COLUMN_MAC = "sportmac";
    /**
     * state of sport
     */
    public static String COLUMN_SPORTSTATE = "sportstate";
    /**
     * speed
     */
    public static String COLUMN_SPEED = "speed";
    /**
     * step count
     */
    public static String COLUMN_STEP_NUM = "stepnum";
    /**
     * distance
     */
    public static String COLUMN_DIST = "distance";
    /**
     * calorics
     */
    public static String COLUMN_CALO = "calorics";
    /**
     * sport time
     */
    public static String COLUMN_SPORTTIME = "sporttime";
    /**
     * time of deep sleep
     */
    public static String COLUMN_DEEPTIME = "deeptime";
    /**
     * time of light sleep
     */
    public static String COLUMN_LIGHTTIME = "lighttime";
    /**
     * time of rest
     */
    public static String COLUMN_DAY_RESTTIME = "dayresttime";
    /**
     * heart rate
     */
    public static String COLUMN_HEARTRATE = "heartrate";
    /**
     * blood oxyen
     */
    public static String COLUMN_BLOODOXY = "bloodoxygen";


    public static String CREATE_SQL  ="CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("
            +COLUMN_DATE+" varchar(20) not null,"
            +COLUMN_MAC+" varchar(20) not null,"
            +COLUMN_SPORTSTATE+" integer,"
            +COLUMN_SPEED+" integer,"
            +COLUMN_STEP_NUM+" integer,"
            +COLUMN_DIST+" integer,"
            +COLUMN_CALO+" integer,"
            +COLUMN_SPORTTIME+" integer,"
            +COLUMN_DEEPTIME+" integer,"
            +COLUMN_LIGHTTIME+" integer,"
            +COLUMN_DAY_RESTTIME+" integer,"
            +COLUMN_HEARTRATE+" integer,"
            +COLUMN_BLOODOXY+" integer,"
            +"PRIMARY KEY("+COLUMN_DATE+","+COLUMN_MAC+")"
            +");";

    private static volatile DbSportData337B sInstance;

    public static DbSportData337B getIntance(Context context){
        if(sInstance == null){
            synchronized (DbSportData337B.class){
                if(sInstance == null){
                    sInstance = new DbSportData337B(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private DbSportData337B(Context context){
        this.mContext = context;
    }

    /**
     * save data to database , if there is no the data, it will insert or update, to see {@link android.database.sqlite.SQLiteDatabase#replace(String, String, ContentValues)}
     * @param sportData the data you want to save
     */
    public void saveOrUpdate(SportData337B sportData){
        DatabaseHelper helper = DatabaseHelper.getInstance(mContext);
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE,sportData.getDate());
        values.put(COLUMN_MAC,sportData.getMac());
        values.put(COLUMN_SPORTSTATE,sportData.getSportState());
        values.put(COLUMN_SPEED,sportData.getSpeed());
        values.put(COLUMN_BLOODOXY,sportData.getBloodOxygen());
        values.put(COLUMN_CALO,sportData.getCalorics());
        values.put(COLUMN_DAY_RESTTIME,sportData.getDayRestTime());
        values.put(COLUMN_STEP_NUM,sportData.getTotalStepNum());
        values.put(COLUMN_DEEPTIME,sportData.getDeepTime());
        values.put(COLUMN_LIGHTTIME,sportData.getLightTime());
        values.put(COLUMN_DIST,sportData.getDistance());
        values.put(COLUMN_HEARTRATE,sportData.getHeartRate());
        values.put(COLUMN_SPORTTIME,sportData.getSportTime());

        helper.replace(TABLE_NAME,null,values);
    }

    /**
     * only return the first one that meet the condition
     * @param selection
     * @param selectionArgs
     * @return return the first record that meet the condition
     */
    public SportData337B findFirst(String selection, String[] selectionArgs){
        DatabaseHelper helper = DatabaseHelper.getInstance(mContext);
        Cursor cursor = helper.query(TABLE_NAME,null,selection,selectionArgs,null);
        if(cursor != null){
            SportData337B sportData = null;
            if(cursor.moveToNext()){
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                String mac = cursor.getString(cursor.getColumnIndex(COLUMN_MAC));
                int sportstate = cursor.getInt(cursor.getColumnIndex(COLUMN_SPORTSTATE));
                int speed = cursor.getInt(cursor.getColumnIndex(COLUMN_SPEED));
                int bloodOxy = cursor.getInt(cursor.getColumnIndex(COLUMN_BLOODOXY));
                int calo = cursor.getInt(cursor.getColumnIndex(COLUMN_CALO));
                int dayresttime = cursor.getInt(cursor.getColumnIndex(COLUMN_DAY_RESTTIME));
                int stepnum = cursor.getInt(cursor.getColumnIndex(COLUMN_STEP_NUM));
                int deeptime = cursor.getInt(cursor.getColumnIndex(COLUMN_DEEPTIME));
                int lighttime = cursor.getInt(cursor.getColumnIndex(COLUMN_LIGHTTIME));
                int dist = cursor.getInt(cursor.getColumnIndex(COLUMN_DIST));
                int heartrate = cursor.getInt(cursor.getColumnIndex(COLUMN_HEARTRATE));
                int sporttime = cursor.getInt(cursor.getColumnIndex(COLUMN_SPORTTIME));
                sportData = new SportData337B(date,mac,sportstate,speed,stepnum,dist,calo,sporttime,deeptime,lighttime,dayresttime,heartrate,bloodOxy);
            }
            cursor.close();
            cursor = null;
            return sportData;
        }
        return null;
    }

    /**
     * to query from database and it will return a list that will be return
     * @param selection
     * @param selectionArgs
     * @return return all data than meet the condition
     */
    public List<SportData337B> findAll(String selection, String[] selectionArgs){
        DatabaseHelper helper = DatabaseHelper.getInstance(mContext);
        Cursor cursor = helper.query(TABLE_NAME,null,selection,selectionArgs,null);
        ArrayList<SportData337B> list = new ArrayList<>();
        if(cursor != null){
            SportData337B sportData = null;
            while (cursor.moveToNext()){
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                String mac = cursor.getString(cursor.getColumnIndex(COLUMN_MAC));
                int sportstate = cursor.getInt(cursor.getColumnIndex(COLUMN_SPORTSTATE));
                int speed = cursor.getInt(cursor.getColumnIndex(COLUMN_SPEED));
                int bloodOxy = cursor.getInt(cursor.getColumnIndex(COLUMN_BLOODOXY));
                int calo = cursor.getInt(cursor.getColumnIndex(COLUMN_CALO));
                int dayresttime = cursor.getInt(cursor.getColumnIndex(COLUMN_DAY_RESTTIME));
                int stepnum = cursor.getInt(cursor.getColumnIndex(COLUMN_STEP_NUM));
                int deeptime = cursor.getInt(cursor.getColumnIndex(COLUMN_DEEPTIME));
                int lighttime = cursor.getInt(cursor.getColumnIndex(COLUMN_LIGHTTIME));
                int dist = cursor.getInt(cursor.getColumnIndex(COLUMN_DIST));
                int heartrate = cursor.getInt(cursor.getColumnIndex(COLUMN_HEARTRATE));
                int sporttime = cursor.getInt(cursor.getColumnIndex(COLUMN_SPORTTIME));
                sportData = new SportData337B(date,mac,sportstate,speed,stepnum,dist,calo,sporttime,deeptime,lighttime,dayresttime,heartrate,bloodOxy);
                list.add(sportData);
            }
            cursor.close();
            cursor = null;
        }
        return list;
    }

}
