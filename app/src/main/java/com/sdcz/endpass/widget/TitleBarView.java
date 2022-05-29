package com.sdcz.endpass.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sdcz.endpass.R;


/**
 * Author: Administrator
 * CreateDate: 2021/8/18 12:48
 * Description: @
 */
public class TitleBarView extends RelativeLayout {

    private ImageView ivBack, ivRight, ivLeft;
    private TextView tvTitle, tvLeft, tvRight;
    private RelativeLayout rlTitleBar;

    private titleBarClick mClick;

//
//    private Bitmap leftImage, rightImage;
//    private String leftText, titleText;
//    private int leftTextColor, titleTextColor ;
//    private int background ;


    public TitleBarView(Context context) {
        this(context, null);
    }

    public TitleBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TitleBarView);
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.TitleBarView_leftTextColors:
                    tvLeft.setTextColor(array.getColor(attr, Color.BLACK));
                    break;
                case R.styleable.TitleBarView_rightTextColors:
                    tvRight.setTextColor(array.getColor(attr, Color.BLACK));
                    break;
                case R.styleable.TitleBarView_themeColors:
                    rlTitleBar.setBackgroundColor(array.getColor(attr, Color.WHITE));
                    break;
                case R.styleable.TitleBarView_leftDrawbles:
                    ivLeft.setImageResource(array.getResourceId(attr, 0));
                    ivLeft.setVisibility(VISIBLE);
                    break;
                case R.styleable.TitleBarView_leftTexts:
                    tvLeft.setText(array.getString(attr));
                    tvLeft.setVisibility(VISIBLE);
                    break;
                case R.styleable.TitleBarView_rightTexts:
                    tvRight.setText(array.getString(attr));
                    tvRight.setVisibility(VISIBLE);
                    break;
                case R.styleable.TitleBarView_centerTextColors:
                    tvTitle.setTextColor(array.getColor(attr, Color.BLACK));
                    break;
                case R.styleable.TitleBarView_centerTitles:
                    tvTitle.setText(array.getString(attr));
                    tvTitle.setVisibility(VISIBLE);
                    break;
                case R.styleable.TitleBarView_rightDrawbles:
                    ivRight.setImageResource(array.getResourceId(attr, 0));
                    ivRight.setVisibility(VISIBLE);
                    break;
                case R.styleable.TitleBarView_backDrawbles:
                    ivBack.setImageResource(array.getResourceId(attr, 0));
                    ivBack.setVisibility(VISIBLE);
                    break;
                case R.styleable.TitleBarView_centerTextSizes:
                    tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, array.getInt(attr,15));
                    break;
                case R.styleable.TitleBarView_leftTextSizes:
                    tvLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, array.getInt(attr,15));
                    break;
                case R.styleable.TitleBarView_rightTextSizes:
                    tvRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, array.getInt(attr,15));
                    break;
//                case R.styleable.TitlebarView_rightText:
//                    tv_right.setText(array.getString(attr));
//                    break;
//                case R.styleable.TitlebarView_rightTextColor:
//                    tv_right.setTextColor(array.getColor(attr, Color.BLACK));
//                    break;
            }
        }
        array.recycle();

        ivBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mClick.ivBackClick();
            }
        });
        ivRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mClick.ivRightClick();
            }
        });


    }

    private void initView(Context context) {
        inflate(context, R.layout.titlebar_view, this);
        ivLeft = findViewById(R.id.ivLeft);
        ivBack = findViewById(R.id.ivBack);
        ivRight = findViewById(R.id.ivRight);
        tvTitle = findViewById(R.id.tvTitle);
        tvLeft = findViewById(R.id.tvLeft);
        tvRight = findViewById(R.id.tvRight);
        rlTitleBar = findViewById(R.id.rlTitleBar);
    }

    public void setOnViewClick(titleBarClick click) {
        this.mClick = click;
    }

    //设置标题
    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
    }

    //设置左标题
    public void setLeftText(String title) {
        if (!TextUtils.isEmpty(title)) {
            tvLeft.setText(title);
            tvLeft.setVisibility(VISIBLE);
        }
    }

    //设置左标题
    public void setRightText(String title) {
        if (!TextUtils.isEmpty(title)) {
            tvRight.setText(title);
            tvRight.setVisibility(VISIBLE);
        }
    }

    //设置标题大小
    public void setTitleSize(int size) {
        if (tvTitle != null) {
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }
    }

    //设置左标题大小
    public void setLeftTextSize(int size) {
        if (tvLeft != null) {
            tvLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }
    }

    //设置左标题大小
    public void setRightTextSize(int size) {
        if (tvRight != null) {
            tvRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }
    }

    //设置左图标
    public void setLeftDrawable(int res) {
        if (ivLeft != null) {
            ivLeft.setImageResource(res);
        }
    }

    //设置右图标
    public void setRightDrawable(int res) {
        if (ivRight != null) {
            ivRight.setImageResource(res);
        }
    }

    //设置返回图标
    public void setBackDrawable(int res) {
        if (ivBack != null) {
            ivBack.setImageResource(res);
        }
    }


    public interface titleBarClick{
        void ivBackClick();
        void ivRightClick();
    }

}
