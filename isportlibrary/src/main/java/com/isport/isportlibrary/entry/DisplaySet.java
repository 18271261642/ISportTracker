package com.isport.isportlibrary.entry;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Created by Marcos Cheng on 2016/9/19.
 *
 * set which interface show at the device
 */
public class DisplaySet implements Parcelable {

    private boolean showLogo;
    private boolean showCala;
    private boolean showDist;
    private boolean showSportTime;
    private boolean showProgress;
    private boolean showEmotion;
    private boolean showAlarm;
    private boolean showSmsMissedCall;
    private boolean showIncomingReminder;
    private boolean showMsgContentPush;
    private boolean showCountDown;///倒计时

    public DisplaySet(){
        
    }

    /**
     * set device interface
     * @param showLogo show logo or not
     * @param showCala show calorics or not
     * @param showDist show distance or not
     * @param showSportTime show sport time or not
     * @param showProgress show progess or not
     * @param showEmotion show emotion or not
     * @param showAlarm show alarm  or not
     * @param showSmsMissedCall show sms and missed call or not
     * @param showIncomingReminder show call remind or not
     * @param showMsgContentPush shao message content push or not
     */
    public DisplaySet(boolean showLogo, boolean showCala, boolean showDist, boolean showSportTime,
                      boolean showProgress, boolean showEmotion, boolean showAlarm, boolean showSmsMissedCall,
                      boolean showIncomingReminder, boolean showMsgContentPush) {
        this.showLogo = showLogo;
        this.showCala = showCala;
        this.showDist = showDist;
        this.showSportTime = showSportTime;
        this.showProgress = showProgress;
        this.showEmotion = showEmotion;
        this.showAlarm = showAlarm;
        this.showSmsMissedCall = showSmsMissedCall;
        this.showIncomingReminder = showIncomingReminder;
        this.showMsgContentPush = showMsgContentPush;
    }

    /**
     * set device interface
     * @param showLogo show logo or not
     * @param showCala show calorics or not
     * @param showDist show distance or not
     * @param showSportTime show sport time or not
     * @param showProgress show progess or not
     * @param showEmotion show emotion or not
     * @param showAlarm show alarm  or not
     * @param showSmsMissedCall show sms and missed call or not
     * @param showIncomingReminder show call remind or not
     * @param showMsgContentPush show message content push or not
     * @param showCountDown show timer count down
     */
    public DisplaySet(boolean showLogo, boolean showCala, boolean showDist, boolean showSportTime,
                      boolean showProgress, boolean showEmotion, boolean showAlarm, boolean showSmsMissedCall,
                      boolean showIncomingReminder, boolean showMsgContentPush,boolean showCountDown) {
        this(showLogo,showCala,showDist,showSportTime,showProgress,showEmotion,showAlarm,showSmsMissedCall,
                showIncomingReminder,showMsgContentPush);
        this.showCountDown = showCountDown;
    }

    public boolean isShowLogo() {
        return showLogo;
    }

    public boolean isShowCala() {
        return showCala;
    }

    public boolean isShowDist() {
        return showDist;
    }

    public boolean isShowSportTime() {
        return showSportTime;
    }

    public boolean isShowProgress() {
        return showProgress;
    }

    public boolean isShowEmotion() {
        return showEmotion;
    }

    public boolean isShowAlarm() {
        return showAlarm;
    }

    public boolean isShowSmsMissedCall() {
        return showSmsMissedCall;
    }

    public boolean isShowIncomingReminder() {
        return showIncomingReminder;
    }

    public boolean isShowMsgContent() {
        return showMsgContentPush;
    }

    public boolean isShowCountDown(){
        return showCountDown;
    }

    public void setShowLogo(boolean showLogo) {
        this.showLogo = showLogo;
    }

    public void setShowCala(boolean showCala) {
        this.showCala = showCala;
    }

    public void setShowDist(boolean showDist) {
        this.showDist = showDist;
    }

    public void setShowSportTime(boolean showSportTime) {
        this.showSportTime = showSportTime;
    }

    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }

    public void setShowEmotion(boolean showEmotion) {
        this.showEmotion = showEmotion;
    }

    public void setShowAlarm(boolean showAlarm) {
        this.showAlarm = showAlarm;
    }

    public void setShowSmsMissedCall(boolean showSmsMissedCall) {
        this.showSmsMissedCall = showSmsMissedCall;
    }

    public void setShowIncomingReminder(boolean showIncomingReminder) {
        this.showIncomingReminder = showIncomingReminder;
    }

    public void setShowMsgContentPush(boolean showMsgContentPush) {
        this.showMsgContentPush = showMsgContentPush;
    }

    public void setShowCountDown(boolean showCountDown){
        this.showCountDown = showCountDown;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] sets = new boolean[]{showLogo, showCala, showDist, showSportTime, showProgress, showEmotion,
        showAlarm, showSmsMissedCall, showIncomingReminder, showMsgContentPush, showCountDown};
        dest.writeBooleanArray(sets);
    }

    public static final Creator<DisplaySet> CREATOR = new Creator<DisplaySet>() {
        @Override
        public DisplaySet createFromParcel(Parcel source) {
            boolean[] sets = new boolean[11];
            source.readBooleanArray(sets);

            return new DisplaySet(sets[0],sets[1],sets[2],sets[3],sets[4],sets[5],sets[6],sets[7],sets[8],sets[9],sets[10]);
        }

        @Override
        public DisplaySet[] newArray(int size) {
            return new DisplaySet[size];
        }
    };
}
