package com.isport.isportlibrary.entry;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.Serializable;

/**
 * Created by chengjiamei on 2016/8/24.
 * sleep setting
 * all the time is 24-hour
 */
public class AutoSleep implements Serializable {

    private final static String SLEEP_PATH = "sleep_path";
    private final static String SLEEP_AUTO = "cjm_auto";
    private final static String SLEEP_IS_SLEEP = "cjm_is_sleep";
    private final static String SLEEP_SLEEP_REMIND = "cjm_sleep_remind";
    private final static String SLEEP_REMIND_TIME = "cjm_remind_time";
    private final static String SLEEP_S_START_HOUR = "cjm_s_start_hour";
    private final static String SLEEP_S_START_MIN = "cjm_s_start_min";
    private final static String SLEEP_S_END_HOUR = "cjm_s_end_hour";
    private final static String SLEEP_S_END_MIN = "cjm_s_end_min";
    private final static String SLEEP_IS_NAP = "cjm_is_nap";
    private final static String SLEEP_NAP_REMIND = "cjm_nap_remind";
    private final static String SLEEP_N_REMIND_TIME = "cjm_n_remind_time";
    private final static String SLEEP_N_START_HOUR = "cjm_n_start_hour";
    private final static String SLEEP_N_START_MIN = "cjm_n_start_min";
    private final static String SLEEP_N_END_HOUR = "cjm_n_end_hour";
    private final static String SLEEP_N_END_MIN = "cjm_n_end_min";
    private final static String SLEEP_TARGET_HOUR = "cjm_target_hour";
    private final static String SLEEP_TARGET_MIN = "cjm_target_min";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private AutoSleep(Context context){
        sharedPreferences = context.getSharedPreferences(SLEEP_PATH,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static AutoSleep getInstance(Context context){
        return new AutoSleep(context);
    }

    /**
     *
     * @param autoSleep
     */
    public void setAutoSleep(boolean autoSleep) {
        editor.putBoolean(SLEEP_AUTO,autoSleep).commit();
    }

    public void setSleep(boolean sleep) {
        editor.putBoolean(SLEEP_IS_SLEEP,sleep).commit();
    }

    public void setSleepRemind(boolean sleepRemind) {
        editor.putBoolean(SLEEP_SLEEP_REMIND,sleepRemind).commit();
    }

    /**
     * unit is minutes that No more than 60 minutes
     * @param sleepRemindTime
     */
    public void setSleepRemindTime(int sleepRemindTime) {
        editor.putInt(SLEEP_REMIND_TIME,sleepRemindTime).commit();
    }

    public void setSleepStartHour(int sleepStartHour) {
        editor.putInt(SLEEP_S_START_HOUR,sleepStartHour).commit();
    }

    public void setSleepStartMin(int sleepStartMin) {
        editor.putInt(SLEEP_S_START_MIN,sleepStartMin).commit();
    }

    public void setSleepEndHour(int sleepEndHour) {
        editor.putInt(SLEEP_S_END_HOUR,sleepEndHour).commit();
    }

    public void setSleepEndMin(int sleepEndMin) {
        editor.putInt(SLEEP_S_END_MIN,sleepEndMin).commit();
    }

    public void setNap(boolean nap) {
        editor.putBoolean(SLEEP_IS_NAP,nap).commit();
    }

    public void setNapRemind(boolean napRemind) {
        editor.putBoolean(SLEEP_NAP_REMIND,napRemind).commit();
    }

    /**
     * no more than 60 minutes
     * @param napRemindTime
     */
    public void setNapRemindTime(int napRemindTime) {
        editor.putInt(SLEEP_N_REMIND_TIME,napRemindTime).commit();
    }

    public void setNapStartHour(int napStartHour) {
        editor.putInt(SLEEP_N_START_HOUR,napStartHour).commit();
    }

    public void setNaoStartMin(int naoStartMin) {
        editor.putInt(SLEEP_N_START_MIN,naoStartMin).commit();
    }

    public void setNapEndHour(int napEndHour) {
        editor.putInt(SLEEP_N_END_HOUR,napEndHour).commit();
    }

    public void setNapEndMin(int napEndMin) {
        editor.putInt(SLEEP_N_END_MIN,napEndMin).commit();
    }

    public void setSleepTargetHour(int sleepTargetHour) {
        editor.putInt(SLEEP_TARGET_HOUR,sleepTargetHour).commit();
    }

    public void setSleepTargetMin(int sleepTargetMin){
        editor.putInt(SLEEP_TARGET_MIN,sleepTargetMin).commit();
    }

    /**
     *
     * @return whether open auto sleep
     */
    public boolean isAutoSleep() {
        return sharedPreferences.getBoolean(SLEEP_AUTO,true);
    }

    public boolean isSleep() {
        return sharedPreferences.getBoolean(SLEEP_IS_SLEEP,false);
    }

    public boolean isSleepRemind() {
        return sharedPreferences.getBoolean(SLEEP_SLEEP_REMIND,false);
    }

    /**
     *
     * @return  no more than 60 minute
     */
    public int getSleepRemindTime() {
        return sharedPreferences.getInt(SLEEP_REMIND_TIME,15);
    }

    public int getSleepStartHour() {
        return sharedPreferences.getInt(SLEEP_S_START_HOUR,22);
    }

    public int getSleepStartMin() {
        return sharedPreferences.getInt(SLEEP_S_START_MIN,0);
    }

    public int getSleepEndHour() {
        return sharedPreferences.getInt(SLEEP_S_END_HOUR,6);
    }

    public int getSleepEndMin() {
        return sharedPreferences.getInt(SLEEP_S_END_MIN,0);
    }

    public boolean isNap() {
        return sharedPreferences.getBoolean(SLEEP_IS_NAP,false);
    }

    public boolean isNapRemind() {
        return sharedPreferences.getBoolean(SLEEP_NAP_REMIND,false);
    }

    /**
     *
     * @return no more than 60 minutes
     */
    public int getNapRemindTime() {
        return sharedPreferences.getInt(SLEEP_N_REMIND_TIME,15);
    }

    public int getNapStartHour() {
        return sharedPreferences.getInt(SLEEP_N_START_HOUR,13);
    }

    public int getNapStartMin() {
        return sharedPreferences.getInt(SLEEP_N_START_MIN,0);
    }

    public int getNapEndHour() {
        return sharedPreferences.getInt(SLEEP_N_END_HOUR,14);
    }

    public int getNapEndMin() {
        return sharedPreferences.getInt(SLEEP_N_END_MIN,0);
    }

    public int getSleepTargetHour() {
        return sharedPreferences.getInt(SLEEP_TARGET_HOUR,8);
    }

    public int getSleepTargetMin() {
        return sharedPreferences.getInt(SLEEP_TARGET_MIN,0);
    }
}
