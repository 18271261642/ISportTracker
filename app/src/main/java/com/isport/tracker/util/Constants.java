package com.isport.tracker.util;

import java.util.Date;

/**
 * Created by Administrator on 2016/10/15.
 */

public class Constants {

    public static final boolean IS_GOOGLE_PLAY = false;//是否是google play版
    public static final boolean IS_FACTORY_VERSION = false;///是否是 工廠使用版本

    public static final String INIT_DATE_STR = "2016-01-01";
    public static final Date INIT_DATE = UtilTools.string2Date(INIT_DATE_STR, "yyyy-MM-dd");
    public static final String SAVEUSERIMAGE = "/avatar.jpg";//头像保存地址
    public static final String LIDL_CONFIG = "vivitar_config";

    public static final String CONFIG_PATH = "vivitar_config_path";

    //public static final Typeface FONT_TYPE = new XmlParserUtil().initTypeFace("fonts/Gotham-Bold.otf");
    public static final String CLIENT_ISPORT = "isport";

    public static final String INFO_CURRENT_DEVICE = "info_current_device";///保存当前连接设备信息
    public static final String W285S_COUNTDOWN = "w285s_countdown";///倒计时
    public static final String IS_AUTO_SAVE_HEART = "is_auto_save_heart";
    public static final String IS_NOTIF_SAVE_HEART = "is_notif_save_heart";
    public static final String IS_HEART_AUTODOWN = "is_heart_autodown";
    public static final String IS_RAISEHAND = "is_raiseHand";
    public static final String IS_POINTER_DIAL = "is_esb_pointer_dial";
    public static final String IS_SPORT_MODE = "is_esb_sport_mode";
    public static final String SPORT_MODE_VALUE = "isport_mode_value";
    public static final String IS_CALL = "is_call";
    public static final String IS_MESSAGE = "is_message";
    public static final String IS_qq = "is_qq";
    public static final String IS_wechat = "is_wechat";
    public static final String IS_facebook = "is_facebook";
    public static final String IS_skye = "is_skye";
    public static final String IS_twitter = "is_twitter";
    public static final String IS_instagram = "is_instagram";
    public static final String IS_linkedin = "is_linkedin";
    public static final String IS_WhatsApp = "is_WhatsApp";
    public static final String IS_THRID = "is_third";
    public static final String IS_RAISEHAND_ALLDAY = "IS_RAISEHAND_allday";
    public static final String IS_AUTOMATICHEARTRATE = "IS_AutomaticHeartRate";
    public static final String IS_AUTOMATICHEARTRATE_TIME = "IS_AUTOMATICHEARTRATE_TIME";
    public static final String IS_RAISEHAND_ALLOFF = "IS_RAISEHAND_alloff";
    public static final String IS_RAISEHAND_ONLYSLEEPMODE = "IS_RAISEHAND_onlysleepmode";
    public static final String IS_HEARTRATE = "is_heartRate";
    public static final String IS906901 = "IS906901";//固件版本90.69.01

    public static final String DEVIE_P674A = "P674A";
    public static final String BLE_FILTER_TRACKER = "tracker";
    public static final String BLE_FILTER_REDBULL = "redbull";

    public static final String PRODUCT_TRACKER = "tracker";
    public static final String PRODUCT_FITNESS_TRACKER_PRO = "fitness_tracker_pro";
    public static final String PRODUCT_HU_TRACKER = "hu_tracker";
    public static final String PRODUCT_REFLEX = "reflexw";
    public static final String PRODUCT_ACTIVA_T = "activa_t";
    public static final String PRODUCT_ETEK = "etek";
    public static final String PRODUCT_ENERGETICS = "energetics";


    //ExtraStrng
    public static final String EXTRA_GOUSERINFO = "gouserinfo";
}
