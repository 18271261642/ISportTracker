package com.isport.isportlibrary.entry;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Created by Marcos Cheng on 2016/8/24.
 *
 * alarm time is 24-hour
 */
public class AlarmEntry implements Parcelable {

    private String description;
    private int startHour;
    private int startMin;
    private boolean isOn;

    /**
     * from bit 0 to bit 6,represents sunday to saturday in turn
     * if one of bit 0 to bit 6 is 1,the bit7 is 1
     */
    private byte repeat;

    /**
     * from bit 0 to bit 6,represents sunday to saturday in turn
     * if one of bit 0 to bit 6 is 1,the bit7 is 1
     *
     * @param startHour
     * @param startMin
     * @param repeat
     */
    public AlarmEntry(int startHour,int startMin,byte repeat,boolean isOn){
        this.startHour = startHour;
        this.startMin = startMin;
        this.repeat = repeat;
        this.isOn = isOn;
    }

    /**
     * if the device support alram health remind,you can set the description and
     * that it will show on the device when alarm trigger.
     * from bit 0 to bit 6,represents sunday to saturday in turn
     * if one of bit 0 to bit 6 is 1,the bit7 is 1
     * @param description
     * @param startHour
     * @param startMin
     * @param repeat
     */
    public AlarmEntry(String description,int startHour,int startMin,byte repeat,boolean isOn){
        this.description = description;
        this.startHour = startHour;
        this.startMin = startMin;
        this.repeat = repeat;
        this.isOn = isOn;
    }


    public void setDescription(String description){
        this.description = description == null?"":description;
    }

    /**
     * 24-hour
     * @param hour
     */
    public void setStartHour(int hour){
        this.startHour = hour;
    }

    public void setStartMin(int min){
        this.startMin = min;
    }

    public void setOn(boolean isOn){
        this.isOn = isOn;
    }

    /**
     * from low bit to hight ,sun-mon-tue-wed-thu-fri-sat(bit0 - bit6) ,if repeat the bit is 1 or 0,
     * if one of them is repeat,the bit7 is 1
     * @param repeat
     */
    public void setRepeat(byte repeat){
        this.repeat = repeat;
    }

    public String getDescription(){
        return this.description;
    }

    public int getStartHour(){
        return startHour;
    }

    public int getStartMin(){
        return startMin;
    }

    /**
     *
     * @return from low bit to hight bit,sun-mon-tue-wed-thu-fri-sat,repeat 1 or 0
     */
    public byte getRepeat(){
        return repeat;
    }

    public boolean isOn(){
        return isOn;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description == null?"":description);
        dest.writeInt(startHour);
        dest.writeInt(startMin);
        dest.writeByte(repeat);
        dest.writeBooleanArray(new boolean[]{isOn});
    }

    public static final Creator<AlarmEntry> CREATOR = new Creator<AlarmEntry>() {
        @Override
        public AlarmEntry createFromParcel(Parcel source) {
            String tpDescription = source.readString();
            int tpStartHour = source.readInt();
            int tpStartMin = source.readInt();
            byte tpRepeat = source.readByte();
            boolean[] tpIsOns = new boolean[1];
            source.readBooleanArray(tpIsOns);

            return new AlarmEntry(tpDescription,tpStartHour, tpStartMin,tpRepeat,tpIsOns[0]);
        }

        @Override
        public AlarmEntry[] newArray(int size) {
            return new AlarmEntry[size];
        }
    };
}
