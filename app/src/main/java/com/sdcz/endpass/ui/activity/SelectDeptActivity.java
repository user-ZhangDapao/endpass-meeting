package com.sdcz.endpass.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaeger.library.StatusBarUtil;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.SelectDeptAdapter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.MailListBean;
import com.sdcz.endpass.presenter.SelectDeptPresenter;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ISelectDeptView;
import com.sdcz.endpass.widget.TitleBarView;

import java.security.KeyStore;

public class SelectDeptActivity extends BaseActivity<SelectDeptPresenter> implements ISelectDeptView, View.OnClickListener {

    private LinearLayout llRoot;
    private ImageView ivBack;
    private TitleBarView titleBar;
    private TextView tvRight;
    private RecyclerView recyclerView;
    private SelectDeptAdapter adapter;

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_select_dept;
    }

    @Override
    protected SelectDeptPresenter createPresenter() {
        return new SelectDeptPresenter(this);
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        llRoot = findViewById(R.id.llRoot);
        ivBack = findViewById(R.id.ivBack);
        tvRight = findViewById(R.id.tvRight);
        titleBar = findViewById(R.id.titleBar);
        recyclerView = findViewById(R.id.recyclerView);
        tvRight.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        tvRight.setText("确认");
        return llRoot;
    }

    @Override
    public void initListener() {
        super.initListener();
        ivBack.setOnClickListener(this);
        tvRight.setOnClickListener(this);
    }

    @Override
    public void initData() {
        super.initData();
        showLoading();
        String deptId = getIntent().getStringExtra(Constants.SharedPreKey.DEPTID);
        String deptName = getIntent().getStringExtra(Constants.SharedPreKey.DEPTNAME);

        if (!"".equals(deptName) && !"".equals(deptId)){
            titleBar.setLeftText(deptName);
            mPresenter.getContactList(this, deptId);
        }else {
            titleBar.setLeftText("选择部门");
            String role = SharedPrefsUtil.getRoleId();
            if (role.equals("1")){
                mPresenter.getContactList(this, "0");
            }else {
                mPresenter.getContactList(this, SharedPrefsUtil.getDeptId() + "");
            }
        }

    }

    @Override
    public void showData(MailListBean data) {
        hideLoading();
        if (data != null){
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new SelectDeptAdapter(this,data.getDeptList());
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new SelectDeptAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String deptId, String groupName) {
                    Intent intent = new Intent(SelectDeptActivity.this , SelectDeptActivity.class);
                    intent.putExtra(Constants.SharedPreKey.DEPTID,deptId);
                    intent.putExtra(Constants.SharedPreKey.DEPTNAME,groupName);
                    startActivityForResult(intent,Constants.SharedPreKey.REQUEST_CODE_2);
                }

                @Override
                public void onRadioButtomClick(String deptId, String groupName) {
                    SharedPrefsUtil.putString(Constants.SharedPreKey.SELECT_DEPT_ID, deptId);
                    SharedPrefsUtil.putString(Constants.SharedPreKey.SELECT_DEPT_NAME, groupName);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.color_4D6B4A));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivBack:
                finish();
                break;
            case R.id.tvRight:
                Intent intent = new Intent();
                intent.putExtra(Constants.SharedPreKey.DEPTID,SharedPrefsUtil.getString(Constants.SharedPreKey.SELECT_DEPT_ID,""));
                intent.putExtra(Constants.SharedPreKey.DEPTID,SharedPrefsUtil.getString(Constants.SharedPreKey.SELECT_DEPT_NAME,""));
                setResult(Constants.SharedPreKey.REQUEST_CODE_201,intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.SharedPreKey.REQUEST_CODE_201){
            setResult(Constants.SharedPreKey.REQUEST_CODE_201,data);
            finish();
        }
    }


}