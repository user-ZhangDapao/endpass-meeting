package com.sdcz.endpass.view;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.inpor.log.Logger;
import com.inpor.manager.util.ScreenDeskUtil;
import com.sdcz.endpass.R;

import java.util.List;

/**
 * 本地视频预览，不调用其他模块，只调用Camera API
 *
 * @Author liwl
 * @Time 2018/12/19
 */
public class PreviewVideoView extends FrameLayout implements SurfaceHolder.Callback, ScreenDeskUtil.DeskStateCallBack {
    private static final String TAG = "PreviewVideoView";
    private Activity myActivity;
    private SurfaceView videoView;
    private SurfaceHolder videoHolder;
    private Camera camera;

    /**
     * ┎┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┒
     * ┊ 功  能： 构造函数                                             ┊
     * ┊                                                             ┊
     * ┊ 参  数：context 上下文                                        ┊
     * ┊ 返回值：                                                      ┊
     * ┖┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┚
     */
    public PreviewVideoView(@NonNull Activity myActivity) {
        this(myActivity, null);
        this.myActivity = myActivity;
        createView();
        ScreenDeskUtil.registerDeskStateCallBack(this);
    }

    /**
     * ┎┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┒
     * ┊ 功  能： 构造函数                                             ┊
     * ┊                                                             ┊
     * ┊ 参  数：context 上下文    attrs 布局属性参数                   ┊
     * ┊ 返回值：                                                     ┊
     * ┖┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┚
     */
    public PreviewVideoView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initView() {
        videoView = new SurfaceView(myActivity);
    }

    private void initValue() {
        videoHolder = videoView.getHolder();
        videoHolder.addCallback(this);
        setFocusable(false);
        setFocusableInTouchMode(false);
    }

    private void initBackground() {
        setBackgroundResource(R.drawable.hst_largevideo_default);
    }

    private void destoryView() {
        removeAllViews();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
    }

    private void createView() {
        initBackground();
        initView();
        initValue();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera(holder);
        if (camera != null) {
            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            Camera.Size optimalSize = getOptimalPreviewSize(sizes, width, height);
            if (optimalSize != null) {
                params.setPreviewSize(optimalSize.width, optimalSize.height);
                camera.setParameters(params);
                camera.startPreview();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            Logger.info(TAG, "surfaceDestroyed");
            camera.release();
            videoHolder.removeCallback(this);
            camera = null;
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double aspectTolerance = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > aspectTolerance) {
                continue;
            }
            if (Math.abs(size.width - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.width - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the
        // requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.width - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.width - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    /**
     * 设置摄像头旋转方向
     *
     * @param activity 当前的视图
     * @param cameraId 当前摄像头id
     * @param camera   当前摄像头
     */
    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        if (rotation == Surface.ROTATION_0) {
            degrees = 0;
        } else if (rotation == Surface.ROTATION_90) {
            degrees = 90;
        } else if (rotation == Surface.ROTATION_180) {
            degrees = 180;
        } else if (rotation == Surface.ROTATION_270) {
            degrees = 270;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;   // compensate the mirror
        } else {
            // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void initCamera(SurfaceHolder holder) {
        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        try {
            if (camera == null) {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                setCameraDisplayOrientation(myActivity, Camera.CameraInfo.CAMERA_FACING_FRONT, camera);
                camera.setPreviewDisplay(holder);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 设置Z轴方向的媒体播放
     *
     * @param order 是否在最顶层
     */
    public void showVideoViewOnTop(boolean order) {
        if (videoView != null) {
            removeAllViews();
            videoView.setZOrderOnTop(order);
            videoView.setZOrderMediaOverlay(order);
            addView(videoView);
            //焦点位置保持在上层
        }
    }

    /**
     * 暂停预览
     */
    public void pauseVideo() {
        if (camera != null) {
            camera.stopPreview();
        }
    }

    /**
     * 开始预览
     */
    public void startVideo() {
        if (camera != null) {
            camera.startPreview();
        }
    }

    /**
     * 隐藏并移除所有子View
     */
    public void releaseVideoView() {
        destoryView();
        ScreenDeskUtil.unRegisterDeskStateCallBack();
    }

    @Override
    public void onBackDesk(boolean isBackDesk) {
        if (isBackDesk) {
            destoryView();
        } else {
            createView();
            showVideoViewOnTop(false);
        }
    }

}
