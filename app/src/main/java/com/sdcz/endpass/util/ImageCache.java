package com.sdcz.endpass.util;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * @description     图片内存缓存
 */
public class ImageCache {

    /**
     * 内存缓存默认 10M
     */
    static final int MEM_CACHE_DEFAULT_SIZE = 10 * 1024 * 1024;

    /**
     * 一级内存缓存基于 LruCache
     */
    private static final LruCache<String, Bitmap> MEM_CACHE = new LruCache<String, Bitmap>(MEM_CACHE_DEFAULT_SIZE) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount();
        }
    };

    /**
     * 清除
     */
    public static void clear() {
        MEM_CACHE.evictAll();
    }

    /**
     * 从内存缓存中拿
     *
     * @param key 建
     */
    public static Bitmap getBitmapFromMem(String key) {
        return MEM_CACHE.get(key);
    }

    /**
     * 加入到内存缓存中
     *
     * @param key 建
     * @param bitmap 位图
     */
    public static void putBitmapToMem(String key, Bitmap bitmap) {
        MEM_CACHE.put(key, bitmap);
    }
}
