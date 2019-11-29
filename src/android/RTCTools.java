/*
 * @Descripttion: 
 * @version: 
 * @Author: zhangbaoyan
 * @Date: 2019-11-22 15:17:53
 * @LastEditors: zhangbaoyan
 * @LastEditTime: 2019-11-22 15:32:04
 */
package cordova.plugin.videotools;

import android.content.Intent;
import com.zx.physician.MainActivity;
import com.zx.physician.rtc.TRTCMainActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RTCTools extends CordovaPlugin{
    private static CallbackContext callbackContext = null;
	@Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
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
     * @param jsonParam
     */
    public void enterRoom(String jsonParam, CallbackContext callbackContext){
        int sdkAppId = -1;
        int roomId = 0;
        String userId  = null;
        String userSig = null;
        try{
            JSONTokener jsonTokener = new JSONTokener(jsonParam);
            JSONObject msgJson = (JSONObject) jsonTokener.nextValue();
            sdkAppId = msgJson.getInt("sdkappid");
            roomId = msgJson.getInt("roomId");
            userId = msgJson.getString("userId");
            userSig = msgJson.getString("userSig");
        }catch (Exception e){
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
//        super.webView.getContext().startActivity(intent);
        ((MainActivity)super.webView.getContext()).showVideoView(roomId,userId,sdkAppId,userSig,"","","","");
    }

    /**
     * 视频挂断
     * @param jsonParam
     * @param callbackContext
     */
    public void hangUp(String jsonParam, CallbackContext callbackContext){
        TRTCMainActivity.exitRoom();
        callbackContext.success();
    }
    /**
     * 通知webview
     */
    public static void noticWebview(){
        callbackContext.success("挂断");
    }
}
