package com.sdcz.endpass.view;


import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.PosBean;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2022/2/16 13:58
 * Description: @
 */
public interface IPosView extends IBaseView {
    void setUserLocationResult(Object o);
    void showData(List<PosBean> list);
}
