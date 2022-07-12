package com.sdcz.endpass.bean;

public class ChannelTypeBean {
    private int roomType;
    private String channelCode;
    private String name;

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
}
