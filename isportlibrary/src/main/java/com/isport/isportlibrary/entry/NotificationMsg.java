package com.isport.isportlibrary.entry;

/**
 * @author Created by Marcos Cheng on 2017/8/10.
 */

public class NotificationMsg {

    /**
     * type of message,the value is between 0x12 and 0x2B
     */
    private int msgType;
    /**
     * content of message that will be sent to ble device
     */
    private byte[] msgContent;

    /**
     *
     * @param msgType type of message
     * @param msgContent content of message
     */
    public NotificationMsg(int msgType, byte[] msgContent) {
        this.msgType = msgType;
        this.msgContent = msgContent;
    }

    /**
     * get type of message
     * @return return type of message
     */
    public int getMsgType() {
        return msgType;
    }

    /**
     * get content of message
     * @return content of message
     */
    public byte[] getMsgContent() {
        return msgContent;
    }
}
