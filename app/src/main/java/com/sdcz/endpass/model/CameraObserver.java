package com.sdcz.endpass.model;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;

import com.blankj.utilcode.util.Utils;
import com.comix.meeting.entities.CameraOption;
import com.comix.meeting.listeners.CameraModelListener;
import com.sdcz.endpass.LiveDataBus;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.bean.CameraEventOnWrap;
import com.sdcz.endpass.bean.MeetingSettingsKey;
import com.sdcz.endpass.callback.OnSettingsChangedListener;
import com.sdcz.endpass.dialog.SimpleTipsDialog2;
import com.inpor.base.sdk.video.VideoManager;
import com.inpor.nativeapi.adaptor.Platform;

import org.greenrobot.eventbus.EventBus;

/**
 * @Description: 绑定MeetingActivity，同步Camera管理
 */
public class CameraObserver implements LifecycleObserver, OnSettingsChangedListener {
    private static final String TAG = "CameraModel";
    /**
     * 请求相机权限
     */
    public static final int PERMISSION_CAMERA = 60;
    /**
     * 后置Camera
     */
    private static final String REAR_CAMERA = "0";
    /**
     * 前置Camera
     */
    private static final String FRONT_CAMERA = "1";


    private final Context context;
    private VideoManager videomanager;
    private String currentCamera;
    private SimpleTipsDialog2 cameraPermissionDeniedDialog;
    private SimpleTipsDialog2 cameraUnknownDialog;
    private boolean init = false;
    private final int disableResId = R.mipmap.disable_camera;
    private boolean background = false;
    private final CameraModelListener cameraModelListener = new CameraModelListener() {

        @Override
        public void onCameraOpened() {
            int orientation = context.getResources().getConfiguration().orientation;
            Log.i(TAG, "orientation: " + orientation);
            videomanager.setOrientation(orientation);
            // 将保存在app的美颜，镜像参数设置到CameraModel

            videomanager.setBeautyLevel(AppCache.getInstance().getBeautyLevel());
            videomanager.setCameraVerFlip(false);
            if (currentCamera.equals(FRONT_CAMERA)) {
                videomanager.setCameraFlip(AppCache.getInstance().isHorFlip());
            } else {
                //videomanager.setCameraFlip(true);
                /*
                VideoParam vp = videomanager.get_video_param();
                vp.width = 1280;
                vp.heigth = 720;
                videomanager.direct_apply_param(vp);
                */
                //videomanager.change_captureType(3);

            }
            videomanager.startPreview();
        }

        @Override
        public void onCameraError(int error) {
            Log.e(TAG, "Camera error, code=" + error);
            showCameraErrorDialog();
        }
    };

    public CameraObserver() {
        this.context = Utils.getApp();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        background = false;
        openCamera();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        MeetingSettingsModel.getInstance().addListener(this);
        initAndOpenCamera();
        MutableLiveData<Integer> orientationLiveData = LiveDataBus.getInstance().
                getLiveData(LiveDataBus.KEY_ORIENTATION);
        orientationLiveData.observeForever(this::orientationChange);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        background = true;
        hideCameraErrorDialog();
        hideUnknownCameraErrorDialog();
        videomanager.releaseCamera();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        videomanager.removeCameraModelListener(cameraModelListener);
        MeetingSettingsModel.getInstance().removeListener(this);
        MutableLiveData<Integer> orientationLiveData = LiveDataBus.getInstance().
                getLiveData(LiveDataBus.KEY_ORIENTATION);
        orientationLiveData.removeObserver(this::orientationChange);
    }

    public void orientationChange(Integer orientation) {
        videomanager.setOrientation(orientation);
        if (AppCache.getInstance().isCameraEnabled() && !background) {
            videomanager.startPreview();
        }
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        if (Camera.getNumberOfCameras() < 2) {
            Log.w(TAG, "the camera device count is " + Camera.getNumberOfCameras());
            return;
        }
        if (REAR_CAMERA.equals(currentCamera)) {
            currentCamera = FRONT_CAMERA;
        } else {
            currentCamera = REAR_CAMERA;


        }
        videomanager.releaseCamera();
        videomanager.switchCamera(context, currentCamera, disableResId);
/*
        if( REAR_CAMERA == currentCamera ) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    //add by baodian for mode 0
                    //videomanager.setCameraFlip(false);
                    //videomanager.applyParam();

                }
            },100);


        }
*/
    }

