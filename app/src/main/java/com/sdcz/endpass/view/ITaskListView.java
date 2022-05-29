package com.sdcz.endpass.view;


import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.ChannelBean;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/6/29 11:13
 * Description: @
 */
public interface ITaskListView extends IBaseView {
    void showData(List<ChannelBean> data);
}
