package com.isport.isportlibrary.controller;

import android.bluetooth.BluetoothDevice;

import com.isport.isportlibrary.entry.SportDayData;

/**
 * @author Created by Marcos Cheng on 2016/8/25.
 * connection state
 */
public interface IBleCmdCallback {

    /**
     * if state of connection changed it will be called
     * @param device the device of current connected
     * @param state state of connection
     */
    public void connectState(BluetoothDevice device, int state);


    public void reconnect();

    public void closeBluAndOpenBlu();


    /**
     *  if state of connection changed it will be called
     * @param device the device of current connected
     * @param state state of connection
     */
    public void connectionError(BluetoothDevice device,int state);

    /**
     * if sync completed or error appear , it will be called
     * see {@link BaseController#STATE_SYNC_COMPLETED}, {@link BaseController#STATE_SYNC_ERROR} , {@link BaseController#STATE_SYNC_TIMEOUT}
     * {@link BaseController#STATE_SYNCING}
     * @param state state of sync
     */
    public void syncState(int state);



    /**
     * real time day data
     * @param dayData
     */
    public void realTimeDayData(SportDayData dayData);

    /**
     * need regrist device info setting callback
     */
    public void needDeviceInfoSettingCallBack();
}
