package com.talkingsdk.tencent;

import org.cocos2dx.lib.Cocos2dxGLSurfaceView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.talkingsdk.Cocos2dxBaseActivity;
import com.talkingsdk.MainApplication;
import com.talkingsdk.SdkBase;


/**
 * 欢迎界面
 * 
 */
public class GameActivity extends Cocos2dxBaseActivity {
    private String TAG = this.getClass().getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        SdkBase sdkBase = MainApplication.getInstance().getSdkInstance();
        //TODO fix code value
        MainApplication.getInstance().notifyGameLogined(sdkBase.getLoginData(), 0);
        
    }
	
	public Cocos2dxGLSurfaceView onCreateView() {
        Cocos2dxGLSurfaceView glSurfaceView = new Cocos2dxGLSurfaceView(this);
        // TestCpp should create stencil buffer
        glSurfaceView.setEGLConfigChooser(5, 6, 5, 0, 16, 8);

        return glSurfaceView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
