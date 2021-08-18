package com.isport.tracker.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.main.settings.UserInfoActivity;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.UtilTools;

import java.nio.charset.Charset;
import java.util.ArrayList;


public class WelcomeActivity extends BaseActivity {
    private Animation myAnimation_Alpha;
    private ImageView bg_welcome;
    private static String CONFIG_INIT = "CONFIG_INIT";///初始配置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initUI();
        setAnimation();


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent intent = null;
                if (Constants.IS_FACTORY_VERSION) {
                    if (isDeviceTypeSelected(WelcomeActivity.this)) {
                        if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_FITNESS_TRACKER_PRO)) {
                            //intent = new Intent(WelcomeActivity.this, com.isport.fitness.activity.MainActivityGroup.class);
                        } else {
                            intent = new Intent(WelcomeActivity.this, MainActivityGroup.class);
                        }
                    } else {
                        setUserInfoSet(WelcomeActivity.this, false);
                        intent = new Intent(WelcomeActivity.this, DeviceTypeActivity.class);
                    }
                } else {
                    if (isUserInfoSet(WelcomeActivity.this)) {
                        if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_FITNESS_TRACKER_PRO)) {
                            //intent = new Intent(WelcomeActivity.this, com.isport.fitness.activity.MainActivityGroup.class);
                        } else {
                            intent = new Intent(WelcomeActivity.this, MainActivityGroup.class);
                        }
                    } else {
                        intent = new Intent(WelcomeActivity.this, UserInfoActivity.class);
                        intent.putExtra(Constants.EXTRA_GOUSERINFO,true);
                    }
                }
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_alpha_enter, R.anim.activity_alpha_exit);
                WelcomeActivity.this.finish();
            }
        }).start();
    }

    public Bitmap getHeadImage(int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Bitmap bitmap = null;
        if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_HU_TRACKER)) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_welcome, options);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_welcome, options);
        }

        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        options.inSampleSize = UtilTools.calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_welcome, options);

        return bitmap;
    }

    private void initUI() {
        bg_welcome = (ImageView) findViewById(R.id.bg_welcome);
        ImageView image_welcome = (ImageView) findViewById(R.id.image_welcome);

        image_welcome.setVisibility(View.INVISIBLE);

        bg_welcome.post(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ENERGETICS)) {
                    bg_welcome.setScaleType(ImageView.ScaleType.CENTER);
                    bg_welcome.setBackgroundColor(Color.WHITE);
                    bg_welcome.setImageResource(R.drawable.background_welcome);
                } else if (!BuildConfig.PRODUCT.equals(Constants.PRODUCT_ETEK)) {
                    ImageView bw = (ImageView) findViewById(R.id.bg_welcome);
                    bw.setVisibility(View.VISIBLE);
                    BitmapDrawable drawable = new BitmapDrawable(getHeadImage(bg_welcome.getWidth(), bg_welcome.getHeight()));
                    //bg_welcome.setBackground(drawable);
                    bw.setImageResource(R.drawable.background_welcome);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myAnimation_Alpha != null && myAnimation_Alpha.hasStarted()) {
            myAnimation_Alpha.cancel();
            myAnimation_Alpha = null;
        }
    }

    private void setAnimation() {
        myAnimation_Alpha = new AlphaAnimation(0.9f, 1.0f);
        myAnimation_Alpha.setDuration(1500);
        myAnimation_Alpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {


            }
        });
        bg_welcome.startAnimation(myAnimation_Alpha);
    }

    public static boolean isFirst(Context context) {
        SharedPreferences share = context.getSharedPreferences("CONFIG_INIT", Activity.MODE_PRIVATE); // 指定操作的文件名称
        SharedPreferences.Editor edit = share.edit();
        return share.getBoolean("is_first", true);
    }

    public static void setFirst(Context context, boolean isfirst) {
        SharedPreferences share = context.getSharedPreferences("CONFIG_INIT", Activity.MODE_PRIVATE); // 指定操作的文件名称
        SharedPreferences.Editor edit = share.edit();
        edit.putBoolean("is_first", isfirst).commit();
    }

    public static boolean isUserInfoSet(Context context) {//用户信息是否被设置
        SharedPreferences share = context.getSharedPreferences("CONFIG_INIT", Activity.MODE_PRIVATE); // 指定操作的文件名称
        SharedPreferences.Editor edit = share.edit();
        return share.getBoolean("info_set", false);
    }

    public static void setUserInfoSet(Context context, boolean isset) {
        SharedPreferences share = context.getSharedPreferences("CONFIG_INIT", Activity.MODE_PRIVATE); // 指定操作的文件名称
        SharedPreferences.Editor edit = share.edit();
        edit.putBoolean("info_set", isset).commit();
    }

    public static boolean isDeviceTypeSelected(Context context) {
        SharedPreferences share = context.getSharedPreferences("CONFIG_INIT", Activity.MODE_PRIVATE); // 指定操作的文件名称
        SharedPreferences.Editor edit = share.edit();
        return share.getBoolean("isDeviceTypeSelected", false);
    }

    public static void setDeviceTypeSelected(Context context, boolean isset) {
        SharedPreferences share = context.getSharedPreferences("CONFIG_INIT", Activity.MODE_PRIVATE); // 指定操作的文件名称
        SharedPreferences.Editor edit = share.edit();
        edit.putBoolean("isDeviceTypeSelected", isset).commit();
    }

}