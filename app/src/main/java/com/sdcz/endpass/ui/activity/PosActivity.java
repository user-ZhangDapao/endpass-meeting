package com.sdcz.endpass.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.PosAdapter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.PosBean;
import com.sdcz.endpass.presenter.PosPresenter;
import com.sdcz.endpass.view.IPosView;

import java.util.List;

/**
 * Author: Administrator
 * CreateDate: 2022/2/16 13:57
 * Description: @
 */
public class PosActivity extends BaseActivity<PosPresenter> implements IPosView, View.OnClickListener {

    private LinearLayout llRoot;
    private ImageView ivBack;
    private EditText etLon;
    private EditText etLat;
    private Button btnChange;
    private RecyclerView rlPos;
    private double Lon;
    private double Lat;
    private PosAdapter adapter;

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_pos;
    }

    @Override
    protected PosPresenter createPresenter() {
        return new PosPresenter(this);
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        llRoot = findViewById(R.id.llRoot);
        etLon = findViewById(R.id.etLon);
        etLat = findViewById(R.id.etLat);
        btnChange = findViewById(R.id.btnChange);
        rlPos = findViewById(R.id.rlPos);
        ivBack = findViewById(R.id.ivBack);
        return llRoot;
    }

    @Override
    public void initListener() {
        btnChange.setOnClickListener(this);
        ivBack.setOnClickListener(this);
    }

    @Override
    public void initData() {
        Lon = getIntent().getDoubleExtra(Constants.SharedPreKey.POS_LON, 0);
        Lat = getIntent().getDoubleExtra(Constants.SharedPreKey.POS_LAT, 0);
        Log.e("TAG", "initData: " + Lon + "*" + Lat );

        adapter = new PosAdapter(this);
//        rlPos.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        rlPos.setLayoutManager(layoutManager);
        rlPos.setAdapter(adapter);

        mPresenter.getUserLocationRecord(this);

        etLat.setText(Lat + "");
        etLon.setText(Lon + "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnChange:
                String changeLat = etLat.getText().toString();
                String changeLon = etLon.getText().toString();
                if (changeLat.isEmpty() || changeLon.isEmpty()) {
                    ToastUtils.showLong( "修改位置信息不能为空");
                    return;
                }
                if (changeLon.equals(Lon) && changeLat.equals(Lat)) {
                    ToastUtils.showLong( "请修改新的位置信息");
                    return;
                }
                mPresenter.setUserLocation(this, changeLon, changeLat);
                showLoading();
                break;
            case R.id.ivBack:
                finish();
                break;
            default:
                break;
        }
    }


    @Override
    public void setUserLocationResult(Object o) {
        hideLoading();
        ToastUtils.showLong("修改成功");
    }

    @Override
    public void showData(List<PosBean> list) {
        if (null != list){
            if (list.size()>0){
                adapter.setData(list);
            }
        }
    }
}
