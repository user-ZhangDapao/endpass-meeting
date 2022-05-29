package com.sdcz.endpass.util;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class PermissionsPageUtils {
    /**
     * 权限请求页适配，不同手机系统跳转到不同的权限请求页
     */

    private static final String TAG = "PermissionPageManager";
    //自己的项目包名
    private final String packageName;

    private static final String MIUI_ACTION = "miui.intent.action.APP_PERM_EDITOR";
    private static final String MIUI_VERSION = "ro.miui.ui.version.name";
    private static final String ANDROID_SETTING = "com.android.settings";
    private static final String SONY_SETTING = "com.sonymobile.cta";
    private static final String HUAWEI_SETTING = "com.huawei.systemmanager";
    private static final String MIUI_SETTING = "com.miui.securitycenter";
    private static final String VIVO_SETTING = "com.vivo.safecenter";
    private static final String OPPO_SETTING = "com.coloros.safecenter";
    private static final String MEIZU_SETTING = "com.meizu.safe.security.SHOW_APPSEC";
    private static final String COOLPAD_SETTING = "com.yulong.android.security:remote";
    private static final int REQUEST_CODE = 1000;

    /**
     * 构造函数
     */
    public PermissionsPageUtils( ) {
        packageName = Utils.getApp().getPackageName();
    }

    /**
     * 根据不同的系统跳转到不同的设置页面，未定义的跳转到Andorid原生设置页
     */
    public void jumpPermissionPage() {
        String name = Build.MANUFACTURER;
        switch (name) {
            case "HUAWEI":
                goHuaWeiMainager();
                break;
            case "vivo":
                goIntentSetting();
                break;
            case "OPPO":
                goOppoMainager();
                break;
            case "Coolpad":
                goCoolpadMainager();
                break;
            case "Meizu":
                goMeizuMainager();
                break;
            case "Xiaomi":
                goXiaoMiMainager();
                break;
            case "samsung":
                goSangXinMainager();
                break;
            case "Sony":
                goSonyMainager();
                break;
            case "LG":
                goLGMainager();
                break;
            default:
                goIntentSetting();
                break;
        }
    }

    private void goLGMainager() {
        try {
            Intent intent = new Intent(packageName);
            ComponentName comp = new ComponentName(ANDROID_SETTING, ANDROID_SETTING
                    + ".Settings$AccessLockSummaryActivity");
            intent.setComponent(comp);
            ActivityUtils.getTopActivity().startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception exception) {
            ToastUtils.showShort("跳转失败");
            exception.printStackTrace();
            goIntentSetting();
        }
    }

    private void goSonyMainager() {
        try {
            Intent intent = new Intent(packageName);
            ComponentName comp = new ComponentName(SONY_SETTING, SONY_SETTING + ".SomcCTAMainActivity");
            intent.setComponent(comp);
            ActivityUtils.getTopActivity().startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception exception) {
            ToastUtils.showShort("跳转失败");
            exception.printStackTrace();
            goIntentSetting();
        }
    }

    private void goHuaWeiMainager() {
        try {
            Intent intent = new Intent(packageName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName(HUAWEI_SETTING, "com.huawei.permissionmanager.ui"
                    + ".MainActivity");
            intent.setComponent(comp);
            ActivityUtils.getTopActivity().startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception exception) {
            ToastUtils.showShort("跳转失败");
            exception.printStackTrace();
            goIntentSetting();
        }
    }

    private static String getMiuiVersion() {
        String line;
        BufferedReader input = null;
        try {
            Process process = Runtime.getRuntime().exec("getprop " + MIUI_VERSION);
            input = new BufferedReader(
                    new InputStreamReader(process.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return line;
    }

    private void goXiaoMiMainager() {
        String rom = getMiuiVersion();
        Log.e(TAG, "goMiaoMiMainager --- rom : " + rom);
        Intent intent = new Intent();
        if ("V6".equals(rom) || "V7".equals(rom)) {
            intent.setAction(MIUI_ACTION);
            intent.setClassName(MIUI_SETTING, "com.miui.permcenter.permissions"
                    + ".AppPermissionsEditorActivity");
            intent.putExtra("extra_pkgname", packageName);
        } else if ("V8".equals(rom) || "V9".equals(rom)) {
            intent.setAction(MIUI_ACTION);
            intent.setClassName(MIUI_SETTING, "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", packageName);
        } else {
            goIntentSetting();
            return;
        }
        try {
            ActivityUtils.getTopActivity().startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException localActivityNotFoundException) {
            localActivityNotFoundException.printStackTrace();
            goIntentSetting();
        }

    }

    private void goMeizuMainager() {
        try {
            Intent intent = new Intent(MEIZU_SETTING);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", packageName);
            ActivityUtils.getTopActivity().startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException localActivityNotFoundException) {
            localActivityNotFoundException.printStackTrace();
            goIntentSetting();
        }
    }

    private void goSangXinMainager() {
        //三星4.3可以直接跳转
        goIntentSetting();
    }

    private void goIntentSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", ActivityUtils.getTopActivity().getPackageName(), null);
        intent.setData(uri);
        try {
            ActivityUtils.getTopActivity().startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void goOppoMainager() {
        doStartApplicationWithPackageName(OPPO_SETTING);
    }

    /**
     * doStartApplicationWithPackageName("com.yulong.android.security:remote")
     * 和Intent open = getPackageManager().getLaunchIntentForPackage("com.yulong.android
     * .security:remote");
     * startActivity(open);
     * 本质上没有什么区别，通过Intent open...打开比调用doStartApplicationWithPackageName方法更快，也是android本身提供的方法
     */
    private void goCoolpadMainager() {
        doStartApplicationWithPackageName(COOLPAD_SETTING);
    }



    /**
     * 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
     *
     * @param packagename 包名
     */
    private void doStartApplicationWithPackageName(String packagename) {
        PackageInfo packageinfo = null;
        try {
            packageinfo = ActivityUtils.getTopActivity().getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException exception) {
            exception.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);
        List<ResolveInfo> resolveinfoList = ActivityUtils.getTopActivity().getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        Log.e(TAG, "resolveinfoList" + resolveinfoList.size());
        for (int i = 0; i < resolveinfoList.size(); i++) {
            Log.e(TAG, resolveinfoList.get(i).activityInfo.packageName + resolveinfoList.get(i)
                    .activityInfo.name);
        }
        try {
            if (resolveinfoList.iterator().hasNext()) {
                ResolveInfo resolveinfo = resolveinfoList.iterator().next();
                String name = resolveinfo.activityInfo.packageName;
                String className = resolveinfo.activityInfo.name;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName(name, className);
                intent.setComponent(cn);
                ActivityUtils.getTopActivity().startActivityForResult(intent, REQUEST_CODE);
            } else {
                goIntentSetting();
            }
        } catch (ActivityNotFoundException localActivityNotFoundException) {
            localActivityNotFoundException.printStackTrace();
            goIntentSetting();
        }

    }
}
