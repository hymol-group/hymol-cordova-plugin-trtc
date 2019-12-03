package com.haoyuanyiliao.cordova.plugins.trtc.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/**
 * 进程保活service
 */

public class KeepLiveService extends Service {
    private final static int GRAY_SERVICE_ID = 1001;
    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT < 18) {
            //Android4.3以下 ，此方法能有效隐藏Notification上的图标
            startForeground(GRAY_SERVICE_ID, new Notification());
        } else if(Build.VERSION.SDK_INT>18 && Build.VERSION.SDK_INT<25){
            //Android4.3 - Android7.0，此方法能有效隐藏Notification上的图标
            Intent innerIntent = new Intent(this, GrayInnerService.class);
           startService(innerIntent);
           startForeground(GRAY_SERVICE_ID, new Notification());
        }else{
            //Android7.1 google修复了此漏洞，暂无解决方法（现状：Android7.1以上app启动后通知栏会出现一条"正在运行"的通知消息）
            startForeground(GRAY_SERVICE_ID, new Notification());
        }


        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 辅助Service
     */
    public static class GrayInnerService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
