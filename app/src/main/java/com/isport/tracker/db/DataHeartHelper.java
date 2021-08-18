package com.isport.tracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.isport.tracker.MyApp;

/**
 * Created by Administrator on 2016/10/28.
 */

public class DataHeartHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "heart.db";
    private static int DB_VERSION = 3;
    private static DataHeartHelper sInstance;

    public DataHeartHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static DataHeartHelper getInstance(){
        if(sInstance == null){
            synchronized (DataHeartHelper.class){
                if(sInstance == null){
                    sInstance = new DataHeartHelper(MyApp.getInstance(),DB_NAME,null,DB_VERSION);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbHeart.TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1 && newVersion == 2) {
            String sql1 = "alter table " + DbHeart.TABLE_NAME+" add column "+ DbHeart.COLUMN_CAL+" long default 0;";
            db.execSQL(sql1);
        }else  if(oldVersion == 2&&newVersion == 3) {
            String sql1 = "alter table " + DbHeart.TABLE_NAME+" add column "+ DbHeart.COLUMN_ISHISTORY+" int default 0;";
            db.execSQL(sql1);
        }else  if(oldVersion == 1&&newVersion == 3) {
            String sql1 = "alter table " + DbHeart.TABLE_NAME+" add column "+ DbHeart.COLUMN_CAL+" long default 0;";
            db.execSQL(sql1);
            String sql2 = "alter table " + DbHeart.TABLE_NAME+" add column "+ DbHeart.COLUMN_ISHISTORY+" int default 0;";
            db.execSQL(sql2);
        }
    }

    public void delete(String tableName,String where,String[] args){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(tableName,where,args);
    }

    public void insert(String tableName, ContentValues values){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(tableName,null,values);
    }

    public void replace(String table, String nullColumnHack, ContentValues initialValues){
        SQLiteDatabase db = getWritableDatabase();
        db.replace(table,nullColumnHack, initialValues);
    }

    public Cursor query(boolean distinct, String table, String[] columns,
                        String selection, String[] selectionArgs, String groupBy,
                        String having, String orderBy, String limit){
        SQLiteDatabase db = getWritableDatabase();
        return db.query(distinct,table,columns,selection,selectionArgs,groupBy,having,orderBy,limit);
    }

    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy){
        SQLiteDatabase db = getWritableDatabase();
        return db.query(table,columns,selection,selectionArgs,groupBy,having,orderBy);
    }

    public void beginTransaction(){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
    }

    public void endTransaction(){
        SQLiteDatabase db = getWritableDatabase();
        db.endTransaction();
    }

    public void setTransactionSuccessful(){
        SQLiteDatabase db = getWritableDatabase();
        db.setTransactionSuccessful();
    }
}
