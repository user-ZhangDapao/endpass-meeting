package com.sdcz.endpass.util;

import com.sdcz.endpass.R;
import com.inpor.nativeapi.adaptor.RoomUserInfo;

/**
 * @Description: 终端类型
 */
public enum Terminal {
    // 桌面终端(win mac)
    DESKTOP_CLIENT(RoomUserInfo.DESKTOP_CLIENT, R.mipmap.ul_ic_pc),
    DESKTOP_WIN(RoomUserInfo.DESKTOP_WIN, R.mipmap.ul_ic_pc), // 桌面终端(win)
    DESKTOP_MAC(RoomUserInfo.DESKTOP_MAC, R.mipmap.ul_ic_pc), // 桌面终端(mac)

    // 硬件终端
    HARDWARE_CLIENT(RoomUserInfo.HARDWARE_CLIENT, R.mipmap.ul_ic_pc),
    HARDWARE_X3(RoomUserInfo.HARDWARE_X3, R.mipmap.ul_ic_pc),
    HARDWARE_V5(RoomUserInfo.HARDWARE_V5, R.mipmap.ul_ic_pc),
    HARDWARE_X5(RoomUserInfo.HARDWARE_X5, R.mipmap.ul_ic_pc),

    // 移动端
    MOBILE_CLIENT(RoomUserInfo.MOBILE_CLIENT, R.mipmap.ul_ic_phone),
    MOBILE_IOS(RoomUserInfo.MOBILE_IOS, R.mipmap.ul_ic_phone),
    MOBILE_ANDROID(RoomUserInfo.MOBILE_ANDROID, R.mipmap.ul_ic_phone),
    MOBILE_ANDROID_TV(RoomUserInfo.MOBILE_ANDROID_TV, R.mipmap.ul_ic_phone),
    MOBILE_ANDROID_A2(RoomUserInfo.MOBILE_ANDROID_A2, R.mipmap.ul_ic_phone),
    MOBILE_ANDROID_D1(RoomUserInfo.MOBILE_ANDROID_D1, R.mipmap.ul_ic_phone),

    // 电话端
    TELEPHONE_CLIENT(RoomUserInfo.TELEPHONE_CLIENT, R.mipmap.ul_icon_call),
    HARDWARE_H323(RoomUserInfo.HARDWARE_H323, R.mipmap.ul_icon_call);

    private final int terminal;
    private final int logo;

    Terminal(int terminal, int logo) {
        this.terminal = terminal;
        this.logo = logo;
    }

    public int getLogo() {
        return logo;
    }

    /**
     * 获取终端类型的图标
     * @param terminal RoomUserInfo中的终端类型
     * @return res id
     */
    public static int getLogo(int terminal) {
        for (Terminal temp : Terminal.values()) {
            if (temp.terminal == terminal) {
                return temp.getLogo();
            }
        }
        // FIXME return a exits drawable id for default
        return R.mipmap.tb_mic;
    }
}
