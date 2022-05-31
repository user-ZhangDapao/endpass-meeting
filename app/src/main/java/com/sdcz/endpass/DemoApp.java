package com.sdcz.endpass;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.ToastUtils;
import com.inpor.sdk.callback.SetServerCallback;
import com.sdcz.endpass.custommade.meetingover._interface._MeetingStateCallBack;
import com.sdcz.endpass.dialog.LoadingDialog;
import com.sdcz.endpass.login.JoinMeetingManager;
import com.sdcz.endpass.util.PermissionUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
//import com.sdcz.endpass.util.YUVUtil;


public class DemoApp extends Application {

    public static Context context;

    //第三方app请求会议结束回调demo
    _MeetingStateCallBack meetingStateCallBack = map -> {

                Integer code = (Integer)map.get("code");
                if( code == 1 )
                {
                    Integer type = 1;
                    if( map.get("type") != null )
                    {
                        type = (Integer) map.get("type");
                    }
                    if( type == 2 ) {
                        ToastUtils.showShort("会议被其他管理员解散");
                    }
                    else
                    {
                        ToastUtils.showShort("会议被您自己解散");
                    }
                }
                else if( code == 2 )
                {
                    Integer type = 1;
                    if( map.get("type") != null )
                    {
                        type = (Integer) map.get("type");
                    }
                    if( type == 2 ) {
                        ToastUtils.showShort("网络异常掉线");
                    }
                    else
                    {
                        ToastUtils.showShort("您主动退出");
                    }
                    //ToastUtils.showShort("您主动退出或者网络异常掉线");
                }
                else if( code == 3 )
                {
                    ToastUtils.showShort("您被管理员踢出会议");
                }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        JoinMeetingManager.getInstance().initSdk(this, "b9264352-4ae2-4065-b48a-887048506660", "df1ae16c-f5a9-4009-89e6-edbd4274a706");
        if (PermissionUtils.checkPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            JoinMeetingManager.getInstance().initLogger(this);
        }


        //此处演示第三方请求会议结束回调
        //_MeetingStateManager.getInstance().addListener(meetingStateCallBack);
        //此处演示自输入YUV本地视频流
/*
        YUVModel.getInstance(this);
        VideoManager videomanager = SdkUtil.getVideoManager();
        videomanager.setLocalVideoCaptureEnable(true);
        videomanager.setYuvDisablePicture(R.mipmap.disable_camera);
*/
    }


    public static Context getContext() {
        return context;
    }



}
