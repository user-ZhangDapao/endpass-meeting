package com.sdcz.endpass.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.sdcz.endpass.bean.BaseShareBean;
import com.sdcz.endpass.bean.MediaShareBean;
import com.sdcz.endpass.model.MediaShareModel;
import com.sdcz.endpass.model.ShareBeanManager;
import com.inpor.nativeapi.adaptor.RoomWndState;
import com.inpor.nativeapi.interfaces.VideoRenderNotify;

import java.util.List;

/**
 * Created by liqk on 2017/10/31.
 */
public class MediaShareView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    boolean isAudio = true;

    Integer audioResourceId = null;
    Integer videoResourceId = null;

    VideoRenderNotify videoRenderNotify = null;

    public MediaShareView(Context context) {
        this(context, null, null);
    }

    /**
     * 构造函数
     */
    public MediaShareView(Context context, Integer audio, Integer video) {
        this(context, audio, video, null);
    }

    /**
     * 构造函数
     */
    public MediaShareView(Context context, Integer audio, Integer video, VideoRenderNotify videoRenderNotify) {
        super(context);

        audioResourceId = audio;
        videoResourceId = video;
        this.videoRenderNotify = videoRenderNotify;

        getHolder().addCallback(this);
        setZOrderMediaOverlay(true);
        setOnTouchListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (videoRenderNotify != null) {
            MediaShareModel.getInstance().changeVideoSurface(this, videoRenderNotify);
        }
        setDefaultPicture();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        setDefaultPicture();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        return false;
    }

    public void setIsAudio(boolean is) {
        isAudio = is;
    }

    public void setVideoRenderNotify(VideoRenderNotify notify) {
        videoRenderNotify = notify;
    }

    public void setDefaultPicture() {
        setDefaultPicture(true);
    }

    /**
     * 刷新媒体共享默认图
     */
    public void setDefaultPicture(boolean isShowPicture) {
        Bitmap bitmap = null;
        Canvas canvas = null;
        Integer resourceId = null;

        List<BaseShareBean> baseShareBeans = ShareBeanManager.getByType(RoomWndState.DataType.DATA_TYPE_MEDIASHARE);
        if (baseShareBeans == null || baseShareBeans.size() <= 0) {
            return;
        }
        MediaShareBean mediaShareBean = (MediaShareBean)baseShareBeans.get(0);
        if (mediaShareBean.isOpeningVideo()) {
            return;
        }

        try {
            if (isAudio) {
                resourceId = audioResourceId;
            } else {
                resourceId = videoResourceId;
            }
            if (resourceId == null || getHeight() == 0 || getWidth() == 0) {
                return;
            }
            bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
            canvas = getHolder().lockCanvas();
            Log.i("MediaShareView", "canvas lock!!! " + canvas);
            if (canvas == null || bitmap == null) {
                return;
            }

            canvas.drawColor(Color.BLACK);
            if (!isShowPicture) {
                return;
            }

            Matrix matrix = new Matrix();
            matrix.set(getMatrix());
            float scaleX = canvas.getWidth() / (float) bitmap.getWidth();
            float scaleY = canvas.getHeight() / (float) bitmap.getHeight();
            float scale = 1;
            scale = scaleX > scaleY ? scaleY : scaleX;
            matrix.postScale(scale, scale);
            int offsetX = (int) (canvas.getWidth() - bitmap.getWidth() * scale) / 2;
            int offsetY = (int) (canvas.getHeight() - bitmap.getHeight() * scale) / 2;
            matrix.postTranslate(offsetX, offsetY);
            canvas.drawBitmap(bitmap, matrix, new Paint());
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.i("MediaShareView", "canvas exception!!!");
        } finally {
            if (canvas != null) {
                try {
                    getHolder().unlockCanvasAndPost(canvas);
                } catch (Exception exception) {
                    Log.e("MediaShareView", exception.getMessage());
                }
            }
        }
    }
}
