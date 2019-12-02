/*
 * @Descripttion: 
 * @version: 
 * @Author: zhangbaoyan
 * @Date: 2019-11-22 15:17:53
 * @LastEditors: zhangbaoyan
 * @LastEditTime: 2019-11-22 17:52:44
 */
package com.haoyuanyiliao.cordova.plugins.trtc;

import android.content.Intent;
import com.haoyuanyiliao.cordova.plugins.trtc.MainActivity;
import com.haoyuanyiliao.cordova.plugins.trtc.rtc.TRTCMainActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class HymolTrtc extends CordovaPlugin {
    private static CallbackContext callbackContext = null;

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        if (action.equals("enterRoom")) {
            this.enterRoom(args.getString(0), callbackContext);
            return true;
        } else if (action.equals("enterRoom")) {
            this.hangUp(args.getString(0), callbackContext);
            return true;
        }
        return false;
    }

    /**
     * 进入房间
     * 
     * @param jsonParam
     */
    public void enterRoom(final String jsonParam, final CallbackContext callbackContext) {
        int sdkAppId = -1;
        int roomId = 0;
        String userId = null;
        String userSig = null;
        String base64Image = null;
        String physicianName = null;
        String physicianRole = null;
        String physicianDesc = null;
        try {
            final JSONTokener jsonTokener = new JSONTokener(jsonParam);
            final JSONObject msgJson = (JSONObject) jsonTokener.nextValue();
            sdkAppId = msgJson.getInt("sdkappid");
            roomId = msgJson.getInt("roomId");
            userId = msgJson.getString("userId");
            userSig = msgJson.getString("userSig");
            base64Image = msgJson.getString("base64Image");
            physicianName = msgJson.getString("physicianName");
            physicianRole = msgJson.getString("physicianRole");
            physicianDesc = msgJson.getString("physicianDesc");
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final Intent intent = new Intent(super.webView.getContext(), TRTCMainActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("userId", userId);
        intent.putExtra("customAudioCapture", false);
        intent.putExtra("customVideoCapture", false);
        intent.putExtra("sdkAppId", sdkAppId);
        intent.putExtra("userSig", userSig);
        RTCTools.callbackContext = callbackContext;
        // super.webView.getContext().startActivity(intent);
        ((MainActivity) super.webView.getContext()).showVideoView(roomId, userId, sdkAppId, userSig, base64Image,
                physicianName, physicianRole, physicianDesc);
    }

    /**
     * 视频挂断
     * 
     * @param jsonParam
     * @param callbackContext
     */
    public void hangUp(final String jsonParam, final CallbackContext callbackContext) {
        TRTCMainActivity.exitRoom();
        callbackContext.success();
    }

    /**
     * 通知webview
     */
    public static void noticWebview() {
        callbackContext.success("挂断");
    }
}
