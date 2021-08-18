package com.isport.tracker.main.settings;

import android.annotation.TargetApi;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.util.Constants;
//import com.pgyersdk.crash.PgyCrashManager;

//import com.joanzapata.pdfview.PDFView;
//import com.joanzapata.pdfview.listener.OnPageChangeListener;

public class AboutUsActivity extends BaseActivity implements OnClickListener {
    private TextView re_back, text_version;
    public static final String ABOUT_FILE = "aaa.pdf";


    Integer pageNumber = 1;

    @TargetApi(19)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_about_us);
        init();

        if(BuildConfig.PRODUCT.equals(Constants.PRODUCT_ENERGETICS)) {

        }else {
            if (Constants.IS_GOOGLE_PLAY) {
                findViewById(R.id.tv_checkupdate).setVisibility(View.GONE);
            } else {
//                PgyCrashManager.register(this.getApplicationContext());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(BuildConfig.PRODUCT.equals(Constants.PRODUCT_ENERGETICS)) {

        }else {
            if (!Constants.IS_GOOGLE_PLAY) {
//                PgyCrashManager.unregister();
            }
        }
    }

    private void init() {
        re_back = (TextView) findViewById(R.id.return_back);
        re_back.setOnClickListener(new OnClickListenerImpl());
        TextView text_link = (TextView) findViewById(R.id.text_link);


        text_link.setVisibility(View.GONE);


        text_version = (TextView) findViewById(R.id.text_version);

        PackageInfo pi;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            text_version.setText(getString(R.string.app_version) + "  " + pi.versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        if(BuildConfig.PRODUCT.equals(Constants.PRODUCT_ENERGETICS)) {

        }else {
            switch (v.getId()) {
                case R.id.tv_checkupdate:
//                    PgyUpdateManager.register(AboutUsActivity.this,
//                                              new UpdateManagerListener() {
//                                                  @Override
//                                                  public void onUpdateAvailable(final String result) {
//                                                      // 将新版本信息封装到AppBean中
//                                                      final AppBean appBean = getAppBeanFromString(result);
//                                                      new android.support.v7.app.AlertDialog.Builder(AboutUsActivity.this)
//                                                              .setTitle(UIUtils.getString(R.string.update_version_ok))
//                                                              .setMessage(UIUtils.getString(R.string.app_version) + ": " +
//                                                                                  appBean.getVersionName())
//                                                              .setNegativeButton(
//                                                                      UIUtils.getString(R.string.confirm),
//                                                                      new DialogInterface.OnClickListener() {
//
//                                                                          @Override
//                                                                          public void onClick(
//                                                                                  DialogInterface dialog,
//                                                                                  int which) {
//                                                                              startDownloadTask(
//                                                                                      AboutUsActivity.this,
//                                                                                      appBean.getDownloadURL());
//                                                                          }
//                                                                      }).show();
//                                                  }
//
//                                                  @Override
//                                                  public void onNoUpdateAvailable() {
//                                                      Toast.makeText(AboutUsActivity.this,R.string.update_version_loast,Toast.LENGTH_SHORT).show();
//                                                  }
//                                              });
                    break;
            }
        }
    }

    private class OnClickListenerImpl implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.return_back:
                    AboutUsActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}