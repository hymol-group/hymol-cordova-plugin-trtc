package com.haoyuanyiliao.cordova.plugins.trtc.service;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;


public class RemoteService extends Service {

    private final static int NOTIFICATION_ID = 1002;
    private static final String TAG = "RemoteService";
    private ServiceConnection serviceConnection;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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

        serviceConnection = new RemoteServiceConnection();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(RemoteService.this, LocalService.class));
        } else {
            startService(new Intent(RemoteService.this, LocalService.class));
        }
        bindService(new Intent(this, LocalService.class), serviceConnection, BIND_AUTO_CREATE);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    class RemoteServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //服务连接后回调
            Log.d(TAG, "bind LocalService");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "main process local service died，make it alive");
            //连接中断后回调
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(RemoteService.this, LocalService.class));
            } else {
                startService(new Intent(RemoteService.this, LocalService.class));
            }
            bindService(new Intent(RemoteService.this, LocalService.class), serviceConnection,
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


}
