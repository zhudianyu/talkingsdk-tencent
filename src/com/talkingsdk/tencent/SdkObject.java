package com.talkingsdk.tencent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.app.Activity;
import android.widget.Toast;
import android.os.Looper;
import android.util.Log;
import android.text.TextUtils;
import android.app.Application;

import android.content.Context;
import android.view.Window;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.talkingsdk.Cocos2dxBaseActivity;
import com.talkingsdk.MainApplication;
import com.talkingsdk.SdkCommonObject;
import com.talkingsdk.models.LoginData;
import com.talkingsdk.models.PayData;

import com.tencent.msdk.WeGame;
import com.tencent.msdk.api.LoginRet;
import com.tencent.msdk.api.MsdkBaseInfo;
import com.tencent.msdk.api.WGPlatform;
import com.tencent.msdk.api.WGQZonePermissions;
import com.tencent.msdk.consts.CallbackFlag;
import com.tencent.msdk.consts.EPlatform;
import com.tencent.msdk.tools.Logger;
import com.tencent.msdk.api.ADRet;
import com.tencent.msdk.api.LocationRet;
import com.tencent.msdk.api.LoginRet;
import com.tencent.msdk.api.QQGroupRet;
import com.tencent.msdk.api.ShareRet;
import com.tencent.msdk.api.TokenRet;
import com.tencent.msdk.api.WGADObserver;
import com.tencent.msdk.api.WGPlatform;
import com.tencent.msdk.api.WGPlatformObserver;
import com.tencent.msdk.api.WGQQGroupObserver;
import com.tencent.msdk.api.WakeupRet;
import com.tencent.msdk.consts.CallbackFlag;
import com.tencent.msdk.consts.TokenType;
import com.tencent.msdk.myapp.autoupdate.WGSaveUpdateObserver;
import com.tencent.msdk.remote.api.RelationRet;
import com.tencent.msdk.tools.Logger;
import com.tencent.tmassistantbase.common.TMAssistantDownloadTaskState;
import com.tencent.tmselfupdatesdk.model.TMSelfUpdateUpdateInfo;
import com.tencent.msdk.api.CallbackRet;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import android.app.ProgressDialog;
import android.os.Message;
import android.os.RemoteException;
import android.os.Handler;
import com.tencent.unipay.plugsdk.IUnipayServiceCallBack;
import com.tencent.unipay.plugsdk.IUnipayServiceCallBackPro;
import com.tencent.unipay.plugsdk.UnipayPlugAPI;
import com.tencent.unipay.plugsdk.UnipayPlugTools;
import com.tencent.unipay.plugsdk.UnipayResponse;
import com.tencent.unipay.request.UnipayGameRequest;
import com.tencent.unipay.request.UnipayGoodsRequest;
import com.tencent.unipay.request.UnipayMonthRequest;
import com.tencent.unipay.request.UnipaySubscribeRequest;
public abstract class SdkObject extends SdkCommonObject {
    private boolean _restart = false;
    private boolean _isNewUser = false;
    private  LoginData _loginData = null;
    private  PayData _payData = null;
    private LoginCode _loginCode = null;

    private String mUserName ;      //登录或注册成功返回的用户名
    private String mToken;          //登录或注册成功返回的Token
    private static final String TAG = SdkObject.class.getSimpleName();

