package com.isport.isportlibrary;

import android.os.Handler;
import android.os.Message;

import com.isport.isportlibrary.controller.BaseController;

public class handlerTest {

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x04) {

            }
        }
    };

    public void startTimer() {
        handler.sendEmptyMessageDelayed(0x04, 1000);//400
    }

    public void cancle() {
        if (handler.hasMessages(0x04)) {
            handler.removeMessages(0x04);
        }
        handler.sendEmptyMessageDelayed(0x04, 1000);//400
    }
}
