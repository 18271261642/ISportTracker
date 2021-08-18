package com.isport.isportlibrary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.isport.isportlibrary.entry.BaseDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by Marcos Cheng on 2016/9/23.
 * Save info of ble device
 */
public class DbBaseDevice extends BaseDb {

    public static String TABLE_NAME = "basedevice";
    /**
     * device name
     */
    public static String COLUMN_NAME = "name";

    /**
     * mac of device
     */
    public static String COLUMN_MAC = "mac";
    /**
     * profile type of device,see {@link com.isport.isportlibrary.controller.BaseController#CMD_TYPE_W311},
     * {@link com.isport.isportlibrary.controller.BaseController#CMD_TYPE_W337B},{@link com.isport.isportlibrary.controller.BaseController#CMD_TYPE_W194}
     */
    public static String COLUMN_PROFILE_TYPE = "profileType";

    /**
     * type of device, for example {@link BaseDevice#TYPE_W311N}
     */
    public static String COLUMN_DEVICE_TYPE = "deviveType";

    /**
     * the time to connect
     */
    public static String COLUMN_CONNECTED_TIME = "connectedTime";
    public static String COLUMN_IS_NOTDISTURB = "isNotDisturb";
    public static String COLUMN_IS_MSGCALLCOUNT = "isMsgCallCount";
    public static String COLUMN_IS_NAPREMIND = "isNapRemind";
    public static String COLUMN_IS_ALARMHEALTHREMIND = "isAlarmHealthRemind";
    public static String COLUMN_IS_WRISTMODE = "isWristMode";
    public static String COLUMN_IS_AUTOSLEEP = "isAutoSleep";
    public static String COLUMN_IS_MEDIACONTROL = "isMediaControl";
    public static String COLUMN_IS_HEARTTIMING = "isHeartTiming";
    public static String COLUMN_IS_HEART_AUTO = "isHeartAuto";
    public static String COLUMN_IS_FIND_DEVICE = "isFindDevice";
    public static String COLUMN_IS_ANTILOST = "isAntiLost";
    public static String COLUMN_IS_SEDENTARYREM = "isSedentaryRem";
    public static String COLUMN_IS_ALARMREMIND = "isAlarmRemind";
    public static String COLUMN_IS_SCREENSET = "isScreenSet";
    public static String COLUMN_IS_DISPLAYSET = "isDisplaySet";


    public static String TABLE_CREATE_SQL = "Create table IF NOT EXISTS'"+TABLE_NAME+"'(" +
            "'"+COLUMN_NAME+"' varchar(50) not null," +
            "'"+COLUMN_MAC+"' varchar(30) not null," +
            "'"+COLUMN_DEVICE_TYPE+"' int," +
            "'"+COLUMN_PROFILE_TYPE+"' int," +
            "'"+COLUMN_CONNECTED_TIME+"' BIGINT," +
            "PRIMARY KEY ('name','mac'));";
    private static DbBaseDevice sInstance;

    private DbBaseDevice(Context context) {
        mContext = context;
    }

    public static DbBaseDevice getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DbBaseDevice.class) {
                if (sInstance == null) {
                    sInstance = new DbBaseDevice(context);
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
    public void update(ContentValues values,String whereCause,String[] whereArgs) {
        DatabaseHelper.getInstance(mContext).update(TABLE_NAME,values,whereCause,whereArgs);
    }

    /**
     * too see {@link DatabaseHelper#delete(String, String, String[])}
     * @param whereCause
     * @param whereArgs
     * @return return count that was been deleted
     */
    public int delete(String whereCause,String[] whereArgs){
        int count = DatabaseHelper.getInstance(mContext).delete(TABLE_NAME,whereCause,whereArgs);
        return count;
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
    public Cursor query(String[] columns,String selection,String[] selectionArgs,String groupBy,String orderBy){
        return DatabaseHelper.getInstance(mContext).query(TABLE_NAME,columns,selection,selectionArgs,groupBy,orderBy);
    }

    /**
     * quert all according the slection, see {@link DatabaseHelper#query(String, String[], String, String[], String)}
     * @param selection
     * @param selectionArgs
     * @param orderby
     * @return
     */
    public List<BaseDevice> findAll(String selection, String[] selectionArgs, String orderby){
        Cursor cursor = DatabaseHelper.getInstance(mContext).query(TABLE_NAME,null,selection,selectionArgs,orderby);
        List<BaseDevice> list = new ArrayList<>();
        if(cursor != null){
            try {
                while (cursor.moveToNext()){
                    list.add(new BaseDevice(getString(cursor,COLUMN_NAME),getString(cursor,COLUMN_MAC),0,getLong(cursor,COLUMN_CONNECTED_TIME),getInt(cursor,COLUMN_PROFILE_TYPE),
                            getInt(cursor,COLUMN_DEVICE_TYPE)));
                }
            }finally {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }


    /**
     * too see {@link #saveOrUpdate(BaseDevice)}
     * @param list the list you want to save
     */
    public void saveOrUpdate(List<BaseDevice> list){
        if(list != null) {
            DatabaseHelper.getInstance(mContext).beginTransaction();
            try {
                for (int i = 0; i < list.size(); i++) {
                    BaseDevice device = list.get(i);
                    saveOrUpdate(device);
                }
                DatabaseHelper.getInstance(mContext).setTransactionSuccessful();
            }finally {
                DatabaseHelper.getInstance(mContext).endTransaction();
            }

        }
    }

    /**
     *  save data to database, if there is a record, will update it, {@link android.database.sqlite.SQLiteDatabase#replace(String, String, ContentValues)} or insert it
     * @param device the you want to save
     */
    public void saveOrUpdate(BaseDevice device){
        StringBuilder builder = new StringBuilder("replace into "+TABLE_NAME+" values(").
                append("'"+device.getName()+"',").append("'"+device.getMac()+"',").append("'"+device.getDeviceType()+"',").
                append(device.getProfileType()+",").append(device.getConnectedTime()+"").
                append(")");
        DatabaseHelper.getInstance(mContext).execSql(builder.toString());
    }

    private ContentValues contentWithDevice(BaseDevice device){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, device.getName());
        values.put(COLUMN_MAC, device.getMac());
        values.put(COLUMN_CONNECTED_TIME, device.getConnectedTime());
        values.put(COLUMN_PROFILE_TYPE, device.getProfileType());
        values.put(COLUMN_DEVICE_TYPE,device.getDeviceType());
        return values;
    }

    private long insert(BaseDevice baseDevice) {
        ContentValues values = contentWithDevice(baseDevice);
        return DatabaseHelper.getInstance(mContext).insert(TABLE_NAME,values);
    }

    private void insert(List<BaseDevice> list){
        if(list!= null){
            DatabaseHelper.getInstance(mContext).beginTransaction();
            try {
                for (int i = 0; i < list.size(); i++) {
                    insert(list.get(i));
                }
                DatabaseHelper.getInstance(mContext).setTransactionSuccessful();
            }finally {
                DatabaseHelper.getInstance(mContext).endTransaction();
            }

        }
    }
}
