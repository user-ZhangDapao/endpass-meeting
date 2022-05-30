package com.sdcz.endpass.presenter;

import android.app.Activity;

import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.PosBean;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.view.IPosView;

import java.util.List;


/**
 * Author: Administrator
 * CreateDate: 2022/2/16 13:58
 * Description: @
 */
public class PosPresenter extends BasePresenter<IPosView> {
    public PosPresenter(IPosView view) {
        super(view);
    }


    public void setUserLocation(Activity activity, String lon, String lat){
        RequestUtils.setUserLocation(lon, lat, new MyObserver<Object>(activity) {
            @Override
            public void onSuccess(Object result) {
                iView.setUserLocationResult(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }

    public void getUserLocationRecord(Activity activity){
        RequestUtils.getUserLocationRecord(new MyObserver<List<PosBean>>(activity) {
            @Override
            public void onSuccess(List<PosBean> result) {
                iView.showData(result);
            }

            @Override
            public void onFailure(Throwable e, String errorMsg) {
                iView.showOnFailure("",errorMsg);
            }
        });
    }
}
