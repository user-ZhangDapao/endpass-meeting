package com.sdcz.endpass.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.Util;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.DemoApp;

/**
 * 图片加载工具类
 * 利用Glide三方依赖
 * Created by Gwt on 2017/11/14.
 */

public class GlideUtils {

    private static final String URL_TAG = "http";
    private static final String URL_LABEL = "https";
    private static Bitmap bitmap;
    private static String baseUrl = Constants.HttpKey.BASE_URL + "/" + Constants.HttpKey.IMAGE_BASE_URL;


    /**
     * Glide显示正常图片
     *
     * @param context
     * @param imageView
     * @param url
     */
    public static void showImage(Context context, ImageView imageView, String url, int defaulRes) {

        if (detectImageUrl(url)) {
            Glide.with(context == null ? DemoApp.getContext() : context).load(url).placeholder(defaulRes).into(imageView);
        } else {
            Glide.with(context == null ? DemoApp.getContext() : context).load(baseUrl + url).placeholder(defaulRes).into(imageView);
        }
    }

    /**
     * Glide显示正常图片
     *
     * @param context
     * @param imageView
     * @param url
     */
    public static void showImageNone(Context context, ImageView imageView, String url, int defaulRes) {

        if (detectImageUrl(url)) {
            Glide.with(context == null ? DemoApp.getContext() : context).load(url).into(imageView);
        } else {
            Glide.with(context == null ? DemoApp.getContext() : context).load(baseUrl + url).into(imageView);
        }
    }

