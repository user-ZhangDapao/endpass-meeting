package com.sdcz.endpass.view;


import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.MailListBean;

/**
 * Author: Administrator
 * CreateDate: 2021/7/8 17:14
 * Description: @
 */
public interface ISelectDeptView extends IBaseView {
    void showData(MailListBean data);
}
