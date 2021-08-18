package com.isport.isportlibrary.entry;

import java.io.Serializable;

/**
 * @author Created by Marcos Cheng on 2016/8/26.
 *
 */
public class HeartData implements Serializable {

    private int heartRate;//
    private long heartTime;

    public HeartData(int heartRate,long heartTime){
        this.heartRate = heartRate;
        this.heartTime = heartTime;
    }

    /**
     *
     * @return heart rate
     */
    public int getHeartRate(){
        return this.heartRate;
    }

    /**
     *
     * @return time when receive the data from device
     */
    public long getHeartTime(){
        return this.heartTime;
    }

    private static int decodeHeartRate(byte[] cmd) {
        int btRate = 0;
        if (null != cmd) {
            if ((cmd[0] & 0x01) == 0 && cmd.length >= 2) {
                btRate = cmd[1];
            } else if ((cmd[0] & 0x01) == 1 && cmd.length >= 3) {
                btRate = bt(cmd, 1);
            }
        }
        return btRate;
    }

    public static int bt(byte[] bData, int nOffset) {
        int nDate = (bData[nOffset] >= 0 ? bData[nOffset] : 256 + bData[nOffset]) * 256 +
                (bData[nOffset + 1] >= 0 ? bData[nOffset + 1] : 256 + bData[nOffset + 1]);
        return nDate;
    }
}
