package com.isport.tracker;

import android.content.Context;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.CallEntry;
import com.isport.isportlibrary.managers.CallManager;
import com.isport.isportlibrary.managers.IsportManager;
import com.isport.isportlibrary.managers.NotiManager;
import com.isport.tracker.bluetooth.BootReceive;
import com.isport.tracker.bluetooth.HamaSmsListener;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.bluetooth.call.CallListener;
import com.isport.tracker.bluetooth.notifications.NotiServiceListener;
import com.isport.tracker.crash.CrashHandler;
import com.isport.tracker.util.Constants;
//import com.pgyersdk.crash.PgyCrashManager;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApp extends MultiDexApplication implements IsportManager.OnIsportManagerListener {

    private String TAG = "MyApp";

    private static MyApp sInstance;

//    public static boolean canReflesh=true;

    public static MyApp getInstance() {
        return sInstance;
    }

    public ExecutorService executorService = Executors.newFixedThreadPool(8);

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        sInstance = this;
        super.onCreate();
        if (!BootReceive.isServiceStart(this, MainService.class.getName())) {
            MainService.getInstance(this);
        }

        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(new HamaSmsListener(), filter);

        /***************** if you want to incoming reminder ******************/
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        CallListener customPhoneListener = new CallListener(this);
        telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        /********************************/
        MainService.getInstance(this);

//        IsportManager.getInstance().init(this);
        IsportManager.getInstance().setOnIsportManagerListener(this, this);

        NotiManager.getInstance(this);

        if (NotiServiceListener.isEnabled(this)) {
            NotiServiceListener.ensureCollectorRunning(this);
        }
        CrashReport.initCrashReport(getApplicationContext(), "068397274a", false);
        if (!Constants.IS_GOOGLE_PLAY) {
//            PgyCrashManager.register(this);
        }
        /*if (BuildConfig.DEBUG) {
            Log.e(TAG, "is Debug");
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return;
            } else {
                LeakCanary.install(this);
            }
        } else {
            Log.e(TAG, "release");
        }*/

        CrashHandler.getInstance().init(this);

        CallManager.getInstance(this).setOnCallManagerListener(new CallManager.OnCallManagerListener() {
            @Override
            public void sendCommingPhoneNumber(Context context, CallEntry callEntry) {
                MainService mainService = MainService.getInstance(context);
                if (mainService != null) {
                    BaseDevice baseDevice = mainService.getCurrentDevice();
                    if (baseDevice != null && callEntry.getType() == CallManager.CALL_STATE_RINGING) {
                        if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ACTIVA_T) && (baseDevice.getDeviceType() ==
                                BaseDevice.TYPE_W307S || baseDevice.getDeviceType() ==
                                BaseDevice.TYPE_W307S_SPACE)) {
                            Log.e(TAG, "不发送信息");
                            return;
                        }
                        if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W311 || baseDevice.getProfileType
                                () == BaseController.CMD_TYPE_NMC) {
                            Log.e(TAG, "W311发送信息");
                            mainService.sendCommingPhoneNumber(callEntry);
                        } else if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W337B) {
                            mainService.sendCommingPhoneNumber(callEntry);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (!Constants.IS_GOOGLE_PLAY) {
//            PgyCrashManager.unregister();
        }
    }


    @Override
    public void sendUnreadSmsCount(int count) {
        if (MainService.getInstance(this) != null) {
            MainService.getInstance(this).sendUnreadSmsCount(count);
        }
    }

    @Override
    public void sendUnReadPhoneCount(int count) {
        if (MainService.getInstance(this) != null) {
            MainService.getInstance(this).sendUnreadPhoneCount(count);
        }
    }
}
