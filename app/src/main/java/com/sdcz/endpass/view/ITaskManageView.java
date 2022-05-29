package com.sdcz.endpass.view;


import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.ChannelBean;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/6/29 11:15
 * Description: @
 */
public interface ITaskManageView extends IBaseView {
    void showData(List<ChannelBean> data);
    void deleteSuccess(Object data);
}
