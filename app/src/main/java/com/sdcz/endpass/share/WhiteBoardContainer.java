package com.sdcz.endpass.share;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.Opcode;
import com.comix.meeting.entities.BaseShareBean;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.entities.WhiteBoard;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.IDataContainer;
import com.sdcz.endpass.widget.MarkZoomWbView;
import com.inpor.base.sdk.permission.PermissionManager;
import com.inpor.nativeapi.adaptor.RolePermission;

import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Locale;


/**
 * @author yinhui
 * @date create at 2019/3/22
 * @description
 */
public class WhiteBoardContainer implements IDataContainer, MarkZoomWbView.MarkZoomWbViewListener {

    private static final String TAG = "WhiteBoardContainer";
    private final SparseArray<Drawable> drawableCache = new SparseArray<>();
    private Context context;
    private WhiteBoard whiteBoard;
    private View containerView;
    private MarkZoomWbView markZoomWbView;
    private TextView wbNameTv;
    private View stateView;
    private ProgressBar loadingView;
    private TextView tvMention;
    private TextView tvName;
    private View infoLayout;
    private TextView tvTotalSize;
    private TextView tvSpeed;
    private TextView tvSpendTime;
    private ProgressBar progressBar;
    private int currentState = -100;
    private int size;

    public WhiteBoardContainer() {

    }

