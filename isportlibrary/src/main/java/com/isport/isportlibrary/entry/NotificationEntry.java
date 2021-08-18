package com.isport.isportlibrary.entry;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by chengjiamei on 2016/8/18.
 */
public class NotificationEntry {
    private final static String CONFIG_PATH = "cjm_noti_path";
    private final static String NOTI_OPEN_NOTI = "cjm_open_noti";
    private final static String NOTI_SHOW_DETAIL = "cjm_show_detail";
    private final static String NOTI_ALLOW_SMS = "cjm_allow_sms";
    private final static String NOTI_ALLOW_APP = "cjm_allow_app";
    private final static String NOTI_ALLOW_CALL = "cjm_allow_call";

    private boolean isOpenNoti;//open notification or not
    private boolean isShowDetail;//show notification detail or not
    private boolean isAllowSMS;//is allow push sms or not
    private boolean isAllowApp;///is allow push app notification or not
    private boolean isAllowCall;///is allow push call or not
    private Context mContext;
    private SharedPreferences share;
    private SharedPreferences.Editor editor;
    private static NotificationEntry sInstance;

    private NotificationEntry(Context context) {
        share = context.getSharedPreferences(CONFIG_PATH, Context.MODE_PRIVATE);
        editor = share.edit();
    }

    public static NotificationEntry getInstance(Context context) {
        if (sInstance == null) {
            synchronized (NotificationEntry.class) {
                if (sInstance == null) {
                    sInstance = new NotificationEntry(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    public void setOpenNoti(boolean openNoti) {
        this.isOpenNoti = openNoti;
        editor.putBoolean(NOTI_OPEN_NOTI, isOpenNoti).commit();
    }

    /**
     * open notification or not
     *
     * @return true open or close
     */
    public boolean isOpenNoti() {
        return share.getBoolean(NOTI_OPEN_NOTI, true);
    }

    public void setShowDetail(boolean showDetail) {
        this.isShowDetail = showDetail;
        editor.putBoolean(NOTI_SHOW_DETAIL, showDetail).commit();
    }

    /**
     * show detail of notification or not
     *
     * @return true show or not
     */
    public boolean isShowDetail() {
        return share.getBoolean(NOTI_SHOW_DETAIL, true);
    }

    public void setAllowSMS(boolean allowSMS) {
        this.isAllowSMS = allowSMS;
        editor.putBoolean(NOTI_ALLOW_SMS, allowSMS).commit();
    }

    /**
     * allow to push sms to ble device
     *
     * @return true allow or not
     */
    public boolean isAllowSMS() {
        return share.getBoolean(NOTI_ALLOW_SMS, true);
    }

    public void setAllowApp(boolean allowApp) {
        this.isAllowApp = allowApp;
        editor.putBoolean(NOTI_ALLOW_APP, allowApp).commit();
    }

    /**
     * allow to push notification to ble device
     *
     * @return true allow or not
     */
    public boolean isAllowApp() {
        return share.getBoolean(NOTI_ALLOW_APP, true);
    }

    public void setAllowCall(boolean allowCall) {
        this.isAllowCall = allowCall;
        editor.putBoolean(NOTI_ALLOW_CALL, allowCall).commit();
    }

    /**
     * allow to push call to ble device
     *
     * @return true allow or not
     */
    public boolean isAllowCall() {
        return share.getBoolean(NOTI_ALLOW_CALL, true);
    }

    /**
     * the notification of app that it's package in the package name list, that list in the xml,{@link com.isport.isportlibrary.services.NotificationService}
     *
     * @param packageName
     * @param defaultValue
     * @return
     */
    public boolean isAllowPackage(String packageName, boolean defaultValue) {
        return share.getBoolean(packageName, defaultValue);
    }

    public void setAllowPackage(String packageName, boolean allow) {
        editor.putBoolean(packageName, allow).commit();
    }

}
