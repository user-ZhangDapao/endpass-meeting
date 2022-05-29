package com.sdcz.endpass.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class MarqueeTextView extends AppCompatTextView {
    /**
     * The default text size
     */
    private static final float DEF_TEXT_SIZE = 25.0F;
    /**
     * The default text scroll speed
     */
    private float speed = 1.5F;
    /**
     * The default set as auto scroll
     */
    private final boolean isScroll = true;
    private Paint paint;
    /**
     * This is to display the content
     */
    private String text;

    /**
     * Draw the starting point of the X coordinate
     */
    private float coordinateX;
    /**
     * Draw the starting point of the Y coordinate
     */
    private float coordinateY;
    /**
     * This is text width
     */
    private float textWidth;
    /**
     * This is View width
     */
    private int viewWidth;

    private int marqueeRepeatLimit;
    private int currentRepeatCount = 0;
    private OnMarqueeFinishListener onMarqueeFinishListener;

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    public MarqueeTextView(Context context) {
        super(context);
        init();

    }

    /**
     * 构造方法
     *
     * @param context 上下文
     * @param attrs   参数集
     */
    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    /**
     * 含三个参数的构造方法
     *
     * @param context      上下文
     * @param attrs        参数集
     * @param defStyleAttr 默认参数
     */
    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    /**
     * Initializes the related parameters
     */
    private void init() {

        if (TextUtils.isEmpty(text)) {
            text = "";
        }
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(DEF_TEXT_SIZE);
    }

    /**
     * 设置文字
     *
     * @param text 需要设置的文字
     */
    public void setText(String text) {
        this.text = text;
        if (TextUtils.isEmpty(this.text)) {
            this.text = "";
        }
        requestLayout();
        invalidate();
    }

    /**
     * Set the text size, if this value is < 0, set to the default size
     *
     * @param textSize 字体大小
     */
    @Override
    public void setTextSize(float textSize) {
        paint.setTextSize(textSize <= 0 ? DEF_TEXT_SIZE : textSize);
        requestLayout();
        invalidate();
    }

    /**
     * 设置字体颜色
     *
     * @param textColor 字体颜色
     */
    @Override
    public void setTextColor(int textColor) {
        paint.setColor(textColor);
        invalidate();
    }

    /**
     * 获取字体滚动速度
     *
     * @return 字体滚动速度
     */
    public float getTextSpeed() {
        return speed;
    }

    /**
     * Set the text scrolling speed, if the value < 0, set to the default is 0
     *
     * @param speed If this value is 0, then stop scrolling
     */
    public void setTextSpeed(float speed) {
        this.speed = speed < 0 ? 0 : speed;
        invalidate();
    }

    /**
     * 获取是否滚动
     *
     * @return 是否滚动
     */
    public boolean isScroll() {
        return isScroll;
    }

    /**
     * 设置是否滚动
     *
     * @param isScroll 是否滚动
     */
    public void setScroll(boolean isScroll) {
        //this.isScroll = isScroll;
        if (!isScroll) {
            marqueeRepeatLimit = -1;
        }

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        textWidth = paint.measureText(text);
        coordinateY = getPaddingTop() + Math.abs(paint.ascent()) + 5;
        viewWidth = measureWidth(widthMeasureSpec);
        int viewHeight = measureHeight(heightMeasureSpec);

        //If you do not call this method, will be thrown "IllegalStateException"
        setMeasuredDimension(viewWidth, viewHeight);
    }

    /**
     * 测量宽度
     *
     * @param measureSpec 测量值
     * @return 返回测量值
     */
    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) paint.measureText(text) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    /**
     * 测量高度
     *
     * @param measureSpec 测量值
     * @return 返回测量值
     */
    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) paint.getTextSize() + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (marqueeRepeatLimit == -1) {
            canvas.drawText(text, coordinateX, coordinateY, paint);
        } else if (marqueeRepeatLimit == 0) {
            canvas.drawText(text, (viewWidth - textWidth) / 2, coordinateY, paint);
        } else if (marqueeRepeatLimit >= currentRepeatCount) {
            canvas.drawText(text, coordinateX, coordinateY, paint);
        } else {
            setText("");
            if (onMarqueeFinishListener != null) {
                onMarqueeFinishListener.onMarqueeFinish();
            }
        }

        if (!isScroll) {
            return;
        }

        coordinateX -= speed;

        if (Math.abs(coordinateX) > textWidth && coordinateX < 0) {
            coordinateX = viewWidth;
            currentRepeatCount++;
        }

        invalidate();

    }

    @Override
    public void setMarqueeRepeatLimit(int marqueeRepeatLimit) {
        this.marqueeRepeatLimit = marqueeRepeatLimit;
        currentRepeatCount = 1;
        coordinateX = viewWidth;
    }

    /**
     * setOnMarqueeFinishListener
     *
     * @param onMarqueeFinishListener Listener
     */
    public void setOnMarqueeFinishListener(OnMarqueeFinishListener onMarqueeFinishListener) {
        if (onMarqueeFinishListener != null) {
            this.onMarqueeFinishListener = onMarqueeFinishListener;
        }
    }

    public interface OnMarqueeFinishListener {
        void onMarqueeFinish();
    }
}
