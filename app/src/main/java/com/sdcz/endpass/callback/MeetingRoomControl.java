package com.sdcz.endpass.callback;

/**
 * @Description: 由MobileMeetingActivity实现，通过接口避免直接传递MobileMeetingActivity
 */
public interface MeetingRoomControl {

    /**
     * 退出会议室
     */
    void quitRoom();
}
