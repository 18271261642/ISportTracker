package com.isport.tracker.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.PagerAdapter;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.isport.isportlibrary.call.SMSBroadcastReceiver;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.Cmd337BController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.isportlibrary.managers.IsportManager;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.HamaSmsListener;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.fragment.BaseFragment;
import com.isport.tracker.fragment.ExerciseFragmentActivity;
import com.isport.tracker.fragment.HeartFragmentActivity;
import com.isport.tracker.fragment.SleepFragmentActivity;
import com.isport.tracker.fragment.WHeartFragment;
import com.isport.tracker.fragment.WSportFragmentActivity;
import com.isport.tracker.main.settings.UserInfoActivity;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.DeviceConfiger;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.MainViewPager;
import com.ypy.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@SuppressLint("NewApi")
public class MainActivityGroup extends BaseActivity implements View.OnClickListener {

    private static final String TAG = MainActivityGroup.class.getSimpleName();
    private ImageView imgHead;
    private List<BaseFragment> fragmentList;
    private ExerciseFragmentActivity exerciseFragmentActivity;
    private SleepFragmentActivity sleepFragmentActivity;
    private HeartFragmentActivity heartFragmentActivity;
    private WSportFragmentActivity wSportFragmentActivity;///337B
    private WHeartFragment wHeartFragment;//337B
    private MainViewPager mViewPager;
    private View indicateView;
    private RadioGroup radioGroup;
    private HamaSmsListener hamaSmsListener;
    private RadioButton rbSteps, rbSleep, rbHeart, rbWStep, rbWHeart;
    private int currentIndex = 0;
    private BaseDevice baseDevice;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    int currentType = -1;
    int currentDeviceIndex = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("MainActivityGroup", "onCreate");
        super.onCreate(savedInstanceState);
        // verifyPermission(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS,
        //      Manifest.permission.BLUETOOTH, Manifest.permission.READ_CALL_LOG});
        verifyPermission(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.MODIFY_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.BLUETOOTH, Manifest.permission.READ_CALL_LOG});
        //  verifyPermission(new String[]{ Manifest.permission.RECEIVE_SMS});

