package com.isport.isportlibrary.entry;

/**
 * @创建者 bear
 * @创建时间 2019/4/19 16:22
 * @描述
 */
public class HistorySportN extends EntityBase {

    private String dateString;
    private String mac;
    private int    stepNum;
    private int    sleepState;
    private int    heartRate;
    private int    index;

    public HistorySportN() {

    }

    public HistorySportN(String mac, String dateString, int stepNum, int sleepState, int heartRate, int index) {
        this.mac = mac;
        this.dateString = dateString;
        this.stepNum = stepNum;
        this.sleepState = sleepState;
        this.heartRate = heartRate;
        this.index = index;
    }

    @Override
    public String toString() {
        return "HistorySportN{" +
                "index=" + index +
                ", dateString='" + dateString + '\'' +
                ", mac='" + mac + '\'' +
                ", stepNum=" + stepNum +
                ", sleepState=" + sleepState +
                ", heartRate=" + heartRate +
                '}';
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getStepNum() {
        return stepNum;
    }

    public void setStepNum(int stepNum) {
        this.stepNum = stepNum;
    }

    public int getSleepState() {
        return sleepState;
    }

    public void setSleepState(int sleepState) {
        this.sleepState = sleepState;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
}
