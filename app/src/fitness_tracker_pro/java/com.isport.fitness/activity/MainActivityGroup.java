package com.isport.fitness.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.Fragment;
import androidx.core.app.FragmentStatePagerAdapter;
import androidx.core.content.LocalBroadcastManager;
import androidx.core.view.PagerAdapter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.fitness.fragment.ExerciseFragmentActivityFitness;
import com.isport.fitness.fragment.HeadFragmentActivity;
import com.isport.fitness.fragment.HeartFragmentActivityFitness;
import com.isport.fitness.fragment.SleepFragmentActivityFitness;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.Cmd337BController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.fragment.BaseFragment;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.main.DeviceTypeActivity;
import com.isport.tracker.main.MenuSetActivity;
import com.isport.tracker.main.settings.ActivityDeviceSetting;
import com.isport.tracker.main.settings.ManageDeviceActivity;
import com.isport.tracker.main.settings.UserInfoActivity;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.DeviceConfiger;
import com.isport.tracker.util.TVDrawableUtils;
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
    private ImageView imgHead, mIvShare;
    private List<BaseFragment> fragmentList;
    private ExerciseFragmentActivityFitness exerciseFragmentActivity;
    private SleepFragmentActivityFitness sleepFragmentActivity;
    private HeartFragmentActivityFitness heartFragmentActivity;
    private HeadFragmentActivity headFragmentActivity;//337B
    private MainViewPager mViewPager;
    private View indicateView;
    private RadioGroup radioGroup;
    private RadioButton rbSteps, rbSleep, rbHeart, rbWStep, rbWHeart, rbHead;
    private int currentIndex = 0;
    private BaseDevice baseDevice;
    TextView mConnectState;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    int currentType = -1;
    int currentDeviceIndex = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("MainActivityGroup", "onCreate");
        super.onCreate(savedInstanceState);
        verifyPermission(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.BLUETOOTH});
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
        regist();
    }

    private void regist() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UserInfoActivity.ACTION_HEAD_CHANGE);
        filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
        filter.addAction(MainService.ACTION_SYNC_COMPLETED);
        filter.addAction(BaseController.ACTION_REAL_DATA);
        filter.addAction(Cmd337BController.ACTION_SPORT_DATA);
        registerReceiver(mReceiver, filter);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
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
                        setTopConnected();
                    } else if (connctstate == BaseController.STATE_DISCONNECTED) {
                        setTopDisconnected();
                    }
                }/*filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
                filter.addAction(MainService.ACTION_SYNC_COMPLETED);
                filter.addAction(BaseController.ACTION_REAL_DATA);*/
            } else if (action.equals(MainService.ACTION_CONNECTE_CHANGE) || action.equals(MainService.ACTION_SYNC_COMPLETED) ||
                    action.equals(BaseController.ACTION_REAL_DATA) || action.equals(Cmd337BController.ACTION_SPORT_DATA)) {
                EventBus.getDefault().post(intent);
                if (action.equals(MainService.ACTION_SYNC_COMPLETED)) {

                } else if (action.equals(BaseController.ACTION_REAL_DATA)) {

                } else if (action.equals(Cmd337BController.ACTION_SPORT_DATA)) {

                }
            }
        }
    };

    private void setTopConnected() {
        TVDrawableUtils.drawLeft(mConnectState, R.drawable.iv_connected);
        mConnectState.setText("");
    }

    private void setTopDisconnected() {
        TVDrawableUtils.drawLeft(mConnectState, R.drawable.iv_disconnected);
        mConnectState.setText(getString(R.string.connect_tips));
    }

    private void initControl() {
        imgHead = (ImageView) findViewById(R.id.image_head);

        getHeadImage();
        mViewPager = (MainViewPager) findViewById(R.id.main_pager);
        mViewPager.setIsScrolled(false);
        indicateView = findViewById(R.id.view_checked_main);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group_menu_main);
        rbSteps = (RadioButton) findViewById(R.id.radio_button_steps);
        rbSleep = (RadioButton) findViewById(R.id.radio_button_sleep);
        rbHeart = (RadioButton) findViewById(R.id.radio_button_heart);
        rbHead = (RadioButton) findViewById(R.id.radio_button_head);
        rbWStep = (RadioButton) findViewById(R.id.radio_button_steps_1);
        rbWHeart = (RadioButton) findViewById(R.id.radio_button_heart_1);
        mConnectState = (TextView) findViewById(R.id.tv_connect_state);
        mIvShare = (ImageView) findViewById(R.id.main_iv_share);
        mIvShare.setOnClickListener(this);
        mConnectState.setOnClickListener(this);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mIvShare.setVisibility(View.VISIBLE);
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
                        } else {
                            currentIndex = 2;
                        }
                        switchFragment();
                        break;
                    case R.id.radio_button_head:
                        currentIndex = 3;
                        switchFragment();
                        mIvShare.setVisibility(View.GONE);
                        break;

                }
            }
        });

        exerciseFragmentActivity = ExerciseFragmentActivityFitness.newInstance();
        sleepFragmentActivity = SleepFragmentActivityFitness.newInstance();
        heartFragmentActivity = HeartFragmentActivityFitness.newInstance();
        headFragmentActivity = HeadFragmentActivity.newInstance();

        updateFragmentUI();

    }

    private void updateFragmentUI() {
        rbHeart.setVisibility(View.GONE);
        rbSleep.setVisibility(View.GONE);
        rbSteps.setVisibility(View.GONE);
        rbWStep.setVisibility(View.GONE);
        rbWHeart.setVisibility(View.GONE);
        if (Constants.IS_FACTORY_VERSION) {
            if (fragmentList != null && fragmentList.size() > 0) {
                for (int i = 0; i < fragmentList.size(); i++) {
                    fragmentList.get(i).clearAdapter();
                }
            }
            fragmentList = new ArrayList<>();
            //MainService mainService = MainService.getInstance(this);
            if (currentType == -1) {
                fragmentList.add(exerciseFragmentActivity);
                fragmentList.add(sleepFragmentActivity);
                fragmentList.add(heartFragmentActivity);
                fragmentList.add(headFragmentActivity);
                rbHeart.setVisibility(View.VISIBLE);
                rbSleep.setVisibility(View.VISIBLE);
                rbSteps.setVisibility(View.VISIBLE);
            } else {
                if (currentType == BaseDevice.TYPE_W311N || currentType == BaseDevice.TYPE_W311T ||  currentType == BaseDevice.TYPE_AT200  || currentType == BaseDevice.TYPE_AS97) {///带心率
                    fragmentList.add(exerciseFragmentActivity);
                    fragmentList.add(sleepFragmentActivity);
                    fragmentList.add(heartFragmentActivity);
                    fragmentList.add(headFragmentActivity);
                    rbHeart.setVisibility(View.VISIBLE);
                    rbSleep.setVisibility(View.VISIBLE);
                    rbSteps.setVisibility(View.VISIBLE);
                } else if (currentType == BaseDevice.TYPE_W307N || currentType == BaseDevice.TYPE_W307S || currentType == BaseDevice.TYPE_AT100 || currentType == BaseDevice.TYPE_W301N ||
                        currentType == BaseDevice.TYPE_W301S ||
                        currentType == BaseDevice.TYPE_W285S || currentType == BaseDevice.TYPE_SAS80 || currentType == BaseDevice.TYPE_W194 || currentType == BaseDevice.TYPE_W240 ||
                        currentType == BaseDevice.TYPE_W240S || currentType == BaseDevice.TYPE_W240B || currentType == BaseDevice.TYPE_ACTIVITYTRACKER ||
                        currentType == BaseDevice.TYPE_W316) {////计步和睡眠
                    fragmentList.add(exerciseFragmentActivity);
                    fragmentList.add(sleepFragmentActivity);
                    fragmentList.add(headFragmentActivity);
                    rbSleep.setVisibility(View.VISIBLE);
                    rbSteps.setVisibility(View.VISIBLE);
                } else if (currentType == BaseDevice.TYPE_P118 || currentType == BaseDevice.TYPE_MILLIONPEDOMETER) {///只有计步
                    fragmentList.add(exerciseFragmentActivity);
                    fragmentList.add(headFragmentActivity);
                    rbSteps.setVisibility(View.VISIBLE);
                } else if (currentType == BaseDevice.TYPE_W337B) {///337B
                    fragmentList.add(headFragmentActivity);
                    rbSteps.setVisibility(View.VISIBLE);
                    rbHeart.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (fragmentList == null) {
                fragmentList = new ArrayList<>();
            }
            if (fragmentList.size() < 4) {
                fragmentList.clear();
                fragmentList.add(exerciseFragmentActivity);
                fragmentList.add(sleepFragmentActivity);
                fragmentList.add(heartFragmentActivity);
                fragmentList.add(headFragmentActivity);
                Log.e(TAG, "fragmentList.size==" + fragmentList.size());
            }
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
                    rbHeart.setVisibility(View.VISIBLE);
                    rbSleep.setVisibility(View.VISIBLE);
                    rbSteps.setVisibility(View.VISIBLE);
                    rbWHeart.setVisibility(View.GONE);
                    rbWStep.setVisibility(View.GONE);
                    currentIndex = 0;
                } else {
                    int devicetypetp = mainService.getCurrentDevice().getDeviceType();
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
                    } else if (devicetypetp == BaseDevice.TYPE_W307N || devicetypetp == BaseDevice.TYPE_W307S || devicetypetp == BaseDevice.TYPE_AT100 || devicetypetp == BaseDevice.TYPE_W301N ||
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
                    }
                }

            }
            if (currentIndex == 0) {
                rbSteps.setChecked(true);
            } else if (currentIndex == 3) {
                rbHeart.setChecked(true);
            }

            switchFragment();

        }
    }

    private void switchFragment() {
        if (mViewPager != null) {
            int cindex = mViewPager.getCurrentItem();
            mViewPager.setCurrentItem(currentIndex, false);
            indicateView.setTranslationX(indicateView.getLayoutParams().width * (currentIndex < 3 ? currentIndex : (currentIndex - 3)));
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
        bitmap = (bitmap == null ? BitmapFactory.decodeResource(getResources(), R.drawable.image_head, options) : bitmap);
        imgHead.setImageBitmap(bitmap);
    }

    public void verifyPermission(String[] permissions) {
        if (permissions != null) {
            List<String> lists = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {

                    }
                    lists.add(permissions[i]);
                }
            }
            if (lists.size() > 0) {
                String[] ps = new String[lists.size()];
                for (int i = 0; i < lists.size(); i++) {
                    ps[i] = lists.get(i);
                }
                ActivityCompat.requestPermissions(this, ps, 1);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getConnectState();
        regist();
        getHeadImage();
    }

    private void getConnectState() {
        MainService mainService = MainService.getInstance(this);
        if (mainService!=null){
            if (mainService.getConnectionState()==BaseController.STATE_CONNECTED){
                setTopConnected();
            }else{
                setTopDisconnected();
            }
        }
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
                                }
                                //builder.create().show();
                            }
                        }
                    }
                    list.add(permissions[i]);
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
            case R.id.main_iv_share:

                break;
            case R.id.tv_connect_state:
                if (MainService.getInstance(this)!=null&&MainService.getInstance(this).getConnectionState()==BaseController.STATE_CONNECTED){
                    intent = new Intent(this, ActivityDeviceSetting.class);
                    startActivity(intent);
                }else {
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    if (adapter == null || !adapter.isEnabled()) {
                        Toast.makeText(this, getString(R.string.turn_bluetooth_first), Toast.LENGTH_LONG).show();
                    } else {
                        intent = new Intent(this, ManageDeviceActivity.class);
                        startActivity(intent);
                    }
                }
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