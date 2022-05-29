package com.sdcz.endpass.share;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.comix.meeting.entities.BaseShareBean;
import com.comix.meeting.entities.VncShareBean;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.IDataContainer;
import com.inpor.base.sdk.share.ScreenShareManager;
import com.inpor.nativeapi.interfaces.VncViewMP;

/**
 *  接收屏幕共享时的处理
 */
public class VncReceptionContainer implements IDataContainer, SurfaceHolder.Callback {

    private VncShareBean vncShareBean;
    private SurfaceView surfaceView;
    private View stateView;
    private ProgressBar loadingView;
    private ImageView pauseView;
    private ImageView stopReceiveView;
    private ScreenShareManager shareModel;

    public VncReceptionContainer() {

    }

    @Override
    public void init(Context context) {
        surfaceView = new SurfaceView(context);
        surfaceView.setZOrderMediaOverlay(false);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        surfaceView.setLayoutParams(layoutParams);
        stateView = View.inflate(context, R.layout.layout_media_state, null);
        loadingView = stateView.findViewById(R.id.loading_view);
        pauseView = stateView.findViewById(R.id.pause_image_view);
        stopReceiveView = stateView.findViewById(R.id.stop_receive_bg);
        stopReceiveView.setImageResource(R.mipmap.default_screen);
        surfaceView.getHolder().addCallback(this);
        shareModel = SdkUtil.getShareManager();
    }

    @Override
    public void release() {
        if (getDataView() != null && getDataView().getParent() != null) {
            ViewGroup parent = (ViewGroup) getDataView().getParent();
            parent.removeView(getDataView());
        }
    }

    @Override
    public void setShareBean(BaseShareBean shareBean) {
        this.vncShareBean = (VncShareBean) shareBean;
    }

    @Override
    public View getDataView() {
        if (vncShareBean == null) {
            return null;
        }
        return surfaceView;
    }

    @Override
    public View getStateView() {
        if (vncShareBean == null) {
            return null;
        }
        return stateView;
    }

    @Override
    public void updateDataView(BaseShareBean shareBean) {
        this.vncShareBean = (VncShareBean) shareBean;
    }

    @Override
    public void updateStateView(BaseShareBean shareBean) {
        if (stateView == null) {
            return;
        }
        int state = shareBean.getState();
        switch (state) {
            case BaseShareBean.STATE_VIDEO_LOADING:
            case BaseShareBean.STATE_WAITING_VIDEO_SURFACE:
                stateView.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.VISIBLE);
                pauseView.setVisibility(View.INVISIBLE);
                stopReceiveView.setVisibility(View.INVISIBLE);
                break;
            case BaseShareBean.STATE_VIDEO_LOADED:
                stateView.setVisibility(View.INVISIBLE);
                stopReceiveView.setVisibility(View.INVISIBLE);
                break;
            case BaseShareBean.STATE_PAUSE:
                stateView.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.INVISIBLE);
                break;
            case BaseShareBean.STATE_STOP_RECEIVE:
                // 显示不接收视频的图片
                stateView.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.INVISIBLE);
                pauseView.setVisibility(View.INVISIBLE);
                stopReceiveView.setVisibility(View.VISIBLE);
                surfaceView.setZOrderMediaOverlay(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        surfaceView.setVisibility(hidden ? View.GONE : View.VISIBLE);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d("VncViewMP_JAVA", "VNC_surfaceCreated()");
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d("VncViewMP_JAVA", "VNC_surfaceChanged()");
        /**
         * 去除原有逻辑 在 surfaceCreated 后 会立马调用surfaceChanged,
         * 此处减少c++曾的绘制
         * shareModel.changeVncSurface(surfaceView);
         */
        shareModel.setVncSurface(surfaceView);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.i("VncViewMP_JAVA", "VNC_surfaceDestroyed "
                + VncViewMP.getInstance().getObjId() + "  " + surfaceView.hashCode());
        /**
         * 此处如果不销毁 在用户体验上更好，在后台进入前台时界面不会重绘导致UI有一次闪动
         * 但是要在合适时机销毁
         */
        shareModel.setVncSurface(null);
    }
}
