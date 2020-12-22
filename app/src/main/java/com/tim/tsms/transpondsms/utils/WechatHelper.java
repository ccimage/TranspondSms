package com.tim.tsms.transpondsms.utils;

import android.util.Log;

class WechatHelper {
    public static void send(final String title){

        final WechatSender wechat = new WechatSender();
        new Thread(new Runnable() {
            @Override
            public void run() {
                wechat.sendMsg(title);
            }
        }).start();
    }
}
