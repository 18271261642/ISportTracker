package com.isport.tracker.bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.isport.tracker.R;
import com.isport.tracker.main.MainActivityGroup;

import java.util.List;

/**
 * Created by feige on 2017/6/13.
 */

@TargetApi(26)
public class MyJobService extends JobService {
    private String TAG = "MyJobService";
    public static int kJobId = 0;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();
        setForegroud();
    }

    private void setForegroud() {


        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INSTANT_APP_FOREGROUND_SERVICE) != PackageManager
                    .PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
                    Intent nfIntent = new Intent(this, MainActivityGroup.class);
                    builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置 PendingIntent
                            .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.logo_isport)) // 设置下拉列表中的图标(大图标)
                            .setContentTitle(this.getString(R.string.app_name)) // 设置下拉列表里的标题
                            .setSmallIcon(R.drawable.logo_isport) // 设置状态栏内的小图标
                            .setContentText("") // 设置上下文内容
                            .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间


                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//修改安卓8.1以上系统报错
                        NotificationChannel notificationChannel = new NotificationChannel("notification_id", "bonlala", NotificationManager.IMPORTANCE_MIN);
                        notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
                        notificationChannel.setShowBadge(false);//是否显示角标
                        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
                        NotificationManager manager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
                        manager.createNotificationChannel(notificationChannel);
                        builder.setChannelId("notification_id");
                    }

                    Notification notification = builder.build(); // 获取构建好的Notification
                    notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
                    // 参数一：唯一的通知标识；参数二：通知消息。
                    this.startForeground(112, notification);// 开始前台服务
                }

            } else {

            }

        } catch (Exception e) {

        }



       /* NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification notif = builder.build();
        startForeground(112,notif);*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        setForegroud();
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.e(TAG, "onStartJob");
        boolean isLocalServiceWork = isServiceWork(this, MainService.class.getName());
        boolean isJobServiceWork = isServiceWork(this, MyJobService.class.getName());
        if (!isLocalServiceWork) {
            this.startForegroundService(new Intent(this, MainService.class));
        }
        if (!isJobServiceWork) {
            this.startForegroundService(new Intent(this, MyJobService.class));
        }
        boolean isInForeground = serviceIsRunningInForeground(this, MyJobService.class.getName());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e(TAG, "onStopJob");
        scheduleJob(MyJobService.this, getJobInfo(MyJobService.this, 10000));
        return false;
    }


    //将任务作业发送到作业调度中去
    public static void scheduleJob(Context context, JobInfo t) {
        Log.i("castiel", "调度job");
        JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(t);
    }

    public static JobInfo getJobInfo(Context context, long time) {
        JobInfo.Builder builder = new JobInfo.Builder(kJobId++, new ComponentName(context, MyJobService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        builder.setPersisted(true);
        builder.setRequiresCharging(false);
        builder.setRequiresDeviceIdle(false);
        //间隔100毫秒
        builder.setMinimumLatency(time);
        return builder.build();
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public static boolean serviceIsRunningInForeground(Context context, String name) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (name.equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    // 判断服务是否正在运行
    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
