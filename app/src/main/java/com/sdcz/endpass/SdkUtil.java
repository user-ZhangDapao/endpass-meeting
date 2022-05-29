package com.sdcz.endpass;

import com.inpor.base.sdk.SdkManager;
import com.inpor.base.sdk.audio.AudioManager;
import com.inpor.base.sdk.chat.ChatManager;
import com.inpor.base.sdk.login.LoginManager;
import com.inpor.base.sdk.meeting.MeetingManager;
import com.inpor.base.sdk.permission.PermissionManager;
import com.inpor.base.sdk.roomlist.RoomListManager;
import com.inpor.base.sdk.share.ScreenShareManager;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.base.sdk.util.ManagerFlag;
import com.inpor.base.sdk.video.VideoManager;
import com.inpor.base.sdk.whiteboard.WBShareManager;

public class SdkUtil {

    public static SdkManager getSdkManager() {
        return SdkManager.getInstance();
    }

    public static AudioManager getAudioManager() {
        return getSdkManager().queryManager(ManagerFlag.SDK_AUDIO);
    }

    public static ChatManager getChatManager() {
        return getSdkManager().queryManager(ManagerFlag.SDK_CHAT);
    }

    public static LoginManager getLoginManager() {
        return getSdkManager().queryManager(ManagerFlag.SDK_LOGIN);
    }

    public static MeetingManager getMeetingManager() {
        return getSdkManager().queryManager(ManagerFlag.SDK_MEETING);
    }

    public static ScreenShareManager getShareManager() {
        return getSdkManager().queryManager(ManagerFlag.SDK_SCREEN_SHARE);
    }

    public static UserManager getUserManager() {
        return getSdkManager().queryManager(ManagerFlag.SDK_USER);
    }

    public static VideoManager getVideoManager() {
        return getSdkManager().queryManager(ManagerFlag.SDK_VIDEO);
    }

    public static PermissionManager getPermissionManager() {
        return getSdkManager().queryManager(ManagerFlag.SDK_PERMISSION);
    }

    public static WBShareManager getWbShareManager() {
        return getSdkManager().queryManager(ManagerFlag.SDK_WHITEBOARD);
    }

    public static RoomListManager getRoomListManager(){
        return getSdkManager().queryManager(ManagerFlag.SDK_FRONT_MEETING);
    }
}
