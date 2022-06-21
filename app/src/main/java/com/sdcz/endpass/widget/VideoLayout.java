package com.sdcz.endpass.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.blankj.utilcode.util.ScreenUtils;
import com.comix.meeting.entities.LayoutType;
import com.comix.meeting.entities.MeetingInfo;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.model.UiEntrance;
import com.sdcz.endpass.model.VideoController;
import com.inpor.nativeapi.adaptor.RoomWndState;

import java.util.HashSet;
import java.util.List;

/**
 * @author yinhui
 * @date create at 2019/2/25
 * @description
 */
public class VideoLayout extends ViewGroup implements VideoController.VideoControllerListener {


    private boolean isfirst = true;
    private static final String TAG = "VideoLayout";
    public static final float DEFAULT_VIEW_SCALE = 16 / 9f;
    public static final float DEFAULT_VIEW_SCALE_1 = 4 / 3f;
    private static final int STANDARD_VISIBLE_COUNT = 3;
    private LayoutType layoutType;
    private RoomWndState.SplitStyle splitStyle;
    private VideoScreenView fullView;
    private boolean hasWindowFullScreen;
    private List<VideoScreenView> views;
    /**
     * 视频数量，有窗口不代表有视频
     */
    private int videoCount;

    private int measureWidth;
    private int measureHeight;
    private int originalWidth;
    private int originalHeight;

    /**
     * 这个scroller是为了平滑滑动
     */
    private final Scroller scroller;
    /**
     * 滑动速度追踪器
     */
    private VelocityTracker velocityTracker;

    private float lastInterceptX;
    private float lastInterceptY;
    /**
     * 是否正在滚动
     */
    private boolean isScrolling;
    /**
     * 手指按下时的getScrollY
     */
    private int scrollStart;
    /**
     * 手指抬起时的getScrollY
     */
    private int scrollEnd;
    private int lastX;
    /**
     * 记录移动时的Y
     */
    private int lastY;
    private int scrollPosition;

    public VideoLayout(Context context) {
        super(context);
        scroller = new Scroller(context);
    }

    /**
     *
     */
    public void subscribe() {
        VideoController.getInstance().setControllerListener(this);
        VideoController.getInstance().init(getContext());
    }

    /**
     *
     */
    public void unSubscribe() {
        VideoController.getInstance().setControllerListener(null);
        VideoController.release();
    }

