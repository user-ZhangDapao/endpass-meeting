package com.sdcz.endpass.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;

import com.sdcz.endpass.R;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 功能：表情编码解析成为spannableString
 */
public class SpanStringUtils {

    private static final String TAG = "SpanStringUtils";

    private SpanStringUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 将包含表情编码解析成为表情字符
     */
    public static SpannableString parseEmotionString(Context context, String str) {

        SpannableString spannableString = new SpannableString(str);

        Pattern pattern = Pattern.compile("/:E[0-9]\\(.*?\\)");
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            int start = matcher.toMatchResult().start();
            int end = matcher.toMatchResult().end();
            String matched = matcher.group();

            //去除匹配字串括号外的内容
            String tempStr = matched.substring(matched.lastIndexOf('('), matched.lastIndexOf(')'));
            String name = tempStr.substring(1);
            ImageSpan span;
            if (name.contains("@")) {
                //如果是@xxx
                Bitmap bitmap = privateChatTag(context, name);
                span = new ImageSpan(context, bitmap);
            } else {
                int resId = getResource(name.toLowerCase());
                if (resId != 0) {
                    span = new ImageSpan(context, resId);
                } else {
                    //若不存在相关素材直接返回
                    continue;
                }
            }
            Log.i(TAG, "start:" + start + " end:" + end);
            spannableString.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }


    /**
     * 映射mipmap的表情资源文件
     */
    public static int getResource(String imageName) {
        Class mipmap = R.mipmap.class;
        try {
            // 表情名字为  bubble_weix.png
            Field field = mipmap.getField("bubble_" + imageName);
            return field.getInt(imageName);
        } catch (NoSuchFieldException noFile) {
            Log.i(TAG, "NoSuchFieldException");
        } catch (IllegalAccessException illegal) {
            Log.i(TAG, "IllegalAccessException");
        }
        return 0;
    }


    /**
     * 处理“私聊@”事件，将@xxx当做一个表情符来处理
     *
     * @param str     输入的内容
     * @param context 上下文
     * @return 将字符串转换为一个Bitmap
     */
    private static Bitmap privateChatTag(Context context, String str) {
        Paint paint = new Paint();
        paint.setTextSize(context.getResources().getDimension(R.dimen.text_size_18sp));
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(context.getResources().getColor(R.color.color_4479FF));
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        Bitmap bmp = Bitmap.createBitmap(rect.width() + 5, rect.height() + 5, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        canvas.drawText(str + " ", 0, rect.height() - fontMetrics.bottom, paint);
        return bmp;
    }
}
