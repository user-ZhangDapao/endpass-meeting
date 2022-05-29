package com.sdcz.endpass.base;

import android.content.Context;
import android.view.View;

import com.comix.meeting.entities.BaseShareBean;

/**
 *   分享时（接收、发起）数据、状态、一些View的基类
 */
public interface IDataContainer {

    void init(Context context);

    void release();

    void setShareBean(BaseShareBean shareBean);

    View getDataView();

    View getStateView();

    void updateDataView(BaseShareBean shareBean);

    void updateStateView(BaseShareBean shareBean);

    default void onHiddenChanged(boolean hidden) {

    }
}
