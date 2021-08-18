package com.isport.isportlibrary.entry;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.TimeZone;

/**
 * Created by chengjiamei on 2016/8/18.
 * save base information of user that some of them will be set to ble device
 * uint is metric,if you save info with inch,you need convert
 */
public class UserInfo {
    private final static String USER_PATH = "cjm_user_config";
    private final static String USER_NAME = "cjm_name";
    private final static String USER_HEAD = "cjm_head";
    private final static String USER_BIRTHDAY = "cjm_birthday";
    private final static String USER_HEIGHT = "cjm_height";
    private final static String USER_WEIGHT = "cjm_weight";
    private final static String USER_GENDER = "cjm_gender";
    private final static String USER_TARGET_STEP = "cjm_target_step";
    private final static String USER_STRIDE_LEN = "cjm_stride_len";
    private final static String USER_ACTIVE_TIME_ZONE = "cjm_activity_time_zone";
    private final static String USER_METRIC_IMPERIAL = "cjm_metric_imperial";
    private final static String USER_WRIST_MODE = "cjm_wrist_mode";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String nickname;
    private String head;///head path
    private String birthday;    //18 yrs - 80yrs
    private float height; //120 cm - 250cm
    private int gender; ///male 1 female 0
    private int weight; ///30kg - 250kg
    private int targetStep;//target step 1000 - 50000
    private int strideLength;/// stride length 30 - 150cm
    private int activeTimeZone;///Active Time Zone
    private int wristMode;

    private UserInfo(Context context){
        sharedPreferences = context.getSharedPreferences(USER_PATH,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static UserInfo getInstance(Context context){
        return new UserInfo(context);
    }

    public void setNickname(String nickname){
        editor.putString(USER_NAME,nickname == null?"":nickname).commit();
    }

    public void setHead(String path){
        if(path != null){
            editor.putString(USER_HEAD,path).commit();
        }
    }

    public void setWristMode(WristMode wristMode){
        if(wristMode != null){
            editor.putBoolean(USER_WRIST_MODE,wristMode.isLeftHand()).commit();
        }
    }

    public WristMode getWristMode(){
        return new WristMode(sharedPreferences.getBoolean(USER_WRIST_MODE,false));
    }

    /**
     * example: 1990-01-01 yyyy-MM-dd
     * @param birthday
     */
    public void setBirthday(String birthday){
        editor.putString(USER_BIRTHDAY,birthday == null?"1990-01-01":birthday).commit();
    }

    /**
     *  uint is metric,if you save info with inch,you need convert
     *  120 cm - 250cm
     *  @param height
     */
    public void setHeight(float height){
        editor.putFloat(USER_HEIGHT,(height>=50 && height<=251)?height:getHeight()).commit();
    }

    /**
     * uint is metric,if you save info with inch,you need convert
     * @param weight 30kg - 250kg
     */
    public void setWeight(float weight){
        editor.putFloat(USER_WEIGHT,(weight>=20 && weight<=661)?weight:getWeight()).commit();
    }

    /**
     * 100 - 300000
     * @param targetStep
     */
    public void setTargetStep(int targetStep){
        editor.putInt(USER_TARGET_STEP,(targetStep>=100 && targetStep<=300000)?targetStep:getTargetStep()).commit();
    }

    /**
     * uint is metric,if you save info with inch,you need convert
     * 30 - 150cm
     * @param strideLength
     */
    public void setStrideLength(float strideLength){
        editor.putFloat(USER_STRIDE_LEN,(strideLength>=30 && strideLength<=150)?strideLength:getStrideLength()).commit();
    }

   /* public void setActiveTimeZone(int activeTimeZone) {
        editor.putInt(USER_ACTIVE_TIME_ZONE,activeTimeZone).commit();
    }*/

    /**
     * if you change the metric/imperial that you must convert height or weight or strideLength,
     * for example,if you change unit sytem to metric,you must convert height or weight or strideLength to metric
     * @param metricImperial 1 0 / Imperial Metric
     */
    public void setMetricImperial(int metricImperial){
        editor.putInt(USER_METRIC_IMPERIAL,metricImperial).commit();
    }

    public void setGender(int gender){
        editor.putInt(USER_GENDER,gender).commit();
    }



    public int getMetricImperial(){
        return sharedPreferences.getInt(USER_METRIC_IMPERIAL,0);
    }

    /**
     * Activity time zone,timezone id
     * @param timeZoneId
     */
    public void setActiveTimeZone(String timeZoneId) {
        editor.putString(USER_ACTIVE_TIME_ZONE,timeZoneId).commit();
    }

    public int getActiveTimeZone (){
        String timezoneId = sharedPreferences.getString(USER_ACTIVE_TIME_ZONE,null);
        if(timezoneId == null){
            timezoneId = TimeZone.getDefault().getID();
        }
        TimeZone timezone = TimeZone.getTimeZone(timezoneId);
        int rawOffSet = timezone.getRawOffset();
        int offset = rawOffSet/3600000;
        return offset;
        /*String timezone = TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT).trim().substring(3).split(":")[0];
        String tmp = timezone.substring(0,1);
        boolean isPlus = true;
        if(tmp.equals("+")){
            timezone = timezone.substring(1,timezone.length());
            isPlus = true;
        }else if (tmp.equals("-")){
            timezone = timezone.substring(1,timezone.length());
            isPlus = false;
        }
        int tz = (isPlus?Integer.valueOf(timezone):((-1)*Integer.valueOf(timezone)));
        return sharedPreferences.getInt(USER_ACTIVE_TIME_ZONE,
                Integer.valueOf(tz));*/
    }

    /**
     *
     * @return nick name
     */
    public String getNickname(){
        return sharedPreferences.getString(USER_NAME,"");
    }

    /**
     *
     * @return the path of head photo
     */
    public String getHead(){
        return sharedPreferences.getString(USER_HEAD,"");
    }

    /**
     *
     * @return example 1990-01-01
     */
    public String getBirthday(){
        return sharedPreferences.getString(USER_BIRTHDAY,"1990-01-01");
    }

    /**
     * uint is metric,if you save info with inch,you need convert
     *
     * @return 120 cm - 250cm
     */
    public float getHeight(){
        return sharedPreferences.getFloat(USER_HEIGHT,170);
    }

    /**
     *
     * @return 30kg - 250kg
     */
    public float getWeight(){
        return sharedPreferences.getFloat(USER_WEIGHT,75);
    }

    /**
     * male 1 female 0
     *
     * @return default is male 1
     */
    public int getGender(){
        return sharedPreferences.getInt(USER_GENDER,1);

    }

    /**
     *
     * @return 1000 - 300000
     */
    public int getTargetStep(){
        return sharedPreferences.getInt(USER_TARGET_STEP,10000);
    }

    /**
     * uint is metric
     *
     * @return stride length   30 - 150cm
     */
    public float getStrideLength(){
        return sharedPreferences.getFloat(USER_STRIDE_LEN,60);
    }
}
