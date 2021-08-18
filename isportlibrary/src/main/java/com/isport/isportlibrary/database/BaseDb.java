package com.isport.isportlibrary.database;

import android.content.Context;
import android.database.Cursor;

/**
 * @author Created by Marcos Cheng on 2016/10/25.
 */

public class BaseDb {

    protected Context mContext;

    /**
     * Summarize
     * @param tableName table name
     * @param columns columns
     * @param selection selection
     * @param selectionArgs selectionArgs
     * @retAurn result of sum
     */
    public float[] sum(String tableName, String[] columns, String selection, String[] selectionArgs) {
        if (columns != null) {
            String[] aliasName = new String[columns.length];
            for (int i=0;i<columns.length;i++){
                aliasName[i] = "sum"+i;
                columns[i] = columns[i]+" as "+aliasName[i];
            }
            float[] tp = new float[columns.length];
            Cursor cursor = null;
            try {
                cursor = DatabaseHelper.getInstance(mContext).query(tableName, columns, selection, selectionArgs, null);
                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                    for (int i = 0; i < columns.length; i++) {
                        tp[i] = cursor.getFloat(cursor.getColumnIndex(aliasName[i]));
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(cursor != null) {
                    cursor.close();
                }
            }

            return tp;
        }
        return null;
    }

    /**
     * get string value by column name form cursor
     * @param cursor
     * @param columnName column name
     * @return result that get from cursor
     */
    public String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    /**
     * get int value by column name from cursor
     * @param cursor
     * @param columnName column name
     * @return result that get from cursor
     */
    public int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }
    /**
     * get float value by column name from cursor
     * @param cursor
     * @param columnName column name
     * @return result that get from cursor
     */
    public float getFloat(Cursor cursor, String columnName) {
        return cursor.getFloat(cursor.getColumnIndex(columnName));
    }

    /**
     * get long value by column name from cursor
     * @param cursor
     * @param columName column name
     * @return result that get from cursor
     */
    public long getLong(Cursor cursor, String columName) {
        return cursor.getLong(cursor.getColumnIndex(columName));
    }

    /**
     * begin transaction
     */
    public void beginTransaction(){
        DatabaseHelper.getInstance(mContext).beginTransaction();
    }

    /**
     * end transaction
     */
    public void endTransaction(){
        DatabaseHelper.getInstance(mContext).endTransaction();
    }

    /**
     * if end transation, remenber to setTransaction success, or it will roll back
     */
    public void setTransactionSuccessful(){
        DatabaseHelper.getInstance(mContext).setTransactionSuccessful();
    }
}
