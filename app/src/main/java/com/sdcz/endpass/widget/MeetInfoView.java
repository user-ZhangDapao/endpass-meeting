package com.sdcz.endpass.widget;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.entities.MeetingInfo;
import com.sdcz.endpass.LiveDataBus;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BasePopupWindowContentView;

public class MeetInfoView extends BasePopupWindowContentView {


    private ConstraintLayout rootLayout;
    private TextView tvMeetingTheme;
    private TextView tvMeetingCode;
    private MeetingInfo roomInfo;

    public MeetInfoView(@NonNull Context context) {
        super(context);
        initView();
        initData();
    }

    private void initView() {
        View rootViewLayout = LayoutInflater.from(getContext()).inflate(R.layout.meeting_info_popup, this, true);
        rootLayout = rootViewLayout.findViewById(R.id.root_layout);
        ImageView ivClose = rootViewLayout.findViewById(R.id.iv_close);
        TextView tvCopy = rootViewLayout.findViewById(R.id.tv_meeting_info_copy);
        tvMeetingTheme = rootViewLayout.findViewById(R.id.meeting_theme);
        tvMeetingCode = rootViewLayout.findViewById(R.id.meeting_code);
        tvMeetingCode = rootViewLayout.findViewById(R.id.meeting_code);
        ivClose.setOnClickListener(v -> dismissPopupWindow());
        tvCopy.setOnClickListener(v -> copyMeetingInfo());
    }

    private void copyMeetingInfo() {
        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        String nickName = SdkUtil.getUserManager().getLocalUser().getNickName();
        String formatInfo = getResources().getString(R.string.meeting_info_copy_format);
        String meetingCopyInfo = String.format(formatInfo, nickName, roomInfo.inviteCode);
        ClipData clipData = ClipData.newPlainText("Label", meetingCopyInfo);
        cm.setPrimaryClip(clipData);
        ToastUtils.showShort(R.string.copy_success);
    }

    private void initData() {
        MutableLiveData<Configuration> liveData = LiveDataBus.getInstance().getLiveData(LiveDataBus.KEY_MEETING_ACTIVITY_CONFIG);
        liveData.observeForever(this::onConfigurationChanged);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏处理
            onLandScape();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {//竖屏处理
            onPortraitScape();
        }
    }

    private void onLandScape() {
        ConstraintLayout.LayoutParams layoutParams = new Constraints.LayoutParams(
                LayoutParams.MATCH_CONSTRAINT,
                LayoutParams.MATCH_CONSTRAINT);
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        layoutParams.topToTop = LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        layoutParams.leftToLeft = R.id.guideline_vertical_left;
        rootLayout.setBackgroundResource(R.drawable.shape_select_shared_right);
        rootLayout.setLayoutParams(layoutParams);
    }

    private void onPortraitScape() {
        ConstraintLayout.LayoutParams layoutParams = new Constraints.LayoutParams(
                LayoutParams.MATCH_CONSTRAINT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.leftToLeft = LayoutParams.PARENT_ID;
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        layoutParams.topToTop = R.id.guideline_horizontal_top;
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        layoutParams.verticalBias = 1;
        rootLayout.setBackgroundResource(R.drawable.shape_select_shared);
        rootLayout.setLayoutParams(layoutParams);
    }

    @Override
    public void recycle() {
        super.recycle();
        MutableLiveData<Configuration> liveData = LiveDataBus.getInstance().getLiveData(LiveDataBus.KEY_MEETING_ACTIVITY_CONFIG);
        liveData.removeObserver(this::onConfigurationChanged);
    }

    public void updateView(@NonNull MeetingInfo info) {
        this.roomInfo = info;
        tvMeetingTheme.setText(info.roomName);
        tvMeetingCode.setText(info.inviteCode);
    }
}
