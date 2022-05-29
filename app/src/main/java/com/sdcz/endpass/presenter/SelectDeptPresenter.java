package com.sdcz.endpass.presenter;

import android.app.Activity;

import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.MailListBean;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.view.ISelectDeptView;

/**
 * Author: Administrator
 * CreateDate: 2021/7/8 17:14
 * Description: @
 */
public class SelectDeptPresenter extends BasePresenter<ISelectDeptView> {
    public SelectDeptPresenter(ISelectDeptView view) {
        super(view);
    }
    public void getContactList(Activity activity, String deptId){
        RequestUtils.getContactList(deptId, new MyObserver<MailListBean>(activity) {
            @Override
            public void onSuccess(MailListBean result) {
                iView.showData(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }
}
