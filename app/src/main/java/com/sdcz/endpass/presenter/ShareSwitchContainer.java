package com.sdcz.endpass.presenter;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.Opcode;
import com.comix.meeting.entities.BaseShareBean;
import com.comix.meeting.listeners.ShareModelListener;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.adapter.ShareSwitchAdapter;
import com.sdcz.endpass.base.BaseContainer;
import com.sdcz.endpass.base.RecyclerViewAdapter;
import com.sdcz.endpass.callback.PopupWindowCommunicationListener;
import com.sdcz.endpass.widget.ShareSwitchView;
import com.inpor.base.sdk.whiteboard.WBShareManager;

import java.util.UUID;

/**
 * @description     切换共享时，弹窗的P层
 */
public class ShareSwitchContainer extends BaseContainer<ShareSwitchView> implements View.OnClickListener,
        ShareSwitchAdapter.ShareTabClickListener, View.OnAttachStateChangeListener,
        ShareModelListener {

    private static final String TAG = "ShareSwitchContainer";
    private final ShareSwitchAdapter switchAdapter;
    private final WBShareManager shareModel;

    /**
     * 构造
     */
    public ShareSwitchContainer(Context context, PopupWindowCommunicationListener
            .PopupWindowCommunicationInterior interior) {
        super(context, interior);
        shareModel = SdkUtil.getWbShareManager();
        view = new ShareSwitchView(context);
        switchAdapter = new ShareSwitchAdapter();
        switchAdapter.setShareTabClickListener(this);
        view.setAdapter(switchAdapter);
        view.setChildrenClickListener(this);
        view.addOnAttachStateChangeListener(this);
    }

    /**
     * 显示菜单内容
     */
    @Override
    public void show() {
        switchAdapter.updateUuid(UUID.randomUUID().toString());
        switchAdapter.clear();
        switchAdapter.addAll(shareModel.getShareBeans());
        switchAdapter.notifyDataSetChanged();
        super.show();
    }

    @Override
    public void onShareTabClick(RecyclerViewAdapter<BaseShareBean> viewAdapter, int position,
                                BaseShareBean item) {
        shareModel.switchShareTab(item.getId());
        dismiss();
    }

    @Override
    public void onShareTabCloseClick(RecyclerViewAdapter<BaseShareBean> viewAdapter, int position,
                                     BaseShareBean item) {
        int result = shareModel.closeWhiteBoard(item.getId());
        if (result == Opcode.NO_PERMISSION) {
            ToastUtils.showShort(R.string.meetingui_permission_not_permitted_admin);
        }
    }

    @Override
    public void onViewAttachedToWindow(View view) {
        shareModel.addWhiteBoardListener(this);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {

    }

    @Override
    public void dismiss() {
        if (shareModel != null) {
            shareModel.removeWhiteBoardListener(this);
        }
        super.dismiss();
    }

    @Override
    public void onShareTabChanged(int type, BaseShareBean shareBean) {
        Log.i(TAG, "onShareTabChanged: type is " + type);
        if (type == SHARE_ADD) {
            // to do something
            switchAdapter.addItem(shareBean);
        } else if (type == SHARE_REMOVE) {
            // 检查是否还有同类型共享
            switchAdapter.removeItem(shareBean);
            if (switchAdapter.getItemCount() == 0) {
                dismiss();
            }
        } else if (type == SHARE_ACTIVE) {
            switchAdapter.notifySelectedItemChanged(shareBean);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.back_iv) {
            dismiss();
        } else if (id == R.id.close_iv) {
            dismiss();
        }
    }
}
