package com.sdcz.endpass.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Money on 2016/12/5.
 */
public class PermissionUtils {
    private static final String TAG = "PermissionUtils";

    /**
     * 获取请求会中权限
     */
    public static List<String> requestMeetingPermission() {
        if (VERSION.SDK_INT < VERSION_CODES.M) {
            return null;
        }
        List<String> permissionList = new ArrayList<>();
        // 在某些机型上请求权限可能会抛出异常，进行捕捉
        try {

            if (!checkPermissions(Utils.getApp(), Manifest.permission.CAMERA)) {
                permissionList.add(Manifest.permission.CAMERA);
            }

            //2
            if (!checkPermissions(Utils.getApp(), Manifest.permission.RECORD_AUDIO)) {
                permissionList.add(Manifest.permission.RECORD_AUDIO);
            }

        } catch (Exception except) {
            Log.e(TAG, except.toString());
        }

        return permissionList;
    }


    /**
     * 获取请求会前权限
     */
    public static List<String> requestBeforeMeetingPermission(Context context) {

        if (VERSION.SDK_INT < VERSION_CODES.M) {
            return null;
        }
        List<String> permissionList = new ArrayList<>();

        // 在某些机型上请求权限可能会抛出异常，进行捕捉
        try {
            if (!checkPermissions(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            // 存储
            if (!checkPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

        } catch (Exception except) {
            Log.e(TAG, except.toString());
        }

        return permissionList;
    }


    /**
     * 请求权限列表中的属性值
     */
    public static void requestPermission(List<String> permissionList, int requestCode) {
        if (permissionList.size() >= 1) {
            int listSize = permissionList.size();
            String[] permissions = permissionList.toArray(new String[listSize]);
            ActivityCompat.requestPermissions(ActivityUtils.getTopActivity(), permissions, requestCode);
        }
    }

    public static void requestPermission(Activity activity, List<String> permissionList) {
        if (permissionList.size() >= 1) {
            int listSize = permissionList.size();
            String[] permissions = permissionList.toArray(new String[listSize]);
            ActivityCompat.requestPermissions(activity, permissions, 1);
        }
    }
    public static void requestPermission(Fragment fragment, List<String> permissionList) {
        if (permissionList.size() >= 1) {
            int listSize = permissionList.size();
            String[] permissions = permissionList.toArray(new String[listSize]);
            fragment.requestPermissions(permissions,1);
        }
    }
    /**
     * 检查权限是否开启
     *
     * @param context    Context
     * @param permission 权限
     * @return true 有权限；false 没有权限
     */
    public static boolean checkPermissions(final Context context, final String permission) {
        if (context == null || TextUtils.isEmpty(permission)) {
            throw new NullPointerException("传入参数不能为空");
        }

        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}