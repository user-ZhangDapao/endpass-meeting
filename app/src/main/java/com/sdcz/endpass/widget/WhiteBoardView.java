package com.sdcz.endpass.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.comix.meeting.ToolIndex;
import com.comix.meeting.entities.WhiteBoard;
import com.comix.meeting.interfaces.IWhiteBoardOperation;
import com.sdcz.endpass.SdkUtil;

/**
 * @author yinhui
 * @date create at 2020/12/2
 * @description
 */
public class WhiteBoardView extends AppCompatImageView implements View.OnTouchListener {

    private static final String TAG = "WhiteBoardView";

    protected WhiteBoard whiteBoard = null;
    /**
     * 是否是标记状态
     */
    protected boolean mark = false;

    /**
     * 当前View在ViewGroud的position
     */
    private int position;

    private Rect drawRect;

    /**
     * 画笔的宽度
     */
    private byte strokeWidth;

    /**
     * 画笔的颜色
     */
    private int color;

    /**
     * 默认为画笔模式
     */
    private int drawTool = ToolIndex.WB_TOOL_PENCIL;

    private Matrix globalMatrix;

    private IWhiteBoardOperation operator;

    public WhiteBoardView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public WhiteBoardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WhiteBoardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context) {
        setOnTouchListener(this);
        drawRect = new Rect();
        whiteBoard = new WhiteBoard();
        // 设置默认画笔宽度为10像素
        setStrokeWidth((byte) 10);
        // 设置默认画笔颜色
        setColor(Color.BLACK);
        globalMatrix = new Matrix();
    }

    /**
     * 获取当前View在ViewGroup的position
     *
     * @return 当前View在ViewGroup的position
     */
    public int getPosition() {
        return position;
    }

    /**
     * 设置当前View在ViewGroup的position
     *
     * @param position 当前View在ViewGroup的position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * 获取当前白板
     *
     * @return 白板
     */
    public WhiteBoard getWhiteBoard() {
        return whiteBoard;
    }

    /**
     * 设置白板
     *
     * @param whiteBoard 白板
     */
    public void setWhiteBoard(WhiteBoard whiteBoard) {
        this.whiteBoard = whiteBoard;
    }

    /**
     * 设置画笔的宽度
     *
     * @param width 画笔宽度值
     */
    public void setStrokeWidth(byte width) {
        this.strokeWidth = width;
    }

    public byte getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * 设置画笔的颜色 (传入的颜色值是Android识别的)
     *
     * @param color 颜色值
     */
    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
    /**
     * 设置画笔工具是pencil还是eraser
     *
     * @param drawTool 1,pencil 2,eraser
     */
    public void setDrawModel(int drawTool) {
        this.drawTool = drawTool;
    }

    public int getDrawModel() {
        return drawTool;
    }

    public boolean isMark() {
        return mark;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (whiteBoard != null) {
            whiteBoard.setResetScale(true);
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (whiteBoard == null || whiteBoard.getId() < 0) {
            return;
        }
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        drawRect.set(0, 0, width, height);
        globalMatrix.set(getImageMatrix());
        IWhiteBoardOperation whiteBoardOperator = getWhiteBoardOperator();
        if (whiteBoardOperator != null) {
            whiteBoardOperator.onDraw(whiteBoard, canvas, globalMatrix, drawRect);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (whiteBoard == null) {
            return false;
        }
        IWhiteBoardOperation whiteBoardOperator = getWhiteBoardOperator();
        if (whiteBoardOperator != null) {
            whiteBoardOperator.onTouch(whiteBoard, event, drawTool, color, strokeWidth);
        }
        return true;
    }

    protected IWhiteBoardOperation getWhiteBoardOperator() {
        if (operator == null) {
            operator = SdkUtil.getWbShareManager().getCurrentWhiteBoardOperation();
        }
        return operator;
    }


    /**
     * 获取白板Width
     *
     * @return Width
     */
    public int getWhiteBoardBitmapWidth() {
        if (whiteBoard == null) {
            return 0;
        }
        return whiteBoard.getBitmapWidth();
    }

    /**
     * 获取白板Height
     *
     * @return Height
     */
    public int getWhiteBoardBitmapHeight() {
        if (whiteBoard == null) {
            return 0;
        }
        return whiteBoard.getBitmapHeight();
    }


    /**
     * 获取白板 OffsetX
     *
     * @return OffsetX
     */
    public int getWhiteBoardOffsetX() {
        if (whiteBoard == null) {
            return 0;
        }
        return whiteBoard.getOffsetX();
    }

    /**
     * 获取白板 OffsetY
     *
     * @return OffsetY
     */
    public int getWhiteBoardOffsetY() {
        if (whiteBoard == null) {
            return 0;
        }

        return whiteBoard.getOffsetY();
    }

    /**
     * 获取白板 Scale
     *
     * @return Scale
     */
    public float getWhiteBoardScale() {
        if (whiteBoard == null) {
            return 0;
        }
        return whiteBoard.getScale();
    }


}