    private String LANG = "java";// 开发语言 java cpp
    protected static int platform = EPlatform.ePlatform_None.val();
    private boolean isFirstLogin = false; 
    private ProgressDialog mAutoLoginWaitingDlg;
//登录支付相关
    private String userId     = "";    
    private String userKey      = "";  
    private String sessionId    = "";   
    private String sessionType  = "";  
    private String zoneId       = "";   
    private String saveValue    = "";   
    private String pf           = "";   
    private String pfKey        = "";  
    private String acctType     = "";  
    private String tokenUrl      = "";
    private int    resId        = 0;
    private byte[] appResData = null;  
    private static int retCode = -100;
    private static String retMessage = "";
    private Handler mhandler;
    private UnipayPlugAPI unipayAPI = null;
    private long pauseTime = 0;
    private boolean firstStart = false;
    @Override
    public void onActivityCreate(Activity parentActivity) {
        super.onActivityCreate(parentActivity);
        //需要先显示游戏界面
        Intent i2 = new Intent( getParentActivity(), GameActivity.class );
        i2.putExtra( "from", "login" );
        //Log.d(TAG, "======登录成功=========");
        getParentActivity().startActivity( i2 );
        getParentActivity().finish();
         firstStart = true;
        Looper looper = Looper.myLooper();
        mhandler = new Handler(looper);
        Logger.d("onCreate");
        // TODO GAME 游戏需自行检测自身是否重复, 检测到吃重复的Activity则要把自己finish掉
        // 注意：游戏需要加上去重判断finish重复的实例，否则可能发生重复拉起游戏的问题。游戏可自行决定重复的判定。
        if (WGPlatform.IsDifferentActivity(getParentActivity())) {
            Logger.d("Warning!Reduplicate game activity was detected.Activity will finish immediately.");
            getParentActivity().finish();
            return;
        }
        _loginData = new LoginData();
         // TODO GAME 初始化MSDK
        /***********************************************************
         *  TODO GAME 接入必须要看， baseInfo值因游戏而异，填写请注意以下说明:      
         *      baseInfo值游戏填写错误将导致 QQ、微信的分享，登录失败 ，切记 ！！！     
         *      只接单一平台的游戏请勿随意填写其余平台的信息，否则会导致公告获取失败  
         *      offerId 为必填，一般为手QAppId
         ***********************************************************/
        MsdkBaseInfo baseInfo = new MsdkBaseInfo();
        baseInfo.qqAppId = "100703379";
        baseInfo.qqAppKey = "4578e54fb3a1bd18e0681bc1c734514e";
        baseInfo.wxAppId = "wxcde873f99466f74a";
        baseInfo.wxAppKey = "bc0994f30c0a12a9908e353cf05d4dea";
        //订阅型测试用offerId
        baseInfo.offerId = "100703379";
        
        // 注意：传入Initialized的activity即this，在游戏运行期间不能被销毁，否则会产生Crash
        WGPlatform.Initialized(getParentActivity(), baseInfo); 
        // 设置拉起QQ时候需要用户授权的项
        WGPlatform.WGSetPermission(WGQZonePermissions.eOPEN_ALL); 
        
        // 设置java层或c++层回调,如果两层都设置了则会只回调到java层
        if (LANG.equals("java")) {
            // 全局回调类，游戏自行实现
            WGPlatform.WGSetObserver(new MsdkCallback());
            // 应用宝更新回调类，游戏自行实现
            //WGPlatform.WGSetSaveUpdateObserver(new SaveUpdateDemoObserver()); 
            // 广告的回调设置
            //WGPlatform.WGSetADObserver(new MsdkADCallback());
            //QQ 加群加好友回调
           // WGPlatform.WGSetQQGroupObserver(new MsdkQQGroupCallback());
        } else {
            // cpp层 回调设置
            // PlatformTest.setObserver(true);
            // PlatformTest.WGSetSaveUpdateObserver();
            // PlatformTest.WGLogPlatformSDKVersion();
            // PlatformTest.SetActivity(this);
        }
         // launchActivity的onCreat()和onNewIntent()中必须调用
        // WGPlatform.handleCallback()。否则会造成微信登录无回调
        if (WGPlatform.wakeUpFromHall(getParentActivity().getIntent())) {
            // 拉起平台为大厅 
            Logger.d("LoginPlatform is Hall");
            Logger.d(getParentActivity().getIntent());
        } else {  
            // 拉起平台不是大厅
            Logger.d("LoginPlatform is not Hall");
            Logger.d(getParentActivity().getIntent());
            WGPlatform.handleCallback(getParentActivity().getIntent());
        }
        isFirstLogin = true;
        //如有初始化 可以写这里
        //支付初始化
        unipayAPI = new UnipayPlugAPI(getParentActivity());
        unipayAPI.setCallBack(unipayStubCallBack);
        
        unipayAPI.bindUnipayService();
       // login();
    }

