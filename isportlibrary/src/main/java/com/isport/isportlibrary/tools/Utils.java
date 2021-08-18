package com.isport.isportlibrary.tools;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.KeyEvent;

import androidx.core.app.ActivityCompat;

import com.android.internal.telephony.ITelephony;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.ContentValues.TAG;

/**
 * @author Created by Marcos Cheng on 2016/8/27.
 */
public class Utils {


   /* public static void sendEndCall(Context mCt, KeyEvent paramKeyEvent) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        rejectCall();
      *//*  if (currentapiVersion > Build.VERSION_CODES.KITKAT) {
            rejectCall();
        } else {*//*
        *//*Intent mintent = new Intent(Intent.ACTION_MEDIA_BUTTON);

        //按下音量
        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK);
        mintent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
        // 通话权限 同意程序拨打电话， 替换系统的拨号器界面
        mCt.sendOrderedBroadcast(mintent, "android.permission.CALL_PRIVILEGED");

        mintent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
        mintent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);

        mCt.sendOrderedBroadcast(mintent, "android.permission.CALL_PRIVILEGED");*//*

    }*/


    public static void sendEndCall(Context mCt, KeyEvent paramKeyEvent) {
        if (ActivityCompat.checkSelfPermission(mCt, Manifest.permission.CALL_PHONE) != PackageManager
                .PERMISSION_GRANTED) {
            Log.e("sendEndCall","crpPhoneOperationListener sendEndCall no CALL_PHONE ANSWER_PHONE_CALLS");
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                if (ActivityCompat.checkSelfPermission(mCt, Manifest.permission.CALL_PHONE) != PackageManager
                        .PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mCt, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager
                        .PERMISSION_GRANTED) {
                    Log.e("sendEndCall","crpPhoneOperationListener sendEndCall no CALL_PHONE ANSWER_PHONE_CALLS");
                    return;
                }
               /* TelecomManager telecomManager = (TelecomManager) mCt.getSystemService(Context.TELECOM_SERVICE);
                telecomManager.endCall();*/
                TelecomManager tm = (TelecomManager) mCt.getSystemService(Context.TELECOM_SERVICE);
                // Logger.myLog("sendEndCall success" + tm);
                if (tm != null) {
                    boolean success = tm.endCall();

                    //Logger.myLog("sendEndCall success" + success);
                    // success == true if call was terminated.
                }

            } else {
                rejectCall();
            }

            // rejectCall();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void rejectCall() {
        try {
            Method method = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
            ITelephony telephony = ITelephony.Stub.asInterface(binder);
            telephony.endCall();
        } catch (NoSuchMethodException e) {
            Log.d(TAG, "", e);
            Log.e("utils", e.toString());
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "", e);
            Log.e("utils", e.toString());
        } catch (Exception e) {
            Log.d(TAG, "", e);
            Log.e("utils", e.toString());
        }
    }

    /**
     * @param
     * @return
     */
    /*private static ITelephony getITelephony(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context
                .getSystemService(TELEPHONY_SERVICE);
        Class<TelephonyManager> c = TelephonyManager.class;
        Method getITelephonyMethod = null;
        try {
            getITelephonyMethod = c.getDeclaredMethod("getITelephony",
                    (Class[]) null); // 获取声明的方法
            getITelephonyMethod.setAccessible(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            ITelephony iTelephony = (ITelephony) getITelephonyMethod.invoke(
                    mTelephonyManager, (Object[]) null); // 获取实例
            return iTelephony;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iTelephony;
    }*/
    public static void sendMusicKey(Context mCt, KeyEvent paramKeyEvent) {
        //broadcastMediaIntent(mCt, paramKeyEvent);
        //@alex this is control music play


        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < Build.VERSION_CODES.KITKAT) {
            try {

                // Get binder from ServiceManager.checkService(String)
                IBinder iBinder = (IBinder) Class.forName("android.os.ServiceManager")
                        .getDeclaredMethod("checkService", String.class)
                        .invoke(null, Context.AUDIO_SERVICE);

                // get audioService from IAudioService.Stub.asInterface(IBinder)
                Object audioService = Class.forName("android.media.IAudioService$Stub")
                        .getDeclaredMethod("asInterface", IBinder.class)
                        .invoke(null, iBinder);

                // Dispatch keyEvent using IAudioService.dispatchMediaKeyEvent(KeyEvent)
                Class.forName("android.media.IAudioService")
                        .getDeclaredMethod("dispatchMediaKeyEvent", KeyEvent.class)
                        .invoke(audioService, paramKeyEvent);

            } catch (Exception e1) {
                e1.printStackTrace();
            }
            /*IAudioService localIAudioService = IAudioService.Stub
                    .asInterface(ServiceManager.getService(Context.AUDIO_SERVICE));
            if (localIAudioService != null) {
                try {
                    localIAudioService.dispatchMediaKeyEvent(paramKeyEvent);
                    return;
                } catch (Exception localException) {

                }
            }*/
        } else {
            AudioManager am = (AudioManager) mCt.getSystemService(Context.AUDIO_SERVICE);
            am.dispatchMediaKeyEvent(paramKeyEvent);
        }

    }

    public static void broadcastMediaIntent(Context context, KeyEvent paramKeyEvent) {

        long eventtime = SystemClock.uptimeMillis();

        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);

        KeyEvent downEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);

        KeyEvent upEvent = new KeyEvent(eventtime, eventtime,
                KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT, 0);

        downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        context.sendOrderedBroadcast(downIntent, null);

        upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        context.sendOrderedBroadcast(upIntent, null);

    }

    /**
     * @param hour
     * @param minute
     * @param subMinute
     * @return
     */
    public static String getTimeBySubMinute(int hour, int minute, int subMinute) {
        String time = null;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.add(Calendar.MINUTE, -subMinute);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        time = sdf.format(c.getTime());
        return time;
    }

    /**
     * @param data
     * @return
     */
    public static int lBytesToInt(byte[] data) {
        int value = 0;
        if (data != null && data.length > 0) {
            int len = data.length - 1;
            for (int i = 0; i <= len; i++) {
                value += ((data[i] & 0x00ff) << (8 * i));
            }
        }
        return value;
    }

    public static int byte2Int(byte bt) {
        return bt & 0x000000ff;
    }

    public static int calueAge(String birthday, String format) {
        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        int curMonth = calendar.get(Calendar.MONTH);
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTime(DateUtil.stringToDate(birthday, format));
        int birYear = calendar.get(Calendar.YEAR);
        int birMonth = calendar.get(Calendar.MONTH);
        int birDay = calendar.get(Calendar.DAY_OF_MONTH);
        if (curYear <= birYear) {
            return 0;
        }
        int dy = curDay - birDay;
        if (dy == 0) {
            return 0;
        } else {
            if (curMonth < birMonth || (curMonth == birMonth && curDay < birDay)) {
                return dy - 1;
            } else {
                return dy;
            }
        }
    }

    public static boolean isGB2312(String str) {
        for (int i = 0; i < str.length(); i++) {
            String bb = str.substring(i, i + 1);
// 生成一个Pattern,同时编译一个正则表达式,其中的u4E00("一"的unicode编码)-\u9FA5("龥"的unicode编码)
            boolean cc = java.util.regex.Pattern.matches("[\u4E00-\u9FA5]", bb);
            if (cc == false) {
                return cc;
            }
        }
        return true;
    }

    public static byte[] addFF(byte[] bt, int start, int end) {
        for (int i = start; i <= end; i++) {
            bt[i] = (byte) 0xff;
        }
        return bt;
    }
}
