package com.sdcz.endpass.widget;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.comix.meeting.entities.BaseShareBean;
import com.comix.meeting.entities.WhiteBoard;
import com.comix.meeting.interfaces.IWhiteBoardOperation;
import com.sdcz.endpass.SdkUtil;
import com.inpor.nativeapi.adaptor.RoomWndState;

/**
 * @author yinhui
 * @date create at 2020/12/2
 * @description
 */
public class MarkZoomWbView extends WhiteBoardView {

    private static final int DEFAULT_MAX_SCALE = 2;
    /**
     * ZoomImageView控件的宽度
     */
    private int imageViewWidth;
    /**
     * ZoomImageView控件的高度
     */
    private int imageViewHeight;
    /**
     * 单边最大放大倍数
     */
    private float maxScale = DEFAULT_MAX_SCALE;
    /**
     * 记录上次手指移动时的横坐标
     */
    private int lastMoveX;
    /**
     * 记录上次手指移动时的纵坐标
     */
    private int lastMoveY;
    /**
     * 记录手指按下时的横坐标
     */
    private int moveDownX;
    /**
     * 记录手指按下时的纵坐标
     */
    private int moveDownY;
    /**
     * 记录手指在横坐标方向上的移动距离
     */
    private int movedDistanceX;
    /**
     * 记录手指在纵坐标方向上的移动距离
     */
    private int movedDistanceY;
    /**
     * 记录两指同时放在屏幕上时，中心点的横坐标值
     */
    private float centerPointX;
    /**
     * 记录两指同时放在屏幕上时，中心点的纵坐标值
     */
    private float centerPointY;
    /**
     * 记录上次两指之间的距离
     */
    private float lastFingerDis;
    /**
     * 记录缩放系数
     */
    private float scaleByScreenInch = 1;

    private boolean isMorePointerUp = false;
    private boolean isUpOrDownFlipPage = false;
    private MarkZoomWbViewListener markZoomWbViewListener;

    public MarkZoomWbView(@NonNull Context context) {
        super(context);
    }

