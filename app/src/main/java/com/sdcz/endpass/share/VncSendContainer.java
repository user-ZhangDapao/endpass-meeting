package com.sdcz.endpass.share;

import android.content.Context;
import android.view.View;

import com.comix.meeting.entities.BaseShareBean;
import com.comix.meeting.entities.VncShareBean;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.IDataContainer;

/**
 * @description    发送屏幕共时的处理
 */
public class VncSendContainer implements IDataContainer {

    private VncShareBean vncShareBean;
    private View view;

    public VncSendContainer() {

    }

    @Override
    public void init(Context context) {
        view = View.inflate(context, R.layout.meetingui_layout_vnc_send, null);
    }

    @Override
    public void release() {
        vncShareBean = null;
    }

    @Override
    public void setShareBean(BaseShareBean shareBean) {
        this.vncShareBean = (VncShareBean) shareBean;
    }

    @Override
    public View getDataView() {
        if (vncShareBean == null) {
            return null;
        }
        return view;
    }

    @Override
    public View getStateView() {
        return null;
    }

    @Override
    public void updateDataView(BaseShareBean shareBean) {

    }

    @Override
    public void updateStateView(BaseShareBean shareBean) {

    }
}
