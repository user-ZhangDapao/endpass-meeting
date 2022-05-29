package com.sdcz.endpass.bean;



import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.inpor.sdk.repository.bean.CompanyUserInfo;
import com.inpor.sdk.repository.bean.InstantMeetingInfo;

import java.util.ArrayList;

import static com.sdcz.endpass.presenter.RoomListViewModel.TYPE_INSTANT_ROOM;

public class InstantMeeting extends BaseRoomData implements MultiItemEntity {

    private String closeTime;
    private String createTime;
    private int creatorId;
    private String inviteCode;
    private int maxUserCount;
    private String meetingName;
    private int meetingTemplate;
    private String openTime;
    private long roomId;
    private ArrayList<CompanyUserInfo> userInfos;
    private int verifyMode;

    @Override
    public String toString() {
        return "InstantMeeting{" +
                "closeTime='" + closeTime + '\'' +
                ", createTime='" + createTime + '\'' +
                ", creatorId=" + creatorId +
                ", inviteCode='" + inviteCode + '\'' +
                ", maxUserCount=" + maxUserCount +
                ", meetingName='" + meetingName + '\'' +
                ", meetingTemplate=" + meetingTemplate +
                ", openTime='" + openTime + '\'' +
                ", roomId=" + roomId +
                ", verifyMode=" + verifyMode +
                '}';
    }

    public static InstantMeeting convert(InstantMeetingInfo info) {
        InstantMeeting meeting = new InstantMeeting();
        meeting.setCloseTime(info.getCloseTime());
        meeting.setCreateTime(info.getCreateTime());
        meeting.setCreatorId(info.getCreatorId());
        meeting.setInviteCode(info.getInviteCode());
        meeting.setMaxUserCount(info.getMaxUserCount());
        meeting.setMeetingName(info.getMeetingName());
        meeting.setMeetingTemplate(info.getMeetingTemplate());
        meeting.setOpenTime(info.getOpenTime());
        meeting.setRoomId(info.getRoomId());
        meeting.setUserInfos(info.getUserInfos());
        meeting.setVerifyMode(info.getVerifyMode());
        return meeting;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public int getMaxUserCount() {
        return maxUserCount;
    }

    public void setMaxUserCount(int maxUserCount) {
        this.maxUserCount = maxUserCount;
    }

    public String getMeetingName() {
        return meetingName;
    }

    public void setMeetingName(String meetingName) {
        this.meetingName = meetingName;
    }

    public int getMeetingTemplate() {
        return meetingTemplate;
    }

    public void setMeetingTemplate(int meetingTemplate) {
        this.meetingTemplate = meetingTemplate;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public ArrayList<CompanyUserInfo> getUserInfos() {
        return userInfos;
    }

    public void setUserInfos(ArrayList<CompanyUserInfo> userInfos) {
        this.userInfos = userInfos;
    }

    public int getVerifyMode() {
        return verifyMode;
    }

    public void setVerifyMode(int verifyMode) {
        this.verifyMode = verifyMode;
    }

    @Override
    public int getItemType() {
        return TYPE_INSTANT_ROOM;
    }

    @Override
    public String getDisplayRoomName() {
        return getMeetingName();
    }

    @Override
    public String getDisplayInviteCode() {
        return getInviteCode();
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
        return null;
    }

    @Override
    public String getDisplayEndTime() {
        return null;
    }
}
