package com.sdcz.endpass.util;

import android.content.Context;

import com.comix.meeting.entities.BaseUser;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.inpor.nativeapi.adaptor.AudioChannel;
import com.inpor.nativeapi.adaptor.RoomUserInfo;
import com.inpor.nativeapi.adaptor.VideoChannel;

/**
 * @Description: 参会人功能封装的方法
 */
public class AttendeeUtils {

    /**
     * 获取用户昵称(如果是本地用户需要添加前缀(我))
     *
     * @param context  context
     * @param attendee 参会人
     * @return 显示的昵称
     */
    public static String getNickName(Context context, BaseUser attendee) {
        if (attendee == null) {
            return "";
        }
        if (!attendee.isLocalUser()) {
            return attendee.getNickName();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(context.getResources().getString(R.string.meetingui_me)).append(")")
                .append(attendee.getNickName());
        return sb.toString();
    }

    /**
     * 获取参会人角色描述
     */
    public static String getRoleDesc(Context context, BaseUser item) {
        StringBuilder sb = new StringBuilder();
        String comma = context.getResources().getString(R.string.meetingui_comma);
        if (item.isMainSpeakerDone()) {
            sb.append(context.getResources().getString(R.string.meetingui_main_speaker));
        } else {
            if (item.isManager()) {
                sb.append(context.getResources().getString(R.string.meetingui_administrator));
            }
        }
        // 如果是共享人，还要判断是否正在共享(正在共享的状态判断：正在显示的页签，太难了)
        if (item.isDataSharer()) {
            boolean isSharing = SdkUtil.getShareManager().isScreenSharing();
            if (isSharing) {
                sb.append(comma).append(context.getResources().getString(R.string.meetingui_sharing));
            }
        }
        if (item.isRecording()) {
            sb.append(comma).append(context.getResources().getString(R.string.meetingui_recording));
        }
        String temp = sb.toString();
        if (temp.startsWith(comma)) {
            temp = temp.replaceFirst(comma, "");
        }
        return temp;
    }

    /**
     * 获取音频渠道状态的logo
     *
     * @param channel 音频状态渠道
     * @return logo
     */
    public static int getMicStateLogo(AudioChannel channel) {
        if (RoomUserInfo.STATE_DONE == channel.state) {
            return R.mipmap.ul_mic_open0;
        }
        if (RoomUserInfo.STATE_WAITING == channel.state) {
            return R.mipmap.ul_mic_applying;
        }
        return R.mipmap.ul_mic_closed;
    }

    /**
     * 麦克风能量值
     *
     * @param energy 0~100
     * @return 对应的logo
     */
    public static int getMicStateLogo(int energy) {
        int chekerNum = 100 / 18;
        chekerNum = energy / chekerNum;
        if (chekerNum < 0) {
            chekerNum = 0;
        }
        switch (chekerNum) {
            case 0:
                return R.mipmap.ul_mic_open0;
            case 1:
                return R.mipmap.ul_mic_open1;
            case 2:
                return R.mipmap.ul_mic_open2;
            case 3:
                return R.mipmap.ul_mic_open3;
            case 4:
                return R.mipmap.ul_mic_open4;
            case 5:
                return R.mipmap.ul_mic_open5;
            case 6:
                return R.mipmap.ul_mic_open6;
            case 7:
                return R.mipmap.ul_mic_open7;
            case 8:
                return R.mipmap.ul_mic_open8;
            case 9:
                return R.mipmap.ul_mic_open9;
            case 10:
                return R.mipmap.ul_mic_open10;
            case 11:
                return R.mipmap.ul_mic_open11;
            case 12:
                return R.mipmap.ul_mic_open12;
            case 13:
                return R.mipmap.ul_mic_open13;
            case 14:
                return R.mipmap.ul_mic_open14;
            case 15:
                return R.mipmap.ul_mic_open15;
            case 16:
                return R.mipmap.ul_mic_open16;
            case 17:
                return R.mipmap.ul_mic_open17;
            default:
                return R.mipmap.ul_mic_open18;
        }

    }

    /**
     * 获取麦克风状态的logo
     *
     * @param attendee 参会人
     * @return 麦克风的状态logo
     */
    public static int getMicStateLogo(BaseUser attendee) {
        if (null == attendee) {
            return R.mipmap.ul_mic_closed;
        }else if (attendee.isSpeechNone()) {
            return R.mipmap.ul_mic_closed;
        } else if (attendee.isSpeechWait()) {
            return R.mipmap.ul_mic_applying;
        } else {
            return getMicStateLogo(attendee.getSoundEnergy());
        }
    }

    /**
     * 获取麦克风对应状态的描述(注意：描述是当前状态下一个状态的描述，而不是当前状态)
     *
     * @param attendee 参会人
     * @return 麦克风状态描述
     */
    public static int getMicStateDesc(BaseUser attendee) {
        if (attendee.isSpeechNone()) {
            return R.string.meetingui_open_mic;
        } else if (attendee.isSpeechWait()) {
            return R.string.meetingui_open_mic;
        } else {
            return R.string.meetingui_close_mic;
        }
    }

    /**
     * 获取视频渠道状态logo
     *
     * @param channel 视频状态渠道
     * @return logo
     */
    public static int getCameraStateLogo(VideoChannel channel) {

        if (RoomUserInfo.STATE_DONE == channel.state) {
            return R.mipmap.ul_camera_open;
        }
        if (RoomUserInfo.STATE_WAITING == channel.state) {
            return R.mipmap.ul_camera_applying;
        }
        return R.mipmap.ul_camera_closed;
    }

    /**
     * 获取相机状态的logo
     *
     * @param attendee 参会人
     * @return 相机状态对应的logo
     */
    public static int getCameraStateLogo(BaseUser attendee) {
        if(null == attendee) {
            return R.mipmap.ul_camera_closed;
        }else if (attendee.isVideoNone(attendee)) {
            return R.mipmap.ul_camera_open;
        } else if (attendee.isVideoWait()) {
            return R.mipmap.ul_camera_applying;
        } else {
            return R.mipmap.ul_camera_closed;
        }
    }

    /**
     * 获取相机状态的描述(注意：描述是当前状态下一个状态的描述，而不是当前状态)
     *
     * @param attendee 参会人
     * @return 相机状态对应的描述
     */
    public static int getCameraStateDesc(BaseUser attendee) {
        if (attendee.isVideoNone(attendee)) {
            return R.string.meetingui_open_camera;
        } else if (attendee.isVideoWait()) {
            return R.string.meetingui_open_camera;
        } else {
            return R.string.meetingui_close_camera;
        }
    }

    /**
     * 获取参会人主持人状态logo
     *
     * @param attendee 参会人
     * @return 参会人主持人状态logo
     */
    public static int getHostStateLogo(BaseUser attendee) {
        if (attendee.isMainSpeakerNone()) {
            return R.mipmap.ul_speaker_off;
        } else if (attendee.isMainSpeakerWait()) {
            return R.mipmap.ul_speaker_off;
        } else {
            return R.mipmap.ul_speaker_on;
        }
    }

    /**
     * 参会人主持人状态描述(注意：描述是当前状态下一个状态的描述，而不是当前状态)
     *
     * @param attendee 参会人
     * @return 参会人主持人状态描述
     */
    public static int getHostStateDesc(BaseUser attendee) {
        if (attendee.isMainSpeakerNone()) {
            return R.string.meetingui_award_main_speaker;
        } else if (attendee.isMainSpeakerWait()) {
            return R.string.meetingui_award_main_speaker;
        } else {
            return R.string.meetingui_cancel_main_speaker;
        }
    }

}



















