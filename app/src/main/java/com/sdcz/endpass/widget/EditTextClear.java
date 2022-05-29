package com.sdcz.endpass.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.sdcz.endpass.R;
import com.sdcz.endpass.callback.SearchCallBack;


/**
 * 带搜索和删除的输入框
 *
 * @author liwl
 * @date 2019/01/22
 */
public class EditTextClear extends androidx.appcompat.widget.AppCompatEditText implements TextWatcher {
    /**
     * 步骤1：定义左侧搜索图标 & 一键删除图标
     */

    private Drawable searchDrawable;
    private SearchCallBack searchCallBack;
    private Boolean isNeedCallBack = true;

    public EditTextClear(Context context) {
        super(context);
        init();
    }

    public EditTextClear(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditTextClear(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setNeedCallBack(Boolean callBack) {
        isNeedCallBack = callBack;
    }

    public void setSearchCallBack(SearchCallBack searchCallBack) {
        this.searchCallBack = searchCallBack;
    }

    private void init() {
        searchDrawable = getResources().getDrawable(R.drawable.search_icon_zoom);
        setCompoundDrawablesWithIntrinsicBounds(searchDrawable, null,
                null, null);
        addTextChangedListener(this);
    }


    @Override
    public void beforeTextChanged(CharSequence sequence, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
    }

    @Override
    public void afterTextChanged(Editable sequence) {
        if (isNeedCallBack && searchCallBack != null && hasFocus()) {
            searchCallBack.searchAciton(sequence.toString() + "");
        }
    }


    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

}