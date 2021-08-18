package com.isport.tracker.bluetooth;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.List;

/**
 * Created by Administrator on 2016/9/14.
 */
public class BootReceive extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("BootReceive", "action = " + intent.getAction());
        final Context ctx = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final PendingResult result = goAsync();
            AsyncHandler.post(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    if (!isServiceStart(ctx, MyJobService.class.getName())) {
                        Intent intent1 = new Intent(ctx, MyJobService.class);
                        ctx.startForegroundService(intent1);
                    }
                    result.finish();
                }
            });
        } else {
            MainService.getInstance(context);
        }
    }

    public static boolean isServiceStart(Context context, String classname) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos = manager.getRunningServices(250);
        if (infos != null && infos.size() > 0) {
            for (int i = 0; i < infos.size(); i++) {
                ActivityManager.RunningServiceInfo info = infos.get(i);
                if (context.getPackageName().equals(info.service.getPackageName()) && info.service.getClassName().equals(classname)) {
                    return true;
                }
            }
        }
        return false;
    }


}

final class AsyncHandler {
    private static final HandlerThread sHandlerThread = new HandlerThread("AsyncHandler");
    private static final Handler sHandler;

    static {
        sHandlerThread.start();
        sHandler = new Handler(sHandlerThread.getLooper());
    }

    public static void post(Runnable r) {
        sHandler.post(r);
    }

    private AsyncHandler() {
    }
}