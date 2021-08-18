package com.isport.isportlibrary.entry;

import com.isport.isportlibrary.managers.CallManager;

/**
 * @author Created by Marcos Cheng on 2017/8/10.
 */

public class CallEntry {

    /**
     * name of contact
     */
    private String name;
    /**
     * phone number
     */
    private String phoneNum;

    /**
     * call type,see {@link CallManager#CALL_STATE_OFFHOOK,CallManager#CALL_STATE_RINGING,CallManager#CALL_STATE_IDLE }
     */
    private int type;

    public CallEntry(int type, String phoneNum,String name) {
        this.name = name;
        this.phoneNum = phoneNum;
        this.type = type;
    }

    public CallEntry(String type, String phoneNum, String name) {
        int state = CallManager.CALL_STATE_IDLE;
        if(type != null) {

        }
    }

    /**
     * {@link CallManager#CALL_STATE_RINGING}
     * {@link CallManager#CALL_STATE_OFFHOOK}
     * {@link CallManager#CALL_STATE_IDLE}
     * @return call type
     */
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     *
     * @return contact name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return phone number of contact
     */
    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
}
