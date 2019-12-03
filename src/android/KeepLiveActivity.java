package com.haoyuanyiliao.cordova.plugins.trtc.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import org.apache.cordova.CordovaActivity;

public class KeepLiveActivity extends CordovaActivity {
    private static KeepLiveActivity instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
        Log.d("KeepLiveActivity", "onDestroy");

    }

    /**
     * 开启保活页面
     */
    public static void startHooligan(Context context) {
        Intent intent = new Intent(context, KeepLiveActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d("KeepLiveActivity", "onDestroy");
    }

    /**
     * 关闭保活页面
     */
    public static void killHooligan() {
        if (instance != null) {
            instance.finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("KeepLiveActivity", "onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("KeepLiveActivity", "onStop");
    }


}
