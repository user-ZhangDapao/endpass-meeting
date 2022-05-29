package com.sdcz.endpass.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RadioButton;

import com.sdcz.endpass.R;


public class PopupWindowToCall extends PopupWindow implements View.OnClickListener {

    private View mMenuView;

    private OnPopWindowClickListener listener;

    private Activity activity;

    public PopupWindowToCall(Activity activity, OnPopWindowClickListener listener, String Num) {
        this.activity =activity;
        initView(activity, listener,Num);
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
        this.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER, 0, winHeight - rect.bottom);
    }

    private void initView(Activity activity, OnPopWindowClickListener listener,String Num) {
        //设置按钮监听
        this.listener = listener;
        initViewSetting(activity,Num);
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

        //TODO: mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框  未解决 无效
//        mMenuView.setOnTouchListener(new View.OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent event) {
//                int height = mMenuView.findViewById(R.id.ll_popu).getTop();
//                int y = (int) event.getY();
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (y < height) {
//                        ToastUtils.show("123123123");
//                        dismiss();
//                    }
//                }
//                return true;
//            }
//        });
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                changeBackgroundAlphaTo(1.0f);

            }
        });
        changeBackgroundAlphaTo(0.7f);

    }

    //弹窗
    private void initViewSetting(Activity context,String Num) {
        RadioButton tvCall;
        RadioButton  tvClose;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.popup_usernum_layout, null);

        tvCall = mMenuView.findViewById(R.id.tvCall);
        if (!"".equals(Num)){
            tvCall.setText("呼叫+" + Num);
        }else {
            tvCall.setText("呼叫+" + "暂无联系电话");
        }
        tvClose = mMenuView.findViewById(R.id.tvClose);

        tvCall.setOnClickListener(this);

        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        listener.onPopWindowClickListener(v);
        dismiss();
    }
    //接口
    public interface OnPopWindowClickListener {
        void onPopWindowClickListener(View view);
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
