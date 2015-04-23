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
import com.tencent.tmgp.hycs.R;
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

// import com.tencent.msdk.api.LocationRet;
import com.tencent.msdk.api.LoginRet;
// import com.tencent.msdk.api.QQGroupRet;
import com.tencent.msdk.api.ShareRet;
import com.tencent.msdk.api.TokenRet;
// import com.tencent.msdk.api.WGADObserver;
import com.tencent.msdk.api.WGPlatform;
import com.tencent.msdk.api.WGPlatformObserver;
// import com.tencent.msdk.api.WGQQGroupObserver;
import com.tencent.msdk.api.WakeupRet;
import com.tencent.msdk.consts.CallbackFlag;
import com.tencent.msdk.consts.TokenType;
// import com.tencent.msdk.myapp.autoupdate.WGSaveUpdateObserver;
// import com.tencent.msdk.remote.api.RelationRet;
import com.tencent.msdk.tools.Logger;
// import com.tencent.tmassistantbase.common.TMAssistantDownloadTaskState;
// import com.tencent.tmselfupdatesdk.model.TMSelfUpdateUpdateInfo;
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

import android.telephony.CellLocation;     
import android.telephony.PhoneStateListener;     
import android.telephony.TelephonyManager; 
import android.os.Build;
import android.bluetooth.BluetoothAdapter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
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
    private  String userId     = "";    
    private  String userKey      = "";  
    private  String sessionId    = "";   
    private  String sessionType  = "";  
    private  String zoneId       = "";   
    private  String saveValue    = "";   
    private  String pf           = "";   
    private  String pfKey        = "";  
    private  String acctType     = "";  
    private  String tokenUrl      = "";
    private int    resId        = 0;
    private byte[] appResData = null;  
    private static int retCode = -100;
    private static String retMessage = "";
    private Handler mhandler;
    private UnipayPlugAPI unipayAPI = null;
    private long pauseTime = 0;
    private boolean firstStart = false;
    private StringBuffer buffer = new StringBuffer();
    @Override
    public void onActivityCreate(Activity parentActivity) {
        super.onActivityCreate(parentActivity);
    }
 
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
         
             getParentActivity().runOnUiThread(new Runnable() {
            public void run() {
                //登出逻辑写这里
                    if (WGPlatform.IsDifferentActivity(getParentActivity())) {
                        Logger.d("Warning!Reduplicate game activity was detected.Activity will finish immediately.");
                        getParentActivity().finish();
                        return;
                    }

                    // TODO GAME 初始化MSDK
                    /***********************************************************
                     *  TODO GAME 接入必须要看， baseInfo值因游戏而异，填写请注意以下说明:      
                     *      baseInfo值游戏填写错误将导致 QQ、微信的分享，登录失败 ，切记 ！！！     
                     *      只接单一平台的游戏请勿随意填写其余平台的信息，否则会导致公告获取失败  
                     *      offerId 为必填，一般为手QAppId
                     ***********************************************************/
                    MsdkBaseInfo baseInfo = new MsdkBaseInfo();
                    baseInfo.qqAppId = "1104477307";
                    baseInfo.qqAppKey = "AIrFZXvaZZFMaNw1";
                    baseInfo.wxAppId = "wx4a0718f61d58cb09";
                    baseInfo.wxAppKey = "87ef343d17bc1707158a629faf966f7f";
                    //订阅型测试用offerId
                    baseInfo.offerId = "1104477307";
                    unipayAPI = new UnipayPlugAPI(getParentActivity());
                    unipayAPI.setCallBack(unipayStubCallBack);
                        
                    unipayAPI.bindUnipayService();
                    // 注意：传入Initialized的activity即getParentActivity()，在游戏运行期间不能被销毁，否则会产生Crash
                    WGPlatform.Initialized(getParentActivity(), baseInfo); 
                    // 设置拉起QQ时候需要用户授权的项
                    WGPlatform.WGSetPermission(WGQZonePermissions.eOPEN_ALL); 
                    WGPlatform.WGSetObserver(new MsdkCallback());
                    if (WGPlatform.wakeUpFromHall(getParentActivity().getIntent())) {
                        // 拉起平台为大厅 
                        Logger.d("LoginPlatform is Hall");
                    } else {  
                        // 拉起平台不是大厅
                        Logger.d("LoginPlatform is not Hall");
                        WGPlatform.handleCallback(getParentActivity().getIntent());
                    }
                  //  WGPlatform.WGLogin(EPlatform.ePlatform_Weixin);
            }
        });
    
        
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (WGPlatform.wakeUpFromHall(intent)) {
            Logger.d("LoginPlatform is Hall");
        } else {
            Logger.d("LoginPlatform is not Hall");
            WGPlatform.handleCallback(intent);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        WGPlatform.onActivityResult(requestCode, resultCode, data);
        Logger.d("onActivityResult");
    }

    @Override
    public void login() {
        //登陆逻辑写这里  ePlatform_Weixin
    getParentActivity().runOnUiThread(new Runnable() {
            public void run() {
                //登出逻辑写这里
           
                    WGPlatform.WGLogin(EPlatform.ePlatform_Weixin);
            }
        });

       
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
        String info = "";
        _loginData = new LoginData();
        if (ret.platform == WeGame.QQPLATID) {
         
            userId = ret.open_id;

            _loginData.setUserId(ret.open_id);
             onLoginedRequest(_loginData, 200);
            toastMakeText("QQ login success");
            //userKey = ret.getTokenByType(TokenType.eToken_QQ_Pay);
        } else if (ret.platform == WeGame.WXPLATID) {
          
            userId = ret.open_id;
            _loginData.setUserId(ret.open_id);
            _loginData.setSessionId(WeGame.getInstance().getLocalTokenByType(
                            TokenType.eToken_WX_Access));
            info += "platform = " + ret.platform + " 微信帐号\n";
            info += "accessToken = "
                    + WeGame.getInstance().getLocalTokenByType(
                            TokenType.eToken_WX_Access) + "\n";
            info += "refreshToken = "
                    + WeGame.getInstance().getLocalTokenByType(
                            TokenType.eToken_WX_Refresh) + "\n";
        }
        info += "openid = " + ret.open_id + "\n";
        info += "flag = " + ret.flag + "\n";
        info += "desc = " + ret.desc + "\n";
        info += "pf = " + ret.pf + "\n";
        info += "pf_key = " + ret.pf_key + "\n";

        SdkObject.this.setLoginData(_loginData);
        // 账号登录成功，此时可用初始化玩家游戏数据
        String tip = "账号登录成功";
        toastMakeText(tip);
        onLoginedRequest(_loginData, 200);
        Toast.makeText(getParentActivity(), info, Toast.LENGTH_LONG)
                .show();
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
                //登出逻辑写这里
                    if (WGPlatform.IsDifferentActivity(getParentActivity())) {
                        Logger.d("Warning!Reduplicate game activity was detected.Activity will finish immediately.");
                        getParentActivity().finish();
                        return;
                    }

                    // TODO GAME 初始化MSDK
                    /***********************************************************
                     *  TODO GAME 接入必须要看， baseInfo值因游戏而异，填写请注意以下说明:      
                     *      baseInfo值游戏填写错误将导致 QQ、微信的分享，登录失败 ，切记 ！！！     
                     *      只接单一平台的游戏请勿随意填写其余平台的信息，否则会导致公告获取失败  
                     *      offerId 为必填，一般为手QAppId
                     ***********************************************************/
                    MsdkBaseInfo baseInfo = new MsdkBaseInfo();
                    baseInfo.qqAppId = "1104477307";
                    baseInfo.qqAppKey = "AIrFZXvaZZFMaNw1";
                    baseInfo.wxAppId = "wx4a0718f61d58cb09";
                    baseInfo.wxAppKey = "87ef343d17bc1707158a629faf966f7f";
                    //订阅型测试用offerId
                    baseInfo.offerId = "1104477307";
                    unipayAPI = new UnipayPlugAPI(getParentActivity());
                    unipayAPI.setCallBack(unipayStubCallBack);
                        
                    unipayAPI.bindUnipayService();
                    // 注意：传入Initialized的activity即getParentActivity()，在游戏运行期间不能被销毁，否则会产生Crash
                    WGPlatform.Initialized(getParentActivity(), baseInfo); 
                    // 设置拉起QQ时候需要用户授权的项
                    WGPlatform.WGSetPermission(WGQZonePermissions.eOPEN_ALL); 
                    WGPlatform.WGSetObserver(new MsdkCallback());
                    if (WGPlatform.wakeUpFromHall(getParentActivity().getIntent())) {
                        // 拉起平台为大厅 
                        Logger.d("LoginPlatform is Hall");
                    } else {  
                        // 拉起平台不是大厅
                        Logger.d("LoginPlatform is not Hall");
                        WGPlatform.handleCallback(getParentActivity().getIntent());
                    }
                    WGPlatform.WGLogin(EPlatform.ePlatform_QQ);
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
       
          getParentActivity().runOnUiThread(new Runnable() {
            public void run() {
     
        //充值游戏币接口，充值默认值由支付SDK设置;

      
                resId = R.drawable.sample_yuanbao;
                
                String discounttype = "InGame";
                String discountUrl = "http://imgcache.qq.com/bossweb/midas/unipay/test/act/actTip.html?_t=1&mpwidth=420&mpheight=250";
                
                //充值游戏币接口，充值默认值由支付SDK设置;
                unipayAPI.setEnv("test");
                unipayAPI.setOfferId("1104477307");
                unipayAPI.setLogEnable(true);
                UnipayPlugTools unipayPlugTools = new UnipayPlugTools(getParentActivity());
                unipayPlugTools.setUrlForTest();
                
                Bitmap bmp = BitmapFactory.decodeResource(getParentActivity().getResources(), resId);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                appResData = baos.toByteArray();
               //  String paySessionId = "openid";
               //  String paySessionType = "kp_actoken";
               //  if ( SdkObject.platform  == EPlatform.ePlatform_QQ.val() )
               //  {
               //       paySessionId = "openid";
               //      paySessionType = "kp_actoken";
               //  }
               // else if(SdkObject.platform  == EPlatform.ePlatform_Weixin.val() )
               // {
               //      paySessionId = "hy_gameid";
               //      paySessionType = "wc_actoken";
               // }
               //  try
               //  {
               //      //充值游戏币
               //      unipayAPI.SaveGameCoinsWithoutNum(userId, userKey, paySessionId, paySessionType, "1", pf, pfKey, acctType, appResData);//(userId, userKey, sessionId, sessionType, pf, pfKey, serviceCode, serviceName, resId, remark);
               //  }
               //  catch (RemoteException e)
               //  {
               //      e.printStackTrace();
               //  }
                //以下是2.3.9调用的接口


                UnipayGameRequest request = new UnipayGameRequest();
                request.offerId = "1104477307";
                request.openId = userId;
                request.openKey = userKey;
                if ( SdkObject.platform  == EPlatform.ePlatform_QQ.val() )
                {
                     request.sessionId = "openid";
                    request.sessionType = "kp_actoken";
                }
               else if(SdkObject.platform  == EPlatform.ePlatform_Weixin.val() )
               {
                    request.sessionId = "hy_gameid";
                    request.sessionType = "wc_actoken";
               }
                request.zoneId = "1";
                request.pf = pf;
                request.pfKey = pfKey;//pfKey;
                Log.d(TAG, "pfkey " + request.pfKey);
                request.acctType = UnipayPlugAPI.ACCOUNT_TYPE_COMMON;;
                request.resData =appResData;
                request.isCanChange= true;
                request.saveValue = "1";
                //request.mpInfo.discountType = discounttype;
               // request.mpInfo.discountUrl  = discountUrl;
                request.extendInfo.unit="码";
                Logger.d("--------------------------------------------");
                Logger.d("userId = "+userId);
               // Logger.d("openid = "+_loginData.getUserId());
                Logger.d("userKey = "+userKey);
                Logger.d("pf = "+pf);
                Logger.d("pfKey = "+pfKey);
                Logger.d("--------------------------------------------");
                unipayAPI.LaunchPay(request, unipayStubCallBackPro);
        
            }

     });
       
          
        

                 
    }


    private double floatToDouble(float f) {
        return Double.parseDouble(String.valueOf(f));
    }

   @Override
    public void onApplicationStart(Application obj) {
    }

    @Override
    public void onApplicationTerminate() {
    }
    //显示浮标
    public void showToolBar()
    {

    }
    //关闭浮标
    public void destroyToolBar()
    {

    }
    //显示用户中心
    public void showUserCenter()
    {
        
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
        getParentActivity().runOnUiThread(new Runnable() {
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
                        userKey = wxAccessToken;
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
                String myuserkey = ret.getTokenByType(TokenType.eToken_QQ_Pay);
                Logger.d("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                Logger.d("userKey:"+userKey);
                Logger.d("myuserkey :"+myuserkey);
                Logger.d("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
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
                getParentActivity().runOnUiThread(new Runnable() {
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