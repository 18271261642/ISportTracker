package com.isport.isportlibrary.entry;

import java.io.Serializable;

/**
 * @author Created by Marcos Cheng on 2016/12/15.
 */

public class SportData337B implements Serializable {

    private String date;
    private String mac;
    private int sportState;//运动状态
    private int speed;///速度
    private int totalStepNum;///总步数
    private int distance;//距离，单位米
    private int calorics;///热量，卡路里
    private int sportTime;///运动时间
    private int deepTime;//深睡时间
    private int lightTime;///浅睡时间
    private int dayRestTime;//白天休息时间
    private int heartRate;///心率时间
    private int bloodOxygen;///血氧

    public SportData337B(String date, String mac, int sportState, int speed, int totalStepNum, int distance, int calorics, int sportTime, int deepTime,
                         int lightTime, int dayRestTime, int heartRate, int bloodOxygen) {
        this.date = date;
        this.mac = mac;
        this.sportState = sportState;
        this.speed = speed;
        this.totalStepNum = totalStepNum;
        this.distance = distance;
        this.calorics = calorics;
        this.deepTime = deepTime;
        this.lightTime = lightTime;
        this.dayRestTime = dayRestTime;
        this.heartRate = heartRate;
        this.bloodOxygen = bloodOxygen;
        this.sportTime = sportTime;
    }

    public String getDate() {
        return date;
    }

    public String getMac() {
        return mac;
    }

    public int getSportTime() {
        return sportTime;
    }

    public int getSportState() {
        return sportState;
    }

    public int getSpeed() {
        return speed;
    }

    public int getTotalStepNum() {
        return totalStepNum;
    }

    public int getDistance() {
        return distance;
    }

    public int getCalorics() {
        return calorics;
    }

    public int getDeepTime() {
        return deepTime;
    }

    public int getLightTime() {
        return lightTime;
    }

    public int getDayRestTime() {
        return dayRestTime;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public int getBloodOxygen() {
        return bloodOxygen;
    }

    public void setSportTime(int sportTime) {
        this.sportTime = sportTime;
    }

    public void setSportState(int sportState) {
        this.sportState = sportState;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setTotalStepNum(int totalStepNum) {
        this.totalStepNum = totalStepNum;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setCalorics(int calorics) {
        this.calorics = calorics;
    }

    public void setDeepTime(int deepTime) {
        this.deepTime = deepTime;
    }

    public void setLightTime(int lightTime) {
        this.lightTime = lightTime;
    }

    public void setDayRestTime(int dayRestTime) {
        this.dayRestTime = dayRestTime;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public void setBloodOxygen(int bloodOxygen) {
        this.bloodOxygen = bloodOxygen;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
