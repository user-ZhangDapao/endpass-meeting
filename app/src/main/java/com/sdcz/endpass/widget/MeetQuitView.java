package com.sdcz.endpass.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BasePopupWindowContentView;


/**
 * @Description 退出会议的PopupWindow
 */
public class MeetQuitView extends BasePopupWindowContentView
        implements View.OnClickListener {
    private final Context context;
    private TextView quitMeetingView;
    private TextView closeMeetingView;
    private TextView titleTextView;
    private TextView cancel;
    private QuitMeetingPopupWindListener quitMeetingPopupWindListener;
    private ConstraintLayout contentView;
    private View divisionLine2;
    private View bgView;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public MeetQuitView(Context context) {
        super(context);
        this.context = context;
        initView();
        initData();
        initEvent();
    }


    private void initView() {
        View rootViewLayout = LayoutInflater.from(context).inflate(R.layout.meeting_quit_popup, this, true);
        quitMeetingView = rootViewLayout.findViewById(R.id.tv_quit_meeting);
        closeMeetingView = rootViewLayout.findViewById(R.id.tv_finish_meeting);
        titleTextView = rootViewLayout.findViewById(R.id.quit_popup_title);
        cancel = rootViewLayout.findViewById(R.id.tv_quit_cancel);
        contentView = rootViewLayout.findViewById(R.id.cl_quit_content);
        divisionLine2 = rootViewLayout.findViewById(R.id.division_line_2);
        bgView = rootViewLayout.findViewById(R.id.bg_view);
    }


    /**
     * 初始化数据
     */
    private void initData() {
    }

    /**
     * 点击事件监听
     */
    private void initEvent() {
        quitMeetingView.setOnClickListener(this);
        closeMeetingView.setOnClickListener(this);
        cancel.setOnClickListener(this);
        bgView.setOnClickListener(this);
    }


    /**
     * 全部权限的退出弹窗
     */
    public void showAllPermissionLayout() {
        titleTextView.setText(R.string.meetingui_quit_room_tips);
        quitMeetingView.setText(R.string.meetingui_temporary_quit_meeting);
        quitMeetingView.setTextColor(context.getResources().getColor(R.color.color_28282D));
        closeMeetingView.setVisibility(View.VISIBLE);
        divisionLine2.setVisibility(View.VISIBLE);
    }

    /**
     * 权限受限的退出弹窗
     */
    public void showRestrictivePermissionLayout() {
        titleTextView.setText(R.string.meetingui_quit_room_restricted);
        quitMeetingView.setText(R.string.meetingui_quit_room);
        quitMeetingView.setTextColor(context.getResources().getColor(R.color.color_F37171));
        closeMeetingView.setVisibility(View.GONE);
        divisionLine2.setVisibility(View.GONE);
    }


    /**
     * 点击事件回调监听
     *
     * @param itemView 被点击的ItemView
     */
    @Override
    public void onClick(View itemView) {

        int id = itemView.getId();
        if (id == R.id.tv_quit_meeting) {
            dismissPopupWindow();
            quitMeetingPopupWindListener.onClickQuitMeetingListener(itemView);
        } else if (id == R.id.tv_finish_meeting) {
            dismissPopupWindow();
            quitMeetingPopupWindListener.onClickCloseMeetingListener(itemView);
        } else if (id == R.id.tv_quit_cancel
                || id == R.id.bg_view) {
            dismissPopupWindow();
        }


    }


    public void addQuitMeetingPopupWindListener(QuitMeetingPopupWindListener quitMeetingPopupWindListener) {
        this.quitMeetingPopupWindListener = quitMeetingPopupWindListener;
    }

    /**
     * 当从竖屏切换到横屏时回调该方法
     */
    @Override
    public void onLandscapeListener() {
        LayoutParams layoutParams = (LayoutParams) contentView.getLayoutParams();
        layoutParams.matchConstraintPercentWidth = 0.35F;
        contentView.setLayoutParams(layoutParams);
    }

    /**
     * 当从横屏切换到竖屏时回调
     */
    @Override
    public void onPortraitListener() {
        LayoutParams layoutParams = (LayoutParams) contentView.getLayoutParams();
        layoutParams.matchConstraintPercentWidth = 0.70F;
        contentView.setLayoutParams(layoutParams);

    }


    /**
     * 事件点击监听接口
     */
    public interface QuitMeetingPopupWindListener {

        /**
         * 点击退出会议 按钮事件监听
         *
         * @param view 被点击的Item
         */
        void onClickQuitMeetingListener(View view);

        /**
         * 点击关闭会议室 事件监听
         *
         * @param view 被点击的item
         */
        void onClickCloseMeetingListener(View view);
    }

    /**
     * 横竖屏切换回调
     *
     * @param newConfig 当前Configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onLandscapeListener();
        } else {
            onPortraitListener();
        }
    }
}
