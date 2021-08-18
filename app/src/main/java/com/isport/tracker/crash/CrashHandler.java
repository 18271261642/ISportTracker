package com.isport.tracker.crash;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import androidx.core.app.ActivityCompat;

import com.isport.isportlibrary.tools.DateUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

/**
 * Created by Administrator on 2017/8/17.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler instance = new CrashHandler();
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;

    private CrashHandler(){

    }

    public static CrashHandler getInstance(){
        return instance;
    }

    public void init(Context context){
        mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            dumpExceptionToSDCard(e);
            uploadExceptionToServer(e);
        }catch (Exception e1) {
            e1.printStackTrace();
        }
        e.printStackTrace();
        ///if has default crach handler
        if(mDefaultExceptionHandler != null) {
            mDefaultExceptionHandler.uncaughtException(t, e);
        }else {
            //Process.killProcess(Process.myPid());
        }
    }

    private StringBuilder getPhoneInfo() {
        StringBuilder builder = new StringBuilder();
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            Calendar calendar = Calendar.getInstance();
            builder.append(DateUtil.dataToString(calendar.getTime(), "yyyy/MM/dd HH:mm:ss")).append("\r\n");
            //当前版本号
            builder.append("App Version:" + pi.versionName + "_" + pi.versionCode).append("\r\n");
            //当前系统
            builder.append("OS version:" + Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT).append("\r\n");
            //制造商
            builder.append("Vendor:" + Build.MANUFACTURER).append("\r\n");
            //手机型号
            builder.append("Model:" + Build.MODEL).append("\r\n");
            //CPU架构
            builder.append("CPU ABI:" + Build.CPU_ABI).append("\r\n");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return builder;
    }

    public void dumpExceptionToSDCard(Throwable ex) throws IOException {
        if(ActivityCompat.checkSelfPermission(mContext , Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Calendar calendar = Calendar.getInstance();
                String date = DateUtil.dataToString(calendar.getTime(), "yyyyMMdd");
                String directory = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"isport/logs";
                File file = new File(directory);
                if(!file.exists()) {
                    file.mkdirs();
                }
                File logfile = new File(directory+File.separator+date+"crashlog.txt");
                if(!logfile.exists()) {
                    logfile.createNewFile();
                }
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(logfile, true)), true);
                pw.write(getPhoneInfo().toString()+"\r\n");
                ex.printStackTrace(pw);
                pw.write("\r\n\r\n");
                pw.close();
            }
        }else {

        }
    }

    public void uploadExceptionToServer(Throwable ex) {

    }
}
