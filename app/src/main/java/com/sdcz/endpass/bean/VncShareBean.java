package com.sdcz.endpass.bean;

import com.inpor.nativeapi.adaptor.RoomWndState;

/**
 * 屏幕共享 bean，包括发送 和 接收的 屏幕共享。
 */
public class VncShareBean extends BaseShareBean {
    public static final int VNC_RECV_VIRTUAL_ID = 1;
    public static final int VNC_SEND_ID = 2;
    public static final int VNC_RECV_ID = 3;

    public boolean isSendVnc;
    protected boolean isOpeningAudio = false;
    protected byte audioId = -1;
    protected long userId;

    VncShareBean(int id, long userId, String title) {
        setType(RoomWndState.DataType.DATA_TYPE_APPSHARE);
        setTitle(title);
        setId(id);
        this.userId = userId;
        this.isSendVnc = id == VNC_SEND_ID;


        //设置接收屏幕共享不显示
        if (!isSendVnc) {
            setShow(false);
        }
    }

    public boolean isOpeningAudio() {
        return isOpeningAudio;
    }

    public byte getAudioId() {
        return audioId;
    }

    public long getUserId() {
        return userId;
    }
}
