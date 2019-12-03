package com.haoyuanyiliao.cordova.plugins.trtc.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.tencent.liteav.TXLiteAVCode;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;
import com.tencent.trtc.TRTCStatistics;
import com.haoyuanyiliao.cordova.plugins.trtc.MainActivity;
import com.haoyuanyiliao.cordova.plugins.trtc.R;
import com.haoyuanyiliao.cordova.plugins.trtc.rtc.Config;
import com.haoyuanyiliao.cordova.plugins.trtc.rtc.TRTCVideoViewLayout;
import com.haoyuanyiliao.cordova.plugins.trtc.rtc.TestCustomVideo.TestRenderVideoFrame;
import com.haoyuanyiliao.cordova.plugins.trtc.util.ScreenUtil;
import com.haoyuanyiliao.cordova.plugins.trtc.util.SystemHelper;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.haoyuanyiliao.cordova.plugins.trtc.HymolTrtc;;

public class VideoViewManager implements TRTCVideoViewLayout.ITRTCVideoViewLayoutListener, View.OnClickListener {
    private String TAG = "VideoViewManager";
    private Context mContext;

    private VideoRootView videoRootView;
    private TRTCVideoViewLayout mVideoViewLayout;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams wmParams;

    private boolean isAddWindowManager = false;
    private boolean bBeautyEnable = false, bEnableVideo = true, bEnableAudio = true, beingLinkMic = false;

    private String mUserIdBeingLink = "";
    private boolean mEnableCustomVideoCapture = false;
    private int iDebugLevel = 0;
    private int mSdkAppId = -1;
    private TRTCCloudDef.TRTCParams trtcParams;     /// TRTC SDK 视频通话房间进入所必须的参数
    private TRTCCloud trtcCloud;              /// TRTC SDK 实例对象
    private TRTCCloudListenerImpl trtcListener;    /// TRTC SDK 回调监听

    private HashSet<String> mRoomMembers = new HashSet<>();


    public VideoViewManager(Context mContext) {
        this.mContext = mContext;

    }

    /**
     * 初始化视频参数
     *
     * @param roomId
     * @param userId
     * @param sdkAppId
     * @param userSig
     */
    public void initData(int roomId, String userId, int sdkAppId, String userSig) {
        mSdkAppId = sdkAppId;
        trtcParams = new TRTCCloudDef.TRTCParams(mSdkAppId, userId, userSig, roomId, "", "");
        videoRootView = new VideoRootView(mContext);
        mVideoViewLayout = videoRootView.mVideoViewLayout;
        mVideoViewLayout.setUserId(trtcParams.userId);
        mVideoViewLayout.setListener(this);
        TXCloudVideoView localVideoView = mVideoViewLayout.getCloudVideoViewByIndex(0);
        localVideoView.setUserId(trtcParams.userId);
        videoRootView.btnConfirm.setOnClickListener(this);
        videoRootView.btnCancel.setOnClickListener(this);
        videoRootView.ivHangUp.setOnClickListener(this);
        videoRootView.ivScale.setOnClickListener(this);
        //创建 TRTC SDK 实例

        trtcListener = new TRTCCloudListenerImpl((MainActivity) mContext);
        trtcCloud = TRTCCloud.sharedInstance(mContext);
        trtcCloud.setListener(trtcListener);

    }


