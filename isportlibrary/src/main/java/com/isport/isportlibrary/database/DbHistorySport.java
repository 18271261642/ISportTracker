package com.isport.isportlibrary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.isport.isportlibrary.entry.HistorySport;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by Marcos Cheng on 2016/9/23.
 * save the detail data of sport and sleep for w311 serial
 * {@link com.isport.isportlibrary.controller.BaseController#CMD_TYPE_W311}
 * there are 288 group  data that every group is 5 minutes for each day
 * {@link HistorySport}
 **/
public class DbHistorySport extends BaseDb {

    public static String TABLE_NAME = "historySport";
    /**
     * datetime
     */
    public static String COLUMN_DATE = "dateString";
    /**
     * mac of device
     */
    public static String COLUMN_MAC = "mac";
    /**
     * step number
     */
    public static String COLUMN_STEP_NUM = "stepNum";
    /**
     * sleep state , it may be deep sleep,light sleep, very light sleep , awake state and no sleep
     * the value is 0(no sleep),128(deep sleep),129(light sleep),130(very light sleep),131(awake)
     */
    public static String COLUMN_SLEEP_STATE = "sleepState";

    public static String TABLE_CREATE_SQL = "Create table if not exists  '" + TABLE_NAME + "'(" +
            "'" + COLUMN_DATE + "' varchar(50) not null," +
            "'" + COLUMN_MAC + "' varchar(30) not null," +
            "'" + COLUMN_STEP_NUM + "' int," +
            "'" + COLUMN_SLEEP_STATE + "' int," +
            "PRIMARY KEY ('" + COLUMN_DATE + "','" + COLUMN_MAC + "'));";


    private static DbHistorySport sInstance;

    private DbHistorySport(Context context) {
        mContext = context;
    }

    public static DbHistorySport getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DbHistorySport.class) {
                if (sInstance == null) {
                    sInstance = new DbHistorySport(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * to see {@link DatabaseHelper#update(String, ContentValues, String, String[])}
     *
     * @param values
     * @param whereCause
     * @param whereArgs
     */
    public void update(ContentValues values, String whereCause, String[] whereArgs) {
        DatabaseHelper.getInstance(mContext).update(TABLE_NAME, values, whereCause, whereArgs);
    }

    /**
     * to see {@link DatabaseHelper#delete(String, String, String[])}
     *
     * @param whereCause
     * @param whereArgs
     */
    public void delete(String whereCause, String[] whereArgs) {
        DatabaseHelper.getInstance(mContext).delete(TABLE_NAME, whereCause, whereArgs);
    }

    /**
     * to see {@link DatabaseHelper#query(String, String[], String, String[], String, String)}
     *
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param orderBy
     * @return
     */
    public Cursor query(String[] columns, String selection, String[] selectionArgs, String groupBy, String orderBy) {
        return DatabaseHelper.getInstance(mContext).query(TABLE_NAME, columns, selection, selectionArgs, groupBy,
                orderBy);

    }

    /**
     * String mac,String dateString, int stepNum, int sleepState
     * COLUMN_DATE COLUMN_MAC is PRIMARY KEY
     *
     * @param selection
     * @param selectionArgs
     * @param orderby
     * @return return a list which size if equal count of cursor
     */
    public List<HistorySport> findAll(String selection, String[] selectionArgs, String orderby) {
        Cursor cursor = null;
        List<HistorySport> list = new ArrayList<>();
        try {
            cursor = DatabaseHelper.getInstance(mContext).query(TABLE_NAME, null, selection, selectionArgs, orderby);
            if (cursor != null) {
                int count = cursor.getCount();
                while (cursor.moveToNext()) {
                    HistorySport historySport = new HistorySport(getString(cursor, COLUMN_MAC), getString(cursor,
                            COLUMN_DATE),
                            getInt(cursor, COLUMN_STEP_NUM), getInt(cursor,
                            COLUMN_SLEEP_STATE));
                    list.add(historySport);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return list;
    }

    /**
     * too see {@link #findAll(String, String[], String)}
     *
     * @param selection
     * @param selectionArgs
     * @return return the first that index is 0
     */
    public HistorySport findFirst(String selection, String[] selectionArgs) {
        Cursor cursor = DatabaseHelper.getInstance(mContext).query(TABLE_NAME, null, selection, selectionArgs, null,
                null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                //String mac,String dateString, int stepNum, int sleepState
                HistorySport historySport = new HistorySport(cursor.getString(cursor.getColumnIndex(COLUMN_MAC)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DATE)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_STEP_NUM)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_SLEEP_STATE)));
                cursor.close();
                cursor = null;
                return historySport;
            }
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        return null;
    }

    /**
     * too see {@link #saveOrUpdate(HistorySport)}
     *
     * @param list the list you want to save
     */
    public void saveOrUpdate(List<HistorySport> list) {
        if (list != null && list.size() > 0) {
            DatabaseHelper.getInstance(mContext).beginTransaction();
            try {
                for (int i = 0; i < list.size(); i++) {
                    HistorySport historySport = list.get(i);
                    saveOrUpdate(historySport);
                }
                DatabaseHelper.getInstance(mContext).setTransactionSuccessful();
            } finally {
                DatabaseHelper.getInstance(mContext).endTransaction();
            }
        }
    }

    /**
     * save data to database, if there is a record, will update it,
     * {@link android.database.sqlite.SQLiteDatabase#replace(String, String, ContentValues)}
     *
     * @param historySport
     */
    public void saveOrUpdate(HistorySport historySport) {
       // Log.e("saveOrUpdate", historySport.toString());
        StringBuilder builder = new StringBuilder("replace into " + TABLE_NAME + " values(").
                append("'" + historySport.getDateString() + "',").append("'" + historySport.getMac() + "',").append
                (historySport.getStepNum() + ",").
                append(historySport.getSleepState()).
                append(")");
        DatabaseHelper.getInstance(mContext).execSql(builder.toString());
    }

    private ContentValues contentWithDevice(HistorySport historySport) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, historySport.getDateString());
        values.put(COLUMN_MAC, historySport.getMac());
        values.put(COLUMN_SLEEP_STATE, historySport.getSleepState());
        values.put(COLUMN_STEP_NUM, historySport.getStepNum());
        return values;
    }

    private long insert(HistorySport historySport) {
        ContentValues values = contentWithDevice(historySport);
        return DatabaseHelper.getInstance(mContext).insert(TABLE_NAME, values);
    }

    private void insert(List<HistorySport> list) {
        if (list != null) {
            DatabaseHelper.getInstance(mContext).beginTransaction();
            try {
                for (int i = 0; i < list.size(); i++) {
                    insert(list.get(i));
                }
                DatabaseHelper.getInstance(mContext).setTransactionSuccessful();
            } finally {
                DatabaseHelper.getInstance(mContext).endTransaction();
            }
        }
    }


}
