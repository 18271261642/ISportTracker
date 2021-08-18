package com.isport.tracker.main;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.db.DbHeart;
import com.isport.tracker.entity.HeartHistory;
import com.isport.tracker.entity.ProgressEntry;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.TimeUtils;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/7.
 */

public class HeartHistoryActivity extends BaseActivity implements View.OnClickListener, AdapterView
        .OnItemClickListener {

    private boolean isDeleteState;///是否处于待删除状态
    private TextView tvDelete;
    private Button btnDelete;
    private List<HeartHistory> historyList;
    private List<HeartHistory> tempList;///临时
    private ListView heartListView;
    private HeartHistAdapter adapter;
    private ProgressDialog progressDialog;
    boolean isFirst = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_hist);

        tvDelete = (TextView) findViewById(R.id.heart_hist_tv_delete);
        btnDelete = (Button) findViewById(R.id.heart_hist_btn_delete);
        heartListView = (ListView) findViewById(R.id.heart_hist_listview);
        heartListView.setOnItemClickListener(this);
        btnDelete.setVisibility(View.GONE);
        historyList = new ArrayList<>();
        tempList = new ArrayList<>();

        adapter = new HeartHistAdapter();
        heartListView.setAdapter(adapter);
        initProgressDialog();
        EventBus.getDefault().register(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
        filter.addAction(MainService.ACTION_HEART_HISTORY_SYNCED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
        MainService mainService = MainService.getInstance(this);
        if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED &&
                mainService.getCurrentDevice() != null && mainService.getCurrentDevice().getDeviceType() ==
                BaseDevice.TYPE_AS97) {
            if (mainService.getCurrentDevice().getName().equalsIgnoreCase("AS97") || mainService.getCurrentDevice().getName().equalsIgnoreCase("SAS87")) {
                mainService.queryHeartHist((byte) 1);
                updateProgressDialog(0);
            } else if (mainService.getCurrentDevice().getName().contains("W311N_") && ConfigHelper.getInstance(this)
                    .getBoolean(Constants.IS906901, false)) {
                mainService.queryHeartHist((byte) 1);
                updateProgressDialog(0);
            }
        }
        loadData();
    }

    public void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setMessage(getString(R.string.syncing_heartrate_data));
    }

    public void updateProgressDialog(int progress) {
        if (!progressDialog.isShowing())
            progressDialog.show();
        progressDialog.setProgress(progress);
        if (progress >= 100 && progressDialog.isShowing()) {
            Toast.makeText(this, getString(R.string.successful_synchronization), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler.hasMessages(0x01)) {
            handler.removeMessages(0x01);
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    public void onEventMainThread(ProgressEntry entry) {
        if (entry != null) {
            if (entry.getProgress() == -1) {
//                Toast.makeText(this, getString(R.string.synchronization_failure), Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } else if (entry.getProgress() == -2) {
                Toast.makeText(this, getString(R.string.sync_heart_nodata), Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } else {
                if (isFirst) {
                    isFirst = false;
                    Toast.makeText(HeartHistoryActivity.this, getString(R.string.sync_heart_save), Toast.LENGTH_LONG)
                            .show();
                }
                updateProgressDialog(entry.getProgress());
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            adapter.notifyDataSetChanged();
        }
    };

    private void loadData() {
        MainService mainService = MainService.getInstance(this);
        if (mainService != null && mainService.getCurrentDevice() != null) {
            final String mac = mainService.getCurrentDevice().getMac();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    historyList = DbHeart.getIntance().getListHistory(DbHeart.COLUMN_MAC + "=?", new String[]{mac},
                            null, null, "datetime(" + DbHeart.COLUMN_DATE +
                                    ") DESC");
                    handler.sendEmptyMessage(0x01);
                }
            }).start();
        }
    }

    public void delete() {
        if (tempList != null && tempList.size() > 0) {
            DbHeart.getIntance().delete(tempList);
            for (int i = 0; i < tempList.size(); i++) {
                historyList.remove(tempList.get(i));
            }
            isDeleteState = false;
            btnDelete.setVisibility(View.GONE);
            tvDelete.setText(getString(R.string.delete));
            for (int i = 0; i < historyList.size(); i++) {
                historyList.get(i).setSelected(false);
            }
            adapter.notifyDataSetChanged();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DbHeart.getIntance().delete(tempList);
                }
            }).start();

            tempList.clear();
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.heart_hist_btn_delete:
                delete();
                break;
            case R.id.heart_hist_tv_delete:
                if (isDeleteState) {
                    isDeleteState = false;
                    btnDelete.setVisibility(View.GONE);
                    tvDelete.setText(getString(R.string.delete));
                    for (int i = 0; i < historyList.size(); i++) {
                        historyList.get(i).setSelected(false);
                    }
                    adapter.notifyDataSetChanged();
                    tempList.clear();
                } else {
                    isDeleteState = true;
                    btnDelete.setVisibility(View.VISIBLE);
                    tvDelete.setText(getString(R.string.cancel));
                    adapter.notifyDataSetChanged();
                    tempList.clear();
                }
                break;
            case R.id.return_back:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!isDeleteState) {
            Intent intent = new Intent(this, HeartRateHistoryDetailActivity.class);
            intent.putExtra("mac", historyList.get(position).getMac());
            intent.putExtra("time", historyList.get(position).getStartDate());

            startActivity(intent);
        } else {
            historyList.get(position).setSelected(!historyList.get(position).isSelected());
            ((HeartHistAdapter) heartListView.getAdapter()).notifyDataSetChanged();
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MainService.ACTION_HEART_HISTORY_SYNCED)) {
                loadData();
            } else if (action.equals(MainService.ACTION_CONNECTE_CHANGE)) {
                int state = intent.getIntExtra(MainService.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
                if (state != BaseController.STATE_CONNECTED) {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }
        }
    };

    private class HeartHistAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return historyList.size();
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
            HistHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(HeartHistoryActivity.this).inflate(R.layout.item_heart_hist, null);
                holder = new HistHolder();
                holder.tvDate = (TextView) convertView.findViewById(R.id.item_heart_hist_tv_date);
                holder.tvAvg = (TextView) convertView.findViewById(R.id.item_heart_hist_tv_avg);
                holder.tvMax = (TextView) convertView.findViewById(R.id.item_heart_hist_tv_max);
                holder.tvMin = (TextView) convertView.findViewById(R.id.item_heart_hist_tv_min);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.item_heart_checkbox);
                convertView.setTag(holder);
            }
            final HeartHistory history = historyList.get(position);
            //历史数据存储
            long time = history.getIsHistory() == 0 ? history.getSize() : history.getSize() * 5;
            holder = (HistHolder) convertView.getTag();
