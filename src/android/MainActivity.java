/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.haoyuanyiliao.cordova.plugins.trtc;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.widget.Toast;

import com.haoyuanyiliao.cordova.plugins.trtc.service.DaemonJobService;
import com.haoyuanyiliao.cordova.plugins.trtc.service.KeepLiveActivity;

import com.haoyuanyiliao.cordova.plugins.trtc.service.LocalService;
import com.haoyuanyiliao.cordova.plugins.trtc.service.RemoteService;
import com.haoyuanyiliao.cordova.plugins.trtc.widget.VideoViewManager;


import org.apache.cordova.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends CordovaActivity {
    private final static int REQ_PERMISSION_CODE = 0x1000;
    private BootCompleteReceiver bootCompleteReceiver;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
        this.checkPermission();

        requestVideoViewPermission();
//        startKeepLiveService();
//        bootCompleteReceiver = new BootCompleteReceiver();
//        registerBrodecast();

    }


    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECEIVE_BOOT_COMPLETED)) {
                permissions.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);
            }

            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        (String[]) permissions.toArray(new String[0]),
                        REQ_PERMISSION_CODE);
                return false;
            }
        }

        return true;
    }

    private Context getContext() {
        return this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_CODE:
                for (int ret : grantResults) {
                    if (PackageManager.PERMISSION_GRANTED != ret) {
                        Toast.makeText(getContext(), "用户没有允许需要的权限，使用可能会受到限制！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    //~~~~~~~~~~~~~以下为视频窗口~~~~~~~~~~~~~~~~~~~~~~~~~~

    private VideoViewManager videoViewManager;
    private int roomId;
    private int sdkAppId;
    private String userId;
    private String userSig;
    private String base64Image;
    private String physicianName;
    private String physicianRole;
    private String physicianDesc;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (videoViewManager != null && videoViewManager.isVideoViewShowing()) {
                        dismissVideoView();
                    }
                    videoViewManager = new VideoViewManager(MainActivity.this);
                    videoViewManager.initData(roomId, userId, sdkAppId, userSig);
                    videoViewManager.showVideoView();
                    videoViewManager.setPhysicianInfo(base64Image, physicianName, physicianRole, physicianDesc);
                    break;
            }
        }
    };

    //视频小窗权限需要单独申请
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestVideoViewPermission() {

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(getContext(), "请打开所需权限，否则使用可能会受到限制！", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), REQ_PERMISSION_CODE);
        }
    }

    //打开视频页面
    public void showVideoView(int roomId, String userId, int sdkAppId, String userSig, String base64Image, String physicianName, String physicianRole, String physicianDesc) {
        this.roomId = roomId;
        this.userId = userId;
        this.sdkAppId = sdkAppId;
        this.userSig = userSig;
        this.base64Image = base64Image;
        this.physicianName = physicianName;
        this.physicianRole = physicianRole;
        this.physicianDesc = physicianDesc;

        mHandler.sendEmptyMessage(0);

    }

    public void dismissVideoView() {
        if (videoViewManager != null) {
            videoViewManager.dismissVideoView();
            videoViewManager = null;
        }
    }

    //开启保活服务
    private void startKeepLiveService() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            DaemonJobService.startJob(this);
        } else {
            startService(new Intent(this, LocalService.class));
            startService(new Intent(this, RemoteService.class));
        }


    }

    //开启视频状态时，点击返回键关闭视频退出房间
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (videoViewManager != null && videoViewManager.isVideoViewShowing()) {
                dismissVideoView();
                return false;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void registerBrodecast() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(bootCompleteReceiver, filter);

    }

    //接受开屏锁屏广播
    public class BootCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                KeepLiveActivity.startHooligan(MainActivity.this);
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                KeepLiveActivity.killHooligan();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(bootCompleteReceiver);
    }
}
