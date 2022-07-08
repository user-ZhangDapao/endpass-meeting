package com.sdcz.endpass.widget;

import static com.comix.meeting.entities.LayoutType.CULTIVATE_LAYOUT;
import static com.comix.meeting.entities.LayoutType.VIDEO_LAYOUT;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.comix.meeting.entities.FullType;
import com.comix.meeting.entities.LayoutType;
import com.comix.meeting.entities.MeetingInfo;
import com.comix.meeting.listeners.LayoutModelListener;
import com.inpor.nativeapi.adaptor.RoomWndState;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.google.gson.Gson;

/**
 * @author yinhui
 * @date create at 2019/2/25
 * @description
 */
public class VariableLayout extends ViewGroup implements LayoutModelListener {

    private static final String TAG = "VariableLayout";
    public static final String ACTION_DATA_LAYOUT_VISIBILITY = "com.sdcz.endpass.DATA_LAYOUT_VISIBILITY_CHANGED";

    public static int DEFAULT_SIZE = 5;
    public static final int STYLE_DATA_ONLY = 10;
    public static final int STYLE_VIDEO_ONLY = 11;
    public static final int STYLE_DATA_VIDEO_MIX = 12;
    private DataLayout dataLayout;
    private VideoLayout videoLayout;

    private MeetingInfo localMeetingInfo;
    private int currentStyle;

    private int videoLeft;
    private int videoTop;
    private boolean dataLayoutShowing;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public VariableLayout(Context context) {
        super(context);
        init(context);

    }

    public VariableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VariableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        DEFAULT_SIZE = SizeUtils.dp2px(1f);
        setBackgroundColor(Color.BLACK);
        dataLayout = new DataLayout(context);
        dataLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(dataLayout);
        videoLayout = new VideoLayout(context);
        videoLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        videoLayout.setBackgroundResource(R.color.color_292C33);
        addView(videoLayout);
    }

    public int getCurrentStyle() {
        return currentStyle;
    }

    public boolean isDataLayoutShowing() {
        return dataLayoutShowing;
    }

    /**
     *
     */
    public void subscribe() {
        SdkUtil.getMeetingManager().addLayoutListener(this);
        dataLayout.subscribe();
        videoLayout.subscribe();
    }

    /**
     *
     */
    public void unSubscribe() {
        SdkUtil.getMeetingManager().removeLayoutListener(this);
        dataLayout.unSubscribe();
        videoLayout.unSubscribe();
    }

    /**
     * 更新布局样式
     *
     * @return 是否成功
     */
    public boolean updateLayoutStyle() {
        int style = parseStyle(localMeetingInfo);
        Log.i(TAG, "updateLayoutStyle: style is " + style);
        if (style == 0 || currentStyle == style) {
            return false;
        }
        currentStyle = style;
        if (currentStyle == STYLE_VIDEO_ONLY) {
            dataLayoutShowing = false;
            dataLayout.onHiddenChanged(true);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(getDataLayoutViIntent(false));
        } else {
            dataLayoutShowing = true;
            dataLayout.onHiddenChanged(false);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(getDataLayoutViIntent(true));
        }
        return true;
    }

    @Override
    public void onLayoutChanged(MeetingInfo meetingInfo) {
        if (meetingInfo == null) {
            return;
        }
        this.localMeetingInfo = meetingInfo;
        Log.i(TAG, "onLayoutChanged:meetingInfo is " + new Gson().toJson(localMeetingInfo));
        videoLayout.setMeetingInfo(localMeetingInfo);
        updateLayoutStyle();
        boolean success = updateLayoutStyle();
        if (success) {
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (currentStyle == 0) {
            return;
        }
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        final LayoutParams dataLayoutLayoutParams = dataLayout.getLayoutParams();
        final LayoutParams videoLayoutLayoutParams = videoLayout.getLayoutParams();
        if (currentStyle == STYLE_DATA_ONLY) {
            dataLayoutLayoutParams.width = width;
            dataLayoutLayoutParams.height = height - SizeUtils.dp2px(1f);
            videoLayoutLayoutParams.width = DEFAULT_SIZE;
            videoLayoutLayoutParams.height = DEFAULT_SIZE;
            videoLeft = 0;
            videoTop = dataLayoutLayoutParams.height;
        } else if (currentStyle == STYLE_VIDEO_ONLY) {
            dataLayoutLayoutParams.width = DEFAULT_SIZE;
            dataLayoutLayoutParams.height = DEFAULT_SIZE;
            videoLayoutLayoutParams.width = width;
            videoLayoutLayoutParams.height = height;
            videoLeft = 0;
            videoTop = 0;
        } else if (currentStyle == STYLE_DATA_VIDEO_MIX) {
            // 竖屏
            if (ScreenUtils.isPortrait()) {
                dataLayoutLayoutParams.width = width;
                videoLayoutLayoutParams.width = LayoutParams.WRAP_CONTENT;
                int singleWidth = width / 3;
                videoLayoutLayoutParams.height = (int) (singleWidth / VideoLayout.DEFAULT_VIEW_SCALE_1);
                dataLayoutLayoutParams.height = height - videoLayoutLayoutParams.height;
                videoLeft = 0;
                videoTop = dataLayoutLayoutParams.height;
            } else {
                dataLayoutLayoutParams.height = height;
                videoLayoutLayoutParams.height = LayoutParams.WRAP_CONTENT;
                int singleHeight = height / 3;
                videoLayoutLayoutParams.width = (int) (singleHeight * VideoLayout.DEFAULT_VIEW_SCALE);
                dataLayoutLayoutParams.width = width - videoLayoutLayoutParams.width;
                videoLeft = dataLayoutLayoutParams.width;
                videoTop = 0;
            }
        }
        measureChild(dataLayout, widthMeasureSpec, heightMeasureSpec);
        measureChild(videoLayout, widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        dataLayout.layout(0, 0, dataLayout.getMeasuredWidth(), dataLayout.getMeasuredHeight());
        int videoRight = videoLeft + videoLayout.getMeasuredWidth();
        int videoBottom = videoTop + videoLayout.getMeasuredHeight();
        videoLayout.layout(videoLeft, videoTop, videoRight, videoBottom);
    }

    private int parseStyle(MeetingInfo meetingInfo) {
        if (meetingInfo == null) {
            return 0;
        }
        if (meetingInfo.layoutType == VIDEO_LAYOUT ||
                SdkUtil.getVideoManager().hasFullScreenVideo()) {
            return STYLE_VIDEO_ONLY;
        } else if (meetingInfo.layoutType == CULTIVATE_LAYOUT ||
                meetingInfo.fullType == FullType.DATA_FULL_SCREEN) {
            return STYLE_DATA_ONLY;
        } else if (meetingInfo.layoutType == LayoutType.STANDARD_LAYOUT ||
                meetingInfo.fullType == FullType.DATA_AND_VIDEO_FULL_SCREEN) {
            return STYLE_DATA_VIDEO_MIX;
        }
        return 0;
    }


    private Intent intent;

    private Intent getDataLayoutViIntent(boolean show) {
        if (intent == null) {
            intent = new Intent(ACTION_DATA_LAYOUT_VISIBILITY);
        }
        intent.putExtra("state", show);
        return intent;
    }


}
