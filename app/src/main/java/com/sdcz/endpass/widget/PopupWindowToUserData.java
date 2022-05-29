package com.sdcz.endpass.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.firebase.auth.UserInfo;
import com.sdcz.endpass.Constants;
import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.UserEntity;
import com.sdcz.endpass.util.GlideUtils;


public class PopupWindowToUserData extends PopupWindow{

    private View mMenuView;

    private OnPopWindowClickListener listener;

    private Activity activity;

    public PopupWindowToUserData(Activity activity, int code, UserEntity info, String userId, OnPopWindowClickListener listener) {
        this.activity = activity;
        initView(activity, listener,code,info,userId);
    }

    public void show(){
        Rect rect = new Rect();
        /*
         * getWindow().getDecorView()得到的View是Window中的最顶层View，可以从Window中获取到该View，
         * 然后该View有个getWindowVisibleDisplayFrame()方法可以获取到程序显示的区域，
         * 包括标题栏，但不包括状态栏。
         */
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int winHeight = activity.getWindow().getDecorView().getHeight();
        this.showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, winHeight - rect.bottom);
    }

    private void initView(Activity activity, OnPopWindowClickListener listener,int code,UserEntity info,String userId) {
        //设置按钮监听
        this.listener = listener;
        initViewSetting( activity ,code , info,userId);
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.anim_bottonbar);
        //实例化一个ColorDrawable颜色为半透明
//        ColorDrawable dw = new ColorDrawable(0xb0000000);
//        //设置SelectPicPopupWindow弹出窗体的背景
//        this.setBackgroundDrawable(dw);
        changeBackgroundAlphaTo(0.7f);
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                todisMiss();
            }
        });

    }

    //弹窗
    private void initViewSetting(Activity context, int code, UserEntity info, String userId) {

        TextView tvName,tvLike,tvClose,tvVoice,tvDeptName,tvPhone,tvUserName,tvState;
        ImageView imgVoice,imageLike,imgVoide,ivheadImg;
        LinearLayout layoutVideo,llRoot;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.popup_concern_layout, null);

        llRoot = mMenuView.findViewById(R.id.llRoot);
        tvName = mMenuView.findViewById(R.id.tv_name);
        tvLike = mMenuView.findViewById(R.id.tv_like);
        tvClose = mMenuView.findViewById(R.id.tv_close);
        tvVoice = mMenuView.findViewById(R.id.tv_voice);
        imgVoice = mMenuView.findViewById(R.id.iv_voice);
        imageLike = mMenuView.findViewById(R.id.iv_like);
        imgVoide = mMenuView.findViewById(R.id.iv_video);
        ivheadImg = mMenuView.findViewById(R.id.iv_headImg);
        layoutVideo = mMenuView.findViewById(R.id.layout_video);
        tvPhone = mMenuView.findViewById(R.id.tv_phone);
        tvDeptName = mMenuView.findViewById(R.id.tv_deptName);
        tvUserName = mMenuView.findViewById(R.id.tv_userName);
        tvState = mMenuView.findViewById(R.id.tvState);
        tvName.setText(info.getNickName());
        tvUserName.setText(info.getUserName());
        tvDeptName.setText(info.getDept() == null ?"":info.getDept().getDeptName());
        tvState.setText(info.getChannelName() == null ? "无任务" :  info.getChannelName() + "中");
        tvPhone.setText(info.getPhonenumber() == null ? "暂无联系电话" : info.getPhonenumber());
        GlideUtils.showCircleImage(context, ivheadImg,info.getAvatar(),R.drawable.icon_head);

        if (userId.equals(info.getUserId())){
            llRoot.setVisibility(View.GONE);
        }else {
            llRoot.setVisibility(View.VISIBLE);
        }

        //是否收藏
        if (code == 0){
            tvLike.setText("取消收藏");
            imageLike.setBackgroundResource(R.drawable.grouping_personnel_collection);
        }else {
            tvLike.setText("收藏");
            imageLike.setBackgroundResource(R.drawable.grouping_personnel_add_normal);
        }

        //是否在线 1不在线
        if (info.getIsOnline() != 1 || null != info.getPhonenumber()){// bu在线
            layoutVideo.setVisibility(View.GONE);
            tvVoice.setText("拨打电话");
            imgVoice.setBackgroundResource(R.drawable.button_popu_phone);

            //语音
            imgVoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(info.getPhonenumber())){
                        listener.onCallPhone(info.getPhonenumber());
                        dismiss();
                        changeBackgroundAlphaTo(0.7f);
                    }else {
                        ToastUtils.showLong("暂无联系电话");
                        dismiss();
                    }

                }
            });
        }else {
            //语音
            imgVoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //创建临时会话
                    listener.onCreatRecord(userId,info.getUserId()+"", Constants.SharedPreKey.CREATERECORD_VOICE);
                    dismiss();
                }
            });
        }

        //视频
        imgVoide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCreatRecord(userId,info.getUserId()+"", Constants.SharedPreKey.CREATERECORD_VOIDE);
                dismiss();
            }
        });
        //收藏
        imageLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCollectUser(userId,info.getUserId() + "");
                dismiss();
            }
        });
        //关闭
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        //TODO: mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框 未解决 无效
//        mMenuView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int height = mMenuView.findViewById(R.id.rl_pupo).getTop();
//                int y = (int) event.getY();
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    if (y < height) {
//                        ToastUtils.show("123123123");
//                        dismiss();
//                    }
//                }
//                return true;
//            }
//        });


    }

    //dismiss
    private void todisMiss() {
        dismiss();
        changeBackgroundAlphaTo(1.0f);
    }

    //接口
    public interface OnPopWindowClickListener {

        void onCreatRecord(String userId,String collectUserId,String recordType);

        void onCollectUser(String userId, String collectUserId);

        void onCallPhone(String phoneNum);

    }

    //背景色变透明
    private void changeBackgroundAlphaTo(float alphaValue) {

        final WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();

        attributes.alpha = alphaValue;//０.０全透明．１.０不透明．

        activity.runOnUiThread(new Runnable() {

            @Override

            public void run() {

                activity.getWindow().setAttributes(attributes);

            }

        });

    }


}
