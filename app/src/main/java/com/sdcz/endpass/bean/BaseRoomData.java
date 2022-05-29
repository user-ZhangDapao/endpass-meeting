package com.sdcz.endpass.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public abstract class BaseRoomData implements MultiItemEntity {

    public abstract String getDisplayRoomName();

    public abstract String getDisplayInviteCode();

    public abstract int getDisplayCurrentUserCount();

    public abstract int getDisplayMaxUserCount();

    public abstract String getDisplayStartTime();

    public abstract String getDisplayEndTime();
}
