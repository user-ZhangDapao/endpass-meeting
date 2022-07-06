package com.sdcz.endpass.presenter;

import android.app.Activity;

import com.google.gson.Gson;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.network.MyObserver;
import com.sdcz.endpass.network.RequestUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.IMainView;

/**
 * Author: Administrator
 * CreateDate: 2021/7/5 16:54
 * Description: @
 */
public class MainPresenter extends BasePresenter<IMainView> {

    public MainPresenter(IMainView view) {
        super(view);
    }

}
