package com.haoyuanyiliao.cordova.plugins.trtc.service;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class LocalService extends Service {

    private final static int NOTIFICATION_ID = 1003;
    private static final String TAG = "LocalService";
    private ServiceConnection serviceConnection;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //此处可以写上一些业务逻辑
        try {
            Notification notification = new Notification();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                startForeground(NOTIFICATION_ID, notification);
            } else {
                startForeground(NOTIFICATION_ID, notification);
                // start InnerService
                startService(new Intent(this, InnerService.class));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        serviceConnection = new LocalServiceConnection();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(LocalService.this, RemoteService.class));
        } else {
            startService(new Intent(LocalService.this, RemoteService.class));
        }
        bindService(new Intent(this, RemoteService.class), serviceConnection, BIND_AUTO_CREATE);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }


    class LocalServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "bind RemoteService");
            //服务连接后回调

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "remote service died，make it alive");
            //连接中断后回调
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(LocalService.this, RemoteService.class));
            } else {
                startService(new Intent(LocalService.this, RemoteService.class));
            }
            bindService(new Intent(LocalService.this, RemoteService.class), serviceConnection,
                    BIND_AUTO_CREATE);
        }
    }


    public static class InnerService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            try {
                startForeground(NOTIFICATION_ID, new Notification());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            stopSelf();
        }

        @Override
        public void onDestroy() {
            stopForeground(true);
            super.onDestroy();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
