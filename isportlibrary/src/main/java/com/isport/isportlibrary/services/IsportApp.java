package com.isport.isportlibrary.services;

import android.app.Application;

import com.isport.isportlibrary.managers.IsportManager;

/**
 * @author Created by Marcos Cheng on 2016/8/18.
 *  User {@link IsportManager} instead
 */
@Deprecated
public class IsportApp extends Application {

    /*public static String TAG = "IsportApp";
    private static IsportApp sInstance;

    public static IsportApp getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerObserver(newMmsContentObserver);
        registerCallObserver(newCallContentObserver);
        sInstance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void sendUnreadSmsCount(int count) {

    }

    public int getNewSmsCount() {
        int result = 0;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        } else {
            Cursor csr = getContentResolver().query(Uri.parse("content://sms"), null, "type = 1 and read = 0", null, null);
            if (csr != null) {
                result = csr.getCount();
                csr.close();
                csr = null;
            }
            Log.e("IsportApp", "result = " + result);
            return result;
        }
    }

    public int getNewMmsCount() {
        int result = 0;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        } else {
            Cursor csr = getContentResolver().query(Uri.parse("content://mms/inbox"), null, "read = 0", null, null);
            if (csr != null) {
                result = csr.getCount();
                csr.close();
                csr = null;
            }
            return result;
        }
    }

    public int readMissCall() {
        int result = 0;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "request permission:" + Manifest.permission.READ_CALL_LOG);
            return 0;
        }
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.TYPE},
                " type=? and new=?", new String[]{CallLog.Calls.MISSED_TYPE + "", "1"}, "date desc");
        if (cursor != null) {
            result = cursor.getCount();
            //if (result != 0) {
            sendUnReadPhoneCount(result);
            //}
            Log.i(TAG, "mMissCallCount>>>>" + result);
            cursor.close();
            cursor = null;
        }
        return result;
    }

    public void sendUnReadPhoneCount(int count) {

    }

    private void registerObserver(ContentObserver smsObser) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            getContentResolver().registerContentObserver(Telephony.Mms.Inbox.CONTENT_URI, true, smsObser);
            getContentResolver().registerContentObserver(Telephony.Sms.Inbox.CONTENT_URI, true, smsObser);
            getContentResolver().registerContentObserver(Telephony.MmsSms.CONTENT_URI, true, newMmsContentObserver);
        } else {
            getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, newMmsContentObserver);
        }
        registerCallObserver(newCallContentObserver);
    }

    *//**
     * if you want get the missed call number over Android O,you must override the method,you must have the permission that
     * is Manifest.permission.READ_CALL_LOG and Manifest.permission.WRITE_CALL_LOG
     * if you need the function that is read the missed call number on device,you can call the method if you get the permissions.
     * just call the method on Android O
     *
     * @param callObser
     *//*
    public void registerCallObserver(ContentObserver callObser) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callObser);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callObser);
            }
        }
    }

    private ContentObserver newMmsContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.i(TAG, "mNewSmsCount" + "hao");
            int mNewSmsCount = getNewSmsCount() + getNewMmsCount();
            sendUnreadSmsCount(mNewSmsCount);
            Log.e(TAG, "mNewSmsCount>>>" + mNewSmsCount);
        }
    };
    private ContentObserver newCallContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            readMissCall();
        }
    };*/
}
