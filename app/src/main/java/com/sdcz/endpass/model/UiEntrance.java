package com.sdcz.endpass.model;

import com.sdcz.endpass.callback.IMenuHelper;

/**
 *  会中一些界面回调事件分发
 */

public class UiEntrance {
    private static volatile UiEntrance instance;
    private IMenuHelper menuHelper;

    public static UiEntrance getInstance() {
        if (instance == null) {
            synchronized (UiEntrance.class) {
                if (instance == null) {
                    instance = new UiEntrance();
                }
            }
        }
        return instance;
    }

    private UiEntrance() {
    }

    public void setMenuHelper(IMenuHelper menuHelper) {
        this.menuHelper = menuHelper;
    }

    public void notify2MenuHelper() {
        if (menuHelper != null) {
            menuHelper.showOrHideTopBottomMenu();
        }
    }
}
