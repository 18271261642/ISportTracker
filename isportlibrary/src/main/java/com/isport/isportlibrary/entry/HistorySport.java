package com.isport.isportlibrary.entry;

/**
 * Created by chengjiamei on 2016/9/21.
 *  the data every 5 minute of w311 serial {@link com.isport.isportlibrary.controller.BaseController#CMD_TYPE_W311}
 */
public class HistorySport extends EntityBase{

    private String dateString;
    private String mac;
    private int stepNum;
    private int sleepState;

    public HistorySport(){

    }

    public HistorySport(String mac,String dateString, int stepNum, int sleepState) {
        this.dateString = dateString;
        this.stepNum = stepNum;
        this.sleepState = sleepState;
        this.mac = mac;
    }

    /**
     *
     * @return return mac of device that the data belong to
     */
    public String getMac() {
        return mac;
    }

    /**
     *
     * @return like 1990-01-01 09:30
     */
    public String getDateString() {
        return dateString;
    }

    /**
     *
     * @return the step number of this 5 minutes
     */
    public int getStepNum() {
        return stepNum;
    }

    /**
     *
     * sleep state , it may be deep sleep,light sleep, very light sleep , awake state and no sleep
     * the value is 0(no sleep),128(deep sleep),129(light sleep),130(very light sleep),131(awake)
     *
     * @return 0x80 deep,0x81 light,0x82 very light, 0x83 wake when sleep,0 wake(no sleep)
     */
    public int getSleepState() {
        return sleepState;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    /**
     * mac of device
     * @param mac mac of device
     */
    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setStepNum(int stepNum) {
        this.stepNum = stepNum;
    }

    public void setSleepState(int sleepState) {
        this.sleepState = sleepState;
    }

    @Override
    public String toString() {
        return "HistorySport{" +
                "dateString='" + dateString + '\'' +
                ", mac='" + mac + '\'' +
                ", stepNum=" + stepNum +
                ", sleepState=" + sleepState +
                '}';
    }
}
