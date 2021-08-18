package com.isport.isportlibrary.managers;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.isport.isportlibrary.entry.CallEntry;

/**
 * Created by Administrator on 2017/9/28.
 */

public class CallManager {

    private String TAG = "CallManager";

    public final static int CALL_STATE_IDLE = 0;
    public final static int CALL_STATE_RINGING = 1;
    public final static int CALL_STATE_OFFHOOK = 2;

    private Context mContext;
    private static CallManager sInstance;
    private OnCallManagerListener callManagerListener;

    private CallManager(Context context) {
        mContext = context;
    }

    public static CallManager getInstance(Context context) {
        if(sInstance == null) {
            synchronized (CallManager.class) {
                if(sInstance == null) {
                    sInstance = new CallManager(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * if there are call coming, call the method and it will be send to ble device if you had called {@link CallManager#setOnCallManagerListener(OnCallManagerListener)}
     * @param context
     * @param state call state, see {@link TelephonyManager#EXTRA_STATE_IDLE},{@link TelephonyManager#EXTRA_STATE_OFFHOOK},{@link TelephonyManager#EXTRA_STATE_RINGING}
     * @param phonenumber phone number
     * @param contactname contact name
     */
    public void handleCall(Context context, String state, String phonenumber, String contactname){
        if(state != null && phonenumber != null) {
            if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                handleCall(context, CALL_STATE_IDLE, phonenumber, contactname == null?"":contactname);
            }else if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                handleCall(context, CALL_STATE_OFFHOOK, phonenumber, contactname == null?"":contactname);
            }else if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                handleCall(context, CALL_STATE_RINGING, phonenumber, contactname == null?"":contactname);
            }
        }
    }

    /**
     * if there are call coming, call the method and it will be send to ble device if you had called {@link CallManager#setOnCallManagerListener(OnCallManagerListener)}
     * @param context
     * @param state the state of call, see {@link #CALL_STATE_IDLE} ,{@link #CALL_STATE_OFFHOOK} ,{@link #CALL_STATE_RINGING}
     * @param phonenumber phone number
     * @param contactname contact name
     */
    public void handleCall(Context context, int state, String phonenumber, String contactname) {
        sendCommingPhoneNumber(context, new CallEntry(state, phonenumber, contactname));
    }

    /**
     *
     * @param context
     * @param callEntry
     */
    private void sendCommingPhoneNumber(Context context,CallEntry callEntry) {
        if(callManagerListener != null) {
            this.callManagerListener.sendCommingPhoneNumber(context, callEntry);
        }
    }

    /**
     *
     * @param listener the OnCallManagerListener will be set, to see {@link OnCallManagerListener}
     */
    public void setOnCallManagerListener(OnCallManagerListener listener) {
        this.callManagerListener = listener;
    }

    /**
     * need implement it if need send phone number or contact name to ble devive
     */
    public interface OnCallManagerListener {
        /**
         *
         * @param context
         * @param callEntry the call info need to send to ble device
         */
        void sendCommingPhoneNumber(Context context,CallEntry callEntry);
    }
}
