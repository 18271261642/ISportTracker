package com.isport.isportlibrary.entry;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Created by Marcos Cheng on 2017/8/16.
 * Heart Record read from Device
 */

public class HeartRecord implements Serializable {

    /**
     * mac of device
     */
    private String mac;
    /**
     * when to start monitor heart data
     */
    private String startTime;
    /**
     * heart data list
     */
    private ArrayList<HeartData> dataList;
    /**
     * max heart rate
     */
    private int max;
    /**
     * min heart rate
     */
    private int min;
    /**
     * avg heart rate
     */
    private int avg;
    /**
     * total heart rate
     */
    private int total;

    public HeartRecord() {

    }

    public HeartRecord(String mac,String time, ArrayList<HeartData> list) {
        this.mac = mac;
        this.startTime = time;
        this.dataList = list;
    }


    /**
     *
     * @return max heart rate
     */
    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    /**
     *
     * @return min heart rate
     */
    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    /**
     *
     * @return avg heart rate
     */
    public int getAvg() {
        return avg;
    }

    public void setAvg(int avg) {
        this.avg = avg;
    }

    /**
     *
     * @return total heart rate, add all
     */
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    /**
     *
     *  @return format is yyyy-MM-dd HH:mm:ss
     */
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     *
     * @return heart data list
     */
    public ArrayList<HeartData> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<HeartData> dataList) {
        this.dataList = dataList;
    }
}
