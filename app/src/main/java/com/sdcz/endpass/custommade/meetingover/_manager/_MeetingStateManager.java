package com.sdcz.endpass.custommade.meetingover._manager;

import android.app.Activity;


import com.sdcz.endpass.custommade.meetingover._interface._MeetingStateCallBack;
import com.sdcz.endpass.ui.MobileMeetingActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//根据第三方需求定制会议状态回调管理类
public class _MeetingStateManager {

    private _MeetingStateManager()
    {
    }
    private static class MyHolder{
        private static final _MeetingStateManager instance = new _MeetingStateManager();
    }
    public static _MeetingStateManager getInstance()
    {
        return MyHolder.instance;
    }

    private List<WeakReference<_MeetingStateCallBack>> observers = Collections.synchronizedList(new ArrayList());
    private WeakReference<Activity> bind_activity = null;
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    //弱引用添加观察者，自行负责观察者存活
    public void addListener(final _MeetingStateCallBack callBack)
    {
        WeakReference<_MeetingStateCallBack> _callBack = new WeakReference<>(callBack);
        observers.add(_callBack);
    }

    public void removeListener(final _MeetingStateCallBack callBack)
    {
        for( WeakReference<_MeetingStateCallBack> _observer : observers )
        {
            _MeetingStateCallBack observer = _observer.get();
            if( observer != null && observer == callBack ) {
                observers.remove(_observer);
                break;
            }
        }
    }

    //弱引用绑定会议activity
    public void bindActivity(final MobileMeetingActivity activity)
    {
        this.bind_activity = new WeakReference<>(activity);
    }

    //离开会议通知回调
    public void notify_quit_meeting(Map<String,Object> map)
    {
        //绑定一次activity就一次通知退会通知机会
        if( this.bind_activity != null && this.bind_activity.get() != null && this.bind_activity.get().isDestroyed() == false ) {
            //一个activity生命周期只能回调一次
            this.bind_activity = null;
            singleThreadExecutor.execute(() -> {
                //单线程按观察者列表添加顺序依次通知
                for( WeakReference<_MeetingStateCallBack> _observer : observers )
                {
                    _MeetingStateCallBack observer = _observer.get();
                    if( observer != null ) {
                        observer.on_quit_room(map);
                    }
                }
            });
        }
    }



}
