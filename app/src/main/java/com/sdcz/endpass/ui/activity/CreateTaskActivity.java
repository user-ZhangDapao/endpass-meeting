package com.sdcz.endpass.ui.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.jaeger.library.StatusBarUtil;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.FlowAdapter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.ChannelBean;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.presenter.CreateTaskPresenter;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ICreateTaskView;
import com.sdcz.endpass.widget.FlowLayout;

import java.util.ArrayList;
import java.util.List;


public class CreateTaskActivity extends BaseActivity<CreateTaskPresenter> implements ICreateTaskView, View.OnClickListener {

    private LinearLayout llRoot;
    private TextView tvTitle;
    private TextView tvDept;
    private TextView tvSelectUser;
    private ImageView ivBack;
    private Button btnCreate;
    private EditText etName;
    private EditText etDetail;
    private FlowLayout mFlowLayout;
    private String deptId;

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_create_task;
    }
    
    @Override
    protected CreateTaskPresenter createPresenter() {
        return new CreateTaskPresenter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPrefsUtil.putListUserInfo(new ArrayList<>());
        SharedPrefsUtil.putString(Constants.SharedPreKey.SELECT_DEPT_ID,"");
        SharedPrefsUtil.putString(Constants.SharedPreKey.SELECT_DEPT_NAME,"");
    }

    @Override
    public View initView() {
        llRoot = findViewById(R.id.llRoot);
        tvTitle = findViewById(R.id.tvTitle);
        ivBack = findViewById(R.id.ivBack);
        etName = findViewById(R.id.etName);
        btnCreate = findViewById(R.id.btnCreate);
        tvSelectUser = findViewById(R.id.tvSelectUser);
        mFlowLayout = findViewById(R.id.flowLayout);
        etDetail = findViewById(R.id.etDetail);
        tvDept = findViewById(R.id.tvDept);
        tvTitle.setText(R.string.create_task);
        ivBack.setVisibility(View.VISIBLE);
        return llRoot;
    }

    @Override
    public void initListener() {
        super.initListener();
        ivBack.setOnClickListener(this);
        btnCreate.setOnClickListener(this);
        tvSelectUser.setOnClickListener(this);
        tvDept.setOnClickListener(this);
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
            case R.id.tvSelectUser:
                startActivityForResult(new Intent(this, SelectUserActivity.class).putExtra(Constants.SharedPreKey.DEPTID,"").putExtra(Constants.SharedPreKey.GROUPNAME,""),Constants.SharedPreKey.REQUEST_CODE_1);
                break;
            case R.id.tvDept:
                startActivityForResult(new Intent(this, SelectDeptActivity.class).putExtra(Constants.SharedPreKey.DEPTID,"").putExtra(Constants.SharedPreKey.DEPTNAME,""),Constants.SharedPreKey.REQUEST_CODE_1);
                break;
            case R.id.btnCreate:
                checkInfo();
                break;
            default:
                break;
        }
    }

    @Override
    public void initData() {
        super.initData();
        tvDept.setText(SharedPrefsUtil.getUserInfo().getDept().getDeptName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.SharedPreKey.REQUEST_CODE_1){
            if (resultCode == Constants.HttpKey.RESPONSE_200){
                if (SharedPrefsUtil.getListUserInfo().size() > 0){
                    mFlowLayout.setVisibility(View.VISIBLE);
                    tvSelectUser.setText("");
                }else {
                    tvSelectUser.setText("请选择参与成员");
                    mFlowLayout.setVisibility(View.GONE);
                }
                // 设置 Adapter
                FlowAdapter adapter = new FlowAdapter(this, SharedPrefsUtil.getListUserInfo());
                mFlowLayout.setAdapter(adapter);
            }else if (resultCode == Constants.SharedPreKey.REQUEST_CODE_201){
                tvDept.setText(data.getStringExtra(Constants.SharedPreKey.DEPTNAME));
                deptId = data.getStringExtra(Constants.SharedPreKey.DEPTID);
            }
        }
    }

    @Override
    public void showData(ChannelBean data) {
        if (data != null){
//            sendRefresh();
            List<String> selectUsers = new ArrayList<>();
            //TODO:添加人员 加逻辑 先判断是否有权限强邀， 然后添加
            if(SharedPrefsUtil.getRoleId().equals("1")){
                ToastUtils.showLong("超级管理员~");
                for (UserEntity info : SharedPrefsUtil.getListUserInfo()){
                    if (info.getChannelName() != null){
//                        mPresenter.deleteChannelUser(this,info.getChannel().getChannelCode(),info.getUserId());
                    }
                    selectUsers.add(info.getUserId()+"");
                }
            }else {
                ToastUtils.showLong("普通管理员~");
                for (UserEntity info : SharedPrefsUtil.getListUserInfo()){
                    if (info.getChannelName() == null){
                        selectUsers.add(info.getUserId()+"");
                    }
                }
            }
            if (selectUsers.size() > 0){
                String[] usersId = selectUsers.toArray(new String[selectUsers.size()]);
                mPresenter.addChannelUser(this, data.getChannelCode(), usersId);
            }else {
                ToastUtils.showLong("您未选择成员，创建任务成功~");
                setResult(Constants.HttpKey.RESPONSE_200);
                finish();
            }

        }
    }

    private void checkInfo() {
        if (TextUtils.isEmpty(etName.getText().toString())){
            ToastUtils.showLong(R.string.input_task_name);
            return;
        }
        if (TextUtils.isEmpty(deptId)){
            deptId = SharedPrefsUtil.getDeptId() + "";
        }
        mPresenter.addChannel(this,etName.getText().toString(),deptId,TextUtils.isEmpty(etDetail.getText().toString()) ? "" :etDetail.getText().toString());
    }

    @Override
    public void addUserResult(Object data,String ChannelCode,String[] userIds) {
//        joinGroupVoiceUser(userIds, ChannelCode,Constants.MEETING_TASK);
        setResult(Constants.HttpKey.RESPONSE_200);
//        sendRefresh();
        finish();
    }

    @Override
    public void onDelete(Object data) {
    }
}