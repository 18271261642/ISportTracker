package com.isport.tracker.main.settings.sport;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.dialogActivity.DialogSetTime;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.view.EasySwitchButton;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.HeartTiming;
import com.ypy.eventbus.EventBus;

public class HeartRateAutoActivity extends BaseActivity implements EasySwitchButton.OnOpenedListener,View.OnClickListener {

	private final static String AUTOHEART_TEST_CONFIG = "AUTO_TEST_CONFIG";
	public final static String KEY_TIME_START_HOUR_ONE = "KEY_TIME_START_HOUR_ONE";
	public final static String KEY_TIME_START_MINUTE_ONE = "KEY_TIME_START_MINUTE_ONE";

	public final static String KEY_TIME_START_HOUR_TWO = "KEY_TIME_START_HOUR_TWO";
	public final static String KEY_TIME_START_MINUTE_TWO = "KEY_TIME_START_MINUTE_TWO";


	public final static String KEY_TIME_START_HOUR_THREE = "KEY_TIME_START_HOUR_THREE";
	public final static String KEY_TIME_START_MINUTE_THREE = "KEY_TIME_START_MINUTE_THREE";

	public final static String KEY_TIME_END_HOUR_ONE = "KEY_TIME_END_HOUR_ONE";
	public final static String KEY_TIME_END_MINUTE_ONE = "KEY_TIME_END_MINUTE_ONE";

	public final static String KEY_TIME_END_HOUR_TWO = "KEY_TIME_END_HOUR_TWO";
	public final static String KEY_TIME_END_MINUTE_TWO = "KEY_TIME_END_MINUTE_TWO";

	public final static String KEY_TIME_END_HOUR_THREE = "KEY_TIME_END_HOUR_THREE";
	public final static String KEY_TIME_END_MINUTE_THREE = "KEY_TIME_END_MINUTE_THREE";

	public final static String KEY_WHOLE_AUTO_HEARTRATE = "KEY_WHOLE_AUTO_HEARTRATE";//总开关
	public final static String KEY_IS_HEART_RATE_ONE = "KEY_IS_HEART_RATE_ONE";//自动测试心率1
	public final static String KEY_IS_HEART_RATE_TWO = "KEY_IS_HEART_RATE_TWO";//自动测试心率2
	public final static String KEY_IS_HEART_RATE_THREE = "KEY_IS_HEART_RATE_THREE";////自动测试心率3


	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	private HeartTiming heartTiming;

