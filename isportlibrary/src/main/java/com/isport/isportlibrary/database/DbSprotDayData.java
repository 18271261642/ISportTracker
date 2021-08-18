package com.isport.isportlibrary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.isport.isportlibrary.entry.SportDayData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by Marcos Cheng on 2016/9/23.
 * 311 series sport data of everyday
 * see {@link SportDayData}
 */
public class DbSprotDayData extends BaseDb {

    public static String TABLE_NAME = "sportDayData";
    /**
     * datatime
     */
    public static String COLUMN_DATE = "dateString";
    /**
     * mac of device
     */
    public static String COLUMN_MAC = "mac";
    /**
     * total step number
     */
    public static String COLUMN_T_STEP = "totalSteps";
    /**
     * total distance you walked
     */
    public static String COLUMN_T_DIST = "totalDist";
    /**
     * total calorics cosumed
     */
    public static String COLUMN_T_CALOR = "totalCaloric";
    /**
     * target steps that you want to walk
     */
    public static String COLUMN_TARGETSTEP = "targetStep";
    /**
     * stridlen
     */
    public static String COLUMN_STRDELEN = "stridLength";
    /**
     * weight you saved to ble device
     */
    public static String COLUMN_WEIGHT = "weight";
    /**
     * time of sleep
     */
    public static String COLUMN_SLEEPTIME = "sleepTime";
    /**
     * time of still
     */
    public static String COLUMN_STILLTIME = "stillTime";
    /**
     * time of walk
     */
    public static String COLUMN_WALKTIME = "walkTime";
    /**
     * time of walk in low speed
     */
    public static String COLUMN_LS_WALKTIME = "lowSpeedWalkTime";
    /**
     * time of walk in middle speed
     */
    public static String COLUMN_MS_WALKTIME = "midSpeedWalkTime";
    /**
     * time of walk in fast speed
     */
    public static String COLUMN_LARS_WALKTIME = "larSpeedWalkTime";
    /**
     * time of run in low speed
     */
    public static String COLUMN_LS_RUNTIME = "lowSpeedRunTime";
    /**
     * time of run in middle speed
     */
    public static String COLUMN_MS_RUNTIME = "midSpeedRunTime";
    /**
     * time of run in fast speed
     */
    public static String COLUMN_LARS_RUNTIME = "larSpeedRunTime";
    /**
     * total time of sport
     */
    public static String COLUMN_T_SPORTTIME = "totalSportTime";
    /**
     * time of target sleep
     */
    public static String COLUMN_TARGET_SLEEP = "targetSleep";

    public static String TABLE_CREATE_SQL = "Create table IF NOT EXISTS '" + TABLE_NAME + "'(" +
            "'" + COLUMN_DATE + "' varchar(50) not null," +
            "'" + COLUMN_MAC + "' varchar(30) not null," +
            "'" + COLUMN_T_STEP + "' int," +
            "'" + COLUMN_T_DIST + "' float," +
            "'" + COLUMN_T_CALOR + "' int," +
            "'" + COLUMN_TARGETSTEP + "' int," +
            "'" + COLUMN_STRDELEN + "' float," +
            "'" + COLUMN_WEIGHT + "' float," +
            "'" + COLUMN_SLEEPTIME + "' int," +
            "'" + COLUMN_STILLTIME + "' int," +
            "'" + COLUMN_WALKTIME + "' int," +
            "'" + COLUMN_LS_WALKTIME + "' int," +
            "'" + COLUMN_MS_WALKTIME + "' int," +
            "'" + COLUMN_LARS_WALKTIME + "' int," +
            "'" + COLUMN_LS_RUNTIME + "' int," +
            "'" + COLUMN_MS_RUNTIME + "' int," +
            "'" + COLUMN_LARS_RUNTIME + "' int," +
            "'" + COLUMN_T_SPORTTIME + "' int," +
            "'" + COLUMN_TARGET_SLEEP + "' int," +
            "PRIMARY KEY ('" + COLUMN_DATE + "','" + COLUMN_MAC + "'));";
    private static DbSprotDayData sInstance;


