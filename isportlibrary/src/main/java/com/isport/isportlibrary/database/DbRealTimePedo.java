package com.isport.isportlibrary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.isport.isportlibrary.entry.PedoRealData;

/**
 *
 * @author Created by Marcos Cheng on 2016/12/21.
 * save real time sport data for w311 serial {@link com.isport.isportlibrary.controller.BaseController#CMD_TYPE_W311}
 */

public class DbRealTimePedo extends BaseDb{

    /**
     * name of device
     */
    public static String COLUMN_DATE = "datestring";

    /**
     * mac of device
     */
    public static String COLUMN_MAC = "mac";

    /**
     * step count
     */
    public static String COLUMN_PEDO = "pedo";

    /**
     * calories
     */
    public static String COLUMN_CALO = "calo";

    /**
     * distance
     */
    public static String COLUMN_DIST = "dist";
    public static String TABLE_NAME = "realdata";

    public static String TABLE_CREATE_SQL = "Create table IF NOT EXISTS '" + TABLE_NAME + "'(" +
            "'" + COLUMN_DATE + "' varchar(50) not null," +
            "'" + COLUMN_MAC + "' varchar(30) not null," +
            "'" + COLUMN_PEDO + "' INTEGER,"+
            "'" + COLUMN_CALO + "' INTEGER,"+
            "'" + COLUMN_DIST + "' FLOAT,"+
            "PRIMARY KEY ('" + COLUMN_DATE + "','" + COLUMN_MAC + "'));";

    private static DbRealTimePedo sInstance;

    private DbRealTimePedo(Context context) {
        mContext = context;
    }

    public static DbRealTimePedo getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DbRealTimePedo.class) {
                if (sInstance == null) {
                    sInstance = new DbRealTimePedo(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * save data to database, if there is a record, will update it, {@link android.database.sqlite.SQLiteDatabase#replace(String, String, ContentValues)} or insert it
     * @param pedoRealData the data you want to update or insert
     */
    public void saveOrUpdate(PedoRealData pedoRealData) {
        StringBuilder builder = new StringBuilder("replace into "+TABLE_NAME+" values(").
                append("'"+pedoRealData.getDateString()+"',").append("'"+pedoRealData.getMac()+"',").append(pedoRealData.getPedoNum()+",").
                append(pedoRealData.getCaloric()+",").append(pedoRealData.getDistance()).
                append(")");
        DatabaseHelper.getInstance(mContext).execSql(builder.toString());
    }

    /**
     * find the fisrt one
     * @param date datetime
     * @param mac mac of device
     * @return return the fisrt record
     */
    public PedoRealData findFirst(String date,String mac){
        Cursor cursor = DatabaseHelper.getInstance(mContext).query(TABLE_NAME,null,COLUMN_DATE+"=? and "+COLUMN_MAC+"=?",new String[]{date,mac},null );
        PedoRealData pedoRealData = null;
        if(cursor != null && cursor.moveToNext()){
            int pedo = cursor.getInt(cursor.getColumnIndex(COLUMN_PEDO));
            int calo = cursor.getInt(cursor.getColumnIndex(COLUMN_CALO));
            float dist = cursor.getFloat(cursor.getColumnIndex(COLUMN_DIST));
            pedoRealData = new PedoRealData(date,mac,pedo,calo,dist);
        }
        if (cursor != null){
            cursor.close();
            cursor = null;
        }
        return pedoRealData;
    }


}
