package com.sdcz.endpass.constant;


/**
 *  常量类
 */
public class Constant {
    /**
     * 使用照相机拍照获取图片
     */
    public static final int SELECT_PIC_BY_TACK_PHOTO = 10001;
    /**
     * 使用相册中的图片
     */
    public static final int SELECT_PIC_BY_PICK_PHOTO = 10002;
    public static final int SCREEN_SHARE_REQUEST_CODE = 10003;
    public static final int SELECT_VIDEO_RECORD = 10011;

    /**
     * 软键盘参数常量
     */
    public static class SoftKeyboardParams {
        //竖屏状态下软键盘的高度
        public static String VERTICAL_SOFT_KEYBOARD_Y_KEY = "vertical_soft_keyboard_height_key";
        //横屏状态下软键盘的高度
        public static String LANDSCAPE_SOFT_KEYBOARD_HEIGHT_KEY = "landscape_soft_keyboard_height_key";
        public static String LANDSCAPE_SOFT_KEYBOARD_Y_KEY = "landscape_soft_keyboard_Y_key";
    }

}