    void setMeetingInfo(MeetingInfo meetingInfo) {
        VideoController.getInstance().checkZOrder(meetingInfo);
        if (layoutType != meetingInfo.layoutType || splitStyle != meetingInfo.splitStyle) {
            layoutType = meetingInfo.layoutType;
            splitStyle = meetingInfo.splitStyle;
            if (getScrollX() != 0 || getScaleY() != 0) {
                scrollTo(0, 0);
            } else {
                onScrollChanged(0, 0, 0, 0);
            }
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        originalHeight = MeasureSpec.getSize(heightMeasureSpec);
        measureWidth = originalWidth;
        measureHeight = originalHeight;
        if (layoutType != null && views != null) {
            if (hasWindowFullScreen) {
                calculateInFullScreen(widthMeasureSpec, heightMeasureSpec);
            } else {
                if (layoutType == LayoutType.CULTIVATE_LAYOUT) {
                    adjustCultivateModeParam(widthMeasureSpec, heightMeasureSpec);
                } else if (layoutType == LayoutType.STANDARD_LAYOUT) {
                    adjustStandardModeParam(widthMeasureSpec, heightMeasureSpec);
                } else if (layoutType == LayoutType.VIDEO_LAYOUT) {
                    adjustSplitStyleParam(widthMeasureSpec, heightMeasureSpec);
                }
            }
        }
        setMeasuredDimension(measureWidth, measureHeight);
        Log.i(TAG, originalWidth + " : " + originalHeight + ", " + measureWidth + " : " + measureHeight);
    }


    private void calculateInFullScreen(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < views.size(); i++) {
            VideoScreenView screen = views.get(i);
            LayoutParams params = screen.getLayoutParams();
            if (screen == fullView) {
                params.width = originalWidth;
                params.height = originalHeight;
            } else {
                params.width = VariableLayout.DEFAULT_SIZE;
                params.height = VariableLayout.DEFAULT_SIZE;
            }
            measureChild(screen, widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void adjustCultivateModeParam(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < views.size(); i++) {
            VideoScreenView screen = views.get(i);
            LayoutParams params = screen.getLayoutParams();
            if (i == 0) {
                params.width = originalWidth;
                params.height = originalHeight;
            } else {
                params.width = VariableLayout.DEFAULT_SIZE;
                params.height = VariableLayout.DEFAULT_SIZE;
            }
            measureChild(screen, widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void adjustStandardModeParam(int widthMeasureSpec, int heightMeasureSpec) {
        final boolean isPortrait = ScreenUtils.isPortrait();
        if (isPortrait) {
            for (int i = 0; i < views.size(); i++) {
                VideoScreenView screen = views.get(i);
                LayoutParams params = screen.getLayoutParams();
                params.width = originalWidth / 3;
                params.height = originalHeight;
                measureChild(screen, widthMeasureSpec, heightMeasureSpec);
            }
            // 重新调整大小
            measureWidth = originalWidth / 3 * views.size();
        } else {
            for (int i = 0; i < views.size(); i++) {
                VideoScreenView screen = views.get(i);
                LayoutParams params = screen.getLayoutParams();
                params.width = originalWidth;
                params.height = originalHeight / 3;
                measureChild(screen, widthMeasureSpec, heightMeasureSpec);
            }
            // 重新调整大小
            measureHeight = originalHeight / 3 * views.size();
        }
    }

    private void adjustSplitStyleParam(int widthMeasureSpec, int heightMeasureSpec) {
        final boolean isPortrait = ScreenUtils.isPortrait();
        for (int i = 0; i < views.size(); i++) {
            VideoScreenView screen = views.get(i);
            LayoutParams params = screen.getLayoutParams();
            switch (splitStyle) {
                case SPLIT_STYLE_1:
                    if (i == 0) {
                        params.width = originalWidth;
                        params.height = originalHeight;
                    } else {
                        params.width = VariableLayout.DEFAULT_SIZE;
                        params.height = VariableLayout.DEFAULT_SIZE;
                    }
                    break;
                case SPLIT_STYLE_2:
                    if (i < 2) {
                        if (isPortrait) {
                            params.width = originalWidth;
                            params.height = originalHeight / 2;
                        } else {
                            params.width = originalWidth / 2;
                            params.height = originalHeight;
                        }
                    } else {
                        params.width = VariableLayout.DEFAULT_SIZE;
                        params.height = VariableLayout.DEFAULT_SIZE;
                    }
                    break;
                case SPLIT_STYLE_P_IN_P:
                    if (i == 0) {
                        params.width = originalWidth;
                        params.height = originalHeight;
                    } else if (i == 1) {
                        if (videoCount == 0 || videoCount >= 2) {
                            params.width = originalWidth / 3;
                            params.height = originalHeight / 3;
                        } else {
                            params.width = VariableLayout.DEFAULT_SIZE;
                            params.height = VariableLayout.DEFAULT_SIZE;
                        }
                    } else {
                        params.width = VariableLayout.DEFAULT_SIZE;
                        params.height = VariableLayout.DEFAULT_SIZE;
                    }
                    break;
                case SPLIT_STYLE_4:
                    if (i < 4) {
                        params.width = originalWidth / 2;
                        params.height = originalHeight / 2;
                    } else {
                        params.width = VariableLayout.DEFAULT_SIZE;
                        params.height = VariableLayout.DEFAULT_SIZE;
                    }
                    break;
                default:
                    // 其他布局全部作为6分屏
                    if (i < 1) {
                        params.width = originalWidth * 2 / 3;
                        params.height = originalHeight * 2 / 3;
                    } else if (i < 6) {
                        params.width = originalWidth / 3;
                        params.height = originalHeight / 3;
                    } else {
                        params.width = VariableLayout.DEFAULT_SIZE;
                        params.height = VariableLayout.DEFAULT_SIZE;
                    }
                    break;
            }
            measureChild(screen, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (layoutType == null || views == null) {
            return;
        }
        if (hasWindowFullScreen) {
            layoutOneScreenMode();
            return;
        }
        Log.e("TAG*TAG*TAG*TAG", views.size() + "");

//        if (layoutType == LayoutType.CULTIVATE_LAYOUT) {
//            layoutOneScreenMode();
//        } else if (layoutType == LayoutType.STANDARD_LAYOUT) {
//            layoutStandardMode();
//        } else if (splitStyle == RoomWndState.SplitStyle.SPLIT_STYLE_1) {
//            layoutOneScreenMode();
//        } else if (splitStyle == RoomWndState.SplitStyle.SPLIT_STYLE_2) {
//            layoutTwoScreenMode();
//        } else if (splitStyle == RoomWndState.SplitStyle.SPLIT_STYLE_P_IN_P) {
//            layoutPicInPicMode();
//        } else if (splitStyle == RoomWndState.SplitStyle.SPLIT_STYLE_4) {
//            layoutFourScreenMode();
//        } else {
//            // 其他布局全部作为6分屏
//            layoutSixScreenMode();
//        }
        layoutPicInPicMode();

        if(isfirst){
            SdkUtil.getMeetingManager().setMeetingLayoutType(LayoutType.VIDEO_LAYOUT,
                    RoomWndState.SplitStyle.SPLIT_STYLE_P_IN_P);
            isfirst = false;
            requestLayout();
        }

    }

    /**
     * 布局一分屏/培训模式
     */
    private void layoutOneScreenMode() {
        for (int i = 0; i < views.size(); i++) {
            VideoScreenView screen = views.get(i);
            LayoutParams lp = screen.getLayoutParams();
            layoutChild(screen, 0, 0, lp.width, lp.height);
            screen.setDisplayMode(VideoScreenView.DISPLAY_MODE_RATIO_INTEGRITY);
        }
    }

    /**
     * 布局标准模式
     */
    private void layoutStandardMode() {
        boolean isPortrait = ScreenUtils.isPortrait();
        if (isPortrait) {
            for (int i = 0; i < views.size(); i++) {
                VideoScreenView screen = views.get(i);
                LayoutParams lp = screen.getLayoutParams();
                int left = i * lp.width;
                int right = left + lp.width;
                layoutChild(screen, left, 0, right, lp.height);
                screen.setDisplayMode(VideoScreenView.DISPLAY_MODE_RATIO_CUT);
            }
        } else {
            for (int i = 0; i < views.size(); i++) {
                VideoScreenView screen = views.get(i);
                LayoutParams lp = screen.getLayoutParams();
                int top = i * lp.height;
                int bottom = top + lp.height;
                layoutChild(screen, 0, top, lp.width, bottom);
                screen.setDisplayMode(VideoScreenView.DISPLAY_MODE_RATIO_CUT);
            }
        }
    }

    /**
     * 布局二分屏模式
     */
    private void layoutTwoScreenMode() {
        boolean isPortrait = ScreenUtils.isPortrait();
        for (int i = 0; i < views.size(); i++) {
            VideoScreenView screen = views.get(i);
            LayoutParams lp = screen.getLayoutParams();
            if (i < 2) {
                if (isPortrait) {
                    int top = lp.height * i;
                    layoutChild(screen, 0, top, lp.width, top + lp.height);
                } else {
                    int left = lp.width * i;
                    layoutChild(screen, left, 0, left + lp.width, lp.height);
                }
            } else {
                layoutChild(screen, 0, 0, lp.width, lp.height);
            }
            screen.setDisplayMode(VideoScreenView.DISPLAY_MODE_RATIO_CUT);
        }
    }

    /**
     * 布局画中画模式
     */
    private void layoutPicInPicMode() {
        for (int i = 0; i < views.size(); i++) {
            VideoScreenView screen = views.get(i);
            LayoutParams lp = screen.getLayoutParams();
            if (i == 0) {
                layoutChild(screen, 0, 0, lp.width, lp.height);
            } else {
                layoutChild(screen, originalWidth - lp.width,
                        originalHeight - lp.height, originalWidth, originalHeight);
            }
            screen.setDisplayMode(VideoScreenView.DISPLAY_MODE_RATIO_CUT);
        }
    }


    /**
     * 布局四分屏模式
     */
    private void layoutFourScreenMode() {
        for (int i = 0; i < views.size(); i++) {
            VideoScreenView screen = views.get(i);
            LayoutParams lp = screen.getLayoutParams();
            if (i < 4) {
                int left = (i % 2) * lp.width;
                int top = (i / 2) * lp.height;
                int right = left + lp.width;
                int bottom = top + lp.height;
                layoutChild(screen, left, top, right, bottom);
            } else {
                layoutChild(screen, 0, 0, lp.width, lp.height);
            }
            screen.setDisplayMode(VideoScreenView.DISPLAY_MODE_RATIO_CUT);
        }
    }

    /**
     * 布局六分屏模式
     */
    private void layoutSixScreenMode() {
        final int headWidth = originalWidth * 2 / 3;
        final int headHeight = originalHeight * 2 / 3;
        for (int i = 0; i < views.size(); i++) {
            VideoScreenView screen = views.get(i);
            LayoutParams lp = screen.getLayoutParams();
            switch (i) {
                case 0:
                    layoutChild(screen, 0, 0, lp.width, lp.height);
                    break;
                case 1:
                    layoutChild(screen, headWidth, 0, originalWidth, lp.height);
                    break;
                case 2:
                    layoutChild(screen, headWidth, lp.height, originalWidth, originalHeight - lp.height);
                    break;
                case 3:
                    layoutChild(screen, 0, headHeight, lp.width, originalHeight);
                    break;
                case 4:
                    layoutChild(screen, lp.width, headHeight, originalWidth - lp.width, originalHeight);
                    break;
                case 5:
                    layoutChild(screen, originalWidth - lp.width, headHeight, originalWidth, originalHeight);
                    break;
                default:
                    layoutChild(screen, originalWidth - lp.width, originalHeight - lp.height,
                            originalWidth, originalHeight);
                    break;
            }
            screen.setDisplayMode(VideoScreenView.DISPLAY_MODE_RATIO_CUT);
        }
    }


    private long lastOnUpTime;
    private long onClickDownTime;
    private Runnable task;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            onClickDownTime = System.currentTimeMillis();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            //处理双击事件
            long currentUpTime = System.currentTimeMillis();
            long differentUpTime = currentUpTime - lastOnUpTime;
            lastOnUpTime = currentUpTime;

            if (differentUpTime < 180) {
                removeCallbacks(task);
                return super.dispatchTouchEvent(ev);
            }

            long differentTime = currentUpTime - onClickDownTime;
            onClickDownTime = 0;

            if (differentTime < 180) {
                task = createTask();
                postDelayed(task, 180);
            }

        }
        return super.dispatchTouchEvent(ev);
    }

    private Runnable createTask() {
        return () -> UiEntrance.getInstance().notify2MenuHelper();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (layoutType == null || layoutType != LayoutType.STANDARD_LAYOUT || hasWindowFullScreen
                || videoCount <= STANDARD_VISIBLE_COUNT) {
            return super.onInterceptTouchEvent(event);
        }
        boolean intercept = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastInterceptX = event.getRawX();
                lastInterceptY = event.getRawY();
                if (ScreenUtils.isPortrait()) {
                    //控件的左边界，与屏幕原点的X轴坐标
                    scrollStart = getScrollX();
                    Log.i(TAG, "handlingHorizontalSliding : scrollStart: " + scrollStart);
                    lastX = (int) event.getX();
                } else {
                    //控件的左边界，与屏幕原点的X轴坐标
                    scrollStart = getScrollY();
                    Log.i(TAG, "handlingVerticalSliding : scrollStart: " + scrollStart);
                    lastY = (int) event.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //检查是横向移动的距离大，还是纵向
                float distanceX = Math.abs(lastInterceptX - event.getRawX());
                float distanceY = Math.abs(lastInterceptY - event.getRawY());
                if (ScreenUtils.isPortrait()) {
                    intercept = distanceX > distanceY;
                } else {
                    intercept = distanceY > distanceX;
                }
                break;
            default:
                break;
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (layoutType == null || layoutType != LayoutType.STANDARD_LAYOUT || hasWindowFullScreen
                || videoCount <= STANDARD_VISIBLE_COUNT || isScrolling) {
            return super.onTouchEvent(event);
        }
        if (ScreenUtils.isPortrait()) {
            return handlingHorizontalSliding(event);
        } else {
            return handlingVerticalSliding(event);
        }
    }

    private boolean handlingHorizontalSliding(MotionEvent event) {
        int childWidth = originalWidth / STANDARD_VISIBLE_COUNT;
        if (childWidth == 0) {
            childWidth = 1;
        }
        int scrollMax = childWidth * videoCount - originalWidth;
        //在onTouchEvent这里，截取event对象
        obtainVelocity(event);
        int eventX = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!scroller.isFinished()) {
                    // 终止滑动
                    scroller.abortAnimation();
                }
                int offsetX = eventX - lastX;
                // 边界值检查
                int scrollX = getScrollX();
                // 向右滑动
                if (offsetX > 0 && scrollX - offsetX < 0) {
                    // 已经到达最左
                    offsetX = 0;
                } else if (offsetX < 0 && scrollX + Math.abs(offsetX) > scrollMax) {
                    // 已经到达最右
                    offsetX = 0;
                }
                scrollBy(-offsetX, 0);
                // 滑动完成后,重新设置LastY位置
                lastX = eventX;
                break;
            case MotionEvent.ACTION_UP:
                scrollEnd = getScrollX();
                Log.i(TAG, "handlingHorizontalSliding : scrollEnd " + scrollEnd);
                int distance = scrollEnd - scrollStart;
                Log.i(TAG, "handlingHorizontalSliding : distance " + distance);
                int position = scrollEnd / childWidth;
                int nextScroll;
                if (scrollEnd % childWidth > childWidth / 2) {
                    // 滚动下一个
                    nextScroll = (position + 1) * childWidth;
                    nextScroll = Math.min(nextScroll, scrollMax);
                } else {
                    // 回滚
                    nextScroll = position * childWidth;
                }
                scroller.startScroll(getScrollX(), 0, nextScroll - getScrollX(), 0);
                isScrolling = true;
                postInvalidate();
                recycleVelocity();
                break;
            default:
                break;
        }
        return true/*super.onTouchEvent(event)*/;
    }

    private boolean handlingVerticalSliding(MotionEvent event) {
        int childHeight = originalHeight / STANDARD_VISIBLE_COUNT;
        if (childHeight == 0) {
            childHeight = 1;
        }
        int scrollMax = childHeight * videoCount - originalHeight;
        //在onTouchEvent这里，截取event对象
        obtainVelocity(event);
        int eventY = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!scroller.isFinished()) {
                    // 终止滑动
                    scroller.abortAnimation();
                }
                int offsetY = eventY - lastY;
                // 边界值检查
                int scrollY = getScrollY();
                // 向下滑动
                if (offsetY > 0 && scrollY - offsetY < 0) {
                    // 已经到达顶端，下拉多少，就往上滚动多少
                    offsetY = 0;
                } else if (offsetY < 0 && scrollY + Math.abs(offsetY) > scrollMax) {
                    // 已经到达底部，上拉多少，就往下滚动多少
                    offsetY = 0;
                }
                scrollBy(0, -offsetY);
                // 滑动完成后,重新设置LastY位置
                lastY = eventY;
                break;
            case MotionEvent.ACTION_UP:
                scrollEnd = getScrollY();
                Log.i(TAG, "handlingVerticalSliding : scrollEnd " + scrollEnd);
                int distance = scrollEnd - scrollStart;
                Log.i(TAG, "handlingVerticalSliding : distance " + distance);
                int position = scrollEnd / childHeight;
                int nextScroll;
                if (scrollEnd % childHeight > childHeight / 2) {
                    // 滚动下一个
                    nextScroll = (position + 1) * childHeight;
                    nextScroll = Math.min(nextScroll, scrollMax);
                } else {
                    // 回滚
                    nextScroll = position * childHeight;
                }
                scroller.startScroll(0, getScrollY(), 0, nextScroll - getScrollY());
                isScrolling = true;
                postInvalidate();
                recycleVelocity();
                break;
            default:
                break;
        }
        return true/*super.onTouchEvent(event)*/;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            if (ScreenUtils.isPortrait()) {
                scrollTo(scroller.getCurrX(), 0);
            } else {
                scrollTo(0, scroller.getCurrY());
            }
            postInvalidate();
        } else {
            if (ScreenUtils.isPortrait()) {
                int childWidth = originalWidth / STANDARD_VISIBLE_COUNT;
                if (childWidth != 0) {
                    scrollPosition = getScrollX() / childWidth;
                }
            } else {
                int childHeight = originalHeight / STANDARD_VISIBLE_COUNT;
                if (childHeight != 0) {
                    scrollPosition = getScrollY() / childHeight;
                }
            }
            isScrolling = false;
        }
    }

    @Override
    protected void onScrollChanged(int left, int top, int oldLeft, int oldTop) {
        // 当前可见范围
        int currentRight = left + originalWidth;
        int currentBottom = top + originalHeight;
        Log.i(TAG, "left = " + left + ", top = " + top + ", right = " + currentRight + ", bottom = " + currentBottom);
        for (int i = 0; i < views.size(); i++) {
            VideoScreenView screenView = views.get(i);
            Log.i(TAG, i + " --> left = " + screenView.getLeft() + ", top = " + screenView.getTop() + ", right = "
                    + screenView.getRight() + ", bottom = " + screenView.getBottom());
            screenView.pauseOrResumeVideo(screenView.getRight() <= left + screenView.getWidth() / 2
                    || screenView.getBottom() <= top + screenView.getHeight() / 2
                    || screenView.getLeft() >= currentRight - screenView.getWidth() / 2
                    || screenView.getTop() >= currentBottom - screenView.getHeight() / 2);
        }
        super.onScrollChanged(left, top, oldLeft, oldTop);
    }

    private void layoutChild(VideoScreenView child, int childLeft, int childTop, int childRight,
                             int childBottom) {
        boolean isPauseState;
        int left = getScrollX();
        int top = getScrollY();
        int right = left + originalWidth;
        int bottom = top + originalHeight;
        int overWidth = child.getLayoutParams().width / 2;
        int overHeight = child.getLayoutParams().height / 2;
        isPauseState = childLeft >= right - overWidth || childTop >= bottom - overHeight
                || childRight <= left + overWidth
                || childBottom <= top + overHeight
                || child.getLayoutParams().width <= VariableLayout.DEFAULT_SIZE
                || child.getLayoutParams().height <= VariableLayout.DEFAULT_SIZE;
        child.layout(childLeft, childTop, childRight, childBottom);
        child.pauseOrResumeVideo(isPauseState);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (layoutType == null || layoutType != LayoutType.STANDARD_LAYOUT
            /*|| videoCount <= STANDARD_VISIBLE_COUNT*/) {
            return;
        }
        // 复原
        if (getScrollX() > 0 || getScrollY() > 0) {
            scrollTo(0, 0);
        }
    }

    /**
     * 初始化加速度检测器
     *
     * @param event touch事件
     */
    private void obtainVelocity(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
    }

    /**
     * 释放资源
     */
    private void recycleVelocity() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    /**
     * 获取加速度
     *
     * @return 加速度
     */
    private int getVelocity(boolean isVertical) {
        if (velocityTracker == null) {
            return 0;
        }
        velocityTracker.computeCurrentVelocity(1000);
        if (isVertical) {
            return (int) velocityTracker.getYVelocity();
        }
        return (int) velocityTracker.getXVelocity();
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        removeView(child);
        //修复BUG-23306
        ViewGroup curViewGroup = (ViewGroup) child.getParent();
        if (curViewGroup != null) {
            curViewGroup.removeView(child);
        }
        super.addView(child, index, params);
    }

    @Override
    public void onVideoScreenAdd(List<VideoScreenView> screenViews, VideoScreenView screenView) {
        Log.i(TAG, "onVideoScreenAdd: ");
        views = screenViews;
        correctionVideoCount();
        addView(screenView);
    }

    @Override
    public void onVideoScreenRemove(List<VideoScreenView> screenViews, VideoScreenView screenView) {
        Log.i(TAG, "onVideoScreenRemove: ");
        views = screenViews;
        if (screenView == fullView) {
            hasWindowFullScreen = false;
            fullView = null;
        }
        correctionVideoCount();
        removeView(screenView);
    }

    @Override
    public void onVideoPositionChange(List<VideoScreenView> screenViews) {
        Log.i(TAG, "onVideoPositionChange: ");
        views = screenViews;
        requestLayout();
    }

    @Override
    public void onVideoFullScreen(VideoScreenView screenView, boolean isFull) {
        Log.i(TAG, "onVideoFullScreen: ");
        hasWindowFullScreen = isFull;
        fullView = hasWindowFullScreen ? screenView : null;
        if (hasWindowFullScreen) {
            if (getScrollX() != 0 || getScaleY() != 0) {
                scrollTo(0, 0);
            }
        }
        // 如果父布局重新排版了那么当前布局就不需要重新排版了
        VariableLayout variableLayout = (VariableLayout) getParent();
        boolean success = variableLayout.updateLayoutStyle();
        requestLayout();
    }

    private void correctionVideoCount() {
        videoCount = 0;
        for (VideoScreenView screenView : views) {
            if (screenView.getVideoInfo() != null) {
                videoCount++;
            }
        }
    }

}
