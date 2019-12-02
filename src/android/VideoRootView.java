package com.haoyuanyiliao.cordova.plugins.trtc.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.haoyuanyiliao.cordova.plugins.trtc.R;
import com.haoyuanyiliao.cordova.plugins.trtc.rtc.TRTCVideoViewLayout;
import com.haoyuanyiliao.cordova.plugins.trtc.util.ScreenUtil;
import com.haoyuanyiliao.cordova.plugins.trtc.widget.roundImage.RoundedImageView;

public class VideoRootView extends LinearLayout {
    private WindowManager windowManager;
    private int startX, startY, x, y;
    private int endX;
    private int halfScreenWidth;
    private int screenWidth;
    private boolean isMove;
    private boolean isTouch;
    private boolean isCancel;
    private boolean isShow;
    public static final int HIDE = 0;
    private Boolean isScale = false;

    private WindowManager.LayoutParams mParams;

    private OnClickListener onClickListener;

    /**********************video view*******************************************/
    public EditText etRoomId, etUserId;
    public ImageView ivHangUp, ivLinkMicLoading, ivScale;
    public TRTCVideoViewLayout mVideoViewLayout;
    public Button btnConfirm, btnCancel;
    public FrameLayout linkMicLayout, linkMicLoading;


    public RelativeLayout mLayoutPhysician;
    public RoundedImageView mIvPhysician;
    public TextView mTvPhysicianName;
    public TextView mTvPhysicianRole;
    public TextView mTvPhysicianDesc;

    private View view;

    public VideoRootView(Context context) {
        super(context);
        initView(context);
    }

    public VideoRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public VideoRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        screenWidth = ScreenUtil.getScreenWidth(context);
        halfScreenWidth = screenWidth / 2;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        view = LayoutInflater.from(context).inflate(R.layout.video_view, this);
        initVideoView();
    }

    private void initVideoView() {
        mVideoViewLayout = view.findViewById(R.id.ll_mainview);
        ivHangUp = view.findViewById(R.id.iv_hang_up);
        etRoomId = view.findViewById(R.id.edit_room_id);
        etUserId = view.findViewById(R.id.edit_user_id);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        btnCancel = view.findViewById(R.id.btn_cancel);
        linkMicLayout = view.findViewById(R.id.layout_linkmic);
        linkMicLoading = view.findViewById(R.id.layout_linkmic_loading);
        ivLinkMicLoading = view.findViewById(R.id.imageview_linkmic_loading);
        ivScale = view.findViewById(R.id.iv_scale);
        mLayoutPhysician = view.findViewById(R.id.layout_physician_info);
        mIvPhysician = view.findViewById(R.id.riv_physician_avatar);
        mTvPhysicianName = view.findViewById(R.id.tv_physician_name);
        mTvPhysicianRole = view.findViewById(R.id.tv_physician_role);
        mTvPhysicianDesc = view.findViewById(R.id.tv_physician_desc);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isShow) {
            isTouch = true;
            //getRawX getRawY：触摸点相对于屏幕的坐标 getX getY：触摸点相对于按钮的坐标
            x = (int) event.getRawX();
            y = (int) event.getRawY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = (int) event.getX();
                    startY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mParams.x = x - startX;
                    mParams.y = y - startY;

                    int xMove = Math.abs((int) (event.getX() - startX));
                    int yMove = Math.abs((int) (event.getY() - startY));

                    if (xMove > 10 || yMove > 10) {
                        //x轴或y轴方向的移动距离大于10个像素，视为拖动，否则视为点击
                        isMove = true;
                        windowManager.updateViewLayout(this, mParams);
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    isTouch = false;
                    endX = (int) event.getRawX();
                    if (!isMove) {
                        if (onClickListener != null) {
                            onClickListener.onClick(view);
                        }
                    } else {
                        isMove = false;
                        windowManager.updateViewLayout(this, mParams);
                    }
                    break;
            }
        } else {
            onClickListener.onClick(view);
        }
        return false;
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setParams(WindowManager.LayoutParams mParams) {
        this.mParams = mParams;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    /**
     * 当为小窗模式时，拦截点击事件，不再分发给外层view。否则小窗不能移动和点击
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isScale) {
            return true;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    //是否为小窗
    public void setScale(boolean isScale) {
        this.isScale = isScale;
    }
}
