package com.isport.isportlibrary.tools;

/**
 * @author Created by Marcos Cheng on 2017/4/6.
 */

public class Constants {
    /**
     * if you want to show log, let it be true ,or false
     * before you release the app, set it false first!
     */
    public static boolean IS_DEBUG = true;
    // public static boolean IS_CUSTOM_DEBUG = false;
    /**
     * if ues the default music control that designed in sdk ,set {@link #MUSIC_DEFAULT} to true, or set to false
     */
    public static boolean MUSIC_DEFAULT = true;

    /**
     * PRODUCT
     */
    public static final String PRODUCT_UFIT = "ufit";
    public static final String PRODUCT_OTHERS = "others";

    /**
     * W337B only support whatsapp and skype
     */
    public final static String KEY_13_PACKAGE = "com.tencent.mobileqq";///qq
    public final static String KEY_14_PACKAGE = "com.tencent.mm";///wechat
    public final static String KEY_15_PACKAGE = "com.skype.raider";///skype
    public final static String KEY_15_PACKAGE_1 = "com.skype.polaris";///skype
    public final static String KEY_15_PACKAGE_2 = "com.skype.rover";///skype
    public final static String KEY_16_PACKAGE = "com.facebook.katana";//facebook
    public final static String KEY_17_PACKAGE = "com.twitter.android";//twitter
    public final static String KEY_18_PACKAGE = "com.linkedin.android";//linkin
    public final static String KEY_19_PACKAGE = "com.instagram.android";//instagram
    public final static String KEY_1A_PACKAGE = "life.inovatyon.ds";
    public final static String KEY_1B_PACKAGE = "com.whatsapp";
    /**
     * add in fireware that version is 89 or up
     */
    public final static String KEY_1C_PACKAGE = "com.facebook.orca";

    /**
     * LocalBroadcastManager.getInstance(context).sendBroadcast() to send broadcast,so you
     * must register broadcastReceiver by LocalBroadcastManager if you need get these info of device
     */
    public final static String ACTION_QUERY_SEDENTARY = "com.isport.isportlibrary.Constants.ACTION_QUERY_SEDENTARY";
    /**
     * the extra is a list of sedentary
     */
    public final static String EXTRA_QUERY_SEDENTARY = "com.isport.isportlibrary.Constants.EXTRA_QUERY_SEDENTARY";

    public final static String ACTION_QUERY_ALARM = "com.isport.isportlibrary.Constants.ACTION_QUERY_ALARM";
    /**
     * the extra is a list of alarm
     */
    public final static String EXTRA_QUERY_ALARM = "com.isport.isportlibrary.Constants.EXTRA_QUERY_ALARM";

    public final static String ACTION_QUERY_DISPLAY = "com.isport.isportlibrary.Constants.ACTION_QUERY_DISPLAY";
    /**
     * the extra is the info of display
     */
    public final static String EXTRA_QUERY_DISPLAY = "com.isport.isportlibrary.Constants.EXTRA_QUERY_DISPLAY";

    public final static String ACTION_QUERY_SLEEP = "com.isport.isportlibrary.Constants.ACTION_QUERY_SLEEP";
    /**
     * the extra is the info of sleep
     */
    public final static String EXTRA_QUERY_SLEEP = "com.isport.isportlibrary.Constants.EXTRA_QUERY_SLEEP";

    public final static String ACTION_QUERY_TIMING_HEART_DETECT = "com.isport.isportlibrary.Constants" +
            ".ACTION_QUERY_TIMING_HEART_DETECT";
    /**
     * the extra is the info of timing heart detect
     */
    public final static String EXTRA_QUERY_TIMING_HEART_DETECT = "com.isport.isportlibrary.Constants" +
            ".EXTRA_QUERY_TIMING_HEART_DETECT";
}
