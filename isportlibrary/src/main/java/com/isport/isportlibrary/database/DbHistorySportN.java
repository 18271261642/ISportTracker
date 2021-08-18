package com.isport.isportlibrary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.isport.isportlibrary.entry.HistorySport;
import com.isport.isportlibrary.entry.HistorySportN;

import java.util.ArrayList;
import java.util.List;

/**
 * @创建者 bear
 * @创建时间 2019/4/19 16:20
 * @描述
 */
public class DbHistorySportN extends BaseDb {

    public static String TABLE_NAME         = "historySportN";
    /**
     * datetime
     */
    public static String COLUMN_DATE        = "dateString";
    /**
     * mac of device
     */
    public static String COLUMN_MAC         = "mac";
    /**
     * step number
     */
    public static String COLUMN_STEP_NUM    = "stepNum";
    /**
     * sleep state , it may be deep sleep,light sleep, very light sleep , awake state and no sleep
     * the value is 0(no sleep),128(deep sleep),129(light sleep),130(very light sleep),131(awake)
     */
    public static String COLUMN_SLEEP_STATE = "sleepState";

    public static String COLUMN_HEART_RATE = "heartRate";

    public static String COLUMN_INDEX = "index";

    public static String TABLE_CREATE_SQL = "Create table if not exists  '" + TABLE_NAME + "'(" +
            "'" + COLUMN_DATE + "' varchar(50) not null," +
            "'" + COLUMN_MAC + "' varchar(30) not null," +
            "'" + COLUMN_STEP_NUM + "' int," +
            "'" + COLUMN_SLEEP_STATE + "' int," +
            "'" + COLUMN_HEART_RATE + "' int," +
            "'" + COLUMN_INDEX + "' int," +
            "PRIMARY KEY ('" + COLUMN_DATE + "','" + COLUMN_MAC + "'));";


    private static DbHistorySportN sInstance;

    private DbHistorySportN(Context context) {
        mContext = context;
    }

    public static DbHistorySportN getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DbHistorySportN.class) {
                if (sInstance == null) {
                    sInstance = new DbHistorySportN(context.getApplicationContext());
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
    public List<HistorySportN> findAll(String selection, String[] selectionArgs, String orderby) {
        Cursor cursor = null;
        List<HistorySportN> list = new ArrayList<>();
        try {
            cursor = DatabaseHelper.getInstance(mContext).query(TABLE_NAME, null, selection, selectionArgs, orderby);
            if (cursor != null) {
                int count = cursor.getCount();
                while (cursor.moveToNext()) {
                    HistorySportN historySport = new HistorySportN(getString(cursor, COLUMN_MAC), getString(cursor,
                            COLUMN_DATE),
                            getInt(cursor, COLUMN_STEP_NUM), getInt(cursor,
                            COLUMN_SLEEP_STATE), getInt(cursor,
                            COLUMN_HEART_RATE), getInt(cursor,
                            COLUMN_INDEX));
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
    public HistorySportN findFirst(String selection, String[] selectionArgs) {
        Cursor cursor = DatabaseHelper.getInstance(mContext).query(TABLE_NAME, null, selection, selectionArgs, null,
                null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                //String mac,String dateString, int stepNum, int sleepState
                HistorySportN historySport = new HistorySportN(cursor.getString(cursor.getColumnIndex(COLUMN_MAC)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DATE)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_STEP_NUM)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_SLEEP_STATE)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_HEART_RATE)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_INDEX)));
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
    public void saveOrUpdate(List<HistorySportN> list) {
        if (list != null && list.size() > 0) {
            DatabaseHelper.getInstance(mContext).beginTransaction();
            try {
                for (int i = 0; i < list.size(); i++) {
                    HistorySportN historySport = list.get(i);
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
    public void saveOrUpdate(HistorySportN historySport) {
        StringBuilder builder = new StringBuilder("replace into " + TABLE_NAME + " values(").
                append("'" + historySport.getDateString() + "',").append("'" + historySport.getMac() + "',").append
                (historySport.getStepNum() + ",").
                append(historySport.getSleepState() + ",").append(historySport.getHeartRate() + ",").append(historySport.getIndex()).
                append(")");
        DatabaseHelper.getInstance(mContext).execSql(builder.toString());
    }

    private ContentValues contentWithDevice(HistorySportN historySport) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, historySport.getDateString());
        values.put(COLUMN_MAC, historySport.getMac());
        values.put(COLUMN_SLEEP_STATE, historySport.getSleepState());
        values.put(COLUMN_STEP_NUM, historySport.getStepNum());
        values.put(COLUMN_HEART_RATE, historySport.getHeartRate());
        values.put(COLUMN_INDEX, historySport.getIndex());
        return values;
    }

    private long insert(HistorySportN historySport) {
        ContentValues values = contentWithDevice(historySport);
        return DatabaseHelper.getInstance(mContext).insert(TABLE_NAME, values);
    }

    private void insert(List<HistorySportN> list) {
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
