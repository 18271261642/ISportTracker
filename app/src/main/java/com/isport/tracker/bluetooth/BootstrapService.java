package com.isport.tracker.bluetooth;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.isport.tracker.R;
import com.isport.tracker.main.MainActivityGroup;


/**
 * Created by feige on 2017/5/26.
 */

public class BootstrapService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(this);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    public static void startForeground(Service context) {


        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.INSTANT_APP_FOREGROUND_SERVICE) != PackageManager
                    .PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Notification.Builder builder = new Notification.Builder(context.getApplicationContext()); //获取一个Notification构造器
                    Intent nfIntent = new Intent(context, MainActivityGroup.class);
                    builder.setContentIntent(PendingIntent.getActivity(context, 0, nfIntent, 0)) // 设置 PendingIntent
                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_isport)) // 设置下拉列表中的图标(大图标)
                            .setContentTitle(context.getString(R.string.app_name)) // 设置下拉列表里的标题
                            .setSmallIcon(R.drawable.logo_isport) // 设置状态栏内的小图标
                            .setContentText("") // 设置上下文内容
                            .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间


                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//修改安卓8.1以上系统报错
                        NotificationChannel notificationChannel = new NotificationChannel("notification_id", "bonlala", NotificationManager.IMPORTANCE_MIN);
                        notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
                        notificationChannel.setShowBadge(false);//是否显示角标
                        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
                        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                        manager.createNotificationChannel(notificationChannel);
                        builder.setChannelId("notification_id");
                    }

                    Notification notification = builder.build(); // 获取构建好的Notification
                    notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
                    // 参数一：唯一的通知标识；参数二：通知消息。
                    context.startForeground(112, notification);// 开始前台服务
                }

            } else {

            }

        } catch (Exception e) {

        }



     /*   NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notif = builder
                .setContentText(context.getString(R.string.application_keep_running))
                .setContentTitle(context.getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.logo_isport)
                .build();

        //notif.contentIntent = pendingIntent;
        notif.flags |= Notification.FLAG_NO_CLEAR; // 点击清除按钮时就会清除消息通知,但是点击通知栏的通知时不会消失
        //notif.flags |= Notification.FLAG_ONGOING_EVENT;
        notif.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        //notif.contentIntent =
        context.startForeground(112, notif);*/
    }

}