    /**
     * format时间
     *
     * @param timeMs 时间，毫秒
     * @return text
     */
    public String formatTime(long timeMs) {
        if (timeMs <= 0) {
            return "00:00";
        }
        int totalSeconds = (int) (timeMs / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * 转换文件大小
     *
     * @param size 大小
     * @return 文本
     */
    public String formatSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString;
        if (size <= 0) {
            fileSizeString = "0B";
        } else if (size < 1024) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1048576) {
            fileSizeString = df.format((double) size / 1024) + "KB";
        } else if (size < 1073741824) {
            fileSizeString = df.format((double) size / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) size / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    @Override
    public void init(Context context) {
        this.context = context;
        size = SizeUtils.dp2px(62f);
        if (containerView == null) {
            containerView = View.inflate(context, R.layout.meetingui_layout_whiteboard_container, null);
            markZoomWbView = containerView.findViewById(R.id.mark_zoom_wb_view);
            wbNameTv = containerView.findViewById(R.id.tv_wb_name);
            markZoomWbView.setMarkZoomWbViewListener(this);
        }
        if (stateView == null) {
            stateView = View.inflate(context, R.layout.meetingui_layout_dataview_loading, null);
            loadingView = stateView.findViewById(R.id.loading_view);
            tvMention = stateView.findViewById(R.id.tv_mention);
            tvName = stateView.findViewById(R.id.tv_name);
            infoLayout = stateView.findViewById(R.id.info_layout);
            tvTotalSize = stateView.findViewById(R.id.tv_total_size);
            tvSpeed = stateView.findViewById(R.id.tv_speed);
            tvSpendTime = stateView.findViewById(R.id.tv_spend_time);
            progressBar = stateView.findViewById(R.id.progressbar);
        }
    }

    @Override
    public void release() {
        Log.i(TAG, "白板資源回收");
        drawableCache.clear();
        this.context = null;
    }

    @Override
    public void setShareBean(BaseShareBean shareBean) {
        this.whiteBoard = (WhiteBoard) shareBean;
    }

    @Override
    public View getDataView() {
        if (whiteBoard == null) {
            return null;
        }
        return containerView;
    }

    @Override
    public View getStateView() {
        if (whiteBoard == null) {
            return null;
        }
        return stateView;
    }

    @Override
    public void updateDataView(BaseShareBean shareBean) {
        this.whiteBoard = (WhiteBoard) shareBean;
        if (markZoomWbView == null) {
            return;
        }
        whiteBoard.setScaleMode(2);
        markZoomWbView.setWhiteBoard(whiteBoard);
        wbNameTv.setText(whiteBoard.getTitle());
        markZoomWbView.invalidate();
    }

    @Override
    public void updateStateView(BaseShareBean shareBean) {
        this.whiteBoard = (WhiteBoard) shareBean;
        if (stateView == null) {
            return;
        }
        tvName.setText(whiteBoard.getTitle());
        int state = whiteBoard.getState();
        Drawable drawable;
        switch (state) {
            case BaseShareBean.STATE_UPLOAD_WAITING:
            case BaseShareBean.STATE_UPLOADING:
                if (currentState != state) {
                    drawable = getDrawableFromCache(R.drawable.upload_loading);
                    loadingView.setIndeterminateDrawable(drawable);
                    tvMention.setText(R.string.meetingui_uploading_mention);
                }
                stateView.setVisibility(View.VISIBLE);
                infoLayout.setVisibility(View.VISIBLE);
                tvTotalSize.setText(formatSize(whiteBoard.getTotalSize()));
                tvSpeed.setText(formatSize(whiteBoard.getUploadSpeed()));
                tvSpendTime.setText(formatTime(whiteBoard.getTimeSpent()));
                progressBar.setProgress(whiteBoard.getProgress());
                break;
            case BaseShareBean.STATE_COVERT_WAITING:
                if (currentState != state) {
                    drawable = getDrawableFromCache(R.drawable.convert_wait_loading);
                    loadingView.setIndeterminateDrawable(drawable);
                    tvMention.setText(R.string.meetingui_convert_wait_mention);
                }
                stateView.setVisibility(View.VISIBLE);
                infoLayout.setVisibility(View.INVISIBLE);
                break;
            case BaseShareBean.STATE_CONVERTING:
                if (currentState != state) {
                    drawable = getDrawableFromCache(R.drawable.convert_loading);
                    loadingView.setIndeterminateDrawable(drawable);
                    tvMention.setText(R.string.meetingui_converting_mention);
                }
                stateView.setVisibility(View.VISIBLE);
                infoLayout.setVisibility(View.INVISIBLE);
                break;
            case BaseShareBean.STATE_LOADING:
                if (currentState != state) {
                    drawable = getDrawableFromCache(R.drawable.video_loading);
                    loadingView.setIndeterminateDrawable(drawable);
                    tvMention.setText(R.string.meetingui_opening_wb);
                }
                stateView.setVisibility(View.VISIBLE);
                infoLayout.setVisibility(View.INVISIBLE);
                break;
            case BaseShareBean.STATE_WB_RECEIVE_FAILED:
                ToastUtils.showShort(R.string.meetingui_open_wb_failed);
                break;
            case BaseShareBean.STATE_LOADED:
            case BaseShareBean.STATE_LOADED_UNSUPPORTED_FILE:
            default:
                if (state == BaseShareBean.STATE_LOADED_UNSUPPORTED_FILE) {
                    // 不支持的图片格式
                    ToastUtils.showShort(R.string.meetingui_unsupported_wb_format);
                }
                stateView.setVisibility(View.INVISIBLE);
                break;
        }
        currentState = state;
    }

    @Override
    public void onWbSingleTap(MotionEvent event) {

    }

    @Override
    public void switchWbNextPage(WhiteBoard wb) {
        BaseUser localUser = SdkUtil.getUserManager().getLocalUser();
        PermissionManager permissionManager = SdkUtil.getPermissionManager();
        boolean hasPermission = permissionManager.checkUserPermission(localUser.getUserId(), true,
                RolePermission.WHITEBOARD_TURN_PAGE);
        if (hasPermission) {
            int code = SdkUtil.getWbShareManager().getCurrentWhiteBoardOperation().nextPage(wb.getId());
            if (code == Opcode.INVALID_OPERATION) {
                ToastUtils.showShort(R.string.meetingui_wb_last_page_tip);
            }
        } else {
            ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
        }
    }

    @Override
    public void switchWbPreviousPage(WhiteBoard wb) {
        BaseUser localUser = SdkUtil.getUserManager().getLocalUser();
        PermissionManager permissionManager = SdkUtil.getPermissionManager();
        boolean hasPermission = permissionManager.checkUserPermission(localUser.getUserId(), true,
                RolePermission.WHITEBOARD_TURN_PAGE);
        if (hasPermission) {
            int code = SdkUtil.getWbShareManager().getCurrentWhiteBoardOperation().previousPage(wb.getId());
            if (code == Opcode.INVALID_OPERATION) {
                ToastUtils.showShort(R.string.meetingui_wb_first_page_tip);
            }
        } else {
            ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
        }
    }

    private Drawable getDrawableFromCache(int id) {
        Drawable drawable = drawableCache.get(id);
        if (drawable == null) {
            drawable = ContextCompat.getDrawable(context, id);
            drawable.setBounds(0, 0, size, size);
            drawableCache.put(id, drawable);
        }
        return drawable;
    }


}
