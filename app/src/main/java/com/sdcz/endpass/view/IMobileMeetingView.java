package com.sdcz.endpass.view;

import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.ChannelTypeBean;
import com.sdcz.endpass.bean.PosBean;
import com.sdcz.endpass.bean.UserEntity;

import java.util.List;

public interface IMobileMeetingView extends IBaseView {
    void showData(Boolean o);
    void venueId(ChannelBean id);
    void showChannelInfo(ChannelBean channelBean);
    void inviteSuccess(Object o);
    void showRoomInfo(ChannelTypeBean o);

}
