package com.isport.tracker.bluetooth.notifications;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.isport.isportlibrary.managers.NotiManager;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Administrator on 2017/9/27.
 */

public class NotiServiceListener extends NotificationListenerService {

    public static Vector<StatusBarNotification> msgVector = new Vector<>();
    public static final String TAG = NotiServiceListener.class.getSimpleName();
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    boolean isFirst = true;
    boolean canSend = false;
    long startTime;
    //            05-16 14:26:23.650 5800-5800/com.isport.tracker E/NotiManager: 2018-05-16 14:26:23
//            05-16 14:26:23.650 5800-5800/com.isport.tracker E/NotiManager: ***isKitKat***true***text***‪+86 177 2047 9861‬: Hhh***title***WhatsApp***tickerText***null***type***27
//            05-16 14:26:23.650 5800-5800/com.isport.tracker E/NotiManager: **content的长度小于15**
//            05-16 14:26:23.650 5800-5800/com.isport.tracker E/NotiManager: **title的长度小于15**
//            05-16 14:26:23.650 5800-5800/com.isport.tracker E/com.isport.isportlibrary.controller.CmdController: ***NotiManager.msgVector长度***0
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    isFirst = true;
                    if (msgVector.size() >= 1)
                        sendMsg(msgVector.get(msgVector.size() - 1));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //记住当前第一条的时间戳，然后按此时间戳100ms内进来的消息覆盖msgVector中的消息,忽略100ms内发发送的信息，只取最后一条

        if (isFirst) {
            isFirst = false;
            msgVector.add(sbn);
//            startTime=System.currentTimeMillis();
            mHandler.sendEmptyMessageDelayed(0x01, 100);
            //设置一个监听，如果100ms内没有消息来就直接发送
        } else {
            if (msgVector.size() >= 1) {
                msgVector.clear();
            }
            msgVector.add(sbn);
        }
        //收到消息先做到list存储，在然后做成一个循环，
//        super.onNotificationPosted(sbn);
//        if (!"com.tencent.mobileqq".equals(sbn.getPackageName())) {
//            return;
//        }
    }

    ConfigHelper helper;
    String mac;
    String name;

    private void sendMsg(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if (notification == null) {
            return;
        }
        // 当 API > 18 时，使用 extras 获取通知的详细信息
        //因为一次会有多条，应该都是分割的，那么在同时来多条的情况下，应该展示最新的那条
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Bundle extras = notification.extras;
            if (extras != null) {
                // 获取通知标题
                String title = extras.getString(Notification.EXTRA_TITLE, "");
                // 获取通知内容
                String content = extras.getString(Notification.EXTRA_TEXT, "");
                Log.e(TAG, title + "***KK***" + content);
                if (!TextUtils.isEmpty(content) && content.contains("Hello")) {
                    Log.e(TAG, "***API > 18***" + content);
                }
            }

        } else {
            // 当 API = 18 时，利用反射获取内容字段
            List<String> textList = getText(notification);
            Log.e(TAG, "***KK***" + textList.toString());
            if (textList != null && textList.size() > 0) {
                for (String text : textList) {
                    if (!TextUtils.isEmpty(text) && text.contains("Hello")) {
                        Log.e(TAG, "***API = 18***" + text);
                        break;
                    }
                }
            }
        }

        NotiManager.getInstance(this).handleNotification(sbn.getPackageName(), sbn.getNotification());
    }

    public List<String> getText(Notification notification) {
        if (null == notification) {
            return null;
        }
        RemoteViews views = notification.bigContentView;
        if (views == null) {
            views = notification.contentView;
        }
        if (views == null) {
            return null;
        }
        // Use reflection to examine the m_actions member of the given RemoteViews object.
        // It's not pretty, but it works.
        List<String> text = new ArrayList<>();
        try {
            Field field = views.getClass().getDeclaredField("mActions");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);
            // Find the setText() and setTime() reflection actions
            for (Parcelable p : actions) {
                Parcel parcel = Parcel.obtain();
                p.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);
                // The tag tells which type of action it is (2 is ReflectionAction, from the source)
                int tag = parcel.readInt();
                if (tag != 2) continue;
                // View ID
                parcel.readInt();
                String methodName = parcel.readString();
                if (null == methodName) {
                    continue;
                } else if (methodName.equals("setText")) {
                    // Parameter type (10 = Character Sequence)
                    parcel.readInt();
                    // Store the actual string
                    String t = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim();
                    text.add(t);
                }
                parcel.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    public static boolean isEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void toggleNotificationListenerService(Context context) {
        Log.e(TAG, "***去重启***");
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, NotiServiceListener.class), PackageManager
                .COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(context, NotiServiceListener.class), PackageManager
                .COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    //确认NotificationMonitor是否开启
    public static void ensureCollectorRunning(Context context) {
        boolean collectorRunning = false;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos = manager.getRunningServices(250);
        if (infos != null && infos.size() > 0) {
            for (int i = 0; i < infos.size(); i++) {
                ActivityManager.RunningServiceInfo info = infos.get(i);
                if (info.service.getClassName().equals(NotiServiceListener.class.getName())) {
                    collectorRunning = true;
                }
            }
        }
        collectorRunning = false;
        if (collectorRunning) {
            return;
        }
        toggleNotificationListenerService(context);
    }
}
