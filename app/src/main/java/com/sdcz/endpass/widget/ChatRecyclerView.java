package com.sdcz.endpass.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @Author wuyr
 * @Date 2021/1/23 15:00
 * @Description
 */
public class ChatRecyclerView extends RecyclerView {
    private OnClickChatRecyclerListener onClickChatRecyclerListener;
    private float onDownY;
    private boolean isOnBottom = true;
    private long onDownTime = 0;
    private static final long DEFAULT_TIME_DEVIATION = 300;
    private static final long DEFAULT_MOVE_DEVIATION = 20;
    private int lastVisibleItem;

    public ChatRecyclerView(@NonNull Context context) {
        super(context);
        initEvent();
    }

    public ChatRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initEvent();
    }

    private void initEvent() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                        int totalItemCount = layoutManager.getItemCount();
                        isOnBottom = lastVisibleItem == totalItemCount - 1;
                    }
                }
            }
        });
    }


    public boolean isOnBottom() {
        return isOnBottom;
    }

    public int getLastVisibleItem() {
        return lastVisibleItem;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                onDownTime = System.currentTimeMillis();
                onDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                long onUpTime = System.currentTimeMillis();
                float onUpY = event.getY();
                float currentMoveDeviation = Math.abs(onUpY - onDownY);
                long timeDeviation = onUpTime - onDownTime;
                if (timeDeviation < DEFAULT_TIME_DEVIATION && currentMoveDeviation < DEFAULT_MOVE_DEVIATION) {
                    if (onClickChatRecyclerListener != null) {
                        onClickChatRecyclerListener.onClickChatWindowListener();
                    }
                }
                onDownY = 0;
                break;
            default:
        }

        return super.dispatchTouchEvent(event);
    }


    public void setOnClickChatRecyclerListener(OnClickChatRecyclerListener onClickChatRecyclerListener) {
        this.onClickChatRecyclerListener = onClickChatRecyclerListener;
    }

    public interface OnClickChatRecyclerListener {
        void onClickChatWindowListener();
    }

}
