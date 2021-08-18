package com.isport.tracker.main.settings;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.dialogActivity.DialogSetAge;
import com.isport.tracker.dialogActivity.DialogSetHeight;
import com.isport.tracker.dialogActivity.DialogSetSex;
import com.isport.tracker.dialogActivity.DialogTakePhoto;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.main.MainActivityGroup;
import com.isport.tracker.main.WelcomeActivity;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.DeviceConfiger;
import com.isport.tracker.util.UtilTools;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class UserInfoActivity extends BaseActivity implements OnClickListener {

    public static final String ACTION_HEAD_CHANGE = "com.isport.tracker.ACTION_HEAD_CHANGE";
    private RelativeLayout re_back;
    private ImageView image_head;
    private LinearLayout ly_age, ly_height, ly_weight, ly_sex, ly_metric;
    private SharedPreferences share = null;
    private Editor edit;
    private TextView age, height, weight, sex, tvMetric;
    private TextView tv_name;
    private TextView weight_user_info_unit;
    private TextView height_user_info_unit;
    private EditText ed_name;
    private boolean isMan = true;
    private String headPath;
    private String name;
    int metric;
    private UserInfo userInfo;
    private View view_line;
    private RelativeLayout rl_head_top;
    private RelativeLayout rl_user;
    private TextView title_name;
    private boolean mBooleanExtra;

    @TargetApi(19)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_profile);
        userInfo = UserInfo.getInstance(this);
        mBooleanExtra = getIntent().getBooleanExtra(Constants.EXTRA_GOUSERINFO, false);
        init();
        initValue();
        MainService mainService = MainService.getInstance(this);
        if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
            //mainService.queryUserInfoFromDevice();
        }

    }

    public void getHeadImage() {
        UserInfo userInfo = UserInfo.getInstance(this);
        String path = userInfo.getHead();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        File file = new File(path);
        Bitmap bitmap = null;
        if (!file.exists()) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_head, options);
        } else {
            bitmap = UtilTools.getBitmap(path, options);
        }
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        options.inSampleSize = UtilTools.calculateInSampleSize(options, DeviceConfiger.dp2px(90), DeviceConfiger.dp2px(90));
        options.inJustDecodeBounds = false;


        bitmap = UtilTools.getBitmap(path, options);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_head, options);
            image_head.setImageResource(R.drawable.image_head);
        } else {
            Drawable drawable = new BitmapDrawable(bitmap);
            image_head.setImageDrawable(drawable);
        }
        //bitmap = (bitmap == null ? BitmapFactory.decodeResource(getResources(), R.drawable.image_head,options) : bitmap);


    }

	/*public void getHeadImage() {
        UserInfo userInfo = UserInfo.getInstance(this);
		String path = userInfo.getHead();
		Bitmap bitmap = UtilTools.getBitmap(path);
		bitmap = (bitmap == null ? BitmapFactory.decodeResource(getResources(), R.drawable.image_head) : bitmap);
		image_head.setImageBitmap(bitmap);
	}*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initValue() {
        getHeadImage();

        metric = userInfo.getMetricImperial();
        ed_name.setText(userInfo.getNickname());
        tv_name.setText(userInfo.getNickname());
        if (userInfo.getNickname() == null || userInfo.getNickname().equals("")) {
            tv_name.setText(getString(R.string.complete_user_info_hint_nick_name));
        }

        float hei = userInfo.getHeight();
        String tp = "";
        if (metric == 0) {
            //tp = getString(R.string.format_cm, userInfo.getHeight()+"");
            float hh = userInfo.getHeight();
            if (hh > 250.9f) {
                hh = 250.9f;
            }
            tp = String.format(Locale.ENGLISH, "%.1f", Math.round(hh * 10) * 0.1) + getString(R.string.ride_cm);
        } else {
            int inch = (Math.round(hei * 100000 / 25400));///将厘米转换成inch

            tp = inch / 10.0f + getString(R.string.inch);
        }
        height.setText(tp);

        float wei = userInfo.getWeight();
        tp = "";
        if (metric == 0) {
            float weiwei = Math.round(userInfo.getWeight() * 10) * 0.1f;
            if (weiwei > 300.9f) {
                weiwei = 300.9f;
            }
            tp = getString(R.string.format_kg, String.format(Locale.ENGLISH, "%.1f", weiwei) + "");
        } else {
            float lbs = Math.round((wei * 100000) / 4535.9237f) * 0.1f;
            if (lbs > 661.9f) {
                lbs = 661.9f;
            }
            tp = getString(R.string.format_lbs, String.format(Locale.ENGLISH, "%.1f", lbs) + "");
        }
        weight.setText(tp);
        age.setText(getAge() + "");
        sex.setText(userInfo.getGender() == 1 ? getString(R.string.user_info_man) : getString(R.string.user_info_woman));
        tvMetric.setText(userInfo.getMetricImperial() == 0 ? getString(R.string.metric) : getString(R.string.Inch));
    }

    private void init() {
        re_back = (RelativeLayout) findViewById(R.id.return_back);
        ly_sex = (LinearLayout) findViewById(R.id.user_info_ly4);
        ly_age = (LinearLayout) findViewById(R.id.user_info_ly1);
        ly_height = (LinearLayout) findViewById(R.id.user_info_ly2);
        ly_weight = (LinearLayout) findViewById(R.id.user_info_ly3);
        ly_metric = (LinearLayout) findViewById(R.id.user_info_ly5);
        view_line = (View) findViewById(R.id.view_line);
        rl_head_top = (RelativeLayout) findViewById(R.id.rl_head_top);
        rl_user = (RelativeLayout) findViewById(R.id.rl_user);
        title_name = (TextView) findViewById(R.id.title_name);
        if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_FITNESS_TRACKER_PRO)) {
            view_line.setVisibility(View.GONE);
            rl_head_top.setVisibility(View.GONE);
            rl_user.setVisibility(View.GONE);
            title_name.setVisibility(View.GONE);
        } else {
            view_line.setVisibility(View.VISIBLE);
            rl_head_top.setVisibility(View.VISIBLE);
            rl_user.setVisibility(View.VISIBLE);
        }
        TextView tvSave = (TextView) findViewById(R.id.tv_userinfo_save);
        if (!mBooleanExtra) {
            tvSave.setVisibility(View.GONE);
        }
        age = (TextView) findViewById(R.id.user_info_age);
        sex = (TextView) findViewById(R.id.user_info_sex);
        height = (TextView) findViewById(R.id.user_info_height);
        weight = (TextView) findViewById(R.id.user_info_weight);
        tvMetric = (TextView) findViewById(R.id.user_info_metric);
        weight_user_info_unit = (TextView) findViewById(R.id.weight_user_info_unit);
        height_user_info_unit = (TextView) findViewById(R.id.height_user_info_unit);
        tv_name = (TextView) findViewById(R.id.complete_user_info_tv_name);
        ed_name = (EditText) findViewById(R.id.complete_user_info_ed_name);
        image_head = (ImageView) findViewById(R.id.complete_user_info_image);
        ly_age.setOnClickListener(this);
        ly_height.setOnClickListener(this);
        ly_weight.setOnClickListener(this);
        ly_sex.setOnClickListener(this);
        tv_name.setOnClickListener(this);
        image_head.setOnClickListener(this);
        re_back.setOnClickListener(this);
        ly_metric.setOnClickListener(this);

        if (!WelcomeActivity.isUserInfoSet(this)) {
            re_back.setVisibility(View.GONE);
        } else {
            ly_metric.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && !WelcomeActivity.isUserInfoSet(this)) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    ed_name.setVisibility(View.GONE);
                    tv_name.setVisibility(View.VISIBLE);
                    String name_temp = ed_name.getText().toString();
                    if (name_temp.equals("")) {
                        name_temp = getString(R.string.complete_user_info_hint_nick_name);
                    }
                    tv_name.setText(name_temp);
                    userInfo.setNickname(name_temp);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            try {
                return !(event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public boolean verifyPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void verifyPermission2() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    public int getAge() {
        UserInfo userInfo = UserInfo.getInstance(this);
        String birthDay = userInfo.getBirthday();
        Calendar calendar = Calendar.getInstance();
        int curyear = calendar.get(Calendar.YEAR);
        calendar.setTime(DateUtil.stringToDate(birthDay, "yyyy-MM-dd"));
        int birYear = calendar.get(Calendar.YEAR);
        return curyear - birYear;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.return_back:
                UserInfoActivity.this.finish();
                break;
            case R.id.complete_user_info_tv_name:
                tv_name.setVisibility(View.GONE);
                ed_name.setVisibility(View.VISIBLE);
                String name_temp = tv_name.getText().toString();
                if (name_temp.equals(getString(R.string.complete_user_info_hint_nick_name))) {
                    name_temp = "";
                }
                ed_name.setText(name_temp);
                break;
            case R.id.complete_user_info_image:
                if (!verifyPermission()) {
                    verifyPermission2();

                } else {
                    intent = new Intent(UserInfoActivity.this, DialogTakePhoto.class);
                    startActivityForResult(intent, 1);
                    overridePendingTransition(R.anim.dialog_enter_anim, R.anim.dialog_exit_anim);
                }
                break;
            case R.id.user_info_ly1:
                intent = new Intent(this, DialogSetAge.class);
                intent.putExtra(DialogSetAge.EXTRA_TYPE, DialogSetAge.TYPE_AGE);
                intent.putExtra(DialogSetAge.EXTRA_AGE, getAge());
                startActivityForResult(intent, 202);
                break;
            case R.id.user_info_ly2:
                intent = new Intent(this, DialogSetHeight.class);
                intent.putExtra(DialogSetHeight.EXTRA_TYPE, DialogSetHeight.TYPE_HEIGHT);
                intent.putExtra(DialogSetHeight.EXTRA_HEIGHT, userInfo.getHeight() + "");
                startActivityForResult(intent, 203);
                break;
            case R.id.user_info_ly3:
                intent = new Intent(this, DialogSetHeight.class);
                intent.putExtra(DialogSetHeight.EXTRA_TYPE, DialogSetHeight.TYPE_WEIGHT);
                intent.putExtra(DialogSetHeight.EXTRA_WEIGHT, userInfo.getWeight() + "");
                startActivityForResult(intent, 204);
                break;
            case R.id.user_info_ly4:
                intent = new Intent(this, DialogSetSex.class);
                intent.putExtra(DialogSetSex.EXTRA_TYPE, DialogSetSex.TYPE_GENDER);
                intent.putExtra(DialogSetSex.EXTRA_IS_LEFTHAND, userInfo.getGender() == 1);
                startActivityForResult(intent, 201);
                break;
            case R.id.tv_userinfo_save:
                if (WelcomeActivity.isUserInfoSet(this)) {

                } else {
                    Intent intent1 = null;
                    if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_FITNESS_TRACKER_PRO)) {
                        //intent1 = new Intent(this, com.isport.fitness.activity.MainActivityGroup.class);
                    } else {
                        intent1 = new Intent(this, MainActivityGroup.class);
                    }
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                    WelcomeActivity.setUserInfoSet(this, true);
                }
                save();
                break;
            case R.id.user_info_ly5:
                intent = new Intent(this, DialogSetSex.class);
                intent.putExtra(DialogSetSex.EXTRA_TYPE, DialogSetSex.TYPE_METRIC);
                intent.putExtra(DialogSetSex.EXTRA_IS_LEFTHAND, userInfo.getMetricImperial() == 0);
                startActivityForResult(intent, 201);
                break;
        }

    }

    public void save() {
        //if (ed_name.getVisibility() == View.VISIBLE) {
        String t1 = ed_name.getText().toString();
        String t2 = tv_name.getText().toString();
        UserInfo.getInstance(this).setNickname(ed_name.getText().toString());
        //} else {
        //}
        finish();
    }

    public void saveInfo() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        UserInfo userInfo = UserInfo.getInstance(this);
        if (data != null && resultCode == 209) {
            userInfo.setGender(data.getBooleanExtra(DialogSetSex.EXTRA_IS_LEFTHAND, true) ? 1 : 0);
            if (isConnected()) {
                MainService.getInstance(this).sendBaseTime();
            }
        } else if (data != null && resultCode == 208) {

        } else if (data != null && resultCode == 207) {
            userInfo.setMetricImperial(data.getBooleanExtra(DialogSetSex.EXTRA_IS_LEFTHAND, true) ? 0 : 1);
        } else if (data != null && resultCode == 210) {//weight
            userInfo.setWeight(Float.valueOf(data.getStringExtra(DialogSetHeight.EXTRA_WEIGHT)));
            if (isConnected()) {
                MainService.getInstance(this).syncUserInfo();
            }
        } else if (data != null && resultCode == 211) {//hegith
            String hei = data.getStringExtra(DialogSetHeight.EXTRA_HEIGHT);
            userInfo.setHeight(Float.valueOf(data.getStringExtra(DialogSetHeight.EXTRA_HEIGHT)));
            if (isConnected()) {
                MainService.getInstance(this).syncUserInfo();
            }
        } else if (data != null && resultCode == 212) {///sleep target

        } else if (data != null && resultCode == 200) {//head path


            String path = data.getStringExtra("head_path");
            // path="storage/emulated/0/JCamera/picture_1576576404836.jpg";

            // Log.e("mCameraUri", resultCode + "" + data.getStringExtra("head_path") + path);
            userInfo.setHead(path);
            //LoadImageUtil.getInstance().displayImagePath(UserInfoActivity.this, path, image_head);
            // LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_HEAD_CHANGE));
        } else if (data != null && resultCode == 214) {//age
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -1 * Integer.parseInt(data.getStringExtra(DialogSetAge.EXTRA_AGE)));
            String birthday = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
            userInfo.setBirthday(birthday);
            if (isConnected()) {
                MainService.getInstance(this).syncUserInfo();
            }

        }

        initValue();
    }

    private boolean isConnected() {
        MainService mainService = MainService.getInstance(this);
        if (mainService == null || (mainService != null && mainService.getConnectionState() != BaseController.STATE_CONNECTED)) {
            Toast.makeText(this, getString(R.string.please_bind), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


}