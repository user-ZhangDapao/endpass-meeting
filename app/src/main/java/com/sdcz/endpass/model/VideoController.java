package com.sdcz.endpass.model;

import android.Manifest;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.ActivityUtils;
import com.comix.meeting.MeetingModule;
import com.comix.meeting.entities.BaseUser;
import com.comix.meeting.entities.LayoutType;
import com.comix.meeting.entities.MeetingInfo;
import com.comix.meeting.entities.VideoInfo;
import com.comix.meeting.listeners.UserModelListenerImpl;
import com.comix.meeting.listeners.VideoModelListener;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.bean.CameraAndAudioEventOnWrap;
import com.sdcz.endpass.bean.CameraEventOnWrap;
import com.sdcz.endpass.util.PermissionUtils;
//import com.sdcz.endpass.util.YUVUtil;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.widget.VideoScreenView;
import com.inpor.base.sdk.audio.AudioManager;
import com.inpor.base.sdk.user.UserManager;
import com.inpor.base.sdk.video.VideoManager;
import com.inpor.nativeapi.adaptor.RoomWndState;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @description     视频控制类
 */
public class VideoController implements VideoModelListener {

    private static final String TAG = "VideoController";
    private static volatile VideoController instance;
    public static final int MAX_VIDEO_NUMBER = 2;
    private Context ctx;
    public String nikeName = "";

    private LinkedList<VideoScreenView> videoScreenViews;
    /**
     * 视频信息的列表
     */
    private final List<VideoInfo> videoInfos = new ArrayList<>(MAX_VIDEO_NUMBER);
    private final List<VideoInfo> tempList = new ArrayList<>();
    private VideoInfo localVideoInfo;
    /**
     * 本地Camera状态：true 不禁用；false 禁用
     */
    private boolean enableCamera = true;
    //add by baodian

    public boolean isEnableCamera() {
        return enableCamera;
    }

    public void setSfkUserId(long sfkUserId) throws JSONException {
        instance.nikeName = SharedPrefsUtil.getJSONValue(Constants.SharedPreKey.AllUserId).getJSONObject(String.valueOf(sfkUserId)).getString("nickName");
    }

    private VideoManager videoModel;
    private UserManager userModel;
    private final UserModelListenerImpl userModelListener =
            new UserModelListenerImpl(UserModelListenerImpl.USER_INFO
                    | UserModelListenerImpl.AUDIO_STATE, UserModelListenerImpl.ThreadMode.MAIN) {
                @Override
                public void onUserChanged(int type, BaseUser user) {
                    for (VideoScreenView screenView : videoScreenViews) {
                        VideoInfo videoInfo = screenView.getVideoInfo();
                        if (videoInfo == null) {
                            break;
                        }
                if (videoInfo.getUserId() == user.getUserId()) {
                    if (type == UserModelListenerImpl.USER_INFO) {
                        screenView.refreshUserInfo(user);
                    } else if (type == UserModelListenerImpl.AUDIO_STATE) {
                        screenView.refreshUserAudioState(user);
                    }
                }
            }
        }
    };

    private VideoControllerListener controllerListener;
    private MeetingModule proxy;

    private VideoController() {
    }

    /**
     * @return VideoController
     */
    public static VideoController getInstance() {
        if (instance == null) {
            synchronized (VideoController.class) {
                if (instance == null) {
                    instance = new VideoController();
                }
            }
        }
        return instance;
    }

    /**
     * 释放
     */
    public static void release() {
        if (instance == null) {
            return;
        }
        synchronized (VideoController.class) {
            if (instance != null) {
                instance.releaseInner();
                instance = null;
            }
        }
    }

