package com.sdcz.endpass.bean;

public class ChannelTypeBean {
    private int roomType;
    private String channelCode;
    private String name;
    private Long roomId;
    private Long inviteCode;

    public int getRoomType() {
        return roomType;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public String getName() {
        return name;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(Long inviteCode) {
        this.inviteCode = inviteCode;
    }
}