    /**
     * Glide显示正常图片
     *
     * @param context
     * @param imageView
     * @param url
     */
    public static void showImageScale(Context context, final ImageView imageView, String url, int defaulRes) {

        if (detectImageUrl(url)) {
            Glide.with(context == null ? DemoApp.getContext() : context)
                    .load(url)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            if (imageView == null) {
                                return false;
                            }
                            if (imageView.getScaleType() != ImageView.ScaleType.FIT_CENTER) {
                                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            }
                            ViewGroup.LayoutParams params = imageView.getLayoutParams();
                            int vw = imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
                            float scale = (float) vw / (float) resource.getIntrinsicWidth();
                            int vh = Math.round(resource.getIntrinsicHeight() * scale);
                            params.height = vh + imageView.getPaddingTop() + imageView.getPaddingBottom();
                            imageView.setLayoutParams(params);
                            return false;
                        }
                    })
                    .placeholder(defaulRes)
                    .into(imageView);
        } else {
            Glide.with(context == null ? DemoApp.getContext() : context).load(baseUrl + url).placeholder(defaulRes).into(imageView);
        }
    }

    /**
     * Glide加载圆形图片
     *
     * @param context
     * @param imageView
     * @param url
     */
    public static void showCircleImage(Context context, ImageView imageView, String url, int defaultRes) {
        if (detectImageUrl(url)) {
            Glide.with(context == null ? DemoApp.getContext() : context)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(defaultRes)
                    .transform(new GlideCircleTransform(context))
                    .into(imageView);
        } else {
            Glide.with(context == null ? DemoApp.getContext() : context)
                    .load( baseUrl + url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(defaultRes)
                    .transform(new GlideCircleTransform(context))
                    .into(imageView);
        }
    }


    /**
     * Glide加载Uri
     *
     * @param context
     * @param uri
     * @param imageView
     * @param defaultRes
     * @param errorRes
     * @param thumbRate
     */
    public static void showUriImage(Context context, Uri uri, ImageView imageView, int defaultRes, int errorRes, float thumbRate) {
        Glide.with(context == null ? DemoApp.getContext() : context)
                .load(uri)
                .centerCrop()
                .thumbnail(thumbRate)
                .placeholder(defaultRes)
                .error(errorRes)
                .into(imageView);
    }


    /**
     * Glide加载园角图片
     *
     * @param context
     * @param imageView
     * @param url
     */
    public static void showRoundCornerImage(Context context, ImageView imageView, String url, int defaultRes) {
        if (detectImageUrl(url)) {
            Glide.with(context == null ? DemoApp.getContext() : context)
                    .load(url)
                    .transform(new CenterCrop(context), new GlideRoundImage(context))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(defaultRes)
                    .crossFade()
                    .into(imageView);
        } else {
            Glide.with(context == null ? DemoApp.getContext() : context)
                    .load(baseUrl + url)
                    .transform(new CenterCrop(context), new GlideRoundImage(context))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(defaultRes)
                    .crossFade()
                    .into(imageView);
        }
    }

    /**
     * Glide加载黑白园角图片
     *
     * @param context
     * @param imageView
     * @param url
     */
//    public static void showRoundCornerBlackImage(Context context, ImageView imageView, String url, int defaultRes) {
//        if (detectImageUrl(url)) {
//            Glide.with(context == null ? DemoApp.getContext() : context)
//                    .load(url)
//                    .transform(new BlackWhiteTransformation(context), new CenterCrop(context), new GlideRoundImage(context))
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .placeholder(defaultRes)
//                    .crossFade()
//                    .into(imageView);
//        } else {
//            Glide.with(context == null ? DemoApp.getContext() : context)
//                    .load( baseUrl + url)
//                    .transform(new BlackWhiteTransformation(context),new CenterCrop(context), new GlideRoundImage(context))
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .placeholder(defaultRes)
//                    .crossFade()
//                    .into(imageView);
//        }
//    }

    /**
     * Glide加载园角图片
     *
     * @param context
     * @param imageView
     * @param url
     */
    public static void showRoundCornerImage(Context context, ImageView imageView, String url) {
        if (detectImageUrl(url)) {
            Glide.with(context == null ? DemoApp.getContext() : context)
                    .load(url)
                    .transform(new CenterCrop(context), new GlideRoundImage(context))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .into(imageView);
        } else {
            Glide.with(context == null ? DemoApp.getContext() : context)
                    .load(baseUrl + url)
                    .transform(new CenterCrop(context), new GlideRoundImage(context))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .into(imageView);
        }
    }


    /**
     * Glide加载园角图片
     *
     * @param context
     * @param imageView
     * @param url
     */
    public static void showRoundCornerImage(Context context, ImageView imageView, Uri url) {
        Glide.with(context == null ? DemoApp.getContext() : context)
                .load(url)
                .transform(new CenterCrop(context), new GlideRoundImage(context))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .into(imageView);
    }

    /**
     * Glide加载园角图片
     *
     * @param context
     * @param imageView
     * @param url
     */
    public static void showRoundCornerImage2(Context context, ImageView imageView, String url) {
        Glide.with(context == null ? DemoApp.getContext() : context)
                .load(url)
                .transform(new CenterCrop(context), new GlideRoundImage(context))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .into(imageView);
    }

    /**
     * Glide将网络图片转换为bitmap
     *
     * @param context
     * @param url
     * @return
     */
    public static Bitmap getImageToBitmap(Context context, String url, int defaultRes) {

        if (detectImageUrl(url)) {
            Glide.with(context == null ? DemoApp.getContext() : context)
                    .load(url)
                    .asBitmap()
                    .placeholder(defaultRes)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            bitmap = resource;
                        }
                    });
            return bitmap == null ? null : bitmap;
        } else {
            Glide.with(context == null ? DemoApp.getContext() : context)
                    .load(baseUrl + url)
                    .asBitmap()
                    .placeholder(defaultRes)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            bitmap = resource;
                        }
                    });
            return bitmap == null ? null : bitmap;
        }
    }

    /**
     * Glide加载本地资源文件
     *
     * @param context
     * @param imageView
     * @param resources
     */
    public static void showLocalImage(Context context, ImageView imageView, int resources, int defaultRes) {
        Glide.with(context == null ? DemoApp.getContext() : context).load(resources).placeholder(defaultRes).into(imageView);
    }

    /**
     * 销毁Glide加载（在Activity的OnDestory()方法中调用）
     *
     * @param activity
     */
    public static void destoryGlide(Activity activity) {
        if (Util.isOnMainThread() && !activity.isFinishing()) {
            Glide.with(activity).pauseRequests();
        }
    }

    /**
     * 检查图片的url是否为全路径或者相对路径
     * 全路径返回true，相对路径返回false
     *
     * @param url
     * @return
     */
    private static boolean detectImageUrl(String url) {
        if (url != null) {
            if (url.startsWith(URL_TAG) || url.startsWith(URL_LABEL)) {
                return true;
            }
        }
        return false;
    }
}
