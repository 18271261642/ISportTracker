package com.isport.isportlibrary.entry;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Created by Marcos Cheng on 2016/8/24.
 * auto test heartrate, 24-hour
 */
public class HeartTiming implements Parcelable {

    private boolean isEnable;
    private boolean isFirstEnable;
    private boolean isSecondEnable;
    private boolean isThirdEnable;
    private int firstStartHour;
    private int firstStartMin;
    private int firstEndHour;
    private int firstEndMin;
    private int secStartHour;
    private int secStartMin;
    private int secEndHour;
    private int secEndMin;
    private int thirdStartHour;
    private int thirdStartMin;
    private int thirdEndHour;
    private int thirdEndMin;

    public HeartTiming(){

    }

    /**
     * Time can not cross
     * first --> morning
     * second --> noon
     * third --> afternoon
     *
     * @param isEnable
     * @param isFirstEnable
     * @param isSecondEnable
     * @param isThirdEnable
     * @param firstStartHour
     * @param firstStartMin
     * @param firstEndHour
     * @param firstEndMin
     * @param secStartHour
     * @param secStartMin
     * @param secEndHour
     * @param secEndMin
     * @param thirdStartHour
     * @param thirdStartMin
     * @param thirdEndHour
     * @param thirdEndMin
     */
    public HeartTiming(boolean isEnable, boolean isFirstEnable, boolean isSecondEnable, boolean isThirdEnable,
                       int firstStartHour, int firstStartMin, int firstEndHour, int firstEndMin,
                       int secStartHour, int secStartMin, int secEndHour, int secEndMin,
                       int thirdStartHour, int thirdStartMin, int thirdEndHour, int thirdEndMin) {
        this.isEnable = isEnable;
        this.isFirstEnable = isFirstEnable;
        this.isSecondEnable = isSecondEnable;
        this.isThirdEnable = isThirdEnable;
        this.firstStartHour = firstStartHour;
        this.firstStartMin = firstStartMin;
        this.firstEndHour = firstEndHour;
        this.firstEndMin = firstEndMin;
        this.secStartHour = secStartHour;
        this.secStartMin = secStartMin;
        this.secEndHour = secEndHour;
        this.secEndMin = secEndMin;
        this.thirdStartHour = thirdStartHour;
        this.thirdStartMin = thirdStartMin;
        this.thirdEndHour = thirdEndHour;
        this.thirdEndMin = thirdEndMin;
    }

    /**
     *     ///first interval
     * @param startHour
     * @param startMin
     * @param endHour
     * @param endMin
     */
    public void setFirst(int startHour,int startMin,int endHour,int endMin){
        this.firstStartHour = startHour;
        this.firstStartMin = startMin;
        this.firstEndHour = endHour;
        this.firstEndMin = endMin;
    }

    ///second interval
    public void setSecond(int startHour,int startMin,int endHour,int endMin){
        this.secStartHour = startHour;
        this.secStartMin = startMin;
        this.secEndHour = endHour;
        this.secEndMin = endMin;
    }

    ///third interval
    public void setThird(int startHour,int startMin,int endHour,int endMin){
        this.thirdStartHour = startHour;
        this.thirdStartMin = startMin;
        this.thirdEndHour = endHour;
        this.thirdEndMin = endMin;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public void setFirstEnable(boolean firstEnable) {
        isFirstEnable = firstEnable;
    }

    public void setSecondEnable(boolean secondEnable) {
        isSecondEnable = secondEnable;
    }

    public void setThirdEnable(boolean thirdEnable) {
        isThirdEnable = thirdEnable;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public boolean isFirstEnable() {
        return isFirstEnable;
    }

    public boolean isSecondEnable() {
        return isSecondEnable;
    }

    public boolean isThirdEnable() {
        return isThirdEnable;
    }

    public int getFirStartHour() {
        return firstStartHour;
    }

    public int getFirstStartMin() {
        return firstStartMin;
    }

    public int getFirstEndHour() {
        return firstEndHour;
    }

    public int getFirstEndMin() {
        return firstEndMin;
    }

    public int getSecStartHour() {
        return secStartHour;
    }

    public int getSecStartMin() {
        return secStartMin;
    }

    public int getSecEndHour() {
        return secEndHour;
    }

    public int getSecEndMin() {
        return secEndMin;
    }

    public int getThirdStartHour() {
        return thirdStartHour;
    }

    public int getThirdStartMin() {
        return thirdStartMin;
    }

    public int getThirdEndHour() {
        return thirdEndHour;
    }

    public int getThirdEndMin() {
        return thirdEndMin;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBooleanArray(new boolean[]{isEnable,isFirstEnable,isSecondEnable,isThirdEnable});
        parcel.writeIntArray(new int[]{firstStartHour , firstStartMin ,firstEndHour, firstEndMin ,
                secStartHour, secStartMin, secEndHour, secEndMin, thirdStartHour, thirdStartMin ,thirdEndHour, thirdEndMin});
    }

    public static final Creator<HeartTiming> CREATOR = new Creator<HeartTiming>() {

        @Override
        public HeartTiming createFromParcel(Parcel parcel) {
            HeartTiming heartTiming = new HeartTiming();
            boolean[] stats = new boolean[4];
            parcel.readBooleanArray(stats);
            int[] values = new int[12];
            parcel.readIntArray(values);
            heartTiming.isEnable = stats[0];
            heartTiming.isFirstEnable = stats[1];
            heartTiming.isSecondEnable = stats[2];
            heartTiming.isThirdEnable = stats[3];

            heartTiming.firstStartHour = values[0];
            heartTiming.firstStartMin = values[1];
            heartTiming.firstEndHour = values[2];
            heartTiming.firstEndMin = values[3];

            heartTiming.secStartHour = values[4];
            heartTiming.secStartMin = values[5];
            heartTiming.secEndHour = values[6];
            heartTiming.secEndMin = values[7];

            heartTiming.thirdStartHour = values[8];
            heartTiming.thirdEndMin = values[9];
            heartTiming.thirdEndHour = values[10];
            heartTiming.thirdEndMin = values[11];

            return heartTiming;
        }

        @Override
        public HeartTiming[] newArray(int i) {
            return new HeartTiming[i];
        }
    };
}
