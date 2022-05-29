package com.sdcz.endpass.widget.emoji;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

/**
 * 功能：监听表情的粘贴事件
 * Created by wangy on 2018/2/26.
 */

public class EmotionEditText extends androidx.appcompat.widget.AppCompatEditText {

    private EditablePasteListener pasteListener;

    public EmotionEditText(Context context) {
        super(context);
    }

    public EmotionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmotionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTextContextMenuItem(int id) {

        final int idPaste = android.R.id.paste;
        try {
            if (super.onTextContextMenuItem(id)) {
                if (pasteListener != null && id == idPaste) {
                    pasteListener.onEditStringPaste();
                }
                return true;
            }
        } catch (Exception ex) {
            Log.i("EmotionEditText", ex.getMessage());
        }

        return false;
    }


    public void setOnPasteListener(EditablePasteListener listener) {
        this.pasteListener = listener;
    }

    public interface EditablePasteListener {
        void onEditStringPaste();
    }
}