    @Override
    public void login() {
        //登陆逻辑写这里
        WGPlatform.WGLogin(EPlatform.ePlatform_QQ);

    }
    // 平台授权成功,让用户进入游戏. 由游戏自己实现登录的逻辑
    public void letUserLogin() {
        LoginRet ret = new LoginRet();
        WGPlatform.WGGetLoginRecord(ret);
        Logger.d("-----------------------------flag: " + ret.flag);
        Logger.d("-----------------------------platform: " + ret.platform);
        if(ret.flag != CallbackFlag.eFlag_Succ){
            Toast.makeText(getParentActivity(), "UserLogin error!!!",
                    Toast.LENGTH_LONG).show();
            Logger.d("UserLogin error!!!");
            letUserLogout();
            return; 
        }
        if (ret.platform == WeGame.QQPLATID) {
            // for(int i = 0; i < nameList.size(); i++) {
            //     if( "QQ登录".equals(nameList.get(i).name)) {
            //         seletedModule = nameList.get(i);
            //         startModule();
            //         break;
            //     } 
            // }
            userId = ret.open_id;
            _loginData.setUserId(ret.open_id);
            toastMakeText("QQ login success");
            //userKey = ret.getTokenByType(TokenType.eToken_QQ_Pay);
        } else if (ret.platform == WeGame.WXPLATID) {
            // for(int i = 0; i < nameList.size(); i++) {
            //     if( "微信登录".equals(nameList.get(i).name)) {
            //         seletedModule = nameList.get(i);
            //         startModule();
            //         break;
            //     }
            // }
        }
    }
    
    // 登出后, 更新view. 由游戏自己实现登出的逻辑
    public void letUserLogout() {
        WGPlatform.WGLogout();
     
    }
    @Override
    public void onActivityDestroy() {
    }
    
    protected void doFinishLoginProcess(int code) {
        String tip = "登录失败，错误代码：" + code;
        toastMakeText(tip);
    }
    
    public void changeAccount() {
        getParentActivity().runOnUiThread(new Runnable() {
            public void run() {
                //切换账号
                login();
            }
        });
    }
   
    /**
     * 登出逻辑
     * */
    @Override
    public void logout() {
        getParentActivity().runOnUiThread(new Runnable() {
            public void run() {
                //登出逻辑写这里
            }
        });
        
    }