    public void setControllerListener(VideoControllerListener controllerListener) {
        this.controllerListener = controllerListener;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void init(Context context) {
        this.ctx = context;
        proxy = SdkUtil.getMeetingManager().getMeetingModule();
        videoScreenViews = new LinkedList<>();
        for (int i = 0; i < MAX_VIDEO_NUMBER; i++) {
            VideoScreenView window = createVideoWindow(context);
            videoScreenViews.add(window);
            if (controllerListener != null) {
                controllerListener.onVideoScreenAdd(videoScreenViews, window);
            }
        }
        //
        videoModel = SdkUtil.getVideoManager();
        userModel = SdkUtil.getUserManager();
        //监听VideoModel
        videoModel.addEventListener(this);
        userModel.addEventListener(userModelListener);
    }

    /**
     * 检查当前布局视频Z序
     *
     * @param meetingInfo 会议信息
     */
    public void checkZOrder(MeetingInfo meetingInfo) {
        if (videoScreenViews == null || videoScreenViews.isEmpty()) {
            return;
        }
        if (meetingInfo.layoutType == LayoutType.CULTIVATE_LAYOUT) {
            videoScreenViews.get(0).setZOrderMediaOverlay(true);
        } else if (meetingInfo.layoutType == LayoutType.VIDEO_LAYOUT
                && meetingInfo.splitStyle == RoomWndState.SplitStyle.SPLIT_STYLE_P_IN_P) {
            videoScreenViews.get(0).setZOrderMediaOverlay(false);
            VideoScreenView window = videoScreenViews.get(1);
            window.setZOrderMediaOverlay(true);
            window.bringToFront();
        }
    }

    /**
     * 启用或者禁用本地视频
     * @param enable true 不禁用；false 禁用
     */
    public void enableVideoStateChanged(boolean enable) {
        if (enableCamera == enable) {
            return; // 状态相同不需要处理
        }
        enableCamera = enable;
        if (videoScreenViews == null || videoScreenViews.isEmpty()) {
            return;
        }
        for (VideoScreenView screenView : videoScreenViews) {
            if (screenView.getVideoInfo() != null && screenView.getVideoInfo().isLocalUser()) {
                screenView.enableVideo(enable);
            }
        }
    }

    //视频权限回调
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_camera_open(CameraEventOnWrap event_on) {

        UserManager userModel = SdkUtil.getUserManager();
        BaseUser localUser = userModel.getLocalUser();
        if( event_on.flag == true )
        {
            videoModel.broadcastVideo(localUser);
        }
        else
        {

        }

        EventBus.getDefault().unregister(this);
    }

    //音视频权限回调
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on_camera_and_audio_open(CameraAndAudioEventOnWrap event_on) {

        UserManager userModel = SdkUtil.getUserManager();
        BaseUser localUser = userModel.getLocalUser();
        if( event_on.flag_camera == true )
        {
            videoModel.broadcastVideo(localUser);
        }
        if( event_on.flag_audio == true )
        {
            AudioManager audioManager = SdkUtil.getAudioManager();
            audioManager.broadcastAudio(localUser);
        }
        EventBus.getDefault().unregister(this);

    }

    @Override
    public void onVideoAdded(List<VideoInfo> list, VideoInfo changeInfo) {

        long localUserId = SdkUtil.getUserManager().getLocalUser().getUserId();
        long changeInfoId = changeInfo.getVideoUser().getUserId();

        ///判断是否是自己
        if (changeInfoId != localUserId && !changeInfo.getVideoUser().getNickName().equals(nikeName)){
            return;
        }

        VideoManager videomanager = SdkUtil.getVideoManager();
        boolean use_local_camera = videomanager.get_use_local_camera();//SharedPreferencesUtils.getBoolean("use_local_camera",true);
        //判断视频权限
        //only for test
        VideoManager videoModel = SdkUtil.getVideoManager();
        if( changeInfo.isLocalUser() )
        {
            List<String> permissionList = PermissionUtils.requestMeetingPermission();
            if ( use_local_camera == true && permissionList != null && (permissionList.contains(Manifest.permission.CAMERA))) {
                //没有权限还能创建视频流，纠正底层bug吧
                if( EventBus.getDefault().isRegistered(this) == false ) {
                    EventBus.getDefault().register(this);
                }
                String[] toBeStored = permissionList.toArray(new String[permissionList.size()]);
                int requestCode = 61;//只申请视频
                if( toBeStored.length == 2 )//音视频一起申请
                {
                    requestCode = 65;
                }
                ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(),
                        toBeStored, requestCode);
                videoModel.broadcastVideo(changeInfo.getVideoUser());
                return;
            }
        }

        changeInfo = copyVideoInfoList(list, changeInfo);
        VideoScreenView screenView;
        if (videoScreenViews.size() >= videoInfos.size()) {
            // 广播的视频小于窗口数时，将视频放置在靠前的空窗口
            screenView = videoScreenViews.get(changeInfo.getPosition());
            if (screenView != null && screenView.getVideoInfo() != null) {
                //如果顺序变化  当前Position下 videoInfo 不为空 则找到空位置添加
                for (int i = 0; i < videoScreenViews.size(); i++) {
                    VideoScreenView view = videoScreenViews.get(i);
                    if (view.getVideoInfo() == null) {
                        screenView = view;
                        boolean interrupt = i >= MAX_VIDEO_NUMBER;
                        screenView.setInterruptReceive(interrupt);
                        break;
                    }
                }
            }
        } else {
            // 广播的视频大于已有窗口数时，创建新的窗口
            screenView = new VideoScreenView(ctx);
            boolean interrupt = videoScreenViews.size() > MAX_VIDEO_NUMBER;
            screenView.setInterruptReceive(interrupt);
            videoScreenViews.add(screenView);
        }
        if (changeInfo.isLocalUser()) {
            localVideoInfo = changeInfo;
        }
        //将videoInfo和screenView绑定
        if (screenView != null && screenView.getVideoInfo() == null) {
            screenView.attachVideoInfo(changeInfo);
            screenView.enableVideo(enableCamera);
        }
        if (controllerListener != null) {
            controllerListener.onVideoScreenAdd(videoScreenViews, screenView);
        }

        //add by baodian
        if( changeInfo != null && changeInfo.isLocalUser() ) {

            /*
            if (use_local_camera == true) {

                if( YUVUtil.get_observe_send_data() != null )
                {
                    YUVUtil.get_observe_send_data().stop_send_data();
                }
                YUVUtil.startup_local_capture(true);

            } else {

                if( YUVUtil.get_observe_send_data() != null )
                {
                    YUVUtil.get_observe_send_data().can_send_data();
                }
                YUVUtil.startup_local_capture(false);
            }*/

            //YUVUtil.getInstance().change_to_use_local_camera(use_local_camera);

            if( use_local_camera == false )
            {
                videomanager.setCameraEnable(!use_local_camera);
            }
            //videomanager.set_use_local_camera(videomanager.get_use_local_camera());
            videomanager.setLocalVideoCaptureEnable(videomanager.get_use_local_camera());
            //videomanager.setLocalVideoCaptureEnable(use_local_camera);

        }

    }

