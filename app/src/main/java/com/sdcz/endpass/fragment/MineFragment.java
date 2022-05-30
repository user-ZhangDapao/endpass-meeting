package com.sdcz.endpass.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.LoginActivity;
import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BaseFragment;
import com.sdcz.endpass.base.BasePresenter;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.presenter.LoginPresenter;
import com.sdcz.endpass.presenter.MinePresenter;
import com.sdcz.endpass.ui.activity.ChangePassActivity;
import com.sdcz.endpass.ui.activity.LoginActivityApp;
import com.sdcz.endpass.ui.activity.PosActivity;
import com.sdcz.endpass.ui.activity.UserInfoActivity;
import com.sdcz.endpass.util.ActivityUtils;
import com.sdcz.endpass.util.GlideUtils;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.view.IMineView;
import com.sdcz.endpass.widget.CustomDialog;

/**
 * Author: Administrator
 * CreateDate: 2021/6/29 11:18
 * Description: @
 */
public class MineFragment extends BaseFragment<MinePresenter> implements IMineView, View.OnClickListener {

    private LinearLayout llUserdata;
    private TextView tvRealname;
    private TextView tvUserName;
    private TextView tvDeptName;
    private TextView tvPhone;
    private TextView tvRole;
    private ImageView ivHead;
    private RelativeLayout rlChangePassw;
    private RelativeLayout rlPos;
    private RelativeLayout layoutOutline;
    private double Lon;
    private double Lat;

    @Override
    protected int provideContentViewId() {
        return R.layout.fragment_mine;
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        llUserdata = rootView.findViewById(R.id.ll_userdata);
        tvRealname = rootView.findViewById(R.id.tv_realname);
        tvDeptName = rootView.findViewById(R.id.tv_deptName);
        tvPhone = rootView.findViewById(R.id.tv_phone);
        ivHead = rootView.findViewById(R.id.ivHead);
        tvUserName = rootView.findViewById(R.id.tv_userName);
        rlChangePassw = rootView.findViewById(R.id.rl_changePassw);
        rlPos = rootView.findViewById(R.id.rl_pos);
        layoutOutline = rootView.findViewById(R.id.layout_outline);
        tvRole = rootView.findViewById(R.id.tvRole);
    }

    @Override
    public void initData() {
        super.initData();
        showLoading();
        mPresenter.getUserInfo(getActivity());
    }

    @Override
    public void initListener() {
        super.initListener();
        llUserdata.setOnClickListener(this);
        rlChangePassw.setOnClickListener(this);
        ivHead.setOnClickListener(this);
        ivHead.setOnClickListener(this);
        layoutOutline.setOnClickListener(this);
        rlPos.setOnClickListener(this);
    }

    @Override
    protected MinePresenter createPresenter() {
        return new MinePresenter(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_userdata:
            case R.id.ivHead:
                startActivityForResult(new Intent(getActivity(), UserInfoActivity.class),1);
                break;
            case R.id.rl_changePassw:
                startActivity(new Intent(getActivity(), ChangePassActivity.class));
                break;
            case R.id.rl_pos:
                Intent intent = new Intent(getContext(), PosActivity.class);
                intent.putExtra(Constants.SharedPreKey.POS_LAT,Lat);
                intent.putExtra(Constants.SharedPreKey.POS_LON,Lon);
                startActivity(intent);                break;
            case R.id.layout_outline:
                showLoginOutDialog();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.HttpKey.RESPONSE_200){
//            mPresenter.getUserInfo(getActivity(),"");
        }
    }

    private void showLoginOutDialog() {

        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
        builder.setMessage("您确认要退出登录吗？");
        builder.setTitle("退出登录");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //设置你的操作事项
                leaveGroup();
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
    public void showData(UserEntity data) {
        hideLoading();
        if (null != data){
//            SharedPrefsUtil.putValue(getActivity(), KeyStore.HEADIMG, data.getHeadImg());
            tvRealname.setText(data.getNickName());
            tvUserName.setText(data.getUserName());
            tvDeptName.setText(data.getDept().getDeptName());
            tvPhone.setText(data.getPhonenumber());
            tvRole.setText(data.getRoles().get(0).getRoleName());
            GlideUtils.showCircleImage(getActivity(), ivHead, data.getAvatar(), R.drawable.icon_head);

            Lon = data.getLon();
            Lat = data.getLat();
//            if (!data.getRole().equals(SharedPrefsUtil.getValue(getActivity(), KeyStore.ROLE, data.getRole()))){
//                SharedPrefsUtil.putValue(getActivity(), KeyStore.ROLE, data.getRole());
//                EventBus.getDefault().post(new EventRefarechLayout());
//            }
        }
    }


    private void leaveGroup() {
//        // join group
//        boolean result = FspManager.getInstance().leaveGroup();
//        if (!result) {
//            Toast.makeText(getContext(), "离开组失败",
//                    Toast.LENGTH_SHORT).show();
//        }else {
//            LeaveGroupResultSuccess();
//        }
        SharedPrefsUtil.clean(getActivity());
        Intent intent = new Intent(getActivity(), LoginActivityApp.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        ActivityUtils.getInstance().finishAll();
    }

//    //离开组成功
//    protected void LeaveGroupResultSuccess() {
//        EventBus.getDefault().post(new EventStopHandler(true));
//        SharedPrefsUtil.putValue(getContext(), KeyStore.IS_LOGIN,false);
//        FspManager.getInstance().loginOut();
//        SharedPreferences user = getActivity().getSharedPreferences("dispatch",MODE_PRIVATE);
//        user.edit().remove(KeyStore.USERID).remove(KeyStore.USERNAME).remove(KeyStore.DEPTNAME).remove(KeyStore.REALNAME).remove(KeyStore.ROLE).remove(KeyStore.PHONE).remove(KeyStore.HEADIMG).remove(KeyStore.DEPTID).remove(KeyStore.TASK_NAME).remove(KeyStore.TASK_CODE).commit();
//        Intent intent = new Intent(getActivity(), LoginActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        ActivityUtils.getInstance().finishAll();
//    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        mPresenter.getUserInfo(getActivity());
//    }
}
