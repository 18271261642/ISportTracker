package com.isport.tracker.dialogActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.isport.isportlibrary.entry.UserInfo;
import com.isport.tracker.R;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.util.Utils;
import com.isport.tracker.view.TosAdapterView;
import com.isport.tracker.view.TosGallery;
import com.isport.tracker.view.WheelView;

import java.util.ArrayList;
import java.util.List;

public class DialogSetAge extends BaseActivity implements OnClickListener,TosAdapterView.OnItemSelectedListener {

	public final static String EXTRA_TYPE = "extra_type";
	public final static String EXTRA_AGE = "extra_age";
	public final static String EXTRA_STRIDE = "extra_stride";
	public final static String EXTRA_COUNTDOWN = "extra_countdown";
	public final static String TYPE_STRIDE = "stride_length";///步长
	public final static String TYPE_AGE = "type_age";
	public final static String TYPE_ALARM_REPEAT = "type_alarm_repeat";
	public final static String TYPE_COUNTDOWN = "count_down_timer";

	private Button mPickerok = null;
	private Button mPickeresc = null;
	private Boolean is_from_foot;
	private TextView mTvTitle;
	private TextView mTvUnit;
	private String type;
	private List<Integer> mAgeList = new ArrayList<Integer>();
	int src = 0;
	WheelView np = null;
	private int mNormalColor;
	int metric;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_light);

		/*int divierId = this.getResources().getIdentifier("android:id/titleDivider", null, null);
	    View divider = findViewById(divierId);
	    divider.setBackgroundColor(Color.parseColor("#754c50"));*/
		mNormalColor = getResources().getColor(R.color.gray_text);
		type = getIntent().getStringExtra(DialogSetAge.EXTRA_TYPE);
		UserInfo userInfo = UserInfo.getInstance(this);
		metric = userInfo.getMetricImperial();
		mPickerok = (Button) findViewById(R.id.numberPickerok);
		mPickeresc = (Button) findViewById(R.id.numberPickercanle);
		//mTvUnit = (TextView) findViewById(R.id.numberPicker_tv_unit);
		mPickeresc.setOnClickListener(this);
		mPickerok.setOnClickListener(this);

		if(type.equals(TYPE_AGE)) {
			setTitle(getString(R.string.user_info_set_age_title));
		}else if(type.equals(TYPE_STRIDE)) {
			setTitle(getString(R.string.step_length));
		}

		np = (WheelView) findViewById(R.id.numberPicker);

		int countTime = 0;
		int stl = 60;
		int age = getIntent().getIntExtra(EXTRA_AGE, 30);
		if(type.equals(TYPE_STRIDE)) {
			float tp = getIntent().getFloatExtra(EXTRA_STRIDE,60.0f);
			stl = Math.round((metric == 0)?tp:(tp/2.54f));
		}
		if(type.equals(TYPE_COUNTDOWN)){
			countTime = getIntent().getIntExtra(EXTRA_COUNTDOWN,1);
		}

		np.setScrollCycle(true);
		if(type.equals(TYPE_AGE)){
			for (int i=0;i<=60;i++){
				mAgeList.add(i+20);
			}
			np.setAdapter(new WheelAdapter());
			np.setSelection(age-20);
			//mTvUnit.setText(getString(R.string.yrs));
			//mTvTitle.setText(getString(R.string.user_info_set_age_title));
		}else if(type.equals(TYPE_STRIDE)){
			if(metric == 0) {
				for (int i = 30; i <= 150; i++) {
					mAgeList.add(i);
				}
				np.setAdapter(new WheelAdapter());
				np.setSelection(stl - 30);
			}else {
				for (int i = 12; i <= 60; i++) {
					mAgeList.add(i);
				}
				np.setAdapter(new WheelAdapter());
				np.setSelection(stl - 12);
			}
			//mTvUnit.setText(getString(R.string.ride_cm));
			//mTvTitle.setText(R.string.set_the_pace);
		}else if(type.equals(TYPE_COUNTDOWN)){
			for (int i=1;i<=60;i++){
				mAgeList.add(i);
			}
			np.setAdapter(new WheelAdapter());
			np.setSelection(countTime-1);
		}
		np.setOnItemSelectedListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			case R.id.numberPickerok:
				if(type.equals(TYPE_AGE)) {
					Intent intent = new Intent();
					intent.putExtra(EXTRA_AGE,""+mAgeList.get(np.getSelectedItemPosition()));
					setResult(214,intent);
				}else if(type.equals(TYPE_STRIDE)){
					Intent intent = new Intent();
					int len = (int)(metric == 0?mAgeList.get(np.getSelectedItemPosition()):mAgeList.get(np.getSelectedItemPosition())*2.54);
					intent.putExtra(EXTRA_STRIDE,""+len);
					setResult(215,intent);
				}else if(type.equals(TYPE_COUNTDOWN)){
					Intent intent = new Intent();
					intent.putExtra(EXTRA_COUNTDOWN,""+(np.getSelectedItemPosition()+1));
					setResult(216,intent);
				}

				finish();
				break;
			case R.id.numberPickercanle:
				finish();
				break;

		}
	}

	@Override
	public void onItemSelected(TosAdapterView<?> parent, View view, int position, long id) {
		((WheelAdapter)parent.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onNothingSelected(TosAdapterView<?> parent) {

	}

	class WheelAdapter extends BaseAdapter {
		int mHeight = 40;

		public WheelAdapter(){
			mHeight = (int) Utils.pixelToDp(DialogSetAge.this, mHeight);
		}

		@Override
		public int getCount() {
			return mAgeList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;

			if (null == convertView) {
				holder = new Holder();
				convertView = new TextView(DialogSetAge.this);
				convertView.setLayoutParams(new TosGallery.LayoutParams(-1, mHeight));
				holder.tvAge = (TextView) convertView;
				holder.tvAge.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
				holder.tvAge.setTextColor(Color.BLACK);
				holder.tvAge.setGravity(Gravity.CENTER);
				convertView.setTag(holder);
			}
			holder = (Holder) convertView.getTag();
			String text ="";
			if(type.equals(TYPE_AGE)){
				text = getString(R.string.format_age,mAgeList.get(position)+"");
			}else if(type.equals(TYPE_STRIDE)){
				text = getString(metric==0?R.string.format_cm:R.string.format_inch,mAgeList.get(position)+"");
			}else if(type.equals(TYPE_COUNTDOWN)){
				text = text+getString(R.string.minute);
			}

			holder.tvAge.setText(text);
			if(position == np.getSelectedItemPosition()){
				holder.tvAge.setTextColor(Color.BLACK);
			}else {
				holder.tvAge.setTextColor(mNormalColor);
			}
			return convertView;
		}

		class Holder {
			TextView tvAge;
		}
	}
}