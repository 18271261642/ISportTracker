package com.isport.tracker.bluetooth.call;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import androidx.core.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.NotificationEntry;
import com.isport.isportlibrary.managers.CallManager;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;

public class CallListener extends PhoneStateListener {
    public final static String CALL_PATH = "cjm_call_path";
    private static final String TAG = "sms";
    private boolean isHandup = false;
    private boolean isCalling = false;
    private Context context;

    //
    public CallListener(Context context) {
        super();
        this.context = context;
    }

    /**
     * get contact's name from database by phone number
     *
     * @param context
     * @param phonenum contact's phone number
     * @return return contact's name
     */
    public static String getContactNameByPhoneNumber(Context context, String phonenum) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        String name = "";
        Cursor cursor = null;
        try {
            String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
            cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, // Which columns to return.
                    ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + phonenum + "'", // WHERE clause.
                    null, // WHERE clause value substitution
                    null); // Sort order.

            if (cursor == null) {
                Log.d(TAG, "getPeople null");
                return null;
            }
            if (null != cursor) {

                Log.e("TGG", "cursor=========================" + cursor.getCount());
                String[] aaa = cursor.getColumnNames();
            }
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                name = cursor.getString(nameFieldColumnIndex);
                if(name != null && !name.equals("")){
                    break;
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return name;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        MainService mainService = MainService.getInstance(context.getApplicationContext());
        NotificationEntry entry = NotificationEntry.getInstance(context.getApplicationContext());
        if (entry.isAllowCall() && mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
            if (Build.VERSION.SDK_INT >= 24) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if (isHandup && !isCalling) {
                        isHandup = false;
                    }
                    isCalling = false;
                    if (incomingNumber != null) {
                        String contactName = contactNameByNumber(context, incomingNumber);
                        sendCommand(context, state, incomingNumber, contactName);
                        Log.i("sms", "CallReceiver Phone Number : " + getContactNameByPhoneNumber(context, incomingNumber));
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    isHandup = true;
                    if (incomingNumber != null) {
                        String contactName = contactNameByNumber(context, incomingNumber);
                        sendCommand(context, state, incomingNumber, contactName);
                        Log.i("sms", "CallReceiver Phone Number : " + getContactNameByPhoneNumber(context, incomingNumber));
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (incomingNumber != null) {
                        String contactName = contactNameByNumber(context, incomingNumber);
                        sendCommand(context, state, incomingNumber, contactName);
                        Log.i("sms", "CallReceiver Phone Number : " + getContactNameByPhoneNumber(context, incomingNumber));
                    }
                    if (isHandup) {
                        isCalling = true;
                    }
                    break;
            }
        }
    }

    String mac;
    String name;
    ConfigHelper helper;

    public void sendCommand(Context context, int state, String incomingNumber, String contactName) {
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


    //query contact name
    public String contactNameByNumber(Context context, String number) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        //String number = "18052369652";
        String name = "";
        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{"display_name"}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(0);
                Log.i(TAG, name);
                return name;
            }
            cursor.close();
            cursor = null;
        }
        return name;
    }
}
