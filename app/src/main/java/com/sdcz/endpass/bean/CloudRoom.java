package com.sdcz.endpass.bean;


import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.inpor.sdk.repository.bean.CloudRoomInfo;

import static com.sdcz.endpass.presenter.RoomListViewModel.TYPE_CLOUD_ROOM;

public class CloudRoom extends BaseRoomData implements MultiItemEntity {
    private int roomId;
    private String roomName;
    private int curUserCount;
    private int maxUserCount;
    private String verifyMode;
    private String ifChairPwd;
    private String ifRoomPwd;
    private String regUserAttendURL;
    private String nonRegUserAttendURL;
    private String attendInviteURL;
    private String hostInviteURL;
    private String userRight;
    private String inviteCode;
    private String nodeId;

    @Override
    public String toString() {
        return "CloudRoom{" +
                "roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                ", curUserCount=" + curUserCount +
                ", maxUserCount=" + maxUserCount +
                ", verifyMode='" + verifyMode + '\'' +
                ", ifChairPwd='" + ifChairPwd + '\'' +
                ", ifRoomPwd='" + ifRoomPwd + '\'' +
                ", regUserAttendURL='" + regUserAttendURL + '\'' +
                ", nonRegUserAttendURL='" + nonRegUserAttendURL + '\'' +
                ", attendInviteURL='" + attendInviteURL + '\'' +
                ", hostInviteURL='" + hostInviteURL + '\'' +
                ", userRight='" + userRight + '\'' +
                ", inviteCode='" + inviteCode + '\'' +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }

    public static CloudRoom convert(CloudRoomInfo info) {
        CloudRoom cloudRoom = new CloudRoom();
        cloudRoom.setRoomId(info.getRoomId());
        cloudRoom.setInviteCode(info.getInviteCode());
        cloudRoom.setRoomName(info.getRoomName());
        cloudRoom.setCurUserCount(info.getCurUserCount());
        cloudRoom.setMaxUserCount(info.getMaxUserCount());
        cloudRoom.setVerifyMode(info.getVerifyMode());
        cloudRoom.setIfChairPwd(info.getIfChairPwd());
        cloudRoom.setIfRoomPwd(info.getIfRoomPwd());
        cloudRoom.setRegUserAttendURL(info.getRegUserAttendURL());
        cloudRoom.setNonRegUserAttendURL(info.getNonRegUserAttendURL());
        cloudRoom.setUserRight(info.getUserRight());
        cloudRoom.setAttendInviteURL(info.getAttendInviteURL());
        cloudRoom.setHostInviteURL(info.getHostInviteURL());
        cloudRoom.setNodeId(info.getNodeId());
        return cloudRoom;
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

    public int getCurUserCount() {
        return curUserCount;
    }

    public void setCurUserCount(int curUserCount) {
        this.curUserCount = curUserCount;
    }

    public int getMaxUserCount() {
        return maxUserCount;
    }

    public void setMaxUserCount(int maxUserCount) {
        this.maxUserCount = maxUserCount;
    }

    public String getVerifyMode() {
        return verifyMode;
    }

    public void setVerifyMode(String verifyMode) {
        this.verifyMode = verifyMode;
    }

    public String getIfChairPwd() {
        return ifChairPwd;
    }

    public void setIfChairPwd(String ifChairPwd) {
        this.ifChairPwd = ifChairPwd;
    }

    public String getIfRoomPwd() {
        return ifRoomPwd;
    }

    public void setIfRoomPwd(String ifRoomPwd) {
        this.ifRoomPwd = ifRoomPwd;
    }

    public String getRegUserAttendURL() {
        return regUserAttendURL;
    }

    public void setRegUserAttendURL(String regUserAttendURL) {
        this.regUserAttendURL = regUserAttendURL;
    }

    public String getNonRegUserAttendURL() {
        return nonRegUserAttendURL;
    }

    public void setNonRegUserAttendURL(String nonRegUserAttendURL) {
        this.nonRegUserAttendURL = nonRegUserAttendURL;
    }

    public String getAttendInviteURL() {
        return attendInviteURL;
    }

    public void setAttendInviteURL(String attendInviteURL) {
        this.attendInviteURL = attendInviteURL;
    }

    public String getHostInviteURL() {
        return hostInviteURL;
    }

    public void setHostInviteURL(String hostInviteURL) {
        this.hostInviteURL = hostInviteURL;
    }

    public String getUserRight() {
        return userRight;
    }

    public void setUserRight(String userRight) {
        this.userRight = userRight;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public int getItemType() {
        return TYPE_CLOUD_ROOM;
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
        return getCurUserCount();
    }

    @Override
    public int getDisplayMaxUserCount() {
        return getMaxUserCount();
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
