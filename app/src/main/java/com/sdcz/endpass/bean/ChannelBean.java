package com.sdcz.endpass.bean;

import java.util.List;

public class ChannelBean {

    private Object searchValue;
    private Object createBy;
    private String createTime;
    private Object updateBy;
    private Object updateTime;
    private Object remark;
    private ParamsBean params;
    private int id;
    private String channelCode;
    private Long roomId;
    private String channelName;
    private String details;
    private int deptId;
    private int createUser;
    private int status;
    private Object endTime;
    private int venue;
    private int sort;
    private String deptName;
    private int peopleCount;
    private List<ChannerUser> channelUserList;
    private Object channelUserIds;
    private List<Long> muteUserIds;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Object getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(Object searchValue) {
        this.searchValue = searchValue;
    }

    public Object getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Object createBy) {
        this.createBy = createBy;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Object getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Object updateBy) {
        this.updateBy = updateBy;
    }

    public Object getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Object updateTime) {
        this.updateTime = updateTime;
    }

    public Object getRemark() {
        return remark;
    }

    public void setRemark(Object remark) {
        this.remark = remark;
    }

    public ParamsBean getParams() {
        return params;
    }

    public void setParams(ParamsBean params) {
        this.params = params;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public int getCreateUser() {
        return createUser;
    }

    public void setCreateUser(int createUser) {
        this.createUser = createUser;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getEndTime() {
        return endTime;
    }

    public void setEndTime(Object endTime) {
        this.endTime = endTime;
    }

    public int getVenue() {
        return venue;
    }

    public void setVenue(int venue) {
        this.venue = venue;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        this.peopleCount = peopleCount;
    }

    public List<ChannerUser> getChannelUserList() {
        return channelUserList;
    }

    public void setChannelUserList(List<ChannerUser> channelUserList) {
        this.channelUserList = channelUserList;
    }

    public Object getChannelUserIds() {
        return channelUserIds;
    }

    public void setChannelUserIds(Object channelUserIds) {
        this.channelUserIds = channelUserIds;
    }

    public List<Long> getMuteUserIds() {
        return muteUserIds;
    }

    public void setMuteUserIds(List<Long> muteUserIds) {
        this.muteUserIds = muteUserIds;
    }

    public static class ParamsBean {
    }

}
