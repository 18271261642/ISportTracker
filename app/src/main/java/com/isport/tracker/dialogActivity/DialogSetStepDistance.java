package com.isport.tracker.dialogActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.isport.tracker.R;
import com.isport.tracker.main.BaseActivity;

public class DialogSetStepDistance extends BaseActivity {
	private RelativeLayout re_man, re_woman;
	private ImageView image_man, image_woman;
	private TextView tv_man, tv_woman;
	private SharedPreferences share = null;
	private Editor edit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_select);
		//share = super.getSharedPreferences(Constants.SHARE_FILE_NAME, Activity.MODE_PRIVATE); // 指定操作的文件名
		edit = share.edit();

		init();
		setDefaultData();
	}

	private void setDefaultData() {
		/*if(getIntent().getBooleanExtra("is_man", true)){
			image_man.setVisibility(View.VISIBLE);
			image_woman.setVisibility(View.INVISIBLE);
			tv_man.setTextColor(Color.parseColor("#3c3c3c"));
			tv_woman.setTextColor(Color.parseColor("#b8b8b8"));
		}else{
			image_man.setVisibility(View.INVISIBLE);
			image_woman.setVisibility(View.VISIBLE);
			tv_man.setTextColor(Color.parseColor("#b8b8b8"));
			tv_woman.setTextColor(Color.parseColor("#3c3c3c"));
		}*/

	}

	private void init() {
		/*tv_man = (TextView) findViewById(R.id.set_sex_man);
		tv_woman = (TextView) findViewById(R.id.set_sex_woman);
		re_man = (RelativeLayout) findViewById(R.id.set_sex_re_man);
		re_woman = (RelativeLayout) findViewById(R.id.set_sex_re_woman);
		image_man = (ImageView) findViewById(R.id.set_sex_man_image);
		image_woman = (ImageView) findViewById(R.id.set_sex_woman_image);
		re_man.setOnClickListener(new OnClickListenerImpl());
		re_woman.setOnClickListener(new OnClickListenerImpl());*/
	}

	private class OnClickListenerImpl implements OnClickListener{
		@Override
		public void onClick(View v) {
			/*Intent intent = new Intent();
			switch (v.getId()) {
			case R.id.set_sex_re_man:
				image_man.setVisibility(View.VISIBLE);
				image_woman.setVisibility(View.INVISIBLE);
				edit.putBoolean("is_man", true).commit();
				intent.putExtra("is_man", true);
				break;
			case R.id.set_sex_re_woman:
				image_man.setVisibility(View.INVISIBLE);
				image_woman.setVisibility(View.VISIBLE);
				edit.putBoolean("is_man", false).commit();
				intent.putExtra("is_man", false);
				break;
			}
			setResult(204, intent);
			finish();*/
		}
	}
}