    protected void toastMakeText(final String text) {
        getParentActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getParentActivity(), text, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    protected void doFinishPayProcess(int code) {
        Log.d(TAG, "支付结束");
        PayCode payCode = PayCode.Success;
        onPaidRequest(_payData, payCode.ordinal());
        MainApplication.getInstance().notifyGamePaid(_payData,
                payCode.ordinal());
    }

    public void pay(PayData payData) {
        _payData = payData;
         Cocos2dxBaseActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
        //支付逻辑写这里
        //充值游戏币接口，充值默认值由支付SDK设置;
        // unipayAPI.setEnv("test");
        
        
        // unipayAPI.setOfferId("900000490");
        // unipayAPI.setLogEnable(true);
        
        
        // resId = R.drawable.sample_yuanbao;
        // Bitmap bmp = BitmapFactory.decodeResource(getParentActivity().getResources(), resId);
        // ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        // appResData = baos.toByteArray();
        
        // String discounttype = "InGame";
        // String discountUrl = "http://imgcache.qq.com/bossweb/midas/unipay/test/act/actTip.html?_t=1&mpwidth=420&mpheight=250";
        
        // UnipayGoodsRequest request = new UnipayGoodsRequest();
        
        
        // try 
        // {
        //     tokenUrl = "/v1/san/900000490/mobile_goods_info?token_id=AC045C29E7F1CE732F299FFC4EEFF47A29408";
        //     unipayAPI.MPSaveGoods(userId, userKey, "openid", "kp_actoken", "1", pf, pfKey, tokenUrl,
        //             appResData, UnipayPlugAPI.PAY_CHANNEL_BANK, discounttype, discountUrl, null);
        // } 
        // catch (Exception e) 
        // {
        // }
      
                resId = R.drawable.sample_yuanbao;
                unipayAPI.setEnv("test");
                unipayAPI.setOfferId("900000490");
                unipayAPI.setLogEnable(true);
                Bitmap bmp = BitmapFactory.decodeResource(getParentActivity().getResources(), resId);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                appResData = baos.toByteArray();
                //货币类型   ACCOUNT_TYPE_COMMON:基础货币； ACCOUNT_TYPE_SECURITY:安全货币
                acctType    = UnipayPlugAPI.ACCOUNT_TYPE_COMMON;
                
                //用户的充值数额（可选，调用相应充值接口即可）
                saveValue   = "60"; 
                    //充值游戏币\
                Logger.d("--------------------------------------------");
                Logger.d("userId = "+userId);
                Logger.d("openid = "+_loginData.getUserId());
                Logger.d("userKey = "+userKey);
                 Logger.d("pf = "+pf);
                 Logger.d("pfKey = "+pfKey);
                 Logger.d("--------------------------------------------");
                try
                {

                    //充值游戏币
                   unipayAPI.SaveGameCoinsWithoutNum(userId, userKey, "openid", "kp_actoken", "1", pf, pfKey, acctType, appResData);//(userId, userKey, sessionId, sessionType, pf, pfKey, serviceCode, serviceName, resId, remark);
                }
                catch (RemoteException e)
                {
                    e.printStackTrace();
                } 
            }
        });
    }


    private double floatToDouble(float f) {
        return Double.parseDouble(String.valueOf(f));
    }

    @Override
    public void onApplicationCreate(Application obj) {
    }

    @Override
    public void onAppTerminate() {
    }

    @Override
    public void setRestartWhenSwitchAccount(boolean restart) {
        _restart = restart;
    }

     @Override
    public LoginCode getLoginCode() {
        return LoginCode.Success;
    }

    private boolean isAppForeground = true;

    @Override
    public void onGameResume() {
        WGPlatform.onResume();

        // 游戏根据自身逻辑判断决定是否要重新验证票据, 由于onResume会被频繁的调用到, 这里的时间间隔由游戏根据自身情况确定
        if (firstStart
                || (pauseTime != 0 && System.currentTimeMillis() / 1000
                        - pauseTime > 1800)) {
            Logger.d("MsdkStat", "start auto login");
            // 模拟游戏自动登录 START
            // WGLoginWithLocalInfo是一个异步接口, 会到后台验证票据, 这里需要我添加加载动画
            startWaiting();
            WGPlatform.WGLoginWithLocalInfo();
            // 模拟游戏自动登录 END
        } else {
            Logger.d("MsdkStat", "do not start auto login");
        }
        firstStart = false;

    }

    @Override
    public void onGameFade() {
        if (!isAppOnForeground()) {
            isAppForeground = false;
        }
    }

    public void onKeyBack() {
        Cocos2dxBaseActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }


     private void startWaiting() {
        Logger.d("startWaiting");
        stopWaiting();
        mAutoLoginWaitingDlg = new ProgressDialog(getParentActivity());
        mAutoLoginWaitingDlg.setTitle("自动登录中...");
        mAutoLoginWaitingDlg.show();
    }

    private void stopWaiting() {
        Logger.d("stopWaiting");
        if (mAutoLoginWaitingDlg != null && mAutoLoginWaitingDlg.isShowing()) {
            mAutoLoginWaitingDlg.dismiss();
        }
    }
    private void toastCallbackInfo(int plat, String what, int flag, String desc) {
        String platStr = "";
        if (plat == EPlatform.ePlatform_QQ.val()) {
            platStr = "QQ游戏中心";
        } else if (plat == EPlatform.ePlatform_Weixin.val()) {
            platStr = "微信";
        } else if (plat == EPlatform.ePlatform_QQHall.val()) {
            platStr = "游戏大厅";
        }
        String msg = "收到" + platStr + what + "回调 ";
        msg += "\nflag :" + flag;
        msg += "\ndesc :" + desc;
        Toast.makeText(getParentActivity(), msg, Toast.LENGTH_LONG).show();
         Logger.d(msg);
    }
