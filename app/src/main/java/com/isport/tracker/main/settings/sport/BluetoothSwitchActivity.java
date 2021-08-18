package com.isport.tracker.main.settings.sport;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.dialogActivity.DialogSetTimingAlarm;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.util.DeviceConfiger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2016/12/12.
 */
public class BluetoothSwitchActivity extends BaseActivity implements View.OnClickListener,RadioGroup.OnCheckedChangeListener,AdapterView.OnItemClickListener{

    private String SWITCH_CONFIG = "switchconfig";
    private String SWITCH_MODE = "switch_mode";
    private TextView tvSwitchMethod;
    private ListView listView;
    private RadioGroup rgMethods;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private SwitchAdapter switchAdapter;
    private Map<Integer,Integer> listMap;
    private ProgressDialog progressDialog;
    private int mode = 0;//00—手动启动，01—实时启动,02--定时启动

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetoothswitch);
        progressDialog = new ProgressDialog(this);
        sharedPreferences = getSharedPreferences(SWITCH_CONFIG, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mode = sharedPreferences.getInt(SWITCH_MODE,0);
        initControl();
    }

    private void initControl(){
        listMap = new HashMap<Integer,Integer>();
        for (int i=0;i<15;i++){
            listMap.put(i,sharedPreferences.getInt(SWITCH_CONFIG+""+i,144));
        }

        tvSwitchMethod = (TextView) findViewById(R.id.tv_manual_method);
        listView = (ListView) findViewById(R.id.bluetooth_listview);
        if(mode != 2){
            listView.setVisibility(View.GONE);
        }
        switchAdapter = new SwitchAdapter();
        listView.setAdapter(switchAdapter);
        listView.setOnItemClickListener(this);
        switchAdapter.notifyDataSetChanged();
        rgMethods = (RadioGroup) findViewById(R.id.switch_rg);
        rgMethods.setOnCheckedChangeListener(this);

        switch (mode){
            case 0:
                rgMethods.check(R.id.switch_rb_1);
                break;
            case 1:
                rgMethods.check(R.id.switch_rb_2);
                break;
            case 2:
                rgMethods.check(R.id.switch_rb_3);
                break;
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_tv:
                finish();
                break;
            case R.id.switch_save:
                save();
                break;
        }
    }

    public void save(){
        MainService mainService = MainService.getInstance(this);
        if(mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED){
            switch (rgMethods.getCheckedRadioButtonId()){
                case R.id.switch_rb_1:
                    mode = 0;
                    break;
                case R.id.switch_rb_2:
                    mode = 1;
                    break;
                case R.id.switch_rb_3:
                    mode = 2;
                    break;
            }
            editor.putInt(SWITCH_MODE,mode).commit();
            Set<Integer> set = listMap.keySet();
            List<Integer> list = new ArrayList<>();
            for (Integer index:set){
                editor.putInt(SWITCH_CONFIG+""+index,listMap.get(index)).commit();
                list.add(listMap.get(index));
            }
            byte[] values = new byte[20];
            values[0] = (byte) 0xbe;
            values[1] = 0x01;
            values[2] = 0x06;
            values[3] = (byte) 0xfe;
            values[4] = (byte) mode;
            int startIndex = 5;
            for (int i=0;i<list.size();i++){
                if (list.get(i) == 144){
                    continue;
                }
                values[startIndex] = (byte) list.get(i).intValue();
                startIndex++;
            }
            if(startIndex<20){
                for (int i=startIndex;i<20;i++){
                    values[startIndex] = (byte) 0xff;
                    startIndex++;
                }
            }
            mainService.sendCustomCmd(values);
            /*20Bytes BE+01+06+FE+蓝牙启动方式（1byte）+定时启动1（1byte）
            +定时启动2（1byte）+定时启动3（1byte）+定时启动4（1byte）
            +定时启动5（1byte）+定时启动6（1byte）+定时启动7（1byte）
            +定时启动8（1byte）+定时启动9（1byte）+定时启动10（1byte）
            +定时启动11（1byte）+定时启动12（1byte）+定时启动13（1byte）
            +定时启动14（1byte）+定时启动15（1byte）*/
            progressDialog.setMessage(getString(R.string.setting));
            progressDialog.show();
            handler.sendEmptyMessageDelayed(0x01, 5000);

        }else {
            Toast.makeText(BluetoothSwitchActivity.this, getResources().getString(R.string.please_connect), Toast.LENGTH_SHORT).show();
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch(checkedId){
            case R.id.switch_rb_1:
                listView.setVisibility(View.GONE);
                break;
            case R.id.switch_rb_2:
                listView.setVisibility(View.GONE);
                break;
            case R.id.switch_rb_3:
                listView.setVisibility(View.VISIBLE);
                switchAdapter.notifyDataSetChanged();
                break;
        }

    }

    int curPos;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        curPos = position;
        Intent intent = new Intent(this, DialogSetTimingAlarm.class);
        int timeindex = listMap.get(position);
        intent.putExtra("time",timeindex);
        intent.putExtra("index",position);
        startActivityForResult(intent,200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null){
            int index = data.getIntExtra("index",0);
            int timeindex = data.getIntExtra("timeindex",144);//144g关
            String time = data.getStringExtra("time");
            if(timeindex == 144){
                if (listMap.keySet().contains(index)) {
                    listMap.remove(index);
                    listMap.put(index, timeindex);
                    switchAdapter.notifyDataSetChanged();
                }
            }else {
                if (!listMap.values().contains(timeindex)) {
                    listMap.remove(index);
                    listMap.put(index, timeindex);
                    switchAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private class SwitchAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 15;
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
            if(convertView == null){
                TextView textView = new TextView(BluetoothSwitchActivity.this);
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.WRAP_CONTENT);
                textView.setPadding(DeviceConfiger.dp2px(10),DeviceConfiger.dp2px(10),DeviceConfiger.dp2px(10),DeviceConfiger.dp2px(10));
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(DeviceConfiger.dp2sp(16));
                convertView = textView;
                holder = new Holder();
                holder.textView = textView;
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();
            int timeindex = listMap.get(position);
            if(timeindex != 144) {
                String tp = String.format("%02d",(timeindex / 6)) + ":" + String.format("%02d",(timeindex * 10) % 60);
                holder.textView.setText(tp);
            }else {
                holder.textView.setText(getString(R.string.close));
            }

            return convertView;
        }

        class Holder {
            TextView textView;
        }
    }

}
