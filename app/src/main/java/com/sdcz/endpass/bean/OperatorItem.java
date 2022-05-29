package com.sdcz.endpass.bean;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Description: 管理员操作参会人的可操作项
 */
public class OperatorItem {
    public static final int NICKNAME = 1;
    public static final int HOST = 2;
    public static final int KECK = 3;
    public static final int MANAGER = 4;
    public static final int WBMARK = 5;

    @IntDef({OperatorItem.NICKNAME, OperatorItem.HOST, OperatorItem.KECK, OperatorItem.MANAGER,OperatorItem.WBMARK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ItemType { }

    private final int type;
    private int logo;
    private int desc;
    private boolean selected;
    private final Runnable action;

    /**
     * 构造函数
     */
    public OperatorItem(@ItemType int type, @DrawableRes int logo, @StringRes int desc, Runnable action) {
        this(type, logo, desc, false, action);
    }

    /**
     * 构造函数
     */
    public OperatorItem(int type, @DrawableRes int logo,
                        @StringRes int desc, boolean selected, Runnable action) {
        this.type = type;
        this.logo = logo;
        this.desc = desc;
        this.selected = selected;
        this.action = action;
    }

    public int getType() {
        return type;
    }

    public int getLogo() {
        return logo;
    }

    public int getDesc() {
        return desc;
    }

    public boolean isSelected() {
        return selected;
    }

    public Runnable getAction() {
        return action;
    }

    public void setLogo(int logo) {
        this.logo = logo;
    }

    public void setDesc(int desc) {
        this.desc = desc;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
