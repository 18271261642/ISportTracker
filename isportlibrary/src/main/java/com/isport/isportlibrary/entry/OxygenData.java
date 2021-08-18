package com.isport.isportlibrary.entry;

/**
 * @author Created by Marcos Cheng on 2016/12/19.
 * W337B series, see {@link com.isport.isportlibrary.controller.BaseController#CMD_TYPE_W337B}
 */

public class OxygenData {

    /**
     * the value of blood oxyen
     */
    int oxygenRate;
    /**
     * the time when receive blood oxyen
     */
    long oxygenTime;

    public OxygenData(int oxygenRate,long oxygenTime){
        this.oxygenRate = oxygenRate;
        this.oxygenTime = oxygenTime;
    }

    public int getoxygenRate(){
        return this.oxygenRate;
    }

    public long getoxygenTime(){
        return this.oxygenTime;
    }

    public static int decodeoxygenRate(byte[] cmd) {
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
