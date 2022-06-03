package com.sdcz.endpass.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

public class MarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {

    private boolean isFrist = true;

    public MarqueeTextView(Context context) {
        super(context,null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 设置单行
        setSingleLine();
//        setMaxLines(1);
        //设置 Ellipsize，setMaxLines(1) 和 setEllipsize 冲突
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        //获取焦距
        setFocusable(true);
        //走马灯的重复次数，-1代表无限重复
        setMarqueeRepeatLimit(-1);
        //强制获得焦点
        setFocusableInTouchMode(true);

    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    /**
     * 使这个 View 永远获得焦距
     * @return true
     */
    @Override
    public boolean isFocused() {
        return true;
    }

    /**
     * 用于 EditText 存在时抢占焦点
     */
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if(focused){
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        }
    }

    /**
     * Window与Window间焦点发生改变时的回调
     * 解决 Dialog 抢占焦点问题
     * @param hasWindowFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if(hasWindowFocus){
            super.onWindowFocusChanged(hasWindowFocus);
        }
    }




    public void setMarqueeText(String str){
        if (!isFrist) {
            return;
        }
        setText(str);
        isFrist = false;
    }

    public void setMarqeeTrue(){
        isFrist = true;
    }

}
