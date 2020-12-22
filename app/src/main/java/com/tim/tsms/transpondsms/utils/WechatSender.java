package com.tim.tsms.transpondsms.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import kotlin.Function;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WechatSender {
    static String TAG = "WechatSender";
    public void sendMsg(String msg){
        try {
            final String sharowMsg = msg;
            HttpResponseCallback cb = new HttpResponseCallback() {
                @Override
                public void onSuccess(String param) {
                    try {
                        sendOutWechat(param, sharowMsg);
                    } catch (Exception ex) {
                        Log.d(TAG,"Exception：" + ex.getLocalizedMessage());
                    }
                }
            };
            getToken(cb);
        } catch (Exception ex) {
            Log.d(TAG,"Exception：" + ex.getLocalizedMessage());
        }
    }
    private void sendOutWechat(String accessToken, String msg) throws Exception {
        String tag = SettingUtil.get_send_util_wechat(Define.SP_MSG_SEND_UTIL_WECHAT_TAG_ID);
        String agent = SettingUtil.get_send_util_wechat(Define.SP_MSG_SEND_UTIL_WECHAT_AGENT_ID);

        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;

        final String msgf = msg;
        String textMsg = "{ \"totag\": \""+tag+"\", \"msgtype\": \"text\", \"agentid\": \""+agent+"\",\"safe\": \"0\", \"text\": {\"content\": \""+msg+"\"}}";
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),
                textMsg);

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"onFailure：" + e.getMessage());
                SendHistory.addHistory("企业微信 错误：" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseStr = response.body().string();
                Log.d(TAG,"Code：" + String.valueOf(response.code())+responseStr);
                SendHistory.addHistory("企业微信:" + String.valueOf(response.code())+responseStr);
            }
        });
    }
    private void getToken(final HttpResponseCallback callback) throws Exception {
        String cropID = SettingUtil.get_send_util_wechat(Define.SP_MSG_SEND_UTIL_WECHAT_CORP_ID);
        String secret = SettingUtil.get_send_util_wechat(Define.SP_MSG_SEND_UTIL_WECHAT_SECRET);
        String url = String.format("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s", cropID, secret);

        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"onFailure：" + e.getMessage());
                SendHistory.addHistory("getToken" + " failure：" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,"Code：" + String.valueOf(response.code()));
                final String responseStr = response.body().string();
                SendHistory.addHistory("getToken success：" + String.valueOf(response.code()) + responseStr);
                if (response.code() == 200) {
                    TokenResponse json = JSON.parseObject(responseStr, TokenResponse.class);
                    int errcode = json.errcode;
                    String token = json.access_token;

                    if (errcode == 0) {
                        callback.onSuccess(token);
                    }
                }
            }
        });
    }
}
class TokenResponse {
    public int errcode;
    public String errmsg;
    public String access_token;
    public int expires_in;
}
@FunctionalInterface
interface HttpResponseCallback {
    void onSuccess(String param);
}