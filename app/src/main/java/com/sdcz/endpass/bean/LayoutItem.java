package com.sdcz.endpass.bean;

import com.comix.meeting.entities.LayoutType;
import com.inpor.nativeapi.adaptor.RoomWndState;

/**
 * @Description: 切换布局UI对应的Item
 */
public class LayoutItem {

    private final LayoutType layoutType;
    private final RoomWndState.SplitStyle splitStyle;
    private final int layoutLogoResId;   // 显示的Logo
    private final int layoutDescResId;   // 显示的布局名称
    private boolean selected = false;

    /**
     * 构造函数
     */
    public LayoutItem(LayoutType layoutType, RoomWndState.SplitStyle splitStyle, int logo, int desc) {
        this.layoutType = layoutType;
        this.splitStyle = splitStyle;
        this.layoutLogoResId = logo;
        this.layoutDescResId = desc;
    }

    public LayoutType getLayoutType() {
        return layoutType;
    }

    public RoomWndState.SplitStyle getSplitStyle() {
        return splitStyle;
    }

    public int getLayoutLogoResId() {
        return layoutLogoResId;
    }

    public int getLayoutDescResId() {
        return layoutDescResId;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}