    /**
     * 打开视频页面
     */
    public void showVideoView() {
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//6.0+
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = ScreenUtil.getScreenWidth(mContext);
        wmParams.height = ScreenUtil.getScreenHeight(mContext) - ScreenUtil.getNavigationBottomHeight(mContext);
        videoRootView.setParams(wmParams);
        videoRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFloatSize(false);
            }
        });
        mWindowManager.addView(videoRootView, wmParams);
        videoRootView.mVideoViewLayout.hintCloudVideoView(true);
        isAddWindowManager = true;
        enterRoom();

    }

    /**
     * 视频页面是否打开
     *
     * @return
     */
    public boolean isVideoViewShowing() {
        return isAddWindowManager;
    }

    /**
     * 进入房间
     */
    private void enterRoom() {
        // 预览前配置默认参数
        setTRTCCloudParam();

        // 开启视频采集预览
        startLocalVideo(true);

//        trtcCloud.setBeautyStyle(TRTCCloudDef.TRTC_BEAUTY_STYLE_SMOOTH, 5, 5, 5);

        trtcCloud.startLocalAudio();

        setVideoFillMode(Config.VIDEO_FILL_MODE);

        setVideoRotation(Config.VIDEO_VERTICAL);

        enableAudioHandFree(Config.AUDIO_HAND_FREEMODE);

        enableGSensor(Config.ENABLE_G_SENSOR_MODE);

        enableAudioVolumeEvaluation(Config.AUDIO_VOLUME_EVALUATION);

        enableVideoEncMirror(Config.ENABLE_VIDEO_ENC_MIRROR);

        setLocalViewMirrorMode(Config.LOCAL_VIDEO_VIEW_MIRROR);

        mRoomMembers.clear();

        trtcCloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL);

        Toast.makeText(mContext, "开始进房", Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置视频通话的视频参数：需要 TRTCSettingDialog 提供的分辨率、帧率和流畅模式等参数
     */
    private void setTRTCCloudParam() {

        // 大画面的编码器参数设置
        // 设置视频编码参数，包括分辨率、帧率、码率等等，这些编码参数来自于 TRTCSettingDialog 的设置
        // 注意（1）：不要在码率很低的情况下设置很高的分辨率，会出现较大的马赛克
        // 注意（2）：不要设置超过25FPS以上的帧率，因为电影才使用24FPS，我们一般推荐15FPS，这样能将更多的码率分配给画质
        TRTCCloudDef.TRTCVideoEncParam encParam = new TRTCCloudDef.TRTCVideoEncParam();
        encParam.videoResolution = Config.VIDEO_RESOLUTION; //640_360
        encParam.videoFps = Config.VIDEO_FPS;
        encParam.videoBitrate = Config.VIDEO_BITRATE;
        encParam.videoResolutionMode = Config.VIDEO_RESOLUTION_MODE;
        trtcCloud.setVideoEncoderParam(encParam);

        TRTCCloudDef.TRTCNetworkQosParam qosParam = new TRTCCloudDef.TRTCNetworkQosParam();
        qosParam.controlMode = Config.CONTROL_MODE;
        qosParam.preference = Config.PREFERENCE;
        trtcCloud.setNetworkQosParam(qosParam);

        //小画面的编码器参数设置
        //TRTC SDK 支持大小两路画面的同时编码和传输，这样网速不理想的用户可以选择观看小画面
        //注意：iPhone & Android 不要开启大小双路画面，非常浪费流量，大小路画面适合 Windows 和 MAC 这样的有线网络环境
        TRTCCloudDef.TRTCVideoEncParam smallParam = new TRTCCloudDef.TRTCVideoEncParam();
        smallParam.videoResolution = Config.SMALL_VIDEO_RESOLUTION;
        smallParam.videoFps = Config.SMALL_VIDEO_FPS;
        smallParam.videoBitrate = Config.SMALL_VIDEO_BITRATE;
        smallParam.videoResolutionMode = Config.SMALL_VIDEO_RESOLUTION_MODE;
        trtcCloud.enableEncSmallVideoStream(Config.ENABLE_SMALL, smallParam);

        trtcCloud.setPriorRemoteVideoStreamType(Config.PRIOR_REMOTE_VIDEO_STREAM_TYPE);
    }

    /**
     * 关闭视频页面，退出聊天房间
     */
    public void dismissVideoView() {
        exitRoom();
        removeFloatWindow();
    }

    /**
     * 退出视频房间
     */
    private void exitRoom() {
        if (trtcCloud != null) {
            trtcCloud.exitRoom();
        }
        // 挂断
        HymolTrtc.noticWebview();
    }


    /**
     * 视频页面缩放
     */
    public void changeFloatSize(boolean isScale) {
        videoRootView.setScale(isScale);
        videoRootView.ivHangUp.setVisibility(isScale ? View.GONE : View.VISIBLE);
        videoRootView.ivScale.setVisibility(isScale ? View.GONE : View.VISIBLE);
        videoRootView.mVideoViewLayout.hintCloudVideoView(true);
        if (isScale) {//缩小

            wmParams.x = ScreenUtil.getScreenWidth(mContext);
            wmParams.y = 0;
            wmParams.width = ScreenUtil.getScreenWidth(mContext) / 3;
            wmParams.height = ScreenUtil.getScreenHeight(mContext) / 3;
            mWindowManager.updateViewLayout(videoRootView, wmParams);
        } else {//放大
            wmParams.x = 0;
            wmParams.y = 0;
            wmParams.width = ScreenUtil.getScreenWidth(mContext);
            wmParams.height = ScreenUtil.getScreenHeight(mContext) - ScreenUtil.getNavigationBottomHeight(mContext);
            mWindowManager.updateViewLayout(videoRootView, wmParams);

        }
        SystemHelper.setTopApp(mContext);
    }

    private void removeFloatWindow() {
        if (mWindowManager != null && videoRootView != null && isAddWindowManager) {
            videoRootView.mLayoutPhysician.setVisibility(View.GONE);
            mWindowManager.removeView(videoRootView);
            isAddWindowManager = false;
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //屏幕缩小
            case R.id.iv_scale:
                changeFloatSize(true);
                break;
            //挂断
            case R.id.iv_hang_up:
                dismissVideoView();
                break;

        }
    }

    /**
     * SDK内部状态回调
     */
    class TRTCCloudListenerImpl extends TRTCCloudListener implements TRTCCloudListener.TRTCVideoRenderListener {

        private WeakReference<MainActivity> mContext;
        private HashMap<String, TestRenderVideoFrame> mCustomRender;

        public TRTCCloudListenerImpl(MainActivity activity) {
            super();
            mContext = new WeakReference<>(activity);
            mCustomRender = new HashMap<>(10);
        }

        /**
         * 加入房间
         */
        @Override
        public void onEnterRoom(long elapsed) {
            final MainActivity activity = mContext.get();
            if (activity != null) {
                Toast.makeText(activity, "加入房间成功", Toast.LENGTH_SHORT).show();
                mVideoViewLayout.onRoomEnter();
                updateCloudMixtureParams();
            }
        }

        /**
         * 离开房间
         */
        @Override
        public void onExitRoom(int reason) {

        }

        /**
         * ERROR 大多是不可恢复的错误，需要通过 UI 提示用户
         */
        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            Log.d(TAG, "sdk callback onError");
            MainActivity activity = mContext.get();
            if (activity != null) {
                Toast.makeText(activity, "onError: " + errMsg + "[" + errCode + "]", Toast.LENGTH_SHORT).show();
                if (errCode == TXLiteAVCode.ERR_ROOM_ENTER_FAIL) {
                    exitRoom();
                }
            }
        }

        /**
         * WARNING 大多是一些可以忽略的事件通知，SDK内部会启动一定的补救机制
         */
        @Override
        public void onWarning(int warningCode, String warningMsg, Bundle extraInfo) {
            Log.d(TAG, "sdk callback onWarning");
        }

        /**
         * 有新的用户加入了当前视频房间
         */
        @Override
        public void onUserEnter(String userId) {
            MainActivity activity = mContext.get();
            if (activity != null) {
                // 创建一个View用来显示新的一路画面
                TXCloudVideoView renderView = mVideoViewLayout.onMemberEnter(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
                if (renderView != null) {
                    // 设置仪表盘数据显示
                    renderView.setVisibility(View.VISIBLE);
                    trtcCloud.showDebugView(iDebugLevel);
                    trtcCloud.setDebugViewMargin(userId, new TRTCCloud.TRTCViewMargin(0.0f, 0.0f, 0.1f, 0.0f));
                    if (mEnableCustomVideoCapture) {
                        TestRenderVideoFrame customRender = new TestRenderVideoFrame(activity);
                        mCustomRender.put(userId, customRender);
                        trtcCloud.setRemoteVideoRenderListener(userId, TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_I420, TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_BYTE_ARRAY, customRender);
                        TextureView textureView = new TextureView(activity);
                        renderView.addVideoView(textureView);
                        customRender.start(textureView);
                    }
                }
            }
        }

        /**
         * 有用户离开了当前视频房间
         */
        @Override
        public void onUserExit(String userId, int reason) {
            MainActivity activity = mContext.get();
            if (activity != null) {
                //停止观看画面
                trtcCloud.stopRemoteView(userId);
                trtcCloud.stopRemoteSubStreamView(userId);
                //更新视频UI
                mVideoViewLayout.onMemberLeave(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
                mVideoViewLayout.onMemberLeave(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
                mRoomMembers.remove(userId);
                updateCloudMixtureParams();
                TestRenderVideoFrame customRender = mCustomRender.get(userId);
                if (customRender != null) {
                    customRender.stop();
                    mCustomRender.remove(userId);
                }
                //跨房连麦
                if (beingLinkMic) {
                    if (userId.equalsIgnoreCase(mUserIdBeingLink)) {
                        stopLinkMic();
                    }
                }
                activity.dismissVideoView();
                Log.d("VideoViewManager", "检测到用户退出");
            }
        }

        /**
         * 有用户屏蔽了画面
         */
        @Override
        public void onUserVideoAvailable(final String userId, boolean available) {
            MainActivity activity = mContext.get();
            if (activity != null) {
                if (available) {
                    final TXCloudVideoView renderView = mVideoViewLayout.onMemberEnter(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
                    if (renderView != null) {
                        // 启动远程画面的解码和显示逻辑，FillMode 可以设置是否显示黑边
                        trtcCloud.setRemoteViewFillMode(userId, TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT);
                        if (mEnableCustomVideoCapture) {
                            trtcCloud.startRemoteView(userId, null);
                        } else {
                            trtcCloud.startRemoteView(userId, renderView);
                        }
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                renderView.setUserId(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
                            }
                        });
                    }

                    mRoomMembers.add(userId);
                    updateCloudMixtureParams();
                } else {
                    trtcCloud.stopRemoteView(userId);
                    //activity.mVideoViewLayout.onMemberLeave(userId+TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);

                    mRoomMembers.remove(userId);
                    updateCloudMixtureParams();
                }
                mVideoViewLayout.updateVideoStatus(userId, available);
            }

        }

        public void onUserSubStreamAvailable(final String userId, boolean available) {
            MainActivity activity = mContext.get();
            if (activity != null) {
                if (available) {
                    final TXCloudVideoView renderView = mVideoViewLayout.onMemberEnter(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
                    if (renderView != null) {
                        // 启动远程画面的解码和显示逻辑，FillMode 可以设置是否显示黑边
                        trtcCloud.setRemoteSubStreamViewFillMode(userId, TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT);
                        trtcCloud.startRemoteSubStreamView(userId, renderView);

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                renderView.setUserId(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
                            }
                        });
                    }

                } else {
                    trtcCloud.stopRemoteSubStreamView(userId);
                    mVideoViewLayout.onMemberLeave(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
                }
            }
        }

        /**
         * 有用户屏蔽了声音
         */
        @Override
        public void onUserAudioAvailable(String userId, boolean available) {
            MainActivity activity = mContext.get();
            if (activity != null) {
                if (available) {
                    final TXCloudVideoView renderView = mVideoViewLayout.onMemberEnter(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
                    if (renderView != null) {
                        renderView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        /**
         * 首帧渲染回调
         */

        public void onFirstVideoFrame(String userId, int width, int height) {
            MainActivity activity = mContext.get();
            if (activity != null) {
                mVideoViewLayout.freshToolbarLayoutOnMemberEnter(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
            }
        }

        public void onStartPublishCDNStream(int err, String errMsg) {

        }

        public void onStopPublishCDNStream(int err, String errMsg) {

        }

        public void onRenderVideoFrame(String userId, int streamType, TRTCCloudDef.TRTCVideoFrame frame) {
//            Log.w(TAG, String.format("onRenderVideoFrame userId: %s, type: %d",userId, streamType));
        }

        public void onUserVoiceVolume(ArrayList<TRTCCloudDef.TRTCVolumeInfo> userVolumes, int totalVolume) {
            for (int i = 0; i < userVolumes.size(); ++i) {
                mVideoViewLayout.updateAudioVolume(userVolumes.get(i).userId, userVolumes.get(i).volume);
            }
        }

        public void onStatistics(TRTCStatistics statics) {

        }

        @Override
        public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {
            MainActivity activity = mContext.get();
            if (activity != null) {
                mVideoViewLayout.updateNetworkQuality(localQuality.userId, localQuality.quality);
                for (TRTCCloudDef.TRTCQuality qualityInfo : remoteQuality) {
                    mVideoViewLayout.updateNetworkQuality(qualityInfo.userId, qualityInfo.quality);
                }

            }
        }

    }

    @Override
    public void onEnableRemoteVideo(String userId, boolean enable) {
        if (enable) {
            final TXCloudVideoView renderView = mVideoViewLayout.getCloudVideoViewByUseId(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
            if (renderView != null) {
                trtcCloud.setRemoteViewFillMode(userId, TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT);
                trtcCloud.startRemoteView(userId, renderView);
                ((MainActivity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        renderView.setUserId(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
                        mVideoViewLayout.freshToolbarLayoutOnMemberEnter(userId);
                    }
                });
            }
        } else {
            trtcCloud.stopRemoteView(userId);
        }

    }

    @Override
    public void onEnableRemoteAudio(String userId, boolean enable) {
        trtcCloud.muteRemoteAudio(userId, !enable);
    }

    @Override
    public void onChangeVideoFillMode(String userId, boolean adjustMode) {
        trtcCloud.setRemoteViewFillMode(userId, adjustMode ? TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT : TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL);
    }

    public void onSwitchCamera(boolean bCameraFront) {
        trtcCloud.switchCamera();
    }

    public void onFillModeChange(boolean bFillMode) {
        setVideoFillMode(bFillMode);
    }

    public void onVideoRotationChange(boolean bVertical) {
        setVideoRotation(bVertical);
    }

    public void onEnableAudioCapture(boolean bEnable) {
        enableAudioCapture(bEnable);
    }

    public void onEnableAudioHandFree(boolean bEnable) {
        enableAudioHandFree(bEnable);
    }

    public void onMirrorLocalVideo(int localViewMirror) {
        setLocalViewMirrorMode(localViewMirror);
    }

    public void onMirrorRemoteVideo(boolean bMirror) {
        enableVideoEncMirror(bMirror);
    }

    public void onEnableGSensor(boolean bEnable) {
        enableGSensor(bEnable);
    }

    public void onEnableAudioVolumeEvaluation(boolean bEnable) {
        enableAudioVolumeEvaluation(bEnable);
    }

    public void onEnableCloudMixture(boolean bEnable) {
        updateCloudMixtureParams();
    }

    public void onClickButtonGetPlayUrl() {
        if (trtcParams == null) {
            return;
        }

        String strStreamID = "3891_" + stringToMd5("" + trtcParams.roomId + "_" + trtcParams.userId + "_main");
        String strPlayUrl = "http://3891.liveplay.myqcloud.com/live/" + strStreamID + ".flv";

//        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//        ClipData clipData = ClipData.newPlainText("Label", strPlayUrl);
//        cm.setPrimaryClip(clipData);
//        Toast.makeText(getApplicationContext(), "播放地址已复制到系统剪贴板！", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, strPlayUrl);
        intent.setType("text/plain");
        mContext.startActivity(Intent.createChooser(intent, "分享"));
    }

    public void onClickButtonLinkMic() {
        if (beingLinkMic) {
            stopLinkMic();
        } else {
            showLinkMicLayout();
        }
    }

    private void setVideoFillMode(boolean bFillMode) {
        if (bFillMode) {
            trtcCloud.setLocalViewFillMode(TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL);
        } else {
            trtcCloud.setLocalViewFillMode(TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT);
        }
    }

    private void setVideoRotation(boolean bVertical) {
        if (bVertical) {
            trtcCloud.setLocalViewRotation(TRTCCloudDef.TRTC_VIDEO_ROTATION_0);
        } else {
            trtcCloud.setLocalViewRotation(TRTCCloudDef.TRTC_VIDEO_ROTATION_90);
        }
    }

    private void enableAudioCapture(boolean bEnable) {
        if (bEnable) {
            trtcCloud.startLocalAudio();
        } else {
            trtcCloud.stopLocalAudio();
        }
    }

    private void enableAudioHandFree(boolean bEnable) {
        if (bEnable) {
            trtcCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER);
        } else {
            trtcCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_EARPIECE);
        }
    }

    private void enableVideoEncMirror(boolean bMirror) {
        trtcCloud.setVideoEncoderMirror(bMirror);
    }

    private void setLocalViewMirrorMode(int mirrorMode) {
        trtcCloud.setLocalViewMirror(mirrorMode);
    }

    private void enableGSensor(boolean bEnable) {
        if (bEnable) {
            trtcCloud.setGSensorMode(TRTCCloudDef.TRTC_GSENSOR_MODE_UIFIXLAYOUT);
        } else {
            trtcCloud.setGSensorMode(TRTCCloudDef.TRTC_GSENSOR_MODE_DISABLE);
        }
    }

    private void enableAudioVolumeEvaluation(boolean bEnable) {
        if (bEnable) {
            trtcCloud.enableAudioVolumeEvaluation(200);
        } else {
            trtcCloud.enableAudioVolumeEvaluation(0);
            mVideoViewLayout.hideAudioVolumeProgressBar();
        }
    }

    private void updateCloudMixtureParams() {
        // 背景大画面宽高
        int videoWidth = 720;
        int videoHeight = 1280;

        // 小画面宽高
        int subWidth = 180;
        int subHeight = 320;

        int offsetX = 5;
        int offsetY = 50;

        int bitrate = 200;

        int resolution = com.haoyuanyiliao.cordova.plugins.trtc.rtc.Config.VIDEO_RESOLUTION;
        switch (resolution) {

            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_160_160: {
                videoWidth = 160;
                videoHeight = 160;
                subWidth = 27;
                subHeight = 48;
                offsetY = 20;
                bitrate = 200;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_320_180: {
                videoWidth = 192;
                videoHeight = 336;
                subWidth = 54;
                subHeight = 96;
                offsetY = 30;
                bitrate = 400;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_320_240: {
                videoWidth = 240;
                videoHeight = 320;
                subWidth = 54;
                subHeight = 96;
                bitrate = 400;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_480_480: {
                videoWidth = 480;
                videoHeight = 480;
                subWidth = 72;
                subHeight = 128;
                bitrate = 600;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360: {
                videoWidth = 368;
                videoHeight = 640;
                subWidth = 90;
                subHeight = 160;
                bitrate = 800;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_480: {
                videoWidth = 480;
                videoHeight = 640;
                subWidth = 90;
                subHeight = 160;
                bitrate = 800;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_960_540: {
                videoWidth = 544;
                videoHeight = 960;
                subWidth = 171;
                subHeight = 304;
                bitrate = 1000;
                break;
            }
            case TRTCCloudDef.TRTC_VIDEO_RESOLUTION_1280_720: {
                videoWidth = 720;
                videoHeight = 1280;
                subWidth = 180;
                subHeight = 320;
                bitrate = 1500;
                break;
            }
        }

        TRTCCloudDef.TRTCTranscodingConfig config = new TRTCCloudDef.TRTCTranscodingConfig();
        config.appId = -1;  // 请从"实时音视频"控制台的帐号信息中获取
        config.bizId = -1;  // 请进入 "实时音视频"控制台 https://console.cloud.tencent.com/rav，点击对应的应用，然后进入“帐号信息”菜单中，复制“直播信息”模块中的"bizid"
        config.videoWidth = videoWidth;
        config.videoHeight = videoHeight;
        config.videoGOP = 3;

        config.videoWidth = videoWidth;
        config.videoHeight = videoHeight;
        config.videoGOP = 3;
        config.videoFramerate = 15;
        config.videoBitrate = bitrate;
        config.audioSampleRate = 48000;
        config.audioBitrate = 64;
        config.audioChannels = 1;

        // 设置混流后主播的画面位置
        TRTCCloudDef.TRTCMixUser broadCaster = new TRTCCloudDef.TRTCMixUser();
        broadCaster.userId = trtcParams.userId; // 以主播uid为broadcaster为例
        broadCaster.zOrder = 0;
        broadCaster.x = 0;
        broadCaster.y = 0;
        broadCaster.width = videoWidth;
        broadCaster.height = videoHeight;

        config.mixUsers = new ArrayList<>();
        config.mixUsers.add(broadCaster);

        // 设置混流后各个小画面的位置
        if (Config.ENABLE_CLOUD_MIXTURE) {
            int index = 0;
            for (String userId : mRoomMembers) {
                TRTCCloudDef.TRTCMixUser audience = new TRTCCloudDef.TRTCMixUser();
                audience.userId = userId;
                audience.zOrder = 1 + index;
                if (index < 3) {
                    // 前三个小画面靠右从下往上铺
                    audience.x = videoWidth - offsetX - subWidth;
                    audience.y = videoHeight - offsetY - index * subHeight - subHeight;
                    audience.width = subWidth;
                    audience.height = subHeight;
                } else if (index < 6) {
                    // 后三个小画面靠左从下往上铺
                    audience.x = offsetX;
                    audience.y = videoHeight - offsetY - (index - 3) * subHeight - subHeight;
                    audience.width = subWidth;
                    audience.height = subHeight;
                } else {
                    // 最多只叠加六个小画面
                }

                config.mixUsers.add(audience);
                ++index;
            }
        }

        trtcCloud.setMixTranscodingConfig(config);
    }

    protected String stringToMd5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void showLinkMicLayout() {

        videoRootView.linkMicLayout.setVisibility(View.VISIBLE);
        videoRootView.etRoomId.setText("");
        videoRootView.etUserId.setText("");
    }

    private void hideLinkMicLayout() {

        videoRootView.linkMicLayout.setVisibility(View.GONE);
    }

    private void startLinkMicLoading() {

        videoRootView.linkMicLoading.setVisibility(View.VISIBLE);

        videoRootView.ivLinkMicLoading.setImageResource(R.drawable.trtc_linkmic_loading);
        AnimationDrawable animation = (AnimationDrawable) videoRootView.ivLinkMicLoading.getDrawable();
        if (animation != null) {
            animation.start();
        }
    }

    private void stopLinkMicLoading() {

        videoRootView.linkMicLoading.setVisibility(View.GONE);

        AnimationDrawable animation = (AnimationDrawable) videoRootView.ivLinkMicLoading.getDrawable();
        if (animation != null) {
            animation.stop();
        }
    }

    private void startLinkMic() {
        String roomId = videoRootView.etRoomId.getText().toString();
        String userId = videoRootView.etUserId.getText().toString();
        if (roomId == null || roomId.isEmpty()) {
            Toast.makeText(mContext, "请输入目标房间名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(mContext, "请输入目标用户ID", Toast.LENGTH_SHORT).show();
            return;
        }

        mUserIdBeingLink = userId;

        trtcCloud.ConnectOtherRoom(String.format("{\"roomId\":%s,\"userId\":\"%s\"}", roomId, userId));

        hideLinkMicLayout();
        startLinkMicLoading();
    }


    private void stopLinkMic() {
        trtcCloud.DisconnectOtherRoom();
    }

    private void startLocalVideo(boolean enable) {
        TXCloudVideoView localVideoView = mVideoViewLayout.getCloudVideoViewByUseId(trtcParams.userId);
        localVideoView.setUserId(trtcParams.userId);
        localVideoView.setVisibility(View.VISIBLE);
        localVideoView.setEnabled(false);
        localVideoView.setFocusable(false);
        if (enable) {
            // 设置 TRTC SDK 的状态
            trtcCloud.enableCustomVideoCapture(mEnableCustomVideoCapture);
            //启动SDK摄像头采集和渲染
            trtcCloud.startLocalPreview(Config.CAMERA_FRONT, localVideoView);
        }
    }

    public void setPhysicianInfo(String base64Data, String name, String role, String desc) {
        if (videoRootView != null) {
            videoRootView.mTvPhysicianName.setText(name);
            videoRootView.mTvPhysicianRole.setText(role);
            videoRootView.mTvPhysicianDesc.setText("简介：" + desc);
            videoRootView.mIvPhysician.setImageBitmap(stringToBitmap(base64Data));
            videoRootView.mLayoutPhysician.setVisibility(View.VISIBLE);
        }


    }

    private Bitmap stringToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

    }

}