    private DbSprotDayData(Context context) {
        mContext = context;
    }

    public static DbSprotDayData getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DbSprotDayData.class) {
                if (sInstance == null) {
                    sInstance = new DbSprotDayData(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * to see {@link DatabaseHelper#update(String, ContentValues, String, String[])}
     * @param values
     * @param whereCause
     * @param whereArgs
     */
    public void update(ContentValues values, String whereCause, String[] whereArgs) {
        DatabaseHelper.getInstance(mContext).update(TABLE_NAME, values, whereCause, whereArgs);
    }

    /**
     * to see {@link DatabaseHelper#delete(String, String, String[])}
     * @param whereCause
     * @param whereArgs
     */
    public void delete(String whereCause, String[] whereArgs) {
        DatabaseHelper.getInstance(mContext).delete(TABLE_NAME, whereCause, whereArgs);
    }

    /**
     * to see {@link DatabaseHelper#query(String, String[], String, String[], String, String)}
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param orderBy
     * @return
     */
    public Cursor query(String[] columns, String selection, String[] selectionArgs, String groupBy, String orderBy) {
        return DatabaseHelper.getInstance(mContext).query(TABLE_NAME, columns, selection, selectionArgs, groupBy, orderBy);
    }

    /*String mac,String dateString, int stepNum, int sleepState
     * COLUMN_DATE COLUMN_MAC is PRIMARY KEY
     **/
    public List<SportDayData> findAll(String[] columns, String selection, String[] selectionArgs, String orderby) {
        Cursor cursor = DatabaseHelper.getInstance(mContext).query(TABLE_NAME, columns, selection, selectionArgs, orderby);
        return parserCursor(cursor);
    }

    private List<SportDayData> parserCursor(Cursor cursor) {
        List<SportDayData> list = new ArrayList<>();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    SportDayData sportDayData = new SportDayData(getString(cursor, COLUMN_MAC), getString(cursor, COLUMN_DATE),
                            getInt(cursor, COLUMN_T_STEP), getFloat(cursor, COLUMN_T_DIST), getInt(cursor, COLUMN_T_CALOR), getInt(cursor, COLUMN_TARGETSTEP)
                            , getFloat(cursor, COLUMN_STRDELEN), getFloat(cursor, COLUMN_WEIGHT), getInt(cursor, COLUMN_SLEEPTIME),
                            getInt(cursor, COLUMN_STILLTIME), getInt(cursor, COLUMN_WALKTIME), getInt(cursor, COLUMN_LS_WALKTIME),
                            getInt(cursor, COLUMN_MS_WALKTIME), getInt(cursor, COLUMN_LARS_WALKTIME), getInt(cursor, COLUMN_LS_RUNTIME),
                            getInt(cursor, COLUMN_MS_RUNTIME), getInt(cursor, COLUMN_LARS_RUNTIME), getInt(cursor, COLUMN_T_SPORTTIME), getInt(cursor, COLUMN_TARGET_SLEEP));
                    list.add(sportDayData);
                }
            } finally {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    /**
     * COLUMN_DATE COLUMN_MAC is PRIMARY KEY
     *
     * @param selection
     * @param selectionArgs
     * @param orderby
     * @return
     */
    public SportDayData findFirst(String selection, String[] selectionArgs, String orderby) {
        Cursor cursor = DatabaseHelper.getInstance(mContext).query(TABLE_NAME, null, selection, selectionArgs, orderby);
        List<SportDayData> list = parserCursor(cursor);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * save the list of data to database, too see {@link #saveOrUpdate(SportDayData)}
     * @param list the data you want to save
     */
    public void saveOrUpdate(List<SportDayData> list) {
        if (list != null) {
            DatabaseHelper.getInstance(mContext).beginTransaction();
            try {
                for (int i = 0; i < list.size(); i++) {
                    SportDayData sportDayData = list.get(i);
                    saveOrUpdate(sportDayData);
                }
                DatabaseHelper.getInstance(mContext).setTransactionSuccessful();
            } finally {
                DatabaseHelper.getInstance(mContext).endTransaction();
            }
        }
    }

    /**
     * save data to database if there is the data will to update it or insert it
     * @param sportDayData the data that you want to save
     */
    public void saveOrUpdate(SportDayData sportDayData) {
        StringBuilder builder = new StringBuilder("replace into " + TABLE_NAME + " values(").
                append("'" + sportDayData.getDateString() + "',").append("'" + sportDayData.getMac() + "',").append(sportDayData.getTotalStep() + ",").
                append(sportDayData.getTotalDist() + ",").append(sportDayData.getTotalCaloric() + ",").append(sportDayData.getTargetStep() + ",").
                append(sportDayData.getStridLength() + ",").append(sportDayData.getWeight() + ",").append(sportDayData.getSleepTime() + ",").
                append(sportDayData.getStillTime() + ",").append(sportDayData.getWalkTime() + ",").append(sportDayData.getLowSpeedWalkTime() + ",").
                append(sportDayData.getMidSpeedWalkTime() + ",").append(sportDayData.getLarSpeedWalkTime() + ",").
                append(sportDayData.getLowSpeedRunTime() + ",").append(sportDayData.getMidSpeedRunTime() + ",").append(sportDayData.getLarSpeedRunTime() + ",").
                append(sportDayData.getTotalSportTime() + ",").append(sportDayData.getTargetSleep()).
                append(")");
        DatabaseHelper.getInstance(mContext).execSql(builder.toString());
    }

    private ContentValues contentWithDevice(SportDayData sportDayData) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, sportDayData.getDateString());
        values.put(COLUMN_MAC, sportDayData.getMac());
        values.put(COLUMN_T_STEP, sportDayData.getTotalStep());
        values.put(COLUMN_T_DIST, sportDayData.getTotalDist());
        values.put(COLUMN_T_CALOR, sportDayData.getTotalCaloric());
        values.put(COLUMN_TARGETSTEP, sportDayData.getTargetStep());
        values.put(COLUMN_STRDELEN, sportDayData.getStridLength());
        values.put(COLUMN_WEIGHT, sportDayData.getWeight());
        values.put(COLUMN_SLEEPTIME, sportDayData.getSleepTime());
        values.put(COLUMN_STILLTIME, sportDayData.getStillTime());
        values.put(COLUMN_WALKTIME, sportDayData.getWalkTime());
        values.put(COLUMN_LS_WALKTIME, sportDayData.getLowSpeedWalkTime());
        values.put(COLUMN_MS_WALKTIME, sportDayData.getMidSpeedWalkTime());
        values.put(COLUMN_LARS_WALKTIME, sportDayData.getLarSpeedWalkTime());
        values.put(COLUMN_LS_RUNTIME, sportDayData.getLowSpeedRunTime());
        values.put(COLUMN_MS_RUNTIME, sportDayData.getMidSpeedRunTime());
        values.put(COLUMN_LARS_RUNTIME, sportDayData.getLarSpeedRunTime());
        values.put(COLUMN_T_SPORTTIME, sportDayData.getTotalSportTime());
        values.put(COLUMN_TARGET_SLEEP, sportDayData.getTargetSleep());
        return values;
    }

    private long insert(SportDayData historySport) {
        ContentValues values = contentWithDevice(historySport);
        return DatabaseHelper.getInstance(mContext).insert(TABLE_NAME, values);
    }

    private void insert(List<SportDayData> list) {
        if (list != null) {
            DatabaseHelper.getInstance(mContext).beginTransaction();
            try {
                for (int i = 0; i < list.size(); i++) {
                    insert(list.get(i));
                }
                setTransactionSuccessful();
            }finally {
                DatabaseHelper.getInstance(mContext).endTransaction();
            }


        }
    }
}