    @Override
    public void onVideoRemoved(List<VideoInfo> list, VideoInfo changeInfo) {
        changeInfo = copyVideoInfoList(list, changeInfo);
        if (changeInfo.isLocalUser()) {
            localVideoInfo = null;

            //YUV输入源需要自己善后
            /*
            if( YUVUtil.getInstance().get_use_local_camera() == false ) {
                if (YUVUtil.getInstance().get_observe_send_data() != null) {
                    YUVUtil.getInstance().get_observe_send_data().stop_send_data();

                    YUVUtil.getInstance().get_observe_send_data().notify_to_draw(false);
                }

                CustomImageOnOffEvent event = new CustomImageOnOffEvent();
                event.on = false;
                EventBus.getDefault().post(event);

            }*/

        }
        VideoScreenView removeView = null;
        for (VideoScreenView screenView : videoScreenViews) {
            VideoInfo curVideoInfo = screenView.getVideoInfo();
            if (curVideoInfo == null) {
                //如果遍历到  空的视频布局跳过
                continue;
            }
            if (curVideoInfo.equals(changeInfo)) {
                //当视频关闭时候异常VideoScreenView，为了保证空视频始终在最后
                screenView.detachVideoInfo();
                //移除掉不再继续循环
                removeView = screenView;
                videoScreenViews.remove(screenView);
                if (controllerListener != null) {
                    controllerListener.onVideoScreenRemove(videoScreenViews, screenView);
                }
                break;
            }
        }
        //当移除视频后，视频View不足最大布局个数时增加一个空VideoScreenView
        if (videoScreenViews.size() < MAX_VIDEO_NUMBER) {
            if (removeView == null) {
                removeView = new VideoScreenView(ctx);
            }
            videoScreenViews.add(removeView);
            if (controllerListener != null) {
                controllerListener.onVideoScreenAdd(videoScreenViews, removeView);
            }
        }
        // 因为需要补位，所以这里要检查z序
        MeetingInfo meetingInfo = proxy.getMeetingInfo();
        checkZOrder(meetingInfo);
    }

    @Override
    public void onVideoPositionChanged(List<VideoInfo> list) {
        copyVideoInfoList(list, null);
        boolean isNeed = false;
        for (int position = 0; position < videoInfos.size(); position++) {
            VideoInfo videoScreenVideoInfo = videoScreenViews.get(position).getVideoInfo();
            if (videoScreenVideoInfo == null) {
                continue;
            }
            VideoInfo orderVideoInfo = videoInfos.get(position);
            if (orderVideoInfo.equals(videoScreenVideoInfo)) {
                continue;
            }
            for (int i = 0; i < videoInfos.size(); i++) {
                VideoScreenView screenView = videoScreenViews.get(i);
                if (screenView == null || screenView.getVideoInfo() == null) {
                    continue;
                }
                VideoInfo videoInfo2 = screenView.getVideoInfo();
                if (orderVideoInfo.equals(videoInfo2)) {
                    isNeed = true;
                    videoScreenViews.add(position, videoScreenViews.remove(i));
                }
            }
        }
        if (!isNeed) {
            return;
        }
        if (controllerListener != null) {
            controllerListener.onVideoPositionChange(videoScreenViews);
        }
        // 交换位置
        MeetingInfo meetingInfo = proxy.getMeetingInfo();
        checkZOrder(meetingInfo);
    }