    public MarkZoomWbView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkZoomWbView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
    }

    public void setMarkZoomWbViewListener(MarkZoomWbViewListener listener) {
        this.markZoomWbViewListener = listener;
    }

    /**
     * 设置翻页方式
     *
     * @param upOrDownFlipPage true -> 上下翻页，false -> 左右翻页
     */
    public void setPageTurningMethod(boolean upOrDownFlipPage) {
        this.isUpOrDownFlipPage = upOrDownFlipPage;
    }

    /**
     * 设置双指移动距离与缩放大小之间的比例
     *
     * @param scaleByScreenInch 比例
     */
    public void setScaleByScreenInch(float scaleByScreenInch) {
        this.scaleByScreenInch = scaleByScreenInch;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            imageViewWidth = getWidth();
            imageViewHeight = getHeight();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (whiteBoard == null) {
            return false;
        }
        //统一处理多点触控的 按下和抬起
        int action = event.getAction() & event.getActionMasked();
        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            if (event.getPointerCount() == 2) {
                lastFingerDis = distanceBetweenFingers(event);
                IWhiteBoardOperation whiteBoardOperator = getWhiteBoardOperator();
                whiteBoardOperator.setFirstAndSecondPoint(whiteBoard,
                        (int) event.getX(0),
                        (int) event.getY(0),
                        (int) event.getX(1),
                        (int) event.getY(1));
            }

        } else if (action == MotionEvent.ACTION_POINTER_UP) {
            isMorePointerUp = true;
        }
        return mark ? markOnTouch(view, event) : notMarkOnTouch(event);
    }

    private boolean markOnTouch(View view, MotionEvent event) {
        boolean flag = false;
        switch (event.getAction() & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isMorePointerUp = false;
                moveDownX = (int) event.getX();
                moveDownY = (int) event.getY();
                flag = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1 && !isMorePointerUp) {
                    flag = true;
                } else if (event.getPointerCount() == 2) {
                    zoomMoveWbByTwoFinger(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                flag = true;
                break;
            default:
                break;
        }
        return !flag || super.onTouch(view, event);
    }

    private boolean notMarkOnTouch(MotionEvent event) {
        switch (event.getAction() & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isMorePointerUp = false;
                prepareScrollWb();
                lastMoveX = (int) event.getX();
                lastMoveY = (int) event.getY();
                moveDownX = lastMoveX;
                moveDownY = lastMoveY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    moveWb((int) event.getX() - lastMoveX, (int) event.getY() - lastMoveY);
                } else if (event.getPointerCount() == 2) {
                    zoomMoveWbByTwoFinger(event);
                }
                lastMoveX = (int) event.getX();
                lastMoveY = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP:
                lastMoveX = (int) event.getX();
                lastMoveY = (int) event.getY();
                if (whiteBoard == null || isMorePointerUp) {
                    return false;
                }
                if (isUpOrDownFlipPage) {
                    return upOrDownFlipPage(event);
                } else {
                    return leftOrRightFlipPage(event);
                }
            default:
                break;
        }
        return true;
    }

    private void prepareScrollWb() {
        if (whiteBoard == null) {
            BaseShareBean shareBean = SdkUtil.getWbShareManager().getActiveShareBean();
            if (shareBean != null && shareBean.getType() == RoomWndState.DataType.DATA_TYPE_WB) {
                whiteBoard = (WhiteBoard) shareBean;
            }
        }
    }

    /**
     * 双指缩放或移动白板
     */
    private void zoomMoveWbByTwoFinger(MotionEvent event) {
        zoomWb(event);

        int newFirstPointX = (int) event.getX(0);
        int newFirstPointY = (int) event.getY(0);
        int newSecondPointX = (int) event.getX(1);
        int newSecondPointY = (int) event.getY(1);
        //计算两指移动时，两个触控点在 X 或 Y 方向的矢量和
        Point firstPoint = whiteBoard.getFirstPoint();
        Point secondPoint = whiteBoard.getSecondPoint();
        if (firstPoint != null && secondPoint != null) {
            int moveX = ((newFirstPointX - firstPoint.x) + (newSecondPointX - secondPoint.x)) / 2;
            int moveY = ((newFirstPointY - firstPoint.y) + (newSecondPointY - secondPoint.y)) / 2;
            moveWb(moveX, moveY);

            //移动后，要记录两个触控点的坐标
            IWhiteBoardOperation whiteBoardOperator = getWhiteBoardOperator();
            whiteBoardOperator.setFirstAndSecondPoint(whiteBoard, newFirstPointX, newFirstPointY,
                    newSecondPointX, newSecondPointY);
        }
    }

    private void zoomWb(MotionEvent event) {
        float fingerDis = distanceBetweenFingers(event);

        //大屏幕设备，通过缩放系数来重新计算两指间的距离，然后算出缩放倍数。
        //以此来降低 真实双指间距离变化带来的抖动影响（大多时候 scaleByScreenInch 大于1）
        //计算后的两指间距离
        float fingerDisByInch = lastFingerDis + (fingerDis - lastFingerDis) / scaleByScreenInch;
        //双指缩放的倍数
        float fingersZoomScale = fingerDisByInch / lastFingerDis;
        //双指缩放后 图片的总缩放倍数
        final float scale = whiteBoard.getScale();
        float tempScale = scale * fingersZoomScale;

        //防止 maxScale 比 图片适应窗口宽度后的scale 小
        float fitImageWidthScale = (float) imageViewWidth / (float) whiteBoard.getBitmapWidth();
        maxScale = fitImageWidthScale > maxScale ? fitImageWidthScale : DEFAULT_MAX_SCALE;

        boolean isUpdateOffset = true;
        if (scale > maxScale) {
            if (tempScale > scale) {
                tempScale = scale;
                isUpdateOffset = false;
            }
        } else {
            //如果白板scale 比maxScale 小，当手指放大后，tempScale 不能超过 maxScale
            if (tempScale > maxScale) {
                tempScale = maxScale;
                isUpdateOffset = false;
            }
        }
        IWhiteBoardOperation whiteBoardOperator = getWhiteBoardOperator();
        if (whiteBoardOperator.scaleIsOverLimited(whiteBoard, tempScale)) {
            return;
        }

        if (isUpdateOffset) {
            centerPointBetweenFingers(event);
            updateZoomOffset(tempScale, fingersZoomScale);
        }

        whiteBoard.setScale(tempScale);
        lastFingerDis = fingerDis;
    }

    private void updateZoomOffset(float tempScale, float fingersZoomScale) {
        final int bitmapWidth = whiteBoard.getBitmapWidth();
        final int bitmapHeight = whiteBoard.getBitmapHeight();
        final float scale = whiteBoard.getScale();
        //计算 X方向 偏移
        float translateX;
        float currentBitmapWidth = bitmapWidth * scale;
        float scaledBitmapWidth = bitmapWidth * tempScale;
        // 如果当前图片宽度小于屏幕宽度，则按屏幕中心的横坐标进行水平缩放。否则按两指的中心点的横坐标进行水平缩放
        if (currentBitmapWidth < imageViewWidth) {
            translateX = (imageViewWidth - scaledBitmapWidth) / 2f;
        } else {
            // centerPointX - whiteBoard.offsetX 为中心点到图片左边缘的距离，乘以缩放倍数为缩放后的距离。画图可能更好理解
            translateX = centerPointX - (centerPointX - whiteBoard.getOffsetX()) * fingersZoomScale;
            // 图片宽度超过屏幕宽度后，X方向的偏移量只能是负值，且不会比 imageViewWidth - scaledBitmapWidth 更小
            if (translateX > 0) {
                translateX = 0;
            } else if (translateX < imageViewWidth - scaledBitmapWidth) {
                translateX = imageViewWidth - scaledBitmapWidth;
            }
        }

        //计算 Y方向 偏移
        float translateY;
        float currentBitmapHeight = bitmapHeight * scale;
        float scaledBitmapHeight = bitmapHeight * tempScale;
        // 如果当前图片高度小于屏幕高度，则按屏幕中心的纵坐标进行垂直缩放。否则按两指的中心点的纵坐标进行垂直缩放
        if (currentBitmapHeight < imageViewHeight) {
            translateY = (imageViewHeight - scaledBitmapHeight) / 2f;
        } else {
            // centerPointY - whiteBoard.offsetY 为中心点到图片上边缘的距离
            translateY = centerPointY - (centerPointY - whiteBoard.getOffsetY()) * fingersZoomScale;
            if (translateY > 0) {
                translateY = 0;
            } else if (translateY < imageViewHeight - scaledBitmapHeight) {
                translateY = imageViewHeight - scaledBitmapHeight;
            }
        }

        whiteBoard.setOffsetX((int) translateX);
        whiteBoard.setOffsetY((int) translateY);
        invalidate();
    }

    /**
     * 非标记时，单指移动白板
     */
    private boolean moveWb(int movedDistanceX, int movedDistanceY) {
        if (isMorePointerUp) {
            return false;
        }

        this.movedDistanceX = movedDistanceX;
        this.movedDistanceY = movedDistanceY;
        return updateMoveOffset();
    }

    private boolean updateMoveOffset() {
        boolean isDragTop = false;
        boolean isDragBottom = false;
        boolean isDragLeft = false;
        boolean isDragRight = false;

        int movedOffsetX = whiteBoard.getOffsetX() + movedDistanceX;
        int movedOffsetY = whiteBoard.getOffsetY() + movedDistanceY;
        final float scale = whiteBoard.getScale();
        int bitmapScaledWidth = (int) (whiteBoard.getBitmapWidth() * scale + 0.5);
        int bitmapScaledHeight = (int) (whiteBoard.getBitmapHeight() * scale + 0.5);

        // 进行边界检查，不允许将图片拖出边界
        if (movedOffsetX > 0 || movedOffsetX < imageViewWidth - bitmapScaledWidth) {
            if (movedDistanceX >= 0) {
                isDragLeft = true;
            } else {
                isDragRight = true;
            }
            movedDistanceX = 0;
        }
        if (movedOffsetY > 0 || movedOffsetY < imageViewHeight - bitmapScaledHeight) {
            if (movedDistanceY >= 0) {
                isDragTop = true;
            } else {
                isDragBottom = true;
            }
            movedDistanceY = 0;
        }
        int offsetX = whiteBoard.getOffsetX() + movedDistanceX;
        int offsetY = whiteBoard.getOffsetY() + movedDistanceY;
        whiteBoard.setOffsetX(offsetX);
        whiteBoard.setOffsetY(offsetY);
        IWhiteBoardOperation whiteBoardOperator = getWhiteBoardOperator();
        whiteBoardOperator.setDragDirection(whiteBoard, isDragLeft, isDragTop, isDragRight, isDragBottom);
        invalidate();
        return true;
    }

    private boolean upOrDownFlipPage(MotionEvent event) {
        if (event.getPointerCount() != 1 || isMorePointerUp) {
            return true;
        }
        int endX = (int) event.getX();
        int distanceX = endX - moveDownX;
        int endY = (int) event.getY();
        int distanceY = endY - moveDownY;
        if (Math.abs(distanceX) < 6 && Math.abs(distanceY) < 6) {
            if (markZoomWbViewListener != null) {
                markZoomWbViewListener.onWbSingleTap(event);
            }
            return false;
        } else if (Math.abs(distanceY) > 200 && whiteBoard != null
                && markZoomWbViewListener != null) {
            if (distanceY < 0) {
                markZoomWbViewListener.switchWbNextPage(whiteBoard);
            } else if (distanceY > 0) {
                markZoomWbViewListener.switchWbPreviousPage(whiteBoard);
            }
            return true;
        }
        return false;
    }

    private boolean leftOrRightFlipPage(MotionEvent event) {
        if (event.getPointerCount() != 1 || isMorePointerUp) {
            return true;
        }
        int endX = (int) event.getX();
        int distanceX = endX - moveDownX;
        int endY = (int) event.getY();
        int distanceY = endY - moveDownY;
        Log.i("MarkZoomWbView", "ACTION_UP endX=" + endX + " downX=" + moveDownX + " value=" + distanceX
                + "Right= " + whiteBoard.isBitmapDragRight());
        if (Math.abs(distanceX) < 6 && Math.abs(distanceY) < 6) {
            if (markZoomWbViewListener != null) {
                markZoomWbViewListener.onWbSingleTap(event);
            }
            return false;
        } else if (Math.abs(distanceX) > 200 && whiteBoard != null
                && markZoomWbViewListener != null) {
            if (distanceX < 0) {
                markZoomWbViewListener.switchWbNextPage(whiteBoard);
            } else if (distanceX > 0) {
                markZoomWbViewListener.switchWbPreviousPage(whiteBoard);
            }
            return true;
        }
        return false;
    }

    /**
     * 计算两个手指之间的距离。
     */
    private float distanceBetweenFingers(MotionEvent event) {
        float disX = Math.abs(event.getX(0) - event.getX(1));
        float disY = Math.abs(event.getY(0) - event.getY(1));
        return (float) Math.sqrt(disX * disX + disY * disY);
    }

    /**
     * 计算两个手指之间中心点的坐标。
     */
    private void centerPointBetweenFingers(MotionEvent event) {
        float point0X = event.getX(0);
        float point0Y = event.getY(0);
        float point1X = event.getX(1);
        float point1Y = event.getY(1);
        centerPointX = (point0X + point1X) / 2;
        centerPointY = (point0Y + point1Y) / 2;
    }

    /**
     * 左右翻页回调
     */
    public interface MarkZoomWbViewListener {

        /**
         * 点击
         *
         * @param event 事件
         */
        void onWbSingleTap(MotionEvent event);

        /**
         * 切换白板下一页
         *
         * @param wb 白板
         */
        void switchWbNextPage(WhiteBoard wb);

        /**
         * 切换白板上一页
         *
         * @param wb 白板
         */
        void switchWbPreviousPage(WhiteBoard wb);
    }
}
