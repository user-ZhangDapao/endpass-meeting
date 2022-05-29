package com.sdcz.endpass.bean;

import com.inpor.nativeapi.adaptor.RoomWndState;

/**
 * 共享的顶级基类
 */
public class BaseShareBean {

    public long id;

    protected boolean show = false;

    protected String title;

    public RoomWndState.DataType type;

    protected long userId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RoomWndState.DataType getType() {
        return type;
    }

    public void setType(RoomWndState.DataType type) {
        this.type = type;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }
}
