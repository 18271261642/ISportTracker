package com.isport.tracker.bluetooth;

import android.app.Activity;

import com.isport.tracker.main.DfuNotiActvity;

import no.nordicsemi.android.dfu.DfuBaseService;

/**
 * Created by Administrator on 2016/11/1.
 */

public class DfuService extends DfuBaseService {
    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        return DfuNotiActvity.class;
    }
}
