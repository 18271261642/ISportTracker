package com.isport.isportlibrary.managers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.Telephony;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import static com.isport.isportlibrary.tools.Constants.IS_DEBUG;

/**
 * @author Created by Marcos Cheng on 2017/9/8.
 * <p>
 * if you want to observer sms , incoming call or push notification to ble device,you must instance {@link #IsportManager} and call {@link #init(Context)} to init
 */

public class IsportManager {

    private String TAG = "IsportManager";
    private Context mContext;
    private static IsportManager instance;
    private OnIsportManagerListener onIsportManagerListener;
    private boolean hadRegSMS = false;
    private boolean hasRegCall = false;

    private IsportManager() {

    }

    /**
     * get instance of IsportManager
     *
     * @return
     */
    public static IsportManager getInstance() {
        if (instance == null) {
            synchronized (IsportManager.class) {
                if (instance == null) {
                    instance = new IsportManager();
                }
            }
        }
        return instance;
    }


    public void setOnIsportManagerListener(OnIsportManagerListener listener, Context context) {
        this.onIsportManagerListener = listener;
        this.mContext = context.getApplicationContext();
    }

    /**
     * if need push notification to ble device, must call the metho
     *
     * @param context
     */
    public void init(Context context) {
        this.mContext = context.getApplicationContext();
        newMmsContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            public void onChange(boolean selfChange) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (IS_DEBUG)
                            Log.i(TAG, "mNewSmsCount" + "hao");
                        int mNewSmsCount = getNewSmsCount() + getNewMmsCount();
                        sendUnreadSmsCount(mNewSmsCount);
                        if (IS_DEBUG)
                            Log.e(TAG, "mNewSmsCount>>>" + mNewSmsCount);
                    }
                }).start();

            }
        };

        newCallContentObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        readMissCall();
                    }
                }).start();
            }
        };
        registerObserver();
    }

    public Context getIsportManagerContext() {
        return this.mContext;
    }

    /**
     * if you have not requeset the permissions that is READ_CALL_LOG and WRITE_CALL_LOG,
     * you need to call it when you grant the permissions.
     * if you do not need the function that to read missed call,ignore please.
     */
    public void registerObserver() {
        registerObserver(newMmsContentObserver);
        registerCallObserver(newCallContentObserver);
    }

    private void sendUnreadSmsCount(int count) {
        if (onIsportManagerListener != null) {
            onIsportManagerListener.sendUnreadSmsCount(count);
        }
    }

    private void sendUnReadPhoneCount(int count) {
        if (onIsportManagerListener != null) {
            onIsportManagerListener.sendUnReadPhoneCount(count);
        }
    }

    private int getNewSmsCount() {
        int result = 0;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        } else {
            Cursor csr = null;
            try {
                csr = mContext.getContentResolver().query(Uri.parse("content://sms"), null, "type = 1 and read = 0", null, null);
                if (csr != null) {
                    result = csr.getCount();
                    csr.close();
                    csr = null;
                }
            } catch (Exception e) {

            } finally {
                if (csr != null)
                    csr.close();
                if (IS_DEBUG)
                    Log.e("IsportApp", "result = " + result);
                return result;
            }

        }
    }

    private int getNewMmsCount() {
        int result = 0;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        } else {
            Cursor csr = null;
            try {
                csr = mContext.getContentResolver().query(Uri.parse("content://mms/inbox"), null, "read = 0", null, null);
                if (csr != null) {
                    result = csr.getCount();
                    csr.close();
                    csr = null;
                }
            } catch (Exception e) {

            } finally {
                if (csr != null)
                    csr.close();
                return result;
            }

        }
    }

    private int readMissCall() {
        int result = 0;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            if (IS_DEBUG)
                Log.e(TAG, "request permission:" + Manifest.permission.READ_CALL_LOG);
            return 0;
        }

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.TYPE},
                    " type=? and new=?", new String[]{CallLog.Calls.MISSED_TYPE + "", "1"}, "date desc");
            if (cursor != null) {
                result = cursor.getCount();
                //if (result != 0) {
                sendUnReadPhoneCount(result);
                //}
                if (IS_DEBUG)
                    Log.i(TAG, "mMissCallCount>>>>" + result);
                cursor.close();
                cursor = null;
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null)
                cursor.close();
            return result;
        }

    }

    private void registerObserver(ContentObserver smsObser) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mContext.getContentResolver().registerContentObserver(Telephony.Mms.Inbox.CONTENT_URI, true, smsObser);
            mContext.getContentResolver().registerContentObserver(Telephony.Sms.Inbox.CONTENT_URI, true, smsObser);
            mContext.getContentResolver().registerContentObserver(Telephony.MmsSms.CONTENT_URI, true, newMmsContentObserver);
        } else {
            mContext.getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, newMmsContentObserver);
        }
        hadRegSMS = true;
        registerCallObserver(newCallContentObserver);
    }

    /**
     * if you want get the missed call number over Android O,you must override the method,you must have the permission that
     * is Manifest.permission.READ_CALL_LOG and Manifest.permission.WRITE_CALL_LOG
     * if you need the function that is read the missed call number on device,you can call the method if you get the permissions.
     * just call the method on Android O
     *
     * @param callObser
     */
    private void registerCallObserver(ContentObserver callObser) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mContext.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callObser);
            hasRegCall = true;
        } else {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                mContext.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callObser);
                hasRegCall = true;
            }
        }
    }

    private ContentObserver newMmsContentObserver;

    private ContentObserver newCallContentObserver;

    /**
     * if you want to send Unread SMS Count or incoming call,you can implement the interface to decide whether to send to band
     */
    public interface OnIsportManagerListener {

        /**
         * send number of unread sms to ble device
         *
         * @param count the number of unread sms that will send to ble device
         */
        void sendUnreadSmsCount(int count);

        /**
         * send number of incoming call to ble device
         *
         * @param count the number of incoming call that will send to ble device
         */
        void sendUnReadPhoneCount(int count);
    }

}
