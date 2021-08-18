package com.isport.tracker.bluetooth.call;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.NotificationEntry;
import com.isport.isportlibrary.managers.CallManager;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;

import java.nio.charset.Charset;

/**
 * @author Created by Marcos Cheng on 2016/12/30.
 * if you run on android 7.0+,CallListener maybe not work,you can use CallReceiver instead,
 * if you want compatiable you need to use both {@link CallReceiver} and {@link CallListener}
 */

public class CallReceiver extends BroadcastReceiver {

    public final static String CALL_PATH = "cjm_call_path";
    private static final String TAG = "sms";
    private boolean isHandup = false;
    private boolean isCalling = false;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            if (Build.VERSION.SDK_INT >= 24) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            MainService mainService = MainService.getInstance(context);
            NotificationEntry entry = NotificationEntry.getInstance(context);
            if (entry.isAllowCall() && mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {

                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (state != null) {
                    if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        if (isHandup && !isCalling) {
                            isHandup = false;
                        }
                        isCalling = false;
                        if (incomingNumber != null) {
                            String contactName = contactNameByNumber(context, incomingNumber);
                            Log.e(TAG, "number = " + incomingNumber + "  contactName = " + contactName);
                            sendCommand(context, state, incomingNumber, contactName);
                        }
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        isHandup = true;
                        if (incomingNumber != null) {
                            String contactName = contactNameByNumber(context, incomingNumber);
                            Log.e(TAG, "number = " + incomingNumber + "  contactName = " + contactName);
                            sendCommand(context, state, incomingNumber, contactName);
                        }
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        if (isHandup) {
                            isCalling = true;
                        }
                        if (incomingNumber != null) {
                            String contactName = contactNameByNumber(context, incomingNumber);
                            Log.e(TAG, "number = " + incomingNumber + "  contactName = " + contactName);
                            sendCommand(context, state, incomingNumber, contactName);
                        }
                    }
                }
            }
        }
    }


    String mac;
    String name;
    ConfigHelper helper;

    public void sendCommand(Context context, String state, String incomingNumber, String contactName) {
        MainService mainService = MainService.getInstance(context);
        if (mainService != null && mainService.getCurrentDevice() != null) {
            mac = mainService.getCurrentDevice().getMac();
            name = mainService.getCurrentDevice().getName();
        } else {
            mac = "";
        }

        if (name.contains("W520")) {
            helper = ConfigHelper.getInstance(context);
            if (helper.getBoolean(Constants.IS_CALL + mac, true)) {
                CallManager.getInstance(context).handleCall(context, state, incomingNumber, contactName);
            }
        } else {
            CallManager.getInstance(context).handleCall(context, state, incomingNumber, contactName);
        }
    }

    /**
     * parser number to bytes for W337B
     *
     * @param type    1 new call and remind??2 miss call??3 listen call??4 hook call
     * @param numName
     * @return
     */
    public static byte[] parser337BNumberNameToByte(int type, String numName) {
        int ypType = 3;
        switch (type) {
            case 0:
                ypType = 3;
                break;
            case 1:
                ypType = 1;
                break;
            case 2:
                ypType = 3;
                break;
        }
        if (numName == null || numName.length() == 0)
            return null;
        byte[] values = new byte[20];
        values[0] = (byte) (ypType == 1 ? 0x01 : 0x00);
        values[1] = (byte) (ypType == 2 ? 0x01 : 0x00);
        byte[] bs = numName.getBytes(Charset.forName("UTF-8"));
        if (bs.length > 18) {
            byte[] tp = new byte[18];
            System.arraycopy(bs, 0, tp, 0, 18);
            bs = tp;
        }
        System.arraycopy(bs, 0, values, 2, bs.length);
        return values;
    }

    //query contact name
    public String contactNameByNumber(Context context, String number) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        //String number = "18052369652";
        Cursor cursor = null;
        String name = "";
        try {

            Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
            ContentResolver resolver = context.getContentResolver();
            cursor = resolver.query(uri, new String[]{"display_name"}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    name = cursor.getString(0);
                    Log.i(TAG, name);
                    return name;
                }
                cursor.close();
                cursor = null;
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return name;
    }
}
