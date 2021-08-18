package com.isport.isportlibrary.entry;

/**
 * @author Created by Marcos Cheng on 2016/9/21.
 * total data of some day
 * uint is metric
 * about the time just a reserve information it may be not correctly,for example sleepTime, stillTime,walkTime ,lowSpeedWalkTime and so on.
 * about the sleep time, please read from {@link com.isport.isportlibrary.database.DbHistorySport} and parse it
 */
public class SportDayData extends EntityBase{

    private String dateString;
    private String mac;
    private int totalStep;
    private float totalDist;
    private int totalCaloric;
    private int targetStep;
    private float stridLength;
    private float weight;
    private int sleepTime;
    private int stillTime;
    private int walkTime;
    private int lowSpeedWalkTime;
    private int midSpeedWalkTime;
    private int larSpeedWalkTime;
    private int lowSpeedRunTime;
    private int midSpeedRunTime;
    private int larSpeedRunTime;
    private int totalSportTime;
    private int targetSleep;

    public SportDayData(String mac,String dateString, int totalStep, float totalDist, int totalCaloric, int targetStep,
                        float stridLength, float weight, int sleepTime, int stillTime, int walkTime, int lowSpeedWalkTime,
                        int midSpeedWalkTime, int larSpeedWalkTime, int lowSpeedRunTime, int midSpeedRunTime, int larSpeedRunTime,
                        int totalSportTime, int targetSleep) {
        this.mac = mac;
        this.dateString = dateString;
        this.totalStep = totalStep;
        this.totalDist = totalDist;
        this.totalCaloric = totalCaloric;
        this.targetStep = targetStep;
        this.stridLength = stridLength;
        this.weight = weight;
        this.sleepTime = sleepTime;
        this.stillTime = stillTime;
        this.walkTime = walkTime;
        this.lowSpeedWalkTime = lowSpeedWalkTime;
        this.midSpeedWalkTime = midSpeedWalkTime;
        this.larSpeedWalkTime = larSpeedWalkTime;
        this.lowSpeedRunTime = lowSpeedRunTime;
        this.midSpeedRunTime = midSpeedRunTime;
        this.larSpeedRunTime = larSpeedRunTime;
        this.totalSportTime = totalSportTime;
        this.targetSleep = targetSleep;
    }

    public SportDayData(){

    }

    /**
     *
     * @return like 1990-01-01
     */
    public String getDateString() {
        return dateString;
    }

    public int getTotalStep() {
        return totalStep;
    }

    public float getTotalDist() {
        return totalDist;
    }

    public int getTotalCaloric() {
        return totalCaloric;
    }

    public int getTargetStep() {
        return targetStep;
    }

    public float getStridLength() {
        return stridLength;
    }

    public float getWeight() {
        return weight;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public int getStillTime() {
        return stillTime;
    }

    public int getWalkTime() {
        return walkTime;
    }

    public int getLowSpeedWalkTime() {
        return lowSpeedWalkTime;
    }

    public int getMidSpeedWalkTime() {
        return midSpeedWalkTime;
    }

    public int getLarSpeedWalkTime() {
        return larSpeedWalkTime;
    }

    public int getLowSpeedRunTime() {
        return lowSpeedRunTime;
    }

    public int getMidSpeedRunTime() {
        return midSpeedRunTime;
    }

    public int getLarSpeedRunTime() {
        return larSpeedRunTime;
    }

    public int getTotalSportTime() {
        return totalSportTime;
    }

    public int getTargetSleep() {
        return targetSleep;
    }


    public String getMac() {
        return mac;
    }

    public void setTotalStep(int totalStep) {
        this.totalStep = totalStep;
    }

    public void setTotalDist(float totalDist) {
        this.totalDist = totalDist;
    }

    public void setTotalCaloric(int totalCaloric) {
        this.totalCaloric = totalCaloric;
    }

    public void setTotalSportTime(int totalSportTime) {
        this.totalSportTime = totalSportTime;
    }

    @Override
    public String toString() {
        return "SportDayData{" +
                "dateString='" + dateString + '\'' +
                ", mac='" + mac + '\'' +
                ", totalStep=" + totalStep +
                ", totalDist=" + totalDist +
                ", totalCaloric=" + totalCaloric +
                ", targetStep=" + targetStep +
                ", stridLength=" + stridLength +
                ", weight=" + weight +
                ", sleepTime=" + sleepTime +
                ", stillTime=" + stillTime +
                ", walkTime=" + walkTime +
                ", lowSpeedWalkTime=" + lowSpeedWalkTime +
                ", midSpeedWalkTime=" + midSpeedWalkTime +
                ", larSpeedWalkTime=" + larSpeedWalkTime +
                ", lowSpeedRunTime=" + lowSpeedRunTime +
                ", midSpeedRunTime=" + midSpeedRunTime +
                ", larSpeedRunTime=" + larSpeedRunTime +
                ", totalSportTime=" + totalSportTime +
                ", targetSleep=" + targetSleep +
                '}';
    }
}