class MsdkCallback implements WGPlatformObserver {
        @SuppressWarnings("unused")
        public void OnLoginNotify(LoginRet ret) {
            // TODO GAME 游戏需要根据自己的逻辑实现自己的MsdkCallback对象
            toastCallbackInfo(ret.platform, "登录", ret.flag, ret.desc);
            Logger.d("-------------------------------------------called");
            Logger.d("-----------------------------------ret.flag" + ret.flag);
            switch (ret.flag) {
            case CallbackFlag.eFlag_Succ:
                stopWaiting();
                // 登陆成功, 读取各种票据
                toastMakeText("openid = "+ret.open_id);
                
                String openId = ret.open_id;
                userId = openId;
                pf = ret.pf;
                pfKey = ret.pf_key;
                SdkObject.platform = ret.platform;
                String wxAccessToken = "";
                long wxAccessTokenExpire = 0;
                String wxRefreshToken = "";
                long wxRefreshTokenExpire = 0;
                long qqRefreshTokenExpire = 0;
                for (TokenRet tr : ret.token) {
                    switch (tr.type) {
                    case TokenType.eToken_WX_Access:
                        wxAccessToken = tr.value;
                        wxAccessTokenExpire = tr.expiration;
                        break;
                    case TokenType.eToken_WX_Refresh:
                        wxRefreshToken = tr.value;
                        wxRefreshTokenExpire = tr.expiration;
                        break;
                    case TokenType.eToken_QQ_Pay:
                        userKey = tr.value;
                        qqRefreshTokenExpire = tr.expiration;
                        break;
                    default:
                        break;
                    }
                }
               
                letUserLogin();
                break;
            case CallbackFlag.eFlag_WX_UserCancel:
            case CallbackFlag.eFlag_WX_NotInstall:
            case CallbackFlag.eFlag_WX_NotSupportApi:
            case CallbackFlag.eFlag_WX_LoginFail:
                // 登陆失败处理
                Logger.d(ret.desc);
                break;
            case CallbackFlag.eFlag_Local_Invalid:
                // 显示登陆界面
                stopWaiting();
            default:
                break;
            }
        }

        public void OnShareNotify(ShareRet ret) {
            // game todo
            toastCallbackInfo(ret.platform, "分享", ret.flag, ret.desc);
            Logger.d("called");
            Logger.d("OnShareNotify","called");
            switch (ret.flag) {
            case CallbackFlag.eFlag_Succ:
                // 分享成功
                SdkObject.platform = ret.platform;
                break;
            case CallbackFlag.eFlag_QQ_UserCancel:
            // case CallbackFlag.eFlag_QQ_NetworkErr:
            //     // 分享失败处理
            //     Logger.d(ret.desc);
            //     break;
            case CallbackFlag.eFlag_WX_UserCancel:
            case CallbackFlag.eFlag_WX_NotInstall:
            case CallbackFlag.eFlag_WX_NotSupportApi:
                // 分享失败处理
                Logger.d(ret.desc);
                break;
            case CallbackFlag.eFlag_Error:
                Logger.d("OnShareNotify","failed");
            default:
                break;
            }
        }

