package com.sdcz.endpass.dialog;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.ToolIndex;
import com.comix.meeting.entities.BaseShareBean;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.entities.WhiteBoard;
import com.comix.meeting.listeners.ShareModelListener;
import com.comix.meeting.listeners.UserModelListenerImpl;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BaseFullScreenDialog;
import com.sdcz.endpass.widget.MarkZoomWbView;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.base.sdk.whiteboard.WBShareManager;

/**
 * @author lqk
 * @date create at 2021/1/11
 * @description
 */
public class MarkWhiteBoardDialog extends BaseFullScreenDialog implements View.OnClickListener,
        ShareModelListener {
    private static final String TAG = "MarkWhiteBoardDialog";
    private MarkZoomWbView whiteBoardView;
    private TextView wbNameTv;

    View stopShareButton;
    ImageView pencilButton;
    ImageView colorPanelButton;
    ImageView earserButton;

    private View[] colorItemViews;

    private PopupWindow popupWindow;
    private int selectedPosition;
    private final WBShareManager shareModel;
    private final UserManager userModel;
    private final WhiteBoard whiteBoard;
    private final View rootView;

    /**
     * 构造
     *
     * @param context 上下文
     */
    public MarkWhiteBoardDialog(Context context, WhiteBoard whiteBoard) {
        super(context);
        this.whiteBoard = whiteBoard;
        shareModel = SdkUtil.getWbShareManager();
        userModel = SdkUtil.getUserManager();
        //super(context, R.style.meetingui_dialog_full_screen_style);
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_mark_white_board, null, true);
        setContentView(rootView);
        initView(context);
        initListener();

        whiteBoardView.setWhiteBoard(whiteBoard);
        whiteBoardView.setMark(true);
        whiteBoardView.setDrawModel(ToolIndex.WB_TOOL_PENCIL);
        whiteBoardView.setColor(context.getResources().getColor(R.color.color_black));
        whiteBoardView.invalidate();
        pencilButton.setSelected(true);
    }

    private void initView(Context context) {
        whiteBoardView = findViewById(R.id.mark_zoom_wb_view);
        wbNameTv = findViewById(R.id.tv_wb_name);

        stopShareButton = findViewById(R.id.stop_share_button);
        pencilButton = findViewById(R.id.pencil_image_view);
        colorPanelButton = findViewById(R.id.color_select_image_view);
        earserButton = findViewById(R.id.earser_image_view);

        wbNameTv.setText(whiteBoard.getTitle());
    }

    private void initListener() {
        stopShareButton.setOnClickListener(this);
        pencilButton.setOnClickListener(this);
        colorPanelButton.setOnClickListener(this);
        earserButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        shareModel.addWhiteBoardListener(this);
        userModel.addEventListener(userModelListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        shareModel.removeWhiteBoardListener(this);
        userModel.removeEventListener(userModelListener);
        if (isColorPickerPanelShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    public void onWhiteBoardChanged(WhiteBoard whiteBoard) {
        if (whiteBoard.getId() != this.whiteBoard.getId()) {
            dismiss();
            return;
        }
        Log.d(TAG, "invalidate " + whiteBoardView);
        whiteBoardView.invalidate();
    }


    /**
     * TODO 主要为了解决:BUG 60047 【电子白板】android6/android7的系统上，标注之后不显示
     * 目前暂时找不到具体原因,  暂时使用这种方法解决
     * 再绘制View完成后,再刷新一次父容器
     */
    @Override
    public void againInvalidate() {
        rootView.postInvalidateDelayed(1);
    }

    @Override
    public void onShareTabChanged(int type, BaseShareBean shareBean) {
        if (type == SHARE_REMOVE && shareBean.getId() == this.whiteBoard.getId()) {
            dismiss();
        } else if (type == SHARE_ACTIVE && shareBean.getId() != this.whiteBoard.getId()) {
            dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == stopShareButton) {
            dismiss();
        } else if (view == pencilButton) {
            whiteBoardView.setDrawModel(ToolIndex.WB_TOOL_PENCIL);
            pencilButton.setSelected(true);
            earserButton.setSelected(false);
        } else if (view == colorPanelButton) {
            showColorPickPopup();
        } else if (view == earserButton) {
            whiteBoardView.setDrawModel(ToolIndex.WB_TOOL_DELETE);
            pencilButton.setSelected(false);
            earserButton.setSelected(true);
        } else {
            updateMarkColor(view);
        }
    }

    private boolean isColorPickerPanelShowing() {
        return popupWindow != null && popupWindow.isShowing();
    }

    private void showColorPickPopup() {
        if (popupWindow == null) {
            popupWindow = new PopupWindow(getContext());
            popupWindow.setBackgroundDrawable(null);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setOnDismissListener(() -> colorPanelButton.setSelected(false));
            View popupView = View.inflate(getContext(), R.layout.meetingui_wb_color_pick_popup, null);
            popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            colorItemViews = new View[8];
            colorItemViews[0] = popupView.findViewById(R.id.rb_black);
            colorItemViews[1] = popupView.findViewById(R.id.rb_gray);
            colorItemViews[2] = popupView.findViewById(R.id.rb_blue);
            colorItemViews[3] = popupView.findViewById(R.id.rb_blue2);
            colorItemViews[4] = popupView.findViewById(R.id.rb_green);
            colorItemViews[5] = popupView.findViewById(R.id.rb_orange);
            colorItemViews[6] = popupView.findViewById(R.id.rb_yellow);
            colorItemViews[7] = popupView.findViewById(R.id.rb_red);
            for (View colorItemView : colorItemViews) {
                colorItemView.setOnClickListener(this);
            }
            popupWindow.setContentView(popupView);
        }
        if (popupWindow.isShowing()) {
            return;
        }
        colorItemViews[selectedPosition].setSelected(true);
        final int popupWidth = popupWindow.getContentView().getMeasuredWidth();
        final int popupHeight = popupWindow.getContentView().getMeasuredHeight();
        int offsetX = colorPanelButton.getWidth() / 2 - popupWidth / 2;
        int parentHeight = ((ViewGroup) colorPanelButton.getParent()).getMeasuredHeight();
        int offsetY = -popupHeight - SizeUtils.dp2px(4f) - parentHeight;
        popupWindow.showAsDropDown(colorPanelButton, offsetX, offsetY, Gravity.TOP);
        colorPanelButton.setSelected(true);
    }

    private void updateMarkColor(View view) {
        try {
            for (int i = 0; i < colorItemViews.length; i++) {
                if (colorItemViews[i] == view) {
                    selectedPosition = i;
                    colorItemViews[i].setSelected(true);
                } else {
                    colorItemViews[i].setSelected(false);
                }
            }
            int colorId = R.color.whiteBoardBlack;
            int id = view.getId();
            if (id == R.id.rb_black) {
                colorId = R.color.whiteBoardBlack;
                colorPanelButton.setImageResource(R.drawable.select_mark_white_board_color_black);
            } else if (id == R.id.rb_gray) {
                colorId = R.color.whiteBoardGray;
                colorPanelButton.setImageResource(R.drawable.select_mark_white_board_color_gray);
            } else if (id == R.id.rb_blue) {
                colorId = R.color.whiteBoardBlue;
                colorPanelButton.setImageResource(R.drawable.select_mark_white_board_color_blue);
            } else if (id == R.id.rb_blue2) {
                colorId = R.color.whiteBoardBlue2;
                colorPanelButton.setImageResource(R.drawable.select_mark_white_board_color_iceblue);
            } else if (id == R.id.rb_green) {
                colorId = R.color.whiteBoardGreen;
                colorPanelButton.setImageResource(R.drawable.select_mark_white_board_color_green);
            } else if (id == R.id.rb_yellow) {
                colorId = R.color.whiteBoardYellow;
                colorPanelButton.setImageResource(R.drawable.select_mark_white_board_color_yellow);
            } else if (id == R.id.rb_orange) {
                colorId = R.color.whiteBoardOrange;
                colorPanelButton.setImageResource(R.drawable.select_mark_white_board_color_orange);
            } else if (id == R.id.rb_red) {
                colorId = R.color.whiteBoardRed;
                colorPanelButton.setImageResource(R.drawable.select_mark_white_board_color_red);
            }
            whiteBoardView.setColor(ContextCompat.getColor(ctx, colorId));
            if (popupWindow != null) {
                popupWindow.dismiss();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private final UserModelListenerImpl userModelListener = new UserModelListenerImpl(
            UserModelListenerImpl.WBMARK_STATE, UserModelListenerImpl.ThreadMode.MAIN) {
        @Override
        public void onUserChanged(int type, BaseUser user) {
            if (type == UserModelListenerImpl.WBMARK_STATE && !shareModel.hasMarkWbRights(whiteBoard)) {
                ToastUtils.showShort(R.string.meetingui_permission_changed);
                dismiss();
            }
        }
    };
}
