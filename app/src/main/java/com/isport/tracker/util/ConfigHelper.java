package com.isport.tracker.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016/10/17.
 */

public class ConfigHelper {

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static ConfigHelper sInstance;

    private void init(Context context){
        if(sharedPreferences == null){
            sharedPreferences = context.getSharedPreferences(Constants.LIDL_CONFIG, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
    }

    private ConfigHelper(Context context){
        init(context);
    }

    public static ConfigHelper getInstance(Context context){
        if(sInstance == null){
            synchronized (ConfigHelper.class){
                if(sInstance == null) {
                    sInstance = new ConfigHelper(context);
                }
            }
        }
        return sInstance;
    }


    public void putString(String key,String value){
        editor.putString(key,value).commit();
    }

    public void putInt(String key,int value){
        editor.putInt(key,value).commit();
    }

    public void putFloat(String key,float value){
        editor.putFloat(key,value).commit();
    }

    public void putBoolean(String key,boolean value){
        editor.putBoolean(key,value).commit();
    }

    public String getString(String key,String delValue) {
        return sharedPreferences.getString(key,delValue);
    }

    public int getInt(String key,int delValue) {
        return sharedPreferences.getInt(key,delValue);
    }

    public float getFloat(String key,float delValue) {
        return sharedPreferences.getFloat(key,delValue);
    }

    public boolean getBoolean(String key,boolean delValue) {
        return sharedPreferences.getBoolean(key,delValue);
    }

    public void remove(String key){
        editor.remove(key).commit();
    }
}
