package com.isport.tracker.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.tracker.R;
import com.isport.tracker.view.CalendarView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarTestActivity extends BaseActivity {
	private CalendarView calendar;
	private ImageButton calendarLeft;
	private TextView calendarCenter;
	private ImageButton calendarRight;
	private SimpleDateFormat format;

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_calendar);
        Calendar calendar = (Calendar)getIntent().getSerializableExtra("calendar");
        format = new SimpleDateFormat("yyyy-MM-dd");
		//获取日历控件对象
		this.calendar = (CalendarView)findViewById(R.id.calendar);
		this.calendar.setSelectMore(false); //单选
		
		calendarLeft = (ImageButton)findViewById(R.id.calendarLeft);
		calendarCenter = (TextView)findViewById(R.id.calendarCenter);
		calendarRight = (ImageButton)findViewById(R.id.calendarRight);
		/*try {
			//设置日历日期
			Date date = format.parse("2015-01-01");
			calendar.setCalendarData(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}*/
		this.calendar.setCurDate(calendar.getTime());
		//获取日历中年月 ya[0]为年，ya[1]为月（格式大家可以自行在日历控件中改）
		String[] ya = this.calendar.getYearAndmonth().split("-");
		if(getResources().getConfiguration().locale.getCountry().equals("CN")){
			calendarCenter.setText(ya[0]+getResources().getString(R.string.year)+ya[1]+getResources().getString(R.string.month));
		}else{
			calendarCenter.setText(ya[1]+"/"+ya[0]);
		}
		
		calendarLeft.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//点击上一月 同样返回年月 
				String leftYearAndmonth = CalendarTestActivity.this.calendar.clickLeftMonth();
				String[] ya = leftYearAndmonth.split("-"); 
				if(getResources().getConfiguration().locale.getCountry().equals("CN")){
					calendarCenter.setText(ya[0]+getResources().getString(R.string.year)+ya[1]+getResources().getString(R.string.month));
				}else{
					calendarCenter.setText(ya[1]+"/"+ya[0]);
				}
				//calendarCenter.setText(ya[0]+getResources().getString(R.string.year)+ya[1]+getResources().getString(R.string.month));
			}
		});
		
		calendarRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//点击下一月
				String rightYearAndmonth = CalendarTestActivity.this.calendar.clickRightMonth();
				String[] ya = rightYearAndmonth.split("-"); 
				if(getResources().getConfiguration().locale.getCountry().equals("CN")){
					calendarCenter.setText(ya[0]+getResources().getString(R.string.year)+ya[1]+getResources().getString(R.string.month));
				}else{
					calendarCenter.setText(ya[1]+"/"+ya[0]);
				}
			}
		});
		
		//设置控件监听，可以监听到点击的每一天（大家也可以在控件中根据需求设定）
		this.calendar.setOnItemClickListener(new CalendarView.OnItemClickListener() {
			
			@Override
			public void OnItemClick(Date selectedStartDate,
					Date selectedEndDate, Date downDate) {
				if(CalendarTestActivity.this.calendar.isSelectMore()){
					Toast.makeText(getApplicationContext(), format.format(selectedStartDate)+"到"+format.format(selectedEndDate), Toast.LENGTH_SHORT).show();
				}else{
					//Toast.makeText(getApplicationContext(), format.format(downDate), Toast.LENGTH_SHORT).show();
					Log.i("date", "date down = " + format.format(downDate));
					
					Intent intent = new Intent();
                    Calendar calendar1=Calendar.getInstance();
                    calendar1.setTime(downDate);
					intent.putExtra("calendar", calendar1);
					setResult(10, intent);
					CalendarTestActivity.this.finish();
				}
			}
		});
		
	}
}
