package com.sdcz.endpass.bean;


import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.inpor.sdk.repository.bean.ScheduleMeetingInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sdcz.endpass.presenter.RoomListViewModel.TYPE_SCHEDULE_ROOM;

public class ScheduleMeeting extends BaseRoomData implements MultiItemEntity {


    public static final SimpleDateFormat timeSdf = new SimpleDateFormat("MM-dd HH:mm");
    private int roomId;
    private String roomName;
    private String hopeStartTime;
    private String hopeEndTime;
    private String inviteCode;

    @Override
    public String toString() {
        return "ScheduleMeeting{" +
                "roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                ", hopeStartTime='" + hopeStartTime + '\'' +
                ", hopeEndTime='" + hopeEndTime + '\'' +
                ", inviteCode='" + inviteCode + '\'' +
                '}';
    }

    public static ScheduleMeeting convert(ScheduleMeetingInfo info) {
        ScheduleMeeting scheduleMeeting = new ScheduleMeeting();
        scheduleMeeting.setRoomId(info.getRoomId());
        scheduleMeeting.setRoomName(info.getRoomName());
        scheduleMeeting.setHopeStartTime(info.getHopeStartTime());
        scheduleMeeting.setHopeEndTime(info.getHopeEndTime());
        scheduleMeeting.setInviteCode(info.getInviteCode());
        return scheduleMeeting;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getHopeStartTime() {
        return hopeStartTime;
    }

    public void setHopeStartTime(String hopeStartTime) {
        this.hopeStartTime = hopeStartTime;
    }

    public String getHopeEndTime() {
        return hopeEndTime;
    }

    public void setHopeEndTime(String hopeEndTime) {
        this.hopeEndTime = hopeEndTime;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    @Override
    public int getItemType() {
        return TYPE_SCHEDULE_ROOM;
    }

    @Override
    public String getDisplayRoomName() {
        return getRoomName();
    }

    @Override
    public String getDisplayInviteCode() {
        return String.valueOf(getRoomId());
    }

    @Override
    public int getDisplayCurrentUserCount() {
        return 0;
    }

    @Override
    public int getDisplayMaxUserCount() {
        return 0;
    }

    @Override
    public String getDisplayStartTime() {
        Date date = TimeUtils.string2Date(getHopeStartTime());
        return TimeUtils.date2String(date, timeSdf);
    }

    @Override
    public String getDisplayEndTime() {
        Date date = TimeUtils.string2Date(getHopeEndTime());
        return TimeUtils.date2String(date, timeSdf);
    }

}