    @Override
    public void onVideoFullStateChanged(VideoInfo changeInfo) {
        VideoScreenView window = findVideoViewByVideoInfo(changeInfo);
        if (window == null) {
            Log.w(TAG, "Did not find the index of the full screen window");
            return;
        }
        changeInfo.copyTo(window.getVideoInfo());
        if (controllerListener != null) {
            controllerListener.onVideoFullScreen(window, changeInfo.isFullScreen());
        }
    }

    private void releaseInner() {
        if (videoModel != null) {
            videoModel.removeEventLisner(this);
            videoModel = null;
        }
        if (userModel != null) {
            userModel.removeEventListener(userModelListener);
            userModel = null;
        }
        closeAllVideos();
        videoInfos.clear();
        if (videoScreenViews != null) {
            videoScreenViews.clear();
            videoScreenViews = null;
        }
        ctx = null;
        controllerListener = null;
    }

    private VideoScreenView createVideoWindow(Context context) {
        VideoScreenView window = new VideoScreenView(context);
        window.setBackgroundResource(R.drawable.video_default_bg);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        window.setLayoutParams(layoutParams);
        window.setPadding(1, 1, 1, 1);
        return window;
    }

    /**
     * ┎┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┒
     * ┊ 功  能：查找  根据VideoInfo 找到 VideoScreenView                     ┊
     * ┊                                                                      ┊
     * ┊ 参  数： videoInfo  需要查找的视频信息                               ┊
     * ┊ 返回值：查找到的VideoScreenView  ，未找到为空                        ┊
     * ┖┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┚
     */
    private VideoScreenView findVideoViewByVideoInfo(VideoInfo videoInfo) {
        for (VideoScreenView screenView : videoScreenViews) {
            VideoInfo info = screenView.getVideoInfo();
            if (info != null && info.equals(videoInfo)) {
                return screenView;
            }
        }
        return null;
    }

    /**
     * ┎┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┒
     * ┊ 功  能：退出会议室时，关闭所有视频，仅在退出会议室时使用             ┊
     * ┊                                                                      ┊
     * ┊ 参  数：无                                                           ┊
     * ┊ 返回值：无                                                           ┊
     * ┖┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┚
     */
    private void closeAllVideos() {
        if (videoScreenViews != null) {
            for (VideoScreenView screenView : videoScreenViews) {
                if (screenView.getVideoInfo() == null) {
                    //如果遍历到  空的视频布局跳过
                    continue;
                }
                screenView.detachVideoInfo();
            }
        }
    }

    /**
     * videoInfos尽量重用对象，而不使用message中的list中新建的对象
     */
    private VideoInfo copyVideoInfoList(List<VideoInfo> videoInfoList, VideoInfo changingVideoInfo) {
        tempList.clear();
        for (VideoInfo videoInfo1 : videoInfoList) {
            boolean found = false;
            for (VideoInfo videoInfo2 : videoInfos) {
                if (videoInfo2.equals(changingVideoInfo)) {
                    changingVideoInfo = changingVideoInfo.copyTo(videoInfo2);
                }
                if (videoInfo1.equals(videoInfo2)) {
                    tempList.add(videoInfo1.copyTo(videoInfo2));
                    found = true;
                    break;
                }
            }
            if (!found) {
                tempList.add(videoInfo1);
            }
        }
        videoInfos.clear();
        videoInfos.addAll(tempList);
        return changingVideoInfo;
    }

    public interface VideoControllerListener {

        /**
         * add view to layout
         *
         * @param screenViews 窗口集合
         * @param screenView  target view
         */
        void onVideoScreenAdd(List<VideoScreenView> screenViews, VideoScreenView screenView);

        /**
         * remove view from layout
         *
         * @param screenViews 窗口集合
         * @param screenView  target view
         */
        void onVideoScreenRemove(List<VideoScreenView> screenViews, VideoScreenView screenView);

        /**
         * change position
         *
         * @param screenViews order list
         */
        void onVideoPositionChange(List<VideoScreenView> screenViews);

        /**
         * 全屏某个窗口
         *
         * @param screenView  target view
         * @param isFull      标记
         */
        void onVideoFullScreen(VideoScreenView screenView, boolean isFull);
    }
}
