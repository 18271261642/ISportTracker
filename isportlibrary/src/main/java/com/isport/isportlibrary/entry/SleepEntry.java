package com.isport.isportlibrary.entry;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Created by Marcos Cheng on 2017/5/24.
 */

public class SleepEntry implements Parcelable {

    private boolean isAutoSleep;
    private boolean isSleep;
    private boolean isSleepRemind;
    private int sleepRemindTime;
    private int sleepStartHour;
    private int sleepStartMin;
    private int sleepEndHour;
    private int sleepEndMin;
    private boolean isNap;
    private boolean isNapRemind;
    private int napRemindTime;
    private int napStartHour;
    private int naoStartMin;
    private int napEndHour;
    private int napEndMin;
    private int sleepTargetHour;
    private int sleepTargetMin;

    public SleepEntry(){

    }

    /**
     * open auto sleep or not, total switch, if isAutoSleep is false, the isSleep and isNap are false too.
     * @return
     */
    public boolean isAutoSleep() {
        return isAutoSleep;
    }

    /**
     *
     * @return open sleep or  not
     */
    public boolean isSleep() {
        return isSleep;
    }

    /**
     *
     * @return open sleep remind or not
     */
    public boolean isSleepRemind() {
        return isSleepRemind;
    }

    /**
     * how long before sleep to remind that you scheduel.
     * for example, if you scheduel to sleep at 22:30 and the sleepRemindTime is 15,
     * the ble device will to vibrate at 22:15 to remind you should to sleep
     * @return
     */
    public int getSleepRemindTime() {
        return sleepRemindTime;
    }

    /**
     * the begin hour to begin sleep that you scheduel
     * for example, you scheduel to sleep at time 22:30, so the begin hour is 22 and the begin minute is 30
     * @return
     */
    public int getSleepStartHour() {
        return sleepStartHour;
    }

    /**
     * the begin minute to begin sleep that you scheduel
     * @return
     */
    public int getSleepStartMin() {
        return sleepStartMin;
    }

    /**
     * the end hour to end sleep that you scheduel
     * for example, you scheduel to end sleep at time 08:30, so the begin hour is 8 and the begin minute is 30
     * @return
     */
    public int getSleepEndHour() {
        return sleepEndHour;
    }

    /**
     * the end minute to end sleep that you scheduel
     * @return
     */
    public int getSleepEndMin() {
        return sleepEndMin;
    }

    public boolean isNap() {
        return isNap;
    }

    public boolean isNapRemind() {
        return isNapRemind;
    }

    public int getNapRemindTime() {
        return napRemindTime;
    }

    public int getNapStartHour() {
        return napStartHour;
    }

    public int getNaoStartMin() {
        return naoStartMin;
    }

    public int getNapEndHour() {
        return napEndHour;
    }

    public int getNapEndMin() {
        return napEndMin;
    }

    public int getSleepTargetHour() {
        return sleepTargetHour;
    }

    public int getSleepTargetMin() {
        return sleepTargetMin;
    }

    public void setAutoSleep(boolean autoSleep) {
        isAutoSleep = autoSleep;
    }

    public void setSleep(boolean sleep) {
        isSleep = sleep;
    }

    public void setSleepRemind(boolean sleepRemind) {
        isSleepRemind = sleepRemind;
    }

    public void setSleepRemindTime(int sleepRemindTime) {
        this.sleepRemindTime = sleepRemindTime;
    }

    public void setSleepStartHour(int sleepStartHour) {
        this.sleepStartHour = sleepStartHour;
    }

    public void setSleepStartMin(int sleepStartMin) {
        this.sleepStartMin = sleepStartMin;
    }

    public void setSleepEndHour(int sleepEndHour) {
        this.sleepEndHour = sleepEndHour;
    }

    public void setSleepEndMin(int sleepEndMin) {
        this.sleepEndMin = sleepEndMin;
    }

    public void setNap(boolean nap) {
        isNap = nap;
    }

    public void setNapRemind(boolean napRemind) {
        isNapRemind = napRemind;
    }

    public void setNapRemindTime(int napRemindTime) {
        this.napRemindTime = napRemindTime;
    }

    public void setNapStartHour(int napStartHour) {
        this.napStartHour = napStartHour;
    }

    public void setNaoStartMin(int naoStartMin) {
        this.naoStartMin = naoStartMin;
    }

    public void setNapEndHour(int napEndHour) {
        this.napEndHour = napEndHour;
    }

    public void setNapEndMin(int napEndMin) {
        this.napEndMin = napEndMin;
    }

    public void setSleepTargetHour(int sleepTargetHour) {
        this.sleepTargetHour = sleepTargetHour;
    }

    public void setSleepTargetMin(int sleepTargetMin) {
        this.sleepTargetMin = sleepTargetMin;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] bs = new boolean[]{isAutoSleep, isSleep, isSleepRemind,isNap, isNapRemind};
        dest.writeBooleanArray(bs);
        dest.writeInt(sleepRemindTime);
        dest.writeInt(sleepStartHour);
        dest.writeInt(sleepStartMin);
        dest.writeInt(sleepEndHour);
        dest.writeInt(sleepEndMin);

        dest.writeInt(napRemindTime);
        dest.writeInt(napStartHour);
        dest.writeInt(naoStartMin);
        dest.writeInt(napEndHour);
        dest.writeInt(napEndMin);

        dest.writeInt(sleepTargetHour);
        dest.writeInt(sleepTargetMin);
    }

    public static final Creator<SleepEntry> CREATOR = new Creator<SleepEntry>() {
        @Override
        public SleepEntry createFromParcel(Parcel source) {
            boolean[] bs = new boolean[5];
            source.readBooleanArray(bs);
            SleepEntry entry = new SleepEntry();
            entry.setAutoSleep(bs[0]);
            entry.setSleep(bs[1]);
            entry.setSleepRemind(bs[2]);
            entry.setNap(bs[3]);
            entry.setNapRemind(bs[4]);
            entry.setSleepRemindTime(source.readInt());
            entry.setSleepStartHour(source.readInt());
            entry.setSleepStartMin(source.readInt());
            entry.setSleepEndHour(source.readInt());
            entry.setSleepEndMin(source.readInt());

            entry.setNapRemindTime(source.readInt());
            entry.setNapStartHour(source.readInt());
            entry.setNaoStartMin(source.readInt());
            entry.setNapEndHour(source.readInt());
            entry.setNapEndMin(source.readInt());
            return entry;
        }

        @Override
        public SleepEntry[] newArray(int size) {
            return new SleepEntry[size];
        }
    };
}
