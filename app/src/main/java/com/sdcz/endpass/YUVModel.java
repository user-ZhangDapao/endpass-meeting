package com.sdcz.endpass;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.utils.BitmapUtils;
import com.inpor.base.sdk.video.VideoManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class YUVModel /*implements YUVUtil.notify_send_data*/{

    public boolean goon = false;
    public boolean disable_goon = false;

    int w = 0;
    int h = 0;

    public static void saveMyBitmap(Bitmap mBitmap) throws IOException {
        //File f = new File("/sdcard/test"  + ".png");
        File f = new File(Environment.getExternalStorageDirectory(), "test.png");
        f.createNewFile();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeBytesToFile(byte[] bs) throws IOException {
        StringBuilder sb = new StringBuilder();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String now = sdf.format(new Date());
        sb.append("TIME:").append(now);
        String s = "69 00 01 03 AA 16";
        //byte[] bs = s.getBytes();
        OutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory()+"/test.data");
        InputStream is = new ByteArrayInputStream(bs);
        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = is.read(buff)) != -1) {
            out.write(buff, 0, len);
        }
        is.close();
        out.close();
    }

    static float scaleWidth = 0;
    static float scaleHeight = 0;

    public static Bitmap scaleBitmap(Bitmap bitmap,float w,float h){
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        float x = 0,y = 0;
        scaleWidth = width;
        scaleHeight = height;
        Bitmap newbmp;
        //Log.e("gacmy","width:"+width+" height:"+height);
        if(w > h){//比例宽度大于高度的情况
            float scale = w/h;
            float tempH = width/scale;
            if(height > tempH){//
                x = 0;
                y=(height-tempH)/2;
                scaleWidth = width;
                scaleHeight = tempH;
            }else{
                scaleWidth = height*scale;
                x = (width - scaleWidth)/2;
                y= 0;
            }
            Log.e("gacmy","scale:"+scale+" scaleWidth:"+scaleWidth+" scaleHeight:"+scaleHeight);
        }else if(w < h){//比例宽度小于高度的情况
            float scale = h/w;
            float tempW = height/scale;
            if(width > tempW){
                y = 0;
                x = (width -tempW)/2;
                scaleWidth = tempW;
                scaleHeight = height;
            }else{
                scaleHeight = width*scale;
                y = (height - scaleHeight)/2;
                x = 0;
                scaleWidth = width;
            }

        }else{//比例宽高相等的情况
            if(width > height){
                x= (width-height)/2;
                y = 0;
                scaleHeight = height;
                scaleWidth = height;
            }else {
                y=(height - width)/2;
                x = 0;
                scaleHeight = width;
                scaleWidth = width;
            }
        }
        try {
            newbmp = Bitmap.createBitmap(bitmap, (int) x, (int) y, (int) scaleWidth, (int) scaleHeight, null, false);// createBitmap()方法中定义的参数x+width要小于或等于bitmap.getWidth()，y+height要小于或等于bitmap.getHeight()
            //bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return newbmp;
    }

    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees,
                    (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();  //Android开发网再次提示Bitmap操作完应该显示的释放
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // Android123建议大家如何出现了内存不足异常，最好return 原始的bitmap对象。.
            }
        }
        return b;
    }

    static List<byte[]> bms = new ArrayList();
    static YUVModel instance;
    public static YUVModel getInstance(Context context)
    {
        if( instance == null )
        {
            ToastUtils.showShort("视频文件初始化开始");
            instance = new YUVModel();
            //instance.disableData = instance.getDisableYUVData(width,height);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

                        File file = new File(Environment.getExternalStorageDirectory(), "test.mp4");

                        try {
                            if(file.exists()) {
                                metadataRetriever.setDataSource(file.getAbsolutePath());//设置数据源为该文件对象指定的绝对路径
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
/*
                        Uri parse = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.test);
                        metadataRetriever.setDataSource(context,parse);
*/
                        String duration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        int durationMs = Integer.parseInt(duration);

                        //每秒取一次
                        for (int i = 0; i < durationMs; i += 1000) {

                            Bitmap frameAtIndex = metadataRetriever.getFrameAtTime(i * 1000);
                            //frameAtIndex = rotate(frameAtIndex,180);
                            instance.w = frameAtIndex.getWidth();
                            instance.h = frameAtIndex.getHeight();
                            byte[] datas = BitmapUtils.bitmapToNV21(frameAtIndex.getWidth(),frameAtIndex.getHeight(), frameAtIndex);
                            bms.add(datas);
                        }
                        metadataRetriever.release();

                        ToastUtils.showShort("视频文件初始化成功，可以开启本地YUV输入！");
                    }
                    catch (Exception e)
                    {
                        int y=4;
                        ToastUtils.showShort("视频文件初始化失败！");
                    }
                    instance.can_send_data();
                }
            }).start();

        }
        return instance;
    }

    public void can_send_data() {
        if( goon == false ) {
            goon = true;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    int play_index = 0;
                    while( goon )
                    {
                        play_index = play_index% bms.size();
                        byte[] data = bms.get(play_index++);
                        try {
                            Thread.sleep(100);
                        }
                        catch (Exception e)
                        {

                        }

                        VideoManager videomanager = SdkUtil.getVideoManager();
/*
                        VideoParam videoParam = videomanager.get_video_param();
                        if( videoParam != null && videoParam.rotationAngle != 180) {
                            videoParam.rotationAngle = 180;
                            videomanager.direct_apply_param(videoParam);
                        }
*/
                        videomanager.writeCustomYuvVideoData(data,w,h);


                    }
                }
            }).start();
        }
    }



}
