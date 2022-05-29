package com.sdcz.endpass.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sdcz.endpass.util.SpanStringUtils;

/**
 * @Author wuyr
 * @Date 2020/12/15 17:19
 * @Description
 */
public class ChatEmotionEditText extends androidx.appcompat.widget.AppCompatEditText {
    private final Context context;

    public ChatEmotionEditText(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public ChatEmotionEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    /**
     * 设置表情符号内容
     *
     * @param emotionStr 表情符号编码
     */
    public void setEmotionText(CharSequence emotionStr) {
        if (TextUtils.isEmpty(emotionStr)) {
            return;
        }
        final String deleteStr = "/:(delete)";
        if (deleteStr.equals(emotionStr)) {
            // 如果点击了最后一个回退按钮,则调用删除键事件
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        } else {
            // 如果点击了表情,则添加到输入框中
            //String emotionName = emotionGvAdapter.getItem(position);

            // 获取当前光标位置,在指定位置上添加表情图片文本
            int curPosition = getSelectionStart();
            String contentStr = getText() != null ? getText().toString() : "";
            StringBuilder sb = new StringBuilder(contentStr);
            sb.insert(curPosition, emotionStr);

            // 特殊文字处理,将表情等转换一下
            setText(SpanStringUtils.parseEmotionString(context, sb.toString()));

            // 将光标设置到新增完表情的右侧
            setSelection(curPosition + emotionStr.length());
        }
    }
}
