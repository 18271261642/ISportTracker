package com.isport.isportlibrary.tools;

import java.util.UUID;

/**
 * Created by Marcos on 2017/12/2.
 */

public class BleConfig {

    public static final UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static String UUID_MAIN_SERVICE = "ff00";
    public static String UUID_HEART_SERVICE= "180d";

    public static String UUID_SEND_DATA_CHAR = "ff01";
    public static String UUID_RECEIVE_DATA_CHAR = "ff02";
    public static String UUID_REALTIME_RECEIVE_DATA_CHAR = "ff04";
    public static String UUID_HEART_CHAR = "2a37";
}
