package com.sdcz.endpass.presenter;

import android.content.Context;
import android.view.View;

import com.comix.meeting.entities.BaseUser;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.BaseContainer;
import com.sdcz.endpass.custommade.meetingover._manager._MeetingStateManager;
import com.sdcz.endpass.widget.MeetQuitView;
import com.inpor.base.sdk.meeting.MeetingManager;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.nativeapi.adaptor.RolePermission;

import java.util.HashMap;
import java.util.Map;

/**
 *  退出会议弹窗的P层
 */
public class MeetingQuitContainer extends BaseContainer<MeetQuitView>
        implements MeetQuitView.QuitMeetingPopupWindListener {
    private MeetingQuitContainerListener meetingQuitContainerListener;
    private final MeetingManager meetingManager;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public MeetingQuitContainer(Context context) {
        super(context);
        view = new MeetQuitView(context);
        view.addQuitMeetingPopupWindListener(this);
        meetingManager = SdkUtil.getMeetingManager();
        initParams();
    }

    private void initParams() {
        UserManager userModel = SdkUtil.getUserManager();
        BaseUser localUser = userModel.getLocalUser();
        boolean hasPermission = SdkUtil.getPermissionManager()
                .checkUserPermission(localUser.getUserId(),true, RolePermission.CLOSE_MEETING);
        if (hasPermission) {
            view.showAllPermissionLayout();
        } else {
            view.showRestrictivePermissionLayout();
        }
    }


    /**
     * 仅仅显示退出会议确认视图，不管是否有权限
     */
    public void onlyShowQuitMeetingView() {
        view.showRestrictivePermissionLayout();
    }


    @Override
    public void onClickQuitMeetingListener(View view) {
        //主动退出会议
        Map<String,Object> reason_map = new HashMap();
        ((HashMap)reason_map).put("code",2);
        ((HashMap)reason_map).put("type",1);
        _MeetingStateManager.getInstance().notify_quit_meeting(reason_map);

        if (meetingQuitContainerListener != null) {
            meetingQuitContainerListener.onQuitMeetingAndFinishActivityListener();
        }
    }

    @Override
    public void onClickCloseMeetingListener(View view) {

        //本地管理员结束会议
        Map<String,Object> reason_map = new HashMap();
        reason_map.put("code",1);
        reason_map.put("type",1);
        _MeetingStateManager.getInstance().notify_quit_meeting(reason_map);

        meetingManager.closeMeeting(0, "");
    }


    public void setMeetingQuitContainerListener(MeetingQuitContainerListener meetingQuitContainerListener) {
        this.meetingQuitContainerListener = meetingQuitContainerListener;
    }

    public interface MeetingQuitContainerListener {
        /**
         * 退出会议室并且关闭Activity
         */
        void onQuitMeetingAndFinishActivityListener();
    }
}
