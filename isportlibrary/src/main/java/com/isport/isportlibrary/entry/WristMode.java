package com.isport.isportlibrary.entry;

/**
 * Created by chengjiamei on 2016/8/24.
 * set wrist mode
 */
public class WristMode {

    private boolean isLeftHand;

    public WristMode(boolean isLeftHand){
        this.isLeftHand = isLeftHand;
    }

    /**
     * the wear method that wear on left hand or right hand
     * @return
     */
    public boolean isLeftHand(){
        return isLeftHand;
    }
}
