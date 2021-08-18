package com.isport.isportlibrary.entry;

import java.io.Serializable;

/**
 * @author Created by Marcos Cheng on 2016/8/24.
 * Screen set, only supported by W194
 */
public class ScreenSet implements Serializable {

    private boolean isProtected;
    private byte screenColor;///

    /**
     *
     * @param isProtected
     * @param screenColor 0 white  1 black
     */
    public ScreenSet(boolean isProtected,byte screenColor){
        this.isProtected = isProtected;
        this.screenColor = screenColor;
    }

    /**
     *
     * @return screen protector is enable or disable
     */
    public boolean isProtected(){
        return this.isProtected;
    }

    public byte getScreenColor(){
        return screenColor;
    }
}
