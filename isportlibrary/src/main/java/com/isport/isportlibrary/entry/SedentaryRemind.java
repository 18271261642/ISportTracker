package com.isport.isportlibrary.entry;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Created by Marcos Cheng on 2016/8/24.
 * Sedentary Remind
 * the time that set for sedentary remind shound not repeat with the time that sleep or nap
 * time is 24-hour
 */
public class SedentaryRemind implements Parcelable {

    private boolean isOn;
    /**
     * how long to remind if no exercise,the uint is minute,it must large than 1 minutes
     */
    public static int noExerceseTime;
    private int beginHour;
    private int beginMin;
    private int endHour;
    private int endMin;

    /**
     * 24-hour
     * for example 13:30 - 15:50 , the begin hour is 13, the begin minute is 30,
     * the end hour is 15, the end minute is 50;
     * @param isOn is on or not
     * @param beginHour the hour to begin sedentary remind
     * @param beginMin  the minute to begin sedentary remind
     * @param endHour the hour to end sedentary remind
     * @param endMin the minute to end sedentary remind
     */
    public SedentaryRemind(boolean isOn,int beginHour,int beginMin,int endHour,int endMin){
        this.isOn = isOn;
        this.beginHour = beginHour;
        this.beginMin = beginMin;
        this.endHour = endHour;
        this.endMin = endMin;
    }

    public boolean isOn(){
        return isOn;
    }

    public int getNoExerceseTime() {
        return noExerceseTime;
    }

    public int getBeginHour() {
        return beginHour;
    }

    public int getBeginMin() {
        return beginMin;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMin() {
        return endMin;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public void setNoExerceseTime(int noex) {
        SedentaryRemind.noExerceseTime = noex;
    }

    public void setBeginHour(int beginHour) {
        this.beginHour = beginHour;
    }

    public void setBeginMin(int beginMin) {
        this.beginMin = beginMin;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public void setEndMin(int endMin) {
        this.endMin = endMin;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBooleanArray(new boolean[]{isOn});
        dest.writeInt(beginHour);
        dest.writeInt(beginMin);
        dest.writeInt(endHour);
        dest.writeInt(endMin);
    }

    public static final Creator<SedentaryRemind> CREATOR = new Creator<SedentaryRemind>() {
        @Override
        public SedentaryRemind createFromParcel(Parcel source) {
            boolean[] tpOnS = new boolean[1];
            source.readBooleanArray(tpOnS);
            int tpBeginHour = source.readInt();
            int tpBeginMin = source.readInt();
            int tpEndHour = source.readInt();
            int tpEndMin = source.readInt();
            SedentaryRemind remind = new SedentaryRemind(tpOnS[0],tpBeginHour,tpBeginMin,tpEndHour,tpEndMin);
            return remind;
        }

        @Override
        public SedentaryRemind[] newArray(int size) {
            return new SedentaryRemind[size];
        }
    };
}