/*
        IntentFilter filters = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(new SMSBroadcastReceiver(), filters);

       IntentFilter filterSm = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(new SMSBroadcastReceiver(), filterSm);*/

        setContentView(R.layout.activity_main_);
        sharedPreferences = getSharedPreferences(DeviceTypeActivity.CONFIG_DEVICE, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        currentType = sharedPreferences.getInt(DeviceTypeActivity.KEY_DEVICE_TYPE, -1);
        currentDeviceIndex = sharedPreferences.getInt(DeviceTypeActivity.KEY_DEVICE_INDEX, 0);
        MainService mainService = MainService.getInstance(this);

        if (Constants.IS_FACTORY_VERSION) {
            if (currentType == -1 && mainService != null) {
                mainService.unBind(mainService.getCurrentDevice());
            }
        }

        initControl();
        initValue();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UserInfoActivity.ACTION_HEAD_CHANGE);
        filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
        filter.addAction(MainService.ACTION_SYNC_COMPLETED);
        filter.addAction(BaseController.ACTION_REAL_DATA);
        filter.addAction(Cmd337BController.ACTION_SPORT_DATA);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(Intent.ACTION_TIME_CHANGED);
        filter1.addAction(Intent.ACTION_TIME_CHANGED);
        filter1.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(globalReceiver, filter1);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hamaSmsListener != null) {
            unregisterReceiver(hamaSmsListener);
        }
        // registerReceiver(new HamaSmsListener(), filter);
        unregisterReceiver(globalReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }


    private void initValue() {
        UserInfo userInfo = UserInfo.getInstance(this);
        getHeadImage();
    }

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            updateFragmentUI();
        }
    };

    private BroadcastReceiver globalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.getDefault().post(intent);
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null)
                return;
            if (action.equals(UserInfoActivity.ACTION_HEAD_CHANGE)) {
                getHeadImage();
            } else if (action.equals(MainService.ACTION_CONNECTE_CHANGE)) {
                EventBus.getDefault().post(intent);
                BaseDevice tpDevice = (BaseDevice) intent.getSerializableExtra(MainService.EXTRA_CONNECT_DEVICE);
                if (Constants.IS_FACTORY_VERSION) {
                    if (tpDevice != null && tpDevice.getDeviceType() != currentType) {
                        Intent intent1 = new Intent(context, MainActivityGroup.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent1);
                    }
                } else {
                    int connctstate = intent.getIntExtra(MainService.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
                    if (connctstate == BaseController.STATE_CONNECTED) {
                        uiHandler.sendEmptyMessage(0x01);
                    }
                }/*filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
                filter.addAction(MainService.ACTION_SYNC_COMPLETED);
                filter.addAction(BaseController.ACTION_REAL_DATA);*/
            } else if (action.equals(MainService.ACTION_CONNECTE_CHANGE) || action.equals(MainService.ACTION_SYNC_COMPLETED) ||
                    action.equals(BaseController.ACTION_REAL_DATA) || action.equals(Cmd337BController.ACTION_SPORT_DATA)) {
                EventBus.getDefault().post(intent);
                if (action.equals(MainService.ACTION_SYNC_COMPLETED)) {
                    //  EventBus.getDefault().post(true);

                 /*   Intent intent1 = new Intent(context, MainActivityGroup.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);*/


                } else if (action.equals(BaseController.ACTION_REAL_DATA)) {

                } else if (action.equals(Cmd337BController.ACTION_SPORT_DATA)) {

                }
            }
        }
    };

    private void initControl() {
        imgHead = (ImageView) findViewById(R.id.image_head);
        imgHead.setOnClickListener(this);
        getHeadImage();
        mViewPager = (MainViewPager) findViewById(R.id.main_pager);
        mViewPager.setIsScrolled(false);
        indicateView = findViewById(R.id.view_checked_main);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group_menu_main);
        rbSteps = (RadioButton) findViewById(R.id.radio_button_steps);
        rbSleep = (RadioButton) findViewById(R.id.radio_button_sleep);
        rbHeart = (RadioButton) findViewById(R.id.radio_button_heart);
        rbWStep = (RadioButton) findViewById(R.id.radio_button_steps_1);
        rbWHeart = (RadioButton) findViewById(R.id.radio_button_heart_1);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (MainService.getInstance(MainActivityGroup.this) != null) {
                    baseDevice = MainService.getInstance(MainActivityGroup.this).getCurrentDevice();
                }
                switch (checkedId) {
                    case R.id.radio_button_steps:
                        currentIndex = 0;
                        switchFragment();
                        break;
                    case R.id.radio_button_sleep:
                        currentIndex = 1;
                        switchFragment();
                        break;
                    case R.id.radio_button_heart:

                        if (baseDevice != null && baseDevice.getDeviceType() == BaseDevice.TYPE_W337B) {
                            currentIndex = 1;
                        } else if (baseDevice != null && baseDevice.getDeviceType() == BaseDevice.TYPE_HEART_RATE) {
                            currentIndex = 0;
                        } else {
                            currentIndex = 2;
                        }
                        switchFragment();
                        break;
                    case R.id.radio_button_steps_1:
                        currentIndex = 3;
                        switchFragment();
                        break;
                    case R.id.radio_button_heart_1:
                        currentIndex = 4;
                        switchFragment();
                        break;
                }
            }
        });

        exerciseFragmentActivity = ExerciseFragmentActivity.newInstance();
        sleepFragmentActivity = SleepFragmentActivity.newInstance();
        heartFragmentActivity = HeartFragmentActivity.newInstance();
        wSportFragmentActivity = WSportFragmentActivity.newInstance();
        wHeartFragment = WHeartFragment.newInstance();
        updateFragmentUI();
    }

    private void updateFragmentUI() {
        rbHeart.setVisibility(View.GONE);
        rbSleep.setVisibility(View.GONE);
        rbSteps.setVisibility(View.GONE);
        rbWStep.setVisibility(View.GONE);
        rbWHeart.setVisibility(View.GONE);
        if (fragmentList == null) {
            fragmentList = new ArrayList<>();
        }
        if (fragmentList.size() < 5) {
            fragmentList.clear();
            fragmentList.add(exerciseFragmentActivity);
            fragmentList.add(sleepFragmentActivity);
            fragmentList.add(heartFragmentActivity);
            fragmentList.add(wSportFragmentActivity);
            fragmentList.add(wHeartFragment);
        }
        if (mViewPager != null) {
            if (mViewPager.getAdapter() == null) {
                mViewPager.setAdapter(new MainPagerAdapter(fragmentList));
            }

            mViewPager.getAdapter().notifyDataSetChanged();
            if (Constants.IS_FACTORY_VERSION) {
                currentIndex = 0;
            } else {
                MainService mainService = MainService.getInstance(this);
                if (mainService == null || (mainService != null && mainService.getCurrentDevice() == null)) {
                    if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ACTIVA_T) || BuildConfig.PRODUCT.equals(Constants.PRODUCT_HU_TRACKER)) {
                        rbHeart.setVisibility(View.GONE);
                    } else {
                        rbHeart.setVisibility(View.VISIBLE);
                    }

                    rbSleep.setVisibility(View.VISIBLE);
                    rbSteps.setVisibility(View.VISIBLE);
                    rbWHeart.setVisibility(View.GONE);
                    rbWStep.setVisibility(View.GONE);
                    currentIndex = 0;
                } else {
                    int devicetypetp = mainService.getCurrentDevice().getDeviceType();
                    String name = mainService.getCurrentDevice().getName();
                    if (devicetypetp == BaseDevice.TYPE_W337B) {
                        currentIndex = 3;
                        rbHeart.setVisibility(View.GONE);
                        rbSleep.setVisibility(View.GONE);
                        rbSteps.setVisibility(View.GONE);
                        rbWHeart.setVisibility(View.VISIBLE);
                        rbWStep.setVisibility(View.VISIBLE);
                    } else if (devicetypetp == BaseDevice.TYPE_W311N || devicetypetp == BaseDevice.TYPE_W311T || devicetypetp == BaseDevice.TYPE_AT200 || devicetypetp == BaseDevice.TYPE_AS97) {///带心率
                        currentIndex = 0;
                        rbHeart.setVisibility(View.VISIBLE);
                        rbSleep.setVisibility(View.VISIBLE);
                        rbSteps.setVisibility(View.VISIBLE);
                        rbWHeart.setVisibility(View.GONE);
                        rbWStep.setVisibility(View.GONE);
                    } else if (devicetypetp == BaseDevice.TYPE_W307H || devicetypetp == BaseDevice.TYPE_W301H || devicetypetp == BaseDevice.TYPE_W307N || devicetypetp == BaseDevice.TYPE_W307S || devicetypetp == BaseDevice.TYPE_W307S_SPACE || devicetypetp == BaseDevice.TYPE_AT100 || devicetypetp == BaseDevice.TYPE_W301N ||
                            devicetypetp == BaseDevice.TYPE_W301S ||
                            devicetypetp == BaseDevice.TYPE_W285S || devicetypetp == BaseDevice.TYPE_SAS80 || devicetypetp == BaseDevice.TYPE_W194 || devicetypetp == BaseDevice.TYPE_W240 ||
                            devicetypetp == BaseDevice.TYPE_W240S || devicetypetp == BaseDevice.TYPE_W240B || devicetypetp == BaseDevice.TYPE_ACTIVITYTRACKER ||
                            devicetypetp == BaseDevice.TYPE_W316) {////计步和睡眠
                        currentIndex = 0;
                        rbSleep.setVisibility(View.VISIBLE);
                        rbSteps.setVisibility(View.VISIBLE);
                        rbHeart.setVisibility(View.GONE);
                        rbWStep.setVisibility(View.GONE);
                        rbWHeart.setVisibility(View.GONE);
                    } else if (devicetypetp == BaseDevice.TYPE_P118 || devicetypetp == BaseDevice.TYPE_MILLIONPEDOMETER) {///只有计步
                        currentIndex = 0;
                        rbSteps.setVisibility(View.VISIBLE);
                        rbHeart.setVisibility(View.GONE);
                        rbWStep.setVisibility(View.GONE);
                        rbWHeart.setVisibility(View.GONE);
                        rbSleep.setVisibility(View.GONE);
                    } else if (devicetypetp == BaseDevice.TYPE_HEART_RATE) {
                        currentIndex = 2;
                        rbSteps.setVisibility(View.GONE);
                        rbHeart.setVisibility(View.VISIBLE);
                        rbWStep.setVisibility(View.GONE);
                        rbWHeart.setVisibility(View.GONE);
                        rbSleep.setVisibility(View.GONE);
                    }
                    if (name != null && (name.equalsIgnoreCase(Constants.DEVIE_P674A))) {
                        rbSleep.setVisibility(View.GONE);
                    }
                }

            }
            if (currentIndex == 0) {
                rbSteps.setChecked(true);
            } else if (currentIndex == 3) {
                rbWStep.setChecked(true);
            }

            switchFragment();

        }
    }

    private void switchFragment() {
        if (mViewPager != null) {
            if (MainService.getInstance(MainActivityGroup.this) != null) {
                baseDevice = MainService.getInstance(MainActivityGroup.this).getCurrentDevice();
            }
            int cindex = mViewPager.getCurrentItem();
            mViewPager.setCurrentItem(currentIndex, false);
            if (baseDevice != null && baseDevice.getDeviceType() == BaseDevice.TYPE_HEART_RATE) {
                indicateView.setTranslationX(0);
            } else {
                indicateView.setTranslationX(indicateView.getLayoutParams().width * (currentIndex < 3 ? currentIndex : (currentIndex - 3)));
            }
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
        options.inSampleSize = UtilTools.calculateInSampleSize(options, DeviceConfiger.dp2px(40), DeviceConfiger.dp2px(40));
        options.inJustDecodeBounds = false;


        bitmap = UtilTools.getBitmap(path, options);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_head, options);
            imgHead.setImageResource(R.drawable.image_head);
        } else {
            Drawable drawable = new BitmapDrawable(bitmap);
            imgHead.setImageDrawable(drawable);
        }
        //bitmap = (bitmap == null ? BitmapFactory.decodeResource(getResources(), R.drawable.image_head,options) : bitmap);


    }

    public void verifyPermission(String[] permissions) {
        if (permissions != null) {
            List<String> lists = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {

                    }
                    lists.add(permissions[i]);
                } else {
                    Log.e(TAG, "权限已经同意 == " + permissions[i]);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (permissions[i].equals(Manifest.permission.READ_SMS)) {
                            Log.e(TAG, "READ_SMS -1-1-1");
                            IsportManager.getInstance().init(this);
                        } else if (permissions[i].equals(Manifest.permission.RECEIVE_SMS)) {
                            Log.e(TAG, "RECEIVE_SMS -1-1-1");
                            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                            hamaSmsListener = new HamaSmsListener();
                            registerReceiver(hamaSmsListener, filter);
                        }
                    }
                }
            }
            if (lists.size() > 0) {
                String[] ps = new String[lists.size()];
                for (int i = 0; i < lists.size(); i++) {
                    ps[i] = lists.get(i);
                }
                ActivityCompat.requestPermissions(this, ps, 1);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                //不能用这句代码，如果用了这句代码有些手机直接奔溃
                // IsportManager.getInstance().registerObserver();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getHeadImage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions != null) {
            List<String> list = new ArrayList<>();
            boolean isShouldShow = false;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (!isShouldShow) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityGroup.this);
                                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                    builder.setMessage(getString(R.string.location_permission));
                                } else if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    builder.setMessage(getString(R.string.storage_permission));
                                } else if (permissions[i].equals(Manifest.permission.RECEIVE_SMS)) {
                                    builder.setMessage(getString(R.string.sms_permission));
                                } else if (permissions[i].equals(Manifest.permission.READ_PHONE_STATE)) {
                                    builder.setMessage(getString(R.string.phone_permission));
                                } else if (permissions[i].equals(Manifest.permission.SEND_SMS)) {
                                    builder.setMessage(getString(R.string.phone_permission));

                                }
                                //builder.create().show();
                            }
                        }

                    }
                    list.add(permissions[i]);
                } else {
                    //同意了
                    IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                    registerReceiver(new SMSBroadcastReceiver(), filter);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (permissions[i].equals(Manifest.permission.READ_SMS)) {
                            Log.e(TAG, "READ_SMS 000");
                            IsportManager.getInstance().init(this);
                        } else if (permissions[i].equals(Manifest.permission.RECEIVE_SMS)) {
                            Log.e(TAG, "RECEIVE_SMS 000");
                            /*IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                            registerReceiver(new HamaSmsListener(), filter);*/
                        }
                    }
                }
            }


        }
    }


    @Override
    public void onClick(View v) {

        Intent intent = null;
        switch (v.getId()) {
            case R.id.image_head:
                intent = new Intent(this, MenuSetActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MainPagerAdapter extends FragmentStatePagerAdapter {
        private List<BaseFragment> listFrag = null;

        public MainPagerAdapter(List<BaseFragment> listFrag) {
            super(getSupportFragmentManager());
            this.listFrag = listFrag;
        }

        @Override
        public Fragment getItem(int position) {
            if (listFrag == null)
                return null;
            return listFrag.get(position);
        }

        @Override
        public int getCount() {
            return listFrag == null ? 0 : listFrag.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            /* try {
                super.restoreState(state, loader);
            }catch (Exception e){

            }*/
        }
    }

}