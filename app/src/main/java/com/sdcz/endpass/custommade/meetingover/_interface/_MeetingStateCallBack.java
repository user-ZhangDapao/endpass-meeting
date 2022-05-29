package com.sdcz.endpass.custommade.meetingover._interface;

import java.util.Map;

//根据第三方需求定制会议状态回调接口
public interface _MeetingStateCallBack {
    /**
     * 用户离开会议室
     *
     * map:(3种情况，如下：)
     * 1、会议被解散(管理员关闭会议室)
     * code:1
     * type:1:本地管理员关闭 2、其他管理员关闭
     *
     * 2、用户主动退出或者网络异常掉线
     * code:2
     * type:1:主动退出 2、网络异常
     *
     * 3、用户被管理员踢出会议
     * code:3
     * userId:踢你的管理员的userId
     */
    void on_quit_room(Map<String,Object> map);
}
