package com.talkingsdk.tencent;

import android.util.Log;
import com.talkingsdk.MainApplication;
import com.talkingsdk.SdkBase;
import com.talkingsdk.EngineNativeActivity;
import android.os.Bundle;


/**
 * 欢迎界面
 *
 */
public class GameActivity extends EngineNativeActivity {
    private String TAG = this.getClass().getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SdkBase sdkBase = MainApplication.getInstance().getSdkInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