        public void OnWakeupNotify(WakeupRet ret) {
            // game todo
            toastCallbackInfo(ret.platform, "拉起", ret.flag, ret.desc);

            Logger.d("OnWakeupNotify called");
            this.logCallbackRet(ret);
            SdkObject.platform = ret.platform;
            // TODO GAME 这里增加处理异账号的逻辑
            if (CallbackFlag.eFlag_Succ == ret.flag
                    || CallbackFlag.eFlag_UrlLogin == ret.flag
                    || CallbackFlag.eFlag_AccountRefresh == ret.flag) {
                Logger.d("login success flag:" + ret.flag);
                Cocos2dxBaseActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //letUserLogin();
                    }
                });
            } else if (ret.flag == CallbackFlag.eFlag_NeedSelectAccount) {
                Logger.d("diff account");
               // showDiffLogin();
            } else if (ret.flag == CallbackFlag.eFlag_NeedLogin) {
                Logger.d("login with url");
                //letUserLogout();
            } else {
                Logger.d("login with url");
                //letUserLogout();
            }
        }

        private void logCallbackRet(CallbackRet cr) {
            Logger.d(cr.toString() + ":flag:" + cr.flag);
            Logger.d(cr.toString() + "desc:" + cr.desc);
            Logger.d(cr.toString() + "platform:" + cr.platform);
        }

        @Override
        public void OnRelationNotify(RelationRet relationRet) {
            Logger.d("OnRelationNotify" + relationRet);
        }

        @Override
        public void OnLocationNotify(RelationRet relationRet) {
            Logger.d(relationRet);
        }

        @Override
        public void OnLocationGotNotify(LocationRet locationRet) {
            Logger.d(locationRet);
        }

        @Override
        public void OnFeedbackNotify(int flag, String desc) {
            Logger.d(String.format(Locale.CHINA, "flag: %d; desc: %s;", flag,
                    desc));
        }

        @Override
        public String OnCrashExtMessageNotify() {
            // 此处游戏补充crash时上报的额外信息
            Logger.d(String.format(Locale.CHINA,
                    "OnCrashExtMessageNotify called"));
            Date nowTime = new Date();
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return "new Upload extra crashing message for rqd1.7.8 on "
                    + time.format(nowTime);
        }
    }
        //回调接口
    IUnipayServiceCallBackPro.Stub unipayStubCallBackPro = new IUnipayServiceCallBackPro.Stub() {
        
        @Override
        public void UnipayNeedLogin() throws RemoteException
        {
            Log.i("UnipayPlugAPI", "UnipayNeedLogin");
            
        }

        @Override
        public void UnipayCallBack(UnipayResponse response) throws RemoteException
        {
            Log.i("UnipayPlugAPI", "UnipayCallBack \n" + 
                    "\nresultCode = " + response.resultCode + 
                    "\npayChannel = "+ response.payChannel + 
                    "\npayState = "+ response.payState + 
                    "\nproviderState = " + response.provideState+
                    "\nsavetype = "+ response.extendInfo);
            
            retCode = response.resultCode;
            retMessage = response.resultMsg;

            handler.sendEmptyMessage(0);
            
        }
    
    };
    
    IUnipayServiceCallBack.Stub unipayStubCallBack = new IUnipayServiceCallBack.Stub() {
        
        @Override
        public void UnipayNeedLogin() throws RemoteException
        {
            Log.i("UnipayPlugAPI", "UnipayNeedLogin");
            
        }

        @Override
        public void UnipayCallBack(int resultCode, int payChannel,
                int payState, int providerState, int saveNum, String resultMsg,
                String extendInfo) throws RemoteException
        {
            Log.i("UnipayPlugAPI", "UnipayCallBack \n" + 
                    "\nresultCode = " + resultCode + 
                    "\npayChannel = "+ payChannel + 
                    "\npayState = "+ payState + 
                    "\nproviderState = " + providerState+
                    "\nsavetype = "+ extendInfo);
            
            retCode = resultCode;
            retMessage = resultMsg;

            handler.sendEmptyMessage(0);
            
        }
    };
    
    Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Toast.makeText(getParentActivity(), "call back retCode=" + String.valueOf(retCode) + " retMessage=" + retMessage, Toast.LENGTH_SHORT).show();
        
            
            if(retCode == -2)
            {//service绑定不成功
                unipayAPI.bindUnipayService();
            }
        }
    };
}