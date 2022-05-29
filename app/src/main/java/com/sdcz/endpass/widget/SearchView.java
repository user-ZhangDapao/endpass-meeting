package com.sdcz.endpass.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.callback.ClearCallBack;
import com.sdcz.endpass.callback.SearchCallBack;
import com.inpor.log.Logger;

/**
 * Created by Carson_Ho on 17/8/10.
 */

public class SearchView extends LinearLayout {
    // 搜索框组件
    public EditTextClear etSearch; // 搜索按键
    /**
     * 初始化成员变量
     */
    private Context context;
    private TextView textView;
    private ImageView ivClear;
    // 回调接口
    private SearchCallBack searchCallBack;// 搜索按键回调接口
    private ClearCallBack clearCallBack; // 返回按键回调接口
    // 自定义属性设置
    // 1. 搜索字体属性设置：大小、颜色 & 默认提示
    private int textColorSearch;
    private String textHintSearch;
    // 2. 搜索框设置：高度 & 颜色

    /**
     * 构造函数
     * 作用：对搜索框进行初始化
     */
    public SearchView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param attrs   属性
     */
    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initAttrs(context, attrs);
        init();
    }

    /**
     * 构造函数
     *
     * @param context      上下文
     * @param attrs        属性
     * @param defStyleAttr 样式
     */
    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initAttrs(context, attrs);
        init();
    }

    /**
     * 作用：初始化自定义属性
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        // 控件资源名称
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Search_View);
        // 搜索框字体大小（dp）
        // 搜索框字体颜色（使用十六进制代码，如#333、#8e8e8e）
        int defaultColor = context.getResources().getColor(R.color.textColorGrayDeep); // 默认颜色 = 灰色
        textColorSearch = typedArray.getColor(R.styleable.Search_View_textColorSearch, defaultColor);
        // 搜索框提示内容（String）
        textHintSearch = typedArray.getString(R.styleable.Search_View_textHintSearch);
        // 搜索框高度
        // 搜索框颜色
        int defaultColor2 = context.getResources().getColor(R.color.textcolor_adb0b5); // 默认颜色 = 白色
        // 释放资源
        typedArray.recycle();
    }

    /**
     * 作用：初始化搜索框
     */
    private void init() {
        initView();
        ivClear.setVisibility(INVISIBLE);
        ivClear.setOnClickListener(view -> {
            //第一次设false，目的是为了不回调搜索结果
            etSearch.setNeedCallBack(false);
            clearInput();
        });

        etSearch.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN && searchCallBack != null) {
                // 1. 点击搜索按键后，根据输入的搜索字段进行查询
                // 注：由于此处需求会根据自身情况不同而不同，所以具体逻辑由开发者自己实现，此处仅留出接口
                searchCallBack.searchAciton(etSearch.getText().toString());
            }
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                //第一次设false，目的是为了不回调搜索结果
                etSearch.setNeedCallBack(false);
                clearInput();
                //返回true，目的时阻止事件继续向下分发
                return true;
            }
            return false;
        });

    }

    /**
     * 设置搜索框是否显示
     *
     * @param visiable 是否显示
     */
    public void setIvClearVisiable(int visiable) {
        ivClear.setVisibility(visiable);
    }

    private void initView() {
        // 1. 绑定R.layout.search_layout作为搜索框的xml文件
        LayoutInflater.from(context).inflate(R.layout.view_search_layout, this);
        // 2. 绑定搜索框EditText
        textView = findViewById(R.id.tv_search);
        etSearch = (EditTextClear) findViewById(R.id.et_search);
        ivClear = findViewById(R.id.iv_clear_text);
        etSearch.setTextSize(SizeUtils.px2sp(getResources().getDimension(R.dimen.size_sp_15)));
        etSearch.setTextColor(textColorSearch);
        etSearch.setHint(textHintSearch);
    }

    /**
     * 点击键盘中搜索键后的操作，用于接口回调
     */
    public void setOnClickSearch(SearchCallBack searchCallBack) {
        this.searchCallBack = searchCallBack;
        etSearch.setSearchCallBack(searchCallBack);
    }

    public void setOnClearSearch(ClearCallBack clearCallBack) {
        this.clearCallBack = clearCallBack;
    }

    public void setSearchEditClickAble() {
        etSearch.setVisibility(GONE);
        textView.setVisibility(VISIBLE);
    }

    /**
     * 清空输入
     */
    public void clearInput() {
        Logger.info("SearchView", "clear Input");
        if (clearCallBack != null) {
            etSearch.setText("");
            ivClear.setVisibility(GONE);
            clearCallBack.clearAciton();
        }
    }

    @Override
    public void clearFocus() {
        super.clearFocus();
        etSearch.clearFocus();
        etSearch.setNeedCallBack(true);
    }

    public EditTextClear getEditView() {
        return etSearch;
    }
}