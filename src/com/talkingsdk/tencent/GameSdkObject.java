package com.talkingsdk.tencent;

import java.util.Map;

import org.json.JSONObject;

import android.util.Log;

import com.talkingsdk.MainApplication;
import com.talkingsdk.models.LoginData;
import com.talkingsdk.models.PayData;
import com.talkingsdk.tencent.SdkObject;
import com.unity3d.player.UnityPlayer;

public class GameSdkObject extends SdkObject {

    private static final String TAG = "GameSdkObject";

   @Override
    public void onLoginedRequest(LoginData lg, int code) {
        String loginDataJsonStr = lg.toJSON();
        Log.d(TAG, "LoginData:" + loginDataJsonStr);
        UnityPlayer.UnitySendMessage(getUnityGameObject(),"OnLoginResult", loginDataJsonStr);
    }

    @Override
    public void onPaidRequest(PayData payData, int code) {
        String loginDataJsonStr = payData.toJSON();
        Log.d(TAG, "payData:" + loginDataJsonStr);
        UnityPlayer.UnitySendMessage(getUnityGameObject(),"OnPayResult", loginDataJsonStr);
    }

    @Override
    public void onLogoutRequest(int code) {
        Log.d("Unity", code + "");
        UnityPlayer.UnitySendMessage(getUnityGameObject(),"OnLogoutResult", code + "");
    }

    @Override
    public void onChangeAccountRequest(LoginData lg, int code) {
         String loginDataJsonStr = lg.toJSON();
        Log.d(TAG, "onChangeAccountRequest LoginData:" + loginDataJsonStr);
        UnityPlayer.UnitySendMessage(getUnityGameObject(),"OnChangeAccountResult", loginDataJsonStr); 
    }


}
