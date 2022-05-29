package com.sdcz.endpass.presenter;

import android.content.Context;

import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BaseContainer;
import com.sdcz.endpass.callback.IMeetingAbandonManagerListener;
import com.sdcz.endpass.widget.MeetingAbandonManagerView;

/**
 * @Description     放弃管理员弹窗的P层
 */
public class MeetingAbandonManagerContainer extends BaseContainer<MeetingAbandonManagerView>
        implements IMeetingAbandonManagerListener {

    public MeetingAbandonManagerContainer(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        view = new MeetingAbandonManagerView(context);
        view.setMeetingAbandonManagerListener(this);
    }

    @Override
    public void onClickCancelListener() {

    }

    @Override
    public void onClickConfirmListener() {
        SdkUtil.getUserManager().abandonTheManager();
    }
}
