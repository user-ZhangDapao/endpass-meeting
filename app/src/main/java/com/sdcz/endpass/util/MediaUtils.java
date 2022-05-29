package com.sdcz.endpass.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;

import java.io.File;

/**
 * 该工具类处理媒体不同的Uri
 */
public class MediaUtils {

    private static final String TAG = "MediaUtils";
    private static Uri takePhotoUri = null;

    /**
     * @return 失败返回null，成功返回图片真实路径
     * @function 获取图片真实路径
     * @Author Money
     */
    public static String getImagePath(Context context, Uri uri) {
        String path = null;
        if (uri == null) {
            return null;
        }
        boolean isKitkat = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT);
        if (isKitkat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                path = getPathFromExternalStorageDocument(uri);
            } else if (isDownloadDocument(uri)) {
                path = getPathForDownloadDocument(context, uri);
            } else if (isMediaDocument(uri)) {
                path = getPathFromMediaDocument(context, uri);
            }
        } else {
            String scheme = uri.getScheme();

            if (scheme.equalsIgnoreCase("content")) {
                if (isGooglePhotosUri(uri)) {
                    path = uri.getLastPathSegment();
                } else {
                    String[] projections = {Media.DATA};
                    path = getPathContentResolver(context, uri, projections, null, null, Media.DATA);
                }
            } else if (scheme.equalsIgnoreCase("file")) {
                path = uri.getPath();
            }
        }
        return path;
    }

    /**
     * @return 失败返回null，成功返回图片的绝对路径
     * @function 从存储类型的Uri获取真实的图片绝对路径
     * @Author Money
     */
    @SuppressLint("NewApi")
    private static String getPathFromExternalStorageDocument(Uri uri) {
        String path = null;

        String documentId = DocumentsContract.getDocumentId(uri);
        String[] split = documentId.split(":");
        String type = split[0];

        if ("primary".equalsIgnoreCase(type)) {
            path = Environment.getExternalStorageDirectory() + "/" + split[1];
        }

        return path;
    }

    /**
     * @return 失败返回null，成功返回图片的绝对路径
     * @function 从下载类型的Uri获取真实的图片绝对路径
     * @Author Money
     */
    @SuppressLint("NewApi")
    private static String getPathForDownloadDocument(Context context, Uri uri) {
        String path;

        String documentId = DocumentsContract.getDocumentId(uri);
        try {
            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                Long.parseLong(documentId));

            String[] projection = {Media.DATA};

            path = getPathContentResolver(context, contentUri, projection, null, null, Media.DATA);
        } catch (Exception exception) {
            path = "";
        }
        return path;
    }

    /**
     * @return 失败返回null，成功返回图片的绝对路径
     * @function 从媒体类型的Uri获取真实的图片绝对路径
     * @Author Money
     */
    @SuppressLint("NewApi")
    private static String getPathFromMediaDocument(Context context, Uri uri) {
        String path;

        String documentId = DocumentsContract.getDocumentId(uri);
        String[] split = documentId.split(":");
        String type = split[0];

        Uri contentUri = null;

        String projection = null;
        String selection = null;

        switch (type) {
            case "image":
                contentUri = Media.EXTERNAL_CONTENT_URI;
                projection = Media.DATA;
                selection = Media._ID + "=?";
                break;
            case "video":
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                projection = MediaStore.Video.Media.DATA;
                selection = MediaStore.Video.Media._ID + "=?";
                break;
            case "audio":
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                projection = MediaStore.Audio.Media.DATA;
                selection = MediaStore.Audio.Media._ID + "=?";
                break;
        }

        String[] projections = {projection};
        String[] selectionArgs = {split[1]};

        path = getPathContentResolver(context, contentUri, projections, selection, selectionArgs, projection);

        return path;
    }

    /**
     * @return 失败返回null，成功返回图片的绝对路径
     * @function 从内容提供者的Uri获取真实的图片绝对路径
     * @Author Money
     */
    private static String getPathContentResolver(Context context, Uri uri, String[] projections, String selection,
        String[] selectionArgs, String projection) {
        String path = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(uri, projections, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int index = cursor.getColumnIndexOrThrow(projection);
                    path = cursor.getString(index);

                    if (TextUtils.isEmpty(path)) {
                        continue;
                    }

                    cursor.close();
                    break;
                } while (cursor.moveToNext());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return path;
    }

    /**
     * @return true 是，false 不是
     * @function Uri是存储类型
     * @Author Money
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @return true 是，false 不是
     * @function Uri是下载类型
     * @Author Money
     */
    private static boolean isDownloadDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @return true 是，false 不是
     * @function Uri是媒体类型
     * @Author Money
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @return true 是，false 不是
     * @function Uri是谷歌相册类型
     * @Author Money
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * 获取拍照URI
     * @return URI
     */
    public static Uri getTakePhotoUri() {
        if (takePhotoUri != null) {
            return takePhotoUri;
        }
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        // 有些手机无法将拍照的照片写入应用的外部存储路径
        File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Pictures");
        if (!file.exists()) {
            file.mkdirs();
        }
        File photoFile = new File(file.getPath(), "temp_photo.jpg");
        takePhotoUri = Uri.fromFile(photoFile);
        return takePhotoUri;
    }
}
