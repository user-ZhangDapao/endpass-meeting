package com.sdcz.endpass.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;

import com.blankj.utilcode.util.ScreenUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BasePopupWindowContentView;
import com.sdcz.endpass.callback.IMeetingAbandonManagerListener;

/**
 * @Author wuyr
 * @Date 2021/1/4 20:10
 * @Description
 */
public class MeetingAbandonManagerView extends BasePopupWindowContentView implements View.OnClickListener {
    private IMeetingAbandonManagerListener meetingAbandonManagerListener;
    private ConstraintLayout parentContentView;

    public MeetingAbandonManagerView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public MeetingAbandonManagerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.meeting_abandon_manager_layout, this, true);
        ConstraintLayout rootView = findViewById(R.id.root_view);
        rootView.setOnClickListener(this);
        TextView cancelTextView = findViewById(R.id.tv_cancel);
        cancelTextView.setOnClickListener(this);
        TextView confirmTextView = findViewById(R.id.tv_confirm);
        confirmTextView.setOnClickListener(this);
        parentContentView = findViewById(R.id.parent_content_view);
        if (!ScreenUtils.isPortrait()) {
            updateLayoutParams(Configuration.ORIENTATION_LANDSCAPE);
        }
    }


    private void updateLayoutParams(int orientation) {
        LayoutParams layoutParams = new Constraints.LayoutParams(0, 0);
        layoutParams.width = 0;
        layoutParams.height = 0;
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.matchConstraintPercentHeight = 0.45F;
            layoutParams.leftToLeft = R.id.guideline_vertical_left;
        } else {
            layoutParams.matchConstraintPercentHeight = 0.23F;
            layoutParams.leftToLeft = LayoutParams.PARENT_ID;
        }
        parentContentView.setLayoutParams(layoutParams);
    }


    @Override
    public void onClick(View itemView) {
        int id = itemView.getId();
        if (id == R.id.tv_cancel) {
            dismissChildrenPopupWindow();
            if (meetingAbandonManagerListener != null) {
                meetingAbandonManagerListener.onClickCancelListener();
            }
        } else if (id == R.id.tv_confirm) {
            dismissChildrenPopupWindow();
            if (meetingAbandonManagerListener != null) {
                meetingAbandonManagerListener.onClickConfirmListener();
            }
        }
    }


    public void setMeetingAbandonManagerListener(IMeetingAbandonManagerListener meetingAbandonManagerListener) {
        this.meetingAbandonManagerListener = meetingAbandonManagerListener;
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
            updateLayoutParams(Configuration.ORIENTATION_LANDSCAPE);
        } else {
            updateLayoutParams(Configuration.ORIENTATION_PORTRAIT);
        }
    }
}
