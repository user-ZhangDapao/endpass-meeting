package com.sdcz.endpass.widget;

import android.Manifest;
import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.MeetingModule;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.entities.LayoutType;
import com.comix.meeting.entities.WhiteBoard;
import com.comix.meeting.listeners.WbCreateListener;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BasePopupWindowContentView;
import com.sdcz.endpass.dialog.SimpleTipsDialog2;
import com.sdcz.endpass.util.DeviceUtil;
import com.inpor.nativeapi.adaptor.RolePermission;
import com.inpor.nativeapi.adaptor.RoomWndState;

/**
 * @Description 底部菜单“共享”弹窗
 */
public class MeetingSelectSharedView extends BasePopupWindowContentView implements View.OnClickListener,
        WbCreateListener {
    private static final String TAG = "MeetingSelectSharedView";
    private final Context context;
    private ConstraintLayout rootView;
    private SelectSharedViewListener selectSharedViewListener;
    private ConstraintLayout contentView;
    private LayoutParams landscapeLayoutParams;
    private LayoutParams portraitLayoutParams;


    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public MeetingSelectSharedView(Context context) {
        super(context);
        this.context = context;
        init();
    }


    private void init() {
        ConstraintLayout rootLayout = (ConstraintLayout) LayoutInflater
                .from(context).inflate(R.layout.meeting_shared_menu, this, true);
        rootView = rootLayout.findViewById(R.id.root_view);
        contentView = rootLayout.findViewById(R.id.content_layout);
        TextView boardItemView = rootLayout.findViewById(R.id.tv_shared_board);
        boardItemView.setOnClickListener(this);
        TextView screenItemView = rootLayout.findViewById(R.id.tv_shared_screen);
        screenItemView.setOnClickListener(this);
        ImageView closeView = rootLayout.findViewById(R.id.im_select_shared_close);
        closeView.setOnClickListener(this);
        //设置点击事件监听
        rootView.setOnClickListener(this);

        if (!DeviceUtil.checkDeviceSupportVncSend(context)) {
            screenItemView.setVisibility(GONE);
        }
        int disEnableColor = ContextCompat.getColor(context, R.color.color_686D7C);
        boolean hasScreenSharePermissions = checkPermissions(RolePermission.CREATE_APPSHARE);
        if (!hasScreenSharePermissions) {
            screenItemView.setTextColor(disEnableColor);
        }

        boolean hasWhiteBoardPermissions = checkPermissions(RolePermission.CREATE_WHITEBOARD);
        if (!hasWhiteBoardPermissions) {
            boardItemView.setTextColor(disEnableColor);
        }
    }

    /**
     * 点击事件监听
     *
     * @param childView 当前被点击的View
     */
    @Override
    public void onClick(View childView) {
        int id = childView.getId();
        if (id == R.id.root_view || id == R.id.im_select_shared_close) {
            dismissPopupWindow();
        } else if (id == R.id.tv_shared_screen) {
            onClickScreenSharedItem();
        } else if (id == R.id.tv_shared_board) {
            onClickWhiteSharedItem();
        }
    }

    private void onClickWhiteSharedItem() {
        boolean hasPermissions = checkPermissions(RolePermission.CREATE_WHITEBOARD);
        if (!hasPermissions) {
            ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
            return;
        }
        SdkUtil.getWbShareManager().createEmptyWhiteBoard(context.getString(R.string.meetingui_white_board), this);
    }

    /**
     * 点击“屏幕共享”Item
     */
    private void onClickScreenSharedItem() {
        boolean hasPermissions = checkPermissions(RolePermission.CREATE_APPSHARE);
        if (!hasPermissions) {
            ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
            return;
        }
        selectSharedViewListener.onClickScreenSharedItemListener();
    }

    @Override
    public void onWbCreated(WhiteBoard whiteBoard) {
        dismissPopupWindow();
        MeetingModule meetingModule = SdkUtil.getMeetingManager().getMeetingModule();
        if (meetingModule.getMeetingInfo().layoutType == LayoutType.VIDEO_LAYOUT) {
            SdkUtil.getMeetingManager().setMeetingLayoutType(LayoutType.STANDARD_LAYOUT,
                    RoomWndState.SplitStyle.SPLIT_STYLE_4);
        }
    }

    @Override
    public void onWbCreateFailed(int code) {
        if (code == WbCreateListener.PERMISSION_DENIED) {
            ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
        } else if (code == WbCreateListener.PERMISSION_OCCUPIED) {
            ToastUtils.showShort(R.string.meetingui_wb_count_limit_tips);
        } else if (code == WbCreateListener.SOMEONE_ALREADY_SHARING) {
            ToastUtils.showShort(R.string.meetingui_share_limit_tip);
        } else {
            ToastUtils.showShort(R.string.meetingui_open_wb_failed);
        }
    }

    /**
     * 当从竖屏切换到横屏时回调该方法
     */
    public void setLandscapeLayoutParams() {
        if (landscapeLayoutParams == null) {
            LayoutParams layoutParams = new LayoutParams(0, 0);
            layoutParams.topToTop = LayoutParams.PARENT_ID;
            layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
            layoutParams.leftToLeft = R.id.guideline_vertical_left_internal;
            layoutParams.rightToRight = LayoutParams.PARENT_ID;
            landscapeLayoutParams = layoutParams;
        }
        contentView.setBackgroundResource(R.drawable.shape_select_shared_right);
        contentView.setLayoutParams(landscapeLayoutParams);
    }

    /**
     * 当初横屏切换到竖屏时回调该方法
     */
    public void setPortraitLayoutParams() {
        if (portraitLayoutParams == null) {
            LayoutParams layoutParams = new LayoutParams(-1, 0);
            layoutParams.leftToLeft = LayoutParams.PARENT_ID;
            layoutParams.rightToRight = LayoutParams.PARENT_ID;
            layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
            layoutParams.topToTop = R.id.guideline_horizontal_top;
            portraitLayoutParams = layoutParams;
        }
        contentView.setBackgroundResource(R.drawable.shape_select_shared);
        contentView.setLayoutParams(portraitLayoutParams);
    }


    /**
     * 权限检查
     *
     * @return true 有权限反之则无权限
     */
    private boolean checkPermissions(RolePermission rolePermission) {
        BaseUser localUser = SdkUtil.getUserManager().getLocalUser();
        return SdkUtil.getPermissionManager()
                .checkUserPermission(localUser.getUserId(), true, rolePermission);
    }


    /**
     * 添加选择共享菜单事件监听
     *
     * @param selectSharedViewListener selectSharedViewListener
     */
    public void addSelectSharedVerticalViewListener(SelectSharedViewListener
                                                            selectSharedViewListener) {
        this.selectSharedViewListener = selectSharedViewListener;
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
            setLandscapeLayoutParams();
        } else {
            setPortraitLayoutParams();
        }
    }


    /**
     * 选择共享菜单事件监听
     */
    public interface SelectSharedViewListener {


        /**
         * 点击屏幕共享Item
         */
        void onClickScreenSharedItemListener();
    }

    private SimpleTipsDialog2 cameraPermissionDeniedDialog;

    private void showCameraPermissionDeniedDialog() {
        if (cameraPermissionDeniedDialog != null) {
            cameraPermissionDeniedDialog.dismiss();
        }
        SimpleTipsDialog2.Builder builder = new SimpleTipsDialog2.Builder();
        cameraPermissionDeniedDialog = builder.title(context.getString(R.string.meetingui_remind))
                .btnLeft(context.getString(R.string.meetingui_cancel))
                .btnRight(context.getString(R.string.meetingui_confirm))
                .tips(context.getString(R.string.meetingui_camera_permission_denied))
                .cancelOnTouchOutside(true)
                .interactionListener(new SimpleTipsDialog2.InteractionListener() {
                    @Override
                    public void onLeftBtnClick(DialogFragment dialog) {
                        dialog.dismiss();
                        cameraPermissionDeniedDialog = null;
                    }

                    @Override
                    public void onRightBtnClick(DialogFragment dialog) {
                        ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(),
                                new String[]{Manifest.permission.CAMERA}, 61);
                        dialog.dismiss();
                    }
                }).build();
        cameraPermissionDeniedDialog.show(
                ((FragmentActivity) context).getSupportFragmentManager(), "camera_permission_dialog");
    }

}
