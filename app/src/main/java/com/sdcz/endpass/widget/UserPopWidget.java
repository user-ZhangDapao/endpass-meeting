package com.sdcz.endpass.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sdcz.endpass.R;
import com.sdcz.endpass.base.BasePopupWindowContentView;

public class UserPopWidget extends BasePopupWindowContentView implements View.OnClickListener {


    private Context context;
    private String channelCode;

    LinearLayout llRoot;


    public UserPopWidget(@NonNull Context context, String channelCode) {
        super(context);
        this.context = context;
        this.channelCode = channelCode;
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.activity_user_pop, this);
        llRoot = findViewById(R.id.llRoot);
    }


    @Override
    public void onClick(View view) {

    }
}