    @Override
    public void onSettingsChanged(String key, Object value) {
        if (MeetingSettingsKey.KEY_HOR_FLIP.equals(key)) {
            videomanager.setCameraFlip((Boolean) value);
            videomanager.applyParam();
        } else if (MeetingSettingsKey.KEY_BEAUTY_LEVEL.equals(key)) {
            videomanager.setBeautyLevel((Integer) value);
            videomanager.applyParam();
        }
    }

    /**
     * 入会初始化会议室时优先调用(创建布局前？)
     */
    private void initAndOpenCamera() {
        videomanager = SdkUtil.getVideoManager();
        CameraOption option = CameraOption.builder()
                .platform(Platform.ANDROID)
                .build();
        videomanager.setCameraOption(option);
        videomanager.addCameraModelListener(cameraModelListener);

        // Camera API：前置1，后置0
        if (Camera.getNumberOfCameras() > 1) {
            currentCamera = FRONT_CAMERA;
        } else {
            currentCamera = REAR_CAMERA;
        }
        openCamera();
    }

    //add by baodian in 2022-03-07
    public void reopen_camera()
    {
        videomanager.releaseCamera();
        videomanager.switchCamera(context, currentCamera, disableResId);
    }

    /**
     * 同Activity的onActivityResult
     */
    public void onActivityResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSION_CAMERA || grantResults.length == 0) {
            CameraEventOnWrap wrap = new CameraEventOnWrap();
            EventBus.getDefault().post(wrap);
            return;
        }
        CameraEventOnWrap wrap = new CameraEventOnWrap();
        int cameraPermission = grantResults[0];
        if (PackageManager.PERMISSION_GRANTED != cameraPermission) {
            wrap.flag = false;
        }
        else {
            wrap.flag = true;
            videomanager.releaseCamera();
            videomanager.switchCamera(context, currentCamera, disableResId);
        }

        if( requestCode == 59 )
        {
            wrap.description = "updateBottomMenuCameraIconState";
        }

        EventBus.getDefault().post(wrap);
    }

    private void openCamera() {
        if (init) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PackageManager.PERMISSION_GRANTED
                    != context.checkSelfPermission(Manifest.permission.CAMERA)) {
                Log.e(TAG, "没有相机权限，无法启动相机");
            } else {
                videomanager.releaseCamera();
                videomanager.switchCamera(context, currentCamera, disableResId);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PackageManager.PERMISSION_GRANTED
                    != context.checkSelfPermission(Manifest.permission.CAMERA)) {
                videomanager.applyParam();
                showCameraErrorDialog();
            }
        }
        init = true;
    }

    private void showCameraErrorDialog() {
        /*
        if (cameraPermissionDeniedDialog != null) {
            cameraPermissionDeniedDialog.dismiss();
        }
        SimpleTipsDialog2.Builder builder = new SimpleTipsDialog2.Builder();
        cameraPermissionDeniedDialog = builder.title(context.getString(R.string.meetingui_remind))
                .btnLeft(context.getString(R.string.meetingui_cancel))
                .btnRight(context.getString(R.string.meetingui_confirm))
                .tips(context.getString(R.string.meetingui_camera_permission_denied))
                .cancelOnTouchOutside(false)
                .interactionListener(new SimpleTipsDialog2.InteractionListener() {
                    @Override
                    public void onLeftBtnClick(DialogFragment dialog) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onRightBtnClick(DialogFragment dialog) {
                        ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(),
                                new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
                        dialog.dismiss();
                    }
                }).build();
        cameraPermissionDeniedDialog.show(
                ((FragmentActivity) ActivityUtils.getTopActivity()).getSupportFragmentManager(), "camera_error_dialog");*/
    }

    private void hideCameraErrorDialog() {
        if (cameraPermissionDeniedDialog != null) {
            cameraPermissionDeniedDialog.dismiss();
            cameraPermissionDeniedDialog = null;
        }
    }

    private void hideUnknownCameraErrorDialog() {
        if (cameraUnknownDialog != null) {
            cameraUnknownDialog.dismiss();
            cameraUnknownDialog = null;
        }
    }
}
