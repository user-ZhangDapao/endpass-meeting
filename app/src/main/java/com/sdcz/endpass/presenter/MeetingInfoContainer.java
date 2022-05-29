package com.sdcz.endpass.presenter;

import android.content.Context;
import android.util.Log;

import com.comix.meeting.entities.MeetingInfo;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BaseContainer;
import com.sdcz.endpass.widget.MeetInfoView;

public class MeetingInfoContainer extends BaseContainer<MeetInfoView> {

    private static final String TAG = "MeetingInfoContainer";

    public MeetingInfoContainer(Context context) {
        super(context);
        view = new MeetInfoView(context);
        initData();
    }

    private void initData() {
        MeetingInfo meetingInfo = SdkUtil.getMeetingManager().getMeetingModule().getMeetingInfo();
        if (meetingInfo == null) {
            Log.e(TAG, "initData: meetingInfo is null;");
            dismiss();
            return;
        }
        view.updateView(meetingInfo);
    }

    @Override
    public void recycle() {
        view.recycle();
        super.recycle();
    }
}