//            String date = history.getStartDate().split(" ")[1];
            String date = history.getStartDate();
            //获取开始时间的时间戳
            long startLong = TimeUtils.changeStrDateToLongDate(date);
            long endLong = startLong + time;
            String startDateStr = TimeUtils.unixTimeToBeijingTime(startLong);
            String endDateStr = TimeUtils.unixTimeToBeijingTime(endLong);
//            holder.tvDate.setText(UtilTools.getDateFormat().format(UtilTools.string2Date(history.getStartDate(),
// "yyyy-MM-dd HH:mm:ss"))+" "+date);
            holder.tvDate.setText(startDateStr + " - " + endDateStr);
            holder.tvAvg.setText(getString(R.string.hr_avg, history.getAvg()));
            holder.tvMax.setText(getString(R.string.hr_max, history.getMax()));
            holder.tvMin.setText(getString(R.string.hr_min, history.getMin()));
            holder.checkBox.setVisibility(isDeleteState ? View.VISIBLE : View.GONE);
            holder.checkBox.setChecked(historyList.get(position).isSelected());
            final int pos = position;
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        tempList.add(history);
                    } else {
                        tempList.remove(history);
                    }
                    if (pos < historyList.size()) {
                        historyList.get(pos).setSelected(isChecked);
                    }
                }
            });
            return convertView;
        }
    }

    class HistHolder {
        CheckBox checkBox;
        TextView tvDate;
        TextView tvMax;
        TextView tvAvg;
        TextView tvMin;
    }
}
