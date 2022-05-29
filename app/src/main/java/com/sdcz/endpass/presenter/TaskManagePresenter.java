package com.sdcz.endpass.presenter;

import android.app.Activity;

import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ITaskManageView;

import java.security.KeyStore;
import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2021/6/29 11:15
 * Description: @
 */
public class TaskManagePresenter extends BasePresenter<ITaskManageView> {

    public TaskManagePresenter(ITaskManageView view) {
        super(view);
    }

    public void getChannelList(Activity activity){
        RequestUtils.getChannelList(new MyObserver<List<ChannelBean>>(activity) {
            @Override
            public void onSuccess(List<ChannelBean> result) {
                iView.showData(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }

    public void deletChannel(Activity activity, String groupId){
        RequestUtils.deletChannel(groupId, new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                iView.deleteSuccess(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }
}
