package com.sdcz.endpass.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.blankj.utilcode.util.ScreenUtils;
import com.sdcz.endpass.R;

/**
 * Created by zhangyb on 2017/10/26.
 */

public class BackToolBar extends Toolbar implements View.OnClickListener {

    private ImageView backImageView;
    private TextView titleTextView;
    private TextView createTextView;
    private TextView callTextView;
    private final Context context;

    private BackListener backListener;
    private CreateListener createListener;
    private CallListener callListener;

    public interface BackListener {
        void onBack();
    }

    public interface CreateListener {
        void onCreate();
    }

    public interface CallListener {
        void onCall();
    }

    public void setOnCallListener(CallListener listener) {
        callListener = listener;
    }

    public void setBackListener(BackListener listener) {
        backListener = listener;
    }

    public void setCreateListener(CreateListener createListener) {
        this.createListener = createListener;
    }

    public BackToolBar(Context context) {
        this(context, null);
    }

    /**
     * 重载函数
     *
     * @param context context
     * @param attrs   attrs
     */
    public BackToolBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 重载函数
     *
     * @param context      context
     * @param attrs        attrs
     * @param defStyleAttr defstyleattr
     */
    public BackToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initListener();
    }

    private void initView() {
        //设置距左右边距为0，否则左右会有空白
        setContentInsetsRelative(0, 0);

        View toolBar = LayoutInflater.from(context).inflate(R.layout.toolbar_layout, this, true);
        backImageView = toolBar.findViewById(R.id.back_imageview);
        titleTextView = toolBar.findViewById(R.id.title_textview);
        createTextView = toolBar.findViewById(R.id.right_one_create);
        callTextView = toolBar.findViewById(R.id.right_one_call);
    }

    private void initListener() {
        backImageView.setOnClickListener(this);
        createTextView.setOnClickListener(this);
        callTextView.setOnClickListener(this);
    }

    /**
     * 设置按钮颜色
     *
     * @param textColorId 颜色ID
     * @param canClick    是否可以点击
     */
    public void setCreateTextViewParam(@NonNull int textColorId, boolean canClick) {
        createTextView.setTextColor(getResources().getColor(textColorId));
        createTextView.setClickable(canClick);
    }

    /**
     * 设置按钮颜色
     *
     * @param textColorId 颜色ID
     * @param canClick    是否可以点击
     */
    public void setCallTextViewParam(@NonNull int textColorId, boolean canClick) {
        callTextView.setTextColor(getResources().getColor(textColorId));
        callTextView.setClickable(canClick);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back_imageview) {
            if (backListener != null) {
                backListener.onBack();
            }
        } else if (view.getId() == R.id.right_one_create) {
            if (createListener != null) {
                createListener.onCreate();
            }
        } else if (view.getId() == R.id.right_one_call && callListener != null) {
            callListener.onCall();
        }
    }

    /**
     * 设置标题text
     *
     * @param text text
     */
    public void setTitleTextView(String text) {
        titleTextView.setText(text);
    }

    public void setCreateTextViewVisibility(int visibility) {
        createTextView.setVisibility(visibility);
    }

    public void setCallTextViewVisibility(int visibility) {
        callTextView.setVisibility(visibility);
    }

    /**
     * 设置导航宽高
     */
    public void setToolBarHeight() {
        if (ScreenUtils.isPortrait()) {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = (int) context.getResources().getDimension(R.dimen.toolbarHeight);
            setLayoutParams(params);
        } else {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = (int) context.getResources().getDimension(R.dimen.toolbarHeightLand);
            setLayoutParams(params);
        }
    }
}
