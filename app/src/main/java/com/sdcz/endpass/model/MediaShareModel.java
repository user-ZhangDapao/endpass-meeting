package com.sdcz.endpass.model;

import android.view.SurfaceView;

import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.bean.BaseShareBean;
import com.sdcz.endpass.bean.MediaShareBean;
import com.inpor.nativeapi.adaptor.RoomWndState;
import com.inpor.nativeapi.interfaces.JniType;
import com.inpor.nativeapi.interfaces.MultiAvmp;
import com.inpor.nativeapi.interfaces.VideoRenderManager;
import com.inpor.nativeapi.interfaces.VideoRenderNotify;

import java.util.List;

/**
 *  媒体分享的数据  M层
 */
public class MediaShareModel {

    private static MediaShareModel instance = null;
    public static MediaShareModel getInstance() {
        if (instance == null) {
            instance = new MediaShareModel();
        }
        return instance;
    }

    private final boolean isVideoEnable = true;
    private boolean isVideoOpening = false;
    private final boolean isAudioOpening = false;
    private final boolean isMediaShare = false;

    /**
     * 切换显示媒体共享的surface
     */
    public synchronized void changeVideoSurface(SurfaceView surfaceView, VideoRenderNotify videoRenderNotify) {
        stopMediaVideo();
        if (surfaceView == null || videoRenderNotify == null) {
            return;
        }
        List<BaseShareBean> baseShareBeans = ShareBeanManager.getByType(RoomWndState.DataType.DATA_TYPE_MEDIASHARE);
        if (baseShareBeans == null || baseShareBeans.size() <= 0) {
            return;
        }
        MediaShareBean mediaShareBean = (MediaShareBean) baseShareBeans.get(0);
        startMediaVideo(mediaShareBean.userId, mediaShareBean.videoId, surfaceView, videoRenderNotify);
    }

    public synchronized void startMediaVideo(long userId, byte mediaId, SurfaceView surfaceView,
                                             VideoRenderNotify videoRenderNotify) {
        if (userId == 0 || surfaceView == null || videoRenderNotify == null || !isVideoEnable) {
            return;
        }
        List<BaseShareBean> baseShareBeans = ShareBeanManager.getByType(RoomWndState.DataType.DATA_TYPE_MEDIASHARE);
        if (baseShareBeans.size() <= 0) {
            return;
        }
        MediaShareBean mediaShareBean = (MediaShareBean) baseShareBeans.get(0);

        if (mediaId != 0 && !mediaShareBean.isOpeningVideo) {
            // 渲染
            long renderId =
                    SdkUtil.getVideoManager().startRemoteVideoView(userId, mediaId, surfaceView,
                            videoRenderNotify);
            // 显示方式
            VideoRenderManager.getInstance().setRemoteRenderDisplayMode(renderId, 3);
            // 启动
            MultiAvmp.getInstance().startRecvMedia(userId, JniType.MULTIAV_MEDIATYPE_VIDEO,
                    mediaId, renderId);

            mediaShareBean.isOpeningVideo = true;
            isVideoOpening = true;
            mediaShareBean.renderId = renderId;
        }
    }
    public synchronized void stopMediaVideo() {
        List<BaseShareBean> baseShareBeans = ShareBeanManager.getByType(RoomWndState.DataType.DATA_TYPE_MEDIASHARE);
        if (baseShareBeans.size() <= 0) {
            return;
        }
        MediaShareBean mediaShareBean = (MediaShareBean) baseShareBeans.get(0);
        if (mediaShareBean.videoId == 0 || !mediaShareBean.isOpeningVideo) {
            return;
        }
        MultiAvmp.getInstance().stopRecvMedia(mediaShareBean.userId,
                JniType.MULTIAV_MEDIATYPE_VIDEO, mediaShareBean.videoId);
        // 移除底层句柄
        SdkUtil.getVideoManager().stopRemoteVideoView(mediaShareBean.renderId);
        mediaShareBean.isOpeningVideo = false;
        isVideoOpening = false;
        removeShareBean(false);
    }

    private void removeShareBean(boolean byUser) {
        if (!isMediaShare && !isAudioOpening && !isVideoOpening) {
            ShareBeanManager.removeByType(RoomWndState.DataType.DATA_TYPE_MEDIASHARE);
        }
    }
}
