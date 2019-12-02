package com.haoyuanyiliao.cordova.plugins.trtc.rtc;

import com.tencent.trtc.TRTCCloudDef;

public class Config {
    // 640_360
    public final static int VIDEO_RESOLUTION = 108;
    public final static int VIDEO_FPS = 15;
    public final static int VIDEO_BITRATE = 600;
    public final static int VIDEO_RESOLUTION_MODE = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;
    public final static int CONTROL_MODE = TRTCCloudDef.VIDEO_QOS_CONTROL_SERVER;
    public final static int PREFERENCE = TRTCCloudDef.TRTC_VIDEO_QOS_PREFERENCE_CLEAR;

    public final static int SMALL_VIDEO_RESOLUTION = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_160_90;
    public final static int SMALL_VIDEO_FPS = 15;
    public final static int SMALL_VIDEO_BITRATE = 100;
    public final static int SMALL_VIDEO_RESOLUTION_MODE = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;

    public final static boolean ENABLE_SMALL = true;
    public final static int PRIOR_REMOTE_VIDEO_STREAM_TYPE = TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SMALL;

    public final static boolean CAMERA_FRONT = true;
    public final static boolean VIDEO_FILL_MODE  = true;
    public final static boolean VIDEO_VERTICAL  = true;
    public final static boolean ENABLE_AUDIO_CAPTURE = true;
    public final static boolean AUDIO_HAND_FREEMODE = true;
    public final static boolean ENABLE_G_SENSOR_MODE = false;
    public final static boolean AUDIO_VOLUME_EVALUATION = true;
    public final static boolean ENABLE_CLOUD_MIXTURE = true;
    public final static boolean ENABLE_VIDEO_ENC_MIRROR = false;
    public final static int     LOCAL_VIDEO_VIEW_MIRROR = 0;//TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_AUTO;
}
