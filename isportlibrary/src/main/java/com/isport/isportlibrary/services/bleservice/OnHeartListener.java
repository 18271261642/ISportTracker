package com.isport.isportlibrary.services.bleservice;

import com.isport.isportlibrary.entry.HeartData;

import java.util.List;

/**
 * @author Created by Marcos Cheng on 2016/8/24.
 * for device that have hreat test function, for example
 **/
public interface OnHeartListener {

    /**
     * the realtime heart data,
     * it will not be called Within a few seconds,for example
     * heartrate of 2016-09-26 11:08:25 is 72
     * and heartrate of 2016-09-26 11:08:27 is 74
     * that you can think that the heart rate of 2016-09-26 11:08:27 is 74
     * @param heartData heart rate info
     */
    public void onHeartChanged(HeartData heartData);

    /**
     * call it after sync heart data
     * @param list
     */
    public void onHistHeartData(List<HeartData> list);
}
