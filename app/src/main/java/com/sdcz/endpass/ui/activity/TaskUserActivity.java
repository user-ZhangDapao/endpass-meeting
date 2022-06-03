package com.sdcz.endpass.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.firebase.auth.UserInfo;
import com.jaeger.library.StatusBarUtil;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.TaskUserAadpter;
import com.sdcz.endpass.base.BaseActivity;
import com.sdcz.endpass.bean.ChannerUser;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.presenter.TaskUserPresenter;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.ITaskUserView;
import com.sdcz.endpass.widget.CustomDialog;
import com.sdcz.endpass.widget.TitleBarView;

import org.greenrobot.eventbus.EventBus;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务成员管理
 */
public class TaskUserActivity extends BaseActivity<TaskUserPresenter> implements ITaskUserView, View.OnClickListener {

    private RecyclerView rvRoot;
    private TaskUserAadpter mAdpter;
    private TextView tvTitle;
    private ImageView ivBack;
    private ImageView ivRight;
    private String channelCode;
    private String channelName;
    private RelativeLayout rlError;
    private TitleBarView titleBar;

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_task_user;
    }
    @Override
    protected TaskUserPresenter createPresenter() {
        return new TaskUserPresenter(this);
    }

    @Override
    public View initView(Bundle savedInstanceState) {
        rvRoot = findViewById(R.id.rvRoot);
        titleBar = findViewById(R.id.titleBar);
        tvTitle = findViewById(R.id.tvTitle);
        ivBack = findViewById(R.id.ivBack);
        ivRight = findViewById(R.id.ivRight);
        rlError = findViewById(R.id.rlError);
        ivBack.setVisibility(View.VISIBLE);
        return rvRoot;
    }

    @Override
    public void initListener() {
        super.initListener();
        ivBack.setOnClickListener(this);
        ivRight.setOnClickListener(this);
        mAdpter.setListener(new TaskUserAadpter.onClickListener() {
            @Override
            public void onDeleteClick(String userId) {
                showDeleteUserDialog(userId);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivRight:

                //Todo
                showPopuWin();
                break;
            case R.id.ivBack:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void initData() {
        super.initData();
        channelCode = getIntent().getStringExtra(Constants.SharedPreKey.CHANNEL_CODE);
        channelName = getIntent().getStringExtra(Constants.SharedPreKey.CHANNEL_NAME);
        titleBar.setLeftText(channelName);
        mPresenter.queryChannelUser(this, channelCode);
        mAdpter = new TaskUserAadpter(getContext());
        rvRoot.setLayoutManager(new LinearLayoutManager(this));
        rvRoot.setAdapter(mAdpter);
    }

    @Override
    public void showData(List<ChannerUser> data) {
        if (data != null){
            mAdpter.setData(data);
            if (data.size() > 0){
                rlError.setVisibility(View.GONE);
                rvRoot.setVisibility(View.VISIBLE);
            }else {
                rlError.setVisibility(View.VISIBLE);
                rvRoot.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDelete(Object data,String userId) {
        ToastUtils.showLong("操作成功");
        mPresenter.queryChannelUser(this, channelCode);
//        FspManager.getInstance().sendUserMsg(userId,Constants.KICK_OUT);
//        sendRefresh();
    }

    @Override
    public void addUserResult(Object data) {
        ToastUtils.showLong("操作成功");
//        ArrayList<String> list = new ArrayList<String>();
//        String[] strings = new String[SharedPrefsUtil.getValue(getContext(),KeyStore.SELECT_USER_LIST).size()];
//        list.toArray(strings);
//        joinGroupVoiceUser(strings,channelCode,Constants.MEETING_TASK);
        SharedPrefsUtil.putListUserInfo(new ArrayList<>());
        mPresenter.queryChannelUser(this, channelCode);
//        sendRefresh();
    }

    @Override
    public void updateChannelResult(Object data) {
        hideLoading();
        titleBar.setLeftText(channelName);
//        EventBus.getDefault().post(new EventRefach());
//        sendRefresh();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.color_4D6B4A));
    }

    private void showDeleteUserDialog(String userId) {

        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setMessage("您确认要删除该成员吗？");
        builder.setTitle("删除成员");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //删除
                mPresenter.deleteChannelUser(TaskUserActivity.this ,channelCode, userId);
            }
        });
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.SharedPreKey.REQUEST_CODE_2){
            if (resultCode == Constants.HttpKey.RESPONSE_200){
                if (SharedPrefsUtil.getListUserInfo().size() > 0){

                    List<String> selectUsers = new ArrayList<>();

                    if(SharedPrefsUtil.getRoleId().equals("1")){
                        ToastUtils.showLong("超级管理员~");
                        for (UserEntity info : SharedPrefsUtil.getListUserInfo()){
                            if (info.getChannelName() != null){
//                                mPresenter.deleteChannelUser(this,info.getChannel().getChannelCode(),info.getUserId());
                            }
                            selectUsers.add(info.getUserId() + "");
                        }
                    }else {
                        ToastUtils.showLong("普通管理员~");
                        for (UserEntity info : SharedPrefsUtil.getListUserInfo()){
                            if (info.getChannelName() == null){
                                selectUsers.add(info.getUserId() + "");
                            }
                        }
                    }

                    String[] usersId = selectUsers.toArray(new String[selectUsers.size()]);
                    if (usersId.length > 0){
                        mPresenter.addChannelUser(this, channelCode, usersId);
                    }

                }
            }else if (resultCode == Constants.SharedPreKey.REQUEST_CODE_201){
//                tvDept.setText(data.getStringExtra(KeyStore.DEPTNAME));
//                deptId = data.getStringExtra(KeyStore.DEPTID);
            }
        }
    }

    private void showPopuWin() {
        //创建 popupWindow
        View contentView = LayoutInflater.from(this).inflate(R.layout.layout_task_user_setting, null);
        PopupWindow popWnd = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //显示 PopupWindow
        View rootview = LayoutInflater.from(this).inflate(R.layout.activity_task_user, null);
        popWnd.showAtLocation(rootview, Gravity.RIGHT | Gravity.TOP, 0, 0);
        popWnd.setBackgroundDrawable(new ColorDrawable(0));
        popWnd.setFocusable(true); // 设置PopupWindow可获得焦点
        popWnd.setTouchable(true); // 设置PopupWindow可触摸
        popWnd.setOutsideTouchable(true); // 设置非PopupWindow区域可触摸
        popWnd.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                changeBackgroundAlphaTo(1.0f);
            }
        });
        TextView tvUpdataName = contentView.findViewById(R.id.tvUpdataName);
        TextView tvAddUser = contentView.findViewById(R.id.tvAddUser);

        tvUpdataName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWnd.dismiss();
                showPopuWin2();
            }
        });
        tvAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(TaskUserActivity.this, SelectUserActivity.class).putExtra(Constants.SharedPreKey.DEPTID,"").putExtra(Constants.SharedPreKey.GROUPNAME,""),Constants.SharedPreKey.REQUEST_CODE_2);
                popWnd.dismiss();
            }
        });

        changeBackgroundAlphaTo(0.7f);

    }

    private void showPopuWin2(){
        CustomDialog.Builder builder = new CustomDialog.Builder(TaskUserActivity.this);
        builder.setTitle("修改名称");
//        builder.setMessage("请在下方输入");
        builder.setEtMessage(channelName + "");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (null == builder.getEtMessage() ){
                    ToastUtils.showLong("请正确输入");
                    return;
                }
                mPresenter.updateChannel(TaskUserActivity.this, channelCode, builder.getEtMessage());
                channelName = builder.getEtMessage();
                dialog.dismiss();
                showLoading();
                //设置你的操作事项
            }
        });
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }



    //背景色变透明
    private void changeBackgroundAlphaTo(float alphaValue) {

        final WindowManager.LayoutParams attributes = getWindow().getAttributes();

        attributes.alpha = alphaValue;//０.０全透明．１.０不透明．

        runOnUiThread(new Runnable() {

            @Override

            public void run() {

                getWindow().setAttributes(attributes);

            }

        });

    }


}