	private EasySwitchButton esbIsEnable,esbOne,esbTwo,esbThree;
	private TextView tvOneStart,tvOneEnd,tvTwoStart,tvTwoEnd,tvThreeStart,tvThreeEnd;
	private View heartView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_auto_heartrate);

		sharedPreferences = getSharedPreferences(AUTOHEART_TEST_CONFIG,MODE_PRIVATE);
		editor = sharedPreferences.edit();

		initControl();
		initValue();
		EventBus.getDefault().register(this);
	}

	private void initControl(){
		esbIsEnable = (EasySwitchButton) findViewById(R.id.switch_whole_heartRate);
		esbOne = (EasySwitchButton) findViewById(R.id.switch_heartRate1);
		esbTwo = (EasySwitchButton) findViewById(R.id.switch_heartRate2);
		esbThree = (EasySwitchButton) findViewById(R.id.switch_heartRate3);

		esbIsEnable.setOnCheckChangedListener(this);
		esbOne.setOnCheckChangedListener(this);
		esbTwo.setOnCheckChangedListener(this);
		esbThree.setOnCheckChangedListener(this);

		tvOneStart = (TextView) findViewById(R.id.tv_start_time1);
		tvOneEnd = (TextView) findViewById(R.id.tv_end_time1);

		tvTwoStart = (TextView) findViewById(R.id.tv_start_time2);
		tvTwoEnd = (TextView) findViewById(R.id.tv_end_time2);

		tvThreeStart = (TextView) findViewById(R.id.tv_start_time3);
		tvThreeEnd = (TextView) findViewById(R.id.tv_end_time3);

		tvOneEnd.setOnClickListener(this);
		tvOneStart.setOnClickListener(this);
		tvTwoEnd.setOnClickListener(this);
		tvTwoStart.setOnClickListener(this);
		tvThreeEnd.setOnClickListener(this);
		tvThreeStart.setOnClickListener(this);

		heartView = findViewById(R.id.layout_sleep_settings);

	}

	private void initValue(){
		boolean isAutoHeart = sharedPreferences.getBoolean(KEY_WHOLE_AUTO_HEARTRATE,false);
		boolean isOne = sharedPreferences.getBoolean(KEY_IS_HEART_RATE_ONE,false);
		boolean isTwo = sharedPreferences.getBoolean(KEY_IS_HEART_RATE_TWO,false);
		boolean isThree = sharedPreferences.getBoolean(KEY_IS_HEART_RATE_THREE,false);

		int oneStartHour = sharedPreferences.getInt(KEY_TIME_START_HOUR_ONE,22);
		int oneStartMin = sharedPreferences.getInt(KEY_TIME_START_MINUTE_ONE,0);
		int oneEndHOur = sharedPreferences.getInt(KEY_TIME_END_HOUR_ONE,6);
		int oneEndMin = sharedPreferences.getInt(KEY_TIME_END_MINUTE_ONE,0);

		int twoStartHour = sharedPreferences.getInt(KEY_TIME_START_HOUR_TWO,13);
		int twoStartMin = sharedPreferences.getInt(KEY_TIME_START_MINUTE_TWO,0);
		int twoEndHOur = sharedPreferences.getInt(KEY_TIME_END_HOUR_TWO,14);
		int twoEndMin = sharedPreferences.getInt(KEY_TIME_END_MINUTE_TWO,0);

		int threeStartHour = sharedPreferences.getInt(KEY_TIME_START_HOUR_THREE,18);
		int threeStartMin = sharedPreferences.getInt(KEY_TIME_START_MINUTE_THREE,0);
		int threeEndHOur = sharedPreferences.getInt(KEY_TIME_END_HOUR_THREE,20);
		int threeEndMin = sharedPreferences.getInt(KEY_TIME_END_MINUTE_THREE,0);

		heartTiming = new HeartTiming(isAutoHeart,isOne,isTwo,isThree,oneStartHour,oneStartMin,oneEndHOur,oneEndMin,
				twoStartHour,twoStartMin,twoEndHOur,twoEndMin,threeStartHour,threeStartMin,threeEndHOur,threeEndMin);
		updateUI();
	}

	private void updateUI(){
		esbIsEnable.setStatus(heartTiming.isEnable());
		esbOne.setStatus(heartTiming.isFirstEnable());
		esbTwo.setStatus(heartTiming.isSecondEnable());
		esbThree.setStatus(heartTiming.isThirdEnable());

		tvOneStart.setText(getTimeString(heartTiming.getFirStartHour(),heartTiming.getFirstStartMin()));
		tvOneEnd.setText(getTimeString(heartTiming.getFirstEndHour(),heartTiming.getFirstEndMin()));

		tvTwoStart.setText(getTimeString(heartTiming.getSecStartHour(),heartTiming.getSecStartMin()));
		tvTwoEnd.setText(getTimeString(heartTiming.getSecEndHour(),heartTiming.getSecEndMin()));

		tvThreeStart.setText(getTimeString(heartTiming.getThirdStartHour(),heartTiming.getThirdStartMin()));
		tvThreeEnd.setText(getTimeString(heartTiming.getThirdEndHour(),heartTiming.getThirdEndMin()));

		heartView.setVisibility(heartTiming.isEnable()?View.VISIBLE:View.GONE);
		tvOneStart.setEnabled(heartTiming.isFirstEnable());
		tvOneEnd.setEnabled(heartTiming.isFirstEnable());
		tvTwoStart.setEnabled(heartTiming.isSecondEnable());
		tvTwoEnd.setEnabled(heartTiming.isSecondEnable());
		tvThreeStart.setEnabled(heartTiming.isThirdEnable());
		tvThreeEnd.setEnabled(heartTiming.isThirdEnable());

	}

	private boolean is24Hour() {
		return android.text.format.DateFormat.is24HourFormat(this);
	}

	public String getTimeString(int hour, int minute) {
		boolean isAm = (hour < 12);
		hour = (is24Hour() ? hour : (hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour)));
		return String.format("%02d", hour) + ":" + String.format("%02d", minute) + (((!is24Hour()) ? (isAm ? getString(R.string.AM) : getString(R.string.PM)) : ""));
	}

	public void onEventMainThread(Message msg){

	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onChecked(View v, boolean isOpened) {
		switch (v.getId()){
			case R.id.switch_whole_heartRate:
				heartTiming.setEnable(isOpened);
				break;
			case R.id.switch_heartRate1:
				heartTiming.setFirstEnable(isOpened);
				break;
			case R.id.switch_heartRate2:
				heartTiming.setSecondEnable(isOpened);
				break;
			case R.id.switch_heartRate3:
				heartTiming.setThirdEnable(isOpened);
				break;
		}
		updateUI();
	}


	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()){
			case R.id.return_back:
				finish();
				break;
			case R.id.text_save:
				save();
				break;
			case R.id.tv_start_time1:
				intent = getIntent(heartTiming.getFirStartHour(),heartTiming.getFirstStartMin());
				startActivityForResult(intent,101);
				break;
			case R.id.tv_start_time2:
				intent = getIntent(heartTiming.getSecStartHour(),heartTiming.getSecStartMin());
				startActivityForResult(intent,102);
				break;
			case R.id.tv_start_time3:
				intent = getIntent(heartTiming.getThirdStartHour(),heartTiming.getThirdStartMin());
				startActivityForResult(intent,103);
				break;
			case R.id.tv_end_time1:
				intent = getIntent(heartTiming.getFirstEndHour(),heartTiming.getFirstEndMin());
				startActivityForResult(intent,104);
				break;
			case R.id.tv_end_time2:
				intent = getIntent(heartTiming.getSecEndHour(),heartTiming.getSecEndMin());
				startActivityForResult(intent,105);
				break;
			case R.id.tv_end_time3:
				intent = getIntent(heartTiming.getThirdEndHour(),heartTiming.getThirdEndMin());
				startActivityForResult(intent,106);
				break;
		}
	}

	public Intent getIntent(int start,int end){
		Intent intent = new Intent(this,DialogSetTime.class);
		intent.putExtra(DialogSetTime.EXTRA_FORMAT,"HH:mm");
		intent.putExtra(DialogSetTime.EXTRA_DATE,String.format("%02d",start)+":"+String.format("%02d",end));
		intent.putExtra(DialogSetTime.EXTRA_IS_24_HOUR,is24Hour());
		intent.putExtra(DialogSetTime.EXTRA_IS_AM,heartTiming.getFirStartHour()<12);
		return intent;
	}

	private void save(){
		if(isConnected()){
			MainService mainService = MainService.getInstance(this);
			editor.putBoolean(KEY_WHOLE_AUTO_HEARTRATE,heartTiming.isEnable()).commit();
			editor.putBoolean(KEY_IS_HEART_RATE_ONE,heartTiming.isFirstEnable()).commit();
			editor.putBoolean(KEY_IS_HEART_RATE_TWO,heartTiming.isSecondEnable()).commit();
			editor.putBoolean(KEY_IS_HEART_RATE_THREE,heartTiming.isThirdEnable()).commit();

			editor.putInt(KEY_TIME_START_HOUR_ONE,heartTiming.getFirStartHour()).commit();
			editor.putInt(KEY_TIME_START_MINUTE_ONE,heartTiming.getFirstStartMin()).commit();
			editor.putInt(KEY_TIME_END_HOUR_ONE,heartTiming.getFirstEndHour()).commit();
			editor.putInt(KEY_TIME_END_MINUTE_ONE,heartTiming.getFirstEndMin()).commit();

			editor.putInt(KEY_TIME_START_HOUR_TWO,heartTiming.getSecStartHour()).commit();
			editor.putInt(KEY_TIME_START_MINUTE_TWO,heartTiming.getSecStartMin()).commit();
			editor.putInt(KEY_TIME_END_HOUR_TWO,heartTiming.getSecEndHour()).commit();
			editor.putInt(KEY_TIME_END_MINUTE_TWO,heartTiming.getSecEndMin()).commit();

			editor.putInt(KEY_TIME_START_HOUR_THREE,heartTiming.getThirdStartHour()).commit();
			editor.putInt(KEY_TIME_START_MINUTE_THREE,heartTiming.getThirdStartMin()).commit();
			editor.putInt(KEY_TIME_END_HOUR_THREE,heartTiming.getThirdEndHour()).commit();
			editor.putInt(KEY_TIME_END_MINUTE_THREE,heartTiming.getThirdEndMin()).commit();

			mainService.setHeartTimingTest(heartTiming);
		}
	}

	private boolean isConnected() {
		MainService mainService = MainService.getInstance(this);
		if (mainService == null || (mainService != null && mainService.getConnectionState() != BaseController.STATE_CONNECTED)) {
			Toast.makeText(this, getString(R.string.please_bind), Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data != null){
			String date = data.getStringExtra(DialogSetTime.EXTRA_DATE);
			boolean isAm = data.getBooleanExtra(DialogSetTime.EXTRA_IS_AM, true);
			boolean is24 = data.getBooleanExtra(DialogSetTime.EXTRA_IS_24_HOUR, true);
			int index = data.getIntExtra(DialogSetTime.EXTRA_INDEX, Integer.valueOf(date.split(":")[0]));
			String[] strs = date.split(":");
			int hour = Integer.valueOf(strs[0]);
			int min = Integer.valueOf(strs[1]);
			if(!is24 && !isAm){
				if(hour<12){
					hour = hour+12;
				}
			}else if(!is24 && isAm && hour == 12){
				hour = 0;
			}
			if(requestCode == 101){
				heartTiming.setFirst(hour,min,heartTiming.getFirstEndHour(),heartTiming.getFirstEndMin());
			}else if(requestCode == 102){
				heartTiming.setSecond(hour,min,heartTiming.getSecEndHour(),heartTiming.getSecEndMin());
			}else if(requestCode == 103){
				heartTiming.setThird(hour,min,heartTiming.getThirdEndHour(),heartTiming.getThirdEndMin());
			}else if(requestCode == 104){
				heartTiming.setFirst(heartTiming.getFirStartHour(),heartTiming.getFirstStartMin(),hour,min);
			}else if(requestCode == 105){
				heartTiming.setSecond(heartTiming.getSecStartHour(),heartTiming.getSecStartMin(),hour,min);
			}else if(requestCode == 106){
				heartTiming.setThird(heartTiming.getThirdStartHour(),heartTiming.getThirdStartMin(),hour,min);
			}
			updateUI();
		}
	}
}