package com.isport.isportlibrary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Created by Marcos Cheng on 2016/9/23.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static int version = 5;
    private static String databaseName = "isport.db";
    private static DatabaseHelper sIntances;
    private SQLiteDatabase db;

    public static DatabaseHelper getInstance(Context context) {
        if (sIntances == null) {
            synchronized (DatabaseHelper.class) {
                if (sIntances == null) {
                    sIntances = new DatabaseHelper(context.getApplicationContext());
                }
            }
        }
        return sIntances;
    }

    private DatabaseHelper(Context context) {
        super(context, databaseName, null, version);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL(DbBaseDevice.TABLE_CREATE_SQL);
        db.execSQL(DbSprotDayData.TABLE_CREATE_SQL);
        db.execSQL(DbHistorySport.TABLE_CREATE_SQL);
        db.execSQL(DbRealTimePedo.TABLE_CREATE_SQL);
        db.execSQL(DbHistorySportN.TABLE_CREATE_SQL);
        db.execSQL(DbSportData337B.CREATE_SQL);
        db.execSQL(DbHeartRateHistory.TABLE_SQL);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        createTables(db);
    }

    /**
     * you can see {@link SQLiteDatabase#replace(String, String, ContentValues)}
     *
     * @param table
     * @param nullColumnHack
     * @param initialValues
     */
    public void replace(String table, String nullColumnHack, ContentValues initialValues) {
        SQLiteDatabase db = getWritableDatabase();
        db.replace(table, nullColumnHack, initialValues);
    }

    /**
     * you can see {@link SQLiteOpenHelper#onUpgrade(SQLiteDatabase, int, int)}
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createTables(db);
        if (oldVersion == 1) {
            //alter table **** add ****  integer/varchar2
            String sql1 = "alter table " + DbBaseDevice.TABLE_NAME + " add " + DbBaseDevice.COLUMN_DEVICE_TYPE + " " +
                    "integer";

            //String sql2 = "BEGIN TRANSACTION;";
            /*String sql3 = "Create TEMPORARY table 't1_backup'(" +
                    "'" + DbBaseDevice.COLUMN_NAME + "' varchar(50) not null," +
                    "'" + DbBaseDevice.COLUMN_MAC + "' varchar(30) not null," +
                    "'" + DbBaseDevice.COLUMN_DEVICE_TYPE + "' int," +
                    "'" + DbBaseDevice.COLUMN_PROFILE_TYPE + "' int," +
                    "'" + DbBaseDevice.COLUMN_CONNECTED_TIME + "' BIGINT," +
                    "PRIMARY KEY ('name','mac'));";*/
            String sql3 = "Create TEMPORARY table 't1_backup'(" +
                    "'" + DbBaseDevice.COLUMN_NAME + "' varchar(50) not null," +
                    "'" + DbBaseDevice.COLUMN_MAC + "' varchar(30) not null," +
                    "'" + DbBaseDevice.COLUMN_DEVICE_TYPE + "' int," +
                    "'" + DbBaseDevice.COLUMN_PROFILE_TYPE + "' int," +
                    "'" + DbBaseDevice.COLUMN_CONNECTED_TIME + "' BIGINT);";
            String sql4 = "INSERT INTO t1_backup SELECT '" + DbBaseDevice.COLUMN_NAME + "','" + DbBaseDevice
                    .COLUMN_MAC + "','" +
                    DbBaseDevice.COLUMN_DEVICE_TYPE + "','" + DbBaseDevice.COLUMN_PROFILE_TYPE + "','" + DbBaseDevice
                    .COLUMN_CONNECTED_TIME +
                    "' FROM " + DbBaseDevice.TABLE_NAME + ";";
            String sql5 = "DROP TABLE " + DbBaseDevice.TABLE_NAME + ";";
            /*String sql6 = "CREATE TABLE " + DbBaseDevice.TABLE_NAME + "('" + DbBaseDevice.COLUMN_NAME + "' varchar
            (50) not null,'" + DbBaseDevice.COLUMN_MAC +
                    "' varchar(30) not null,'" + DbBaseDevice.COLUMN_DEVICE_TYPE + "' int,'" + DbBaseDevice
                    .COLUMN_PROFILE_TYPE +
                    "' int,'" + DbBaseDevice.COLUMN_CONNECTED_TIME + "' BIGINT,PRIMARY KEY ('" + DbBaseDevice
                    .COLUMN_NAME + "','" + DbBaseDevice.COLUMN_MAC + "'));";*/
            String sql6 = "CREATE TABLE " + DbBaseDevice.TABLE_NAME + "('" + DbBaseDevice.COLUMN_NAME + "' varchar" +
                    "(50) not null,'" + DbBaseDevice.COLUMN_MAC +
                    "' varchar(30) not null,'" + DbBaseDevice.COLUMN_DEVICE_TYPE + "' int,'" + DbBaseDevice
                    .COLUMN_PROFILE_TYPE +
                    "' int,'" + DbBaseDevice.COLUMN_CONNECTED_TIME + "' BIGINT);";
            String sql7 = "INSERT INTO " + DbBaseDevice.TABLE_NAME + " SELECT '" + DbBaseDevice.COLUMN_NAME + "','" +
                    DbBaseDevice.COLUMN_MAC + "','" + DbBaseDevice.COLUMN_DEVICE_TYPE + "','" +
                    DbBaseDevice.COLUMN_PROFILE_TYPE + "','" + DbBaseDevice.COLUMN_CONNECTED_TIME + "' FROM t1_backup;";
            String sql8 = "DROP TABLE t1_backup;";
            //"COMMIT;";
            db.execSQL(sql1);
            db.beginTransaction();

            db.execSQL(sql3);
            db.execSQL(sql4);
            db.execSQL(sql5);
            db.execSQL(sql6);
            db.execSQL(sql7);
            db.execSQL(sql8);
            db.setTransactionSuccessful();
            db.endTransaction();

        }

        if (oldVersion == 1 || oldVersion == 2) {
            db.execSQL(DbSportData337B.CREATE_SQL);
        }

        this.db = db;
    }

    /**
     * you can see {@link SQLiteDatabase#insert(String, String, ContentValues)}
     *
     * @param table
     * @param values
     * @return
     */
    public long insert(String table, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(table, null, values);
    }

    /**
     * delete from table , see {@link SQLiteDatabase#delete(String, String, String[])}
     *
     * @param tablename
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public int delete(String tablename, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(tablename, whereClause, whereArgs);
    }

    /**
     * update database, see {@link SQLiteDatabase#update(String, ContentValues, String, String[])}
     *
     * @param table
     * @param values
     * @param whereClause
     * @param whereArgs
     */
    public void update(String table, ContentValues values, String whereClause,
                       String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(table, values, whereClause, whereArgs);
    }

    /**
     * query according selection, see
     * {@link SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)}
     *
     * @param tablename
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param orderby
     * @return
     */
    public Cursor query(String tablename, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String orderby) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(tablename, columns, selection, selectionArgs, groupBy,
                        null, orderby, null);
    }

    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy){
        SQLiteDatabase db = getWritableDatabase();
        return db.query(table,columns,selection,selectionArgs,groupBy,having,orderBy);
    }

    /**
     * query according selection, see {@link #query(String, String[], String, String[], String, String)}
     *
     * @param tablename
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param orderby
     * @return
     */
    public Cursor query(String tablename, String[] columns, String selection,
                        String[] selectionArgs, String orderby) {
        return query(tablename, columns, selection, selectionArgs, null,
                     orderby);
    }

    /**
     * exec sql, see {@link SQLiteDatabase#execSQL(String)}
     *
     * @param sql the sql String that you want to execute
     */
    public void execSql(String sql) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
    }

    /**
     * begin transaction
     * see {@link SQLiteDatabase#beginTransaction()}
     */
    public void beginTransaction() {
        this.db = getWritableDatabase();
        db.beginTransaction();
    }

    /**
     * end transaction
     * see {@link SQLiteDatabase#endTransaction()}
     */
    public void endTransaction() {
        this.db = getWritableDatabase();
        db.endTransaction();
    }

    /**
     * set Transaction Successful
     * see {@link SQLiteDatabase#setTransactionSuccessful()}
     */
    public void setTransactionSuccessful() {
        this.db = getWritableDatabase();
        db.setTransactionSuccessful();
    }

}
