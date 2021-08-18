package com.isport.isportlibrary.entry;

import java.io.Serializable;

/**
 * @author Created by Marcos Cheng on 2016/12/21.
 * data of real-time
 */

public class PedoRealData implements Serializable{

    private String dateString;
    private String mac;
    private int pedoNum;
    private int caloric;
    private float distance;

    /**
     *
     * @param dateString datetime
     * @param mac mac of device
     * @param pedoNum step number
     * @param caloric caloric
     * @param distance distance
     */
    public PedoRealData(String dateString, String mac, int pedoNum, int caloric, float distance) {
        this.dateString = dateString;
        this.mac = mac;
        this.pedoNum = pedoNum;
        this.caloric = caloric;
        this.distance = distance;
    }

    /**
     *
     * @return caloric had consumed
     */
    public int getCaloric() {
        return caloric;
    }

    /**
     *
     * @return distance had walked
     */
    public float getDistance() {
        return distance;
    }

    /**
     *
     * @return datetime when produce these data
     */
    public String getDateString() {
        return dateString;
    }

    /**
     *
     * @return mac of ble device
     */
    public String getMac() {
        return mac;
    }

    /**
     * step number
     * @return
     */
    public int getPedoNum() {
        return pedoNum;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setPedoNum(int pedoNum) {
        this.pedoNum = pedoNum;
    }

    public void setCaloric(int caloric) {
        this.caloric = caloric;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
