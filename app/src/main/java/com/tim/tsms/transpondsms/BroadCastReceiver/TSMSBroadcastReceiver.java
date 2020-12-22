package com.tim.tsms.transpondsms.BroadCastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

import com.tim.tsms.transpondsms.utils.SendHistory;
import com.tim.tsms.transpondsms.utils.SendUtil;
import com.tim.tsms.transpondsms.utils.SettingUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TSMSBroadcastReceiver  extends BroadcastReceiver {
    private String TAG = "TSMSBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String receiveAction = intent.getAction();
        Log.d(TAG,"onReceive intent "+receiveAction);
        if(receiveAction.equals("android.provider.Telephony.SMS_RECEIVED")){

            Object[] object=(Object[]) intent.getExtras().get("pdus");

            for (Object pdus : object) {
                StringBuilder sb=new StringBuilder();
                byte[] pdusMsg=(byte[]) pdus;
                SmsMessage sms=SmsMessage.createFromPdu(pdusMsg);
                // String mobile=sms.getOriginatingAddress();//发送短信的手机号
                String content=sms.getMessageBody();//短信内容
                if (checkSmsFilterOr(content)) {
                    //下面是获取短信的发送时间
                    Date date=new Date(sms.getTimestampMillis());
                    String date_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                    //追加到StringBuilder中
                    sb.append("短信：" + content + "\n 时间：" + date_time+"\n");
                    SendUtil.send_msg(sb.toString());
                    Log.d(TAG,"转发了："+sb.toString());
                    SendHistory.addHistoryAnyway(  "转发了："+sb.toString());
                }
            }
        }
    }

    // 检查短信内容是否符合过滤条件
    private boolean checkSmsFilterOr(String content) {
        String filter = SettingUtil.get_sms_filter();
        if (filter == null || filter.length() <= 0) {
            return false;
        }
        String[] orStr = filter.split("\\|");
        for (int j = 0; j < orStr.length; j++) {
            boolean oneMatch = checkSmsFilterAnd(orStr[j], content);
            if (oneMatch) {
                return true;
            }
        }
        return false;
    }
    private boolean checkSmsFilterAnd(String filter, String content) {
        String[] andStr = filter.split(";");
        for (int i = 0; i < andStr.length; i++) {
            boolean oneMatch = content.indexOf(andStr[i]) >= 0;
            if (!oneMatch) {
                return false;
            }
        }
        return true;
    }

}
