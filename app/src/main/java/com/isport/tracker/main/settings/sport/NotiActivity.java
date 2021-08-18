package com.isport.tracker.main.settings.sport;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.NotificationEntry;
import com.isport.isportlibrary.managers.NotiManager;
import com.isport.isportlibrary.tools.Constants;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.view.EasySwitchButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/28.
 */
public class NotiActivity extends BaseActivity implements View.OnClickListener{

    private ListView mListView;
    private String[] pkNames;
    List<Map<String,Object>> mList;
    NotificationEntry notiEntry;
    private List<String> list337B = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti_settings);
        notiEntry = NotificationEntry.getInstance(this);
        PackageManager manager = getPackageManager();
        List<PackageInfo> installedPackages = manager.getInstalledPackages(0);
        BaseDevice baseDevice = MainService.getInstance(this).getCurrentDevice();
        list337B.add(Constants.KEY_15_PACKAGE_2);
        list337B.add(Constants.KEY_15_PACKAGE_1);
        list337B.add(Constants.KEY_15_PACKAGE);
        list337B.add(Constants.KEY_1B_PACKAGE);
        int type = 0;
        if(baseDevice != null){
            type = baseDevice.getDeviceType();
        }
        mList = new ArrayList<>();
        pkNames = NotiManager.pkNames;
        for (int i=0;i<installedPackages.size();i++){
            PackageInfo info = installedPackages.get(i);
            Log.e("NotiActivity",info.packageName);
            for(int j=0;j<pkNames.length;j++){
                if(info.packageName.equals(pkNames[j])){
                    Map map = new HashMap();
                    map.put("package",info);
                    map.put("isopen", notiEntry.isAllowPackage(pkNames[j],true));
                    if(type == BaseDevice.TYPE_W337B && list337B.contains(pkNames[j])){
                        mList.add(map);
                    }else if(type == BaseDevice.TYPE_W337B){
                        continue;
                    }else {
                        mList.add(map);
                    }
                    break;
                }
            }
            if(mList.size() == pkNames.length){
                //break;
            }
        }
        mListView = (ListView) findViewById(R.id.noti_setting_list);
        mListView.setAdapter(new MyAdapter());
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if(convertView == null ){
                convertView = LayoutInflater.from(NotiActivity.this).inflate(R.layout.activity_noti_item,null);
                holder = new Holder();
                holder.icon = (ImageView) convertView.findViewById(R.id.noti_item_icon);
                holder.name = (TextView) convertView.findViewById(R.id.noti_item_tv_content);
                holder.button = (EasySwitchButton) convertView.findViewById(R.id.noti_item_esw);
                convertView.setTag(holder);
            }
            Map map = mList.get(position);
            final boolean isopen = (boolean) map.get("isopen");
            final PackageInfo info = (PackageInfo) map.get("package");
            holder = (Holder) convertView.getTag();
            holder.button.setStatus(isopen);
            holder.button.setOnCheckChangedListener(new EasySwitchButton.OnOpenedListener() {
                @Override
                public void onChecked(View v, boolean isOpened) {
                    mList.get(position).put("isopen",isOpened);
                    notiEntry.setAllowPackage(info.packageName.toString(),isOpened);
                }
            });
            holder.name.setText(info.applicationInfo.loadLabel(getPackageManager()));
            holder.icon.setBackground(info.applicationInfo.loadIcon(getPackageManager()));

            return convertView;
        }

        class Holder {
            ImageView icon;
            TextView name;
            EasySwitchButton button;
        }
    }
}
