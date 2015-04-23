package com.talkingsdk.tencent;

import android.os.Bundle;
import com.talkingsdk.StartBaseActivity;
import com.tencent.msdk.api.WGPlatform;
import com.tencent.msdk.api.WGPlatformObserver;
import android.content.Intent;
import android.util.Log;
import com.tencent.msdk.tools.Logger;
import com.tencent.msdk.api.MsdkBaseInfo;
import com.tencent.msdk.api.WGPlatform;
import com.tencent.msdk.api.WGQZonePermissions;
/**
 * 欢迎界面
 *
 */
public class StartActivity extends StartBaseActivity{
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
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
}