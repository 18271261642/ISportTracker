package com.isport.tracker.main.settings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.tracker.R;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.util.UtilTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Administrator on 2017/1/4.
 */

public class LogActivity extends BaseActivity implements View.OnClickListener {

    TextView tvLog;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        tvLog = (TextView) findViewById(R.id.log_tv);
        if (BaseController.logBuilder == null) {
            BaseController.logBuilder = new StringBuilder();
        }
        tvLog.setText(BaseController.logBuilder.toString());
        scrollView = (ScrollView) findViewById(R.id.log_scrollview);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void verifyPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_tv:
                finish();
                break;
            case R.id.tv_clear:

                BaseController.logBuilder = new StringBuilder();
                tvLog.setText(BaseController.logBuilder.toString());

                break;
            case R.id.btn_save_log:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "you do not access to write extenal storage", Toast.LENGTH_LONG).show();
                } else {
                    saveLog();
                }
                break;
        }
    }

    private void saveLog() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            FileOutputStream outputStream = null;
            try {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/isport/" + UtilTools.date2String(Calendar.getInstance().getTime(), "yyyyMMddHHmmss") + ".txt";
                File file = new File(path);
                if (!file.getParentFile().exists()) {
                    file.mkdirs();
                }
                if (!file.exists()) {
                    file.createNewFile();
                }
                outputStream = new FileOutputStream(file);
                if(BaseController.logBuilder == null){

                }else {
                    byte[] bs = BaseController.logBuilder.toString().getBytes();
                    outputStream.write(bs);
                    outputStream.flush();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(outputStream != null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } else {
            Toast.makeText(this, "extenal storage not mounted", Toast.LENGTH_LONG).show();
        }
    }
}
