package com.sdcz.endpass.view;

import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.ChannelTypeBean;

public interface IMobileMeetingView extends IBaseView {
    void showData(Boolean o);
    void venueId(long id);
    void showChannelInfo(ChannelBean channelBean);
    void inviteSuccess(Object o);
    void showRoomInfo(ChannelTypeBean o);
}
