package com.sdcz.endpass.util;

import android.os.Build;

/**
 * @Description: 判断手机厂商
 */
public class BrandUtil {
    /**
     * 手机品牌
     */
    // 小米
    public static final String PHONE_XIAOMI = "xiaomi";
    // 华为
    public static final String PHONE_HUAWEI1 = "Huawei";
    // 华为
    public static final String PHONE_HUAWEI2 = "HUAWEI";
    // 华为
    public static final String PHONE_HUAWEI3 = "HONOR";
    // 魅族
    public static final String PHONE_MEIZU = "Meizu";
    // 索尼
    public static final String PHONE_SONY = "sony";
    // 三星
    public static final String PHONE_SAMSUNG = "samsung";
    // LG
    public static final String PHONE_LG = "lg";
    // HTC
    public static final String PHONE_HTC = "htc";
    // NOVA
    public static final String PHONE_NOVA = "nova";
    // OPPO
    public static final String PHONE_OPPO = "OPPO";
    // 乐视
    public static final String PHONE_LeMobile = "LeMobile";
    // 联想
    public static final String PHONE_LENOVO = "lenovo";
    /**
     * @Description: 是否是华为
     * @Author: xingwt
     * @return:
     * @Parame:
     * @CreateDate: 2021/3/31 11:20
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/31 11:20
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public static boolean checkoutHW() {
        String brand = Build.BRAND;
        return (PHONE_HUAWEI1.equals(brand)
                || PHONE_HUAWEI2.equals(brand)
                || PHONE_HUAWEI3.equals(brand));
    }
}
