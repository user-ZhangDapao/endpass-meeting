package com.sdcz.endpass.view;


import com.sdcz.endpass.base.IBaseView;
import com.sdcz.endpass.bean.UserEntity;

/**
 * Author: Administrator
 * CreateDate: 2021/6/29 11:18
 * Description: @
 */
public interface IMineView extends IBaseView {
    void showData(UserEntity data);
}
