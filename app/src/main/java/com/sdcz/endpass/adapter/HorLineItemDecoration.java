package com.sdcz.endpass.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @Description: RecyclerView 水平线条分割线(颜色+高度，可选顶部和底部)
 */
public class HorLineItemDecoration extends RecyclerView.ItemDecoration {

    private final int height;
    private final boolean showHeadLine;   // 第一个Item前面是否显示分割线
    private final boolean showBottomLine; // 最后一个Item底部是否显示分割线
    private final Paint backgroundPaint;

    private HorLineItemDecoration(Builder builder) {
        height = builder.height;
        showHeadLine = builder.showHeadLine;
        showBottomLine = builder.showBottomLine;
        backgroundPaint = new Paint();
        backgroundPaint.setColor(builder.lineColor);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               RecyclerView parent, @NonNull RecyclerView.State state) {
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null) {
            return;
        }
        int childCount = adapter.getItemCount();
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        if (position > 0 && position == childCount - 1) {
            outRect.top = height;
            if (showBottomLine) {
                outRect.bottom = height;
            }
        } else {
            if (showHeadLine || position > 0) {
                outRect.top = height;
            }
        }
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        if (parent.getLayoutManager() == null || height <= 0) {
            return;
        }
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null) {
            return;
        }
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        RecyclerView.LayoutParams params;
        View child;
        for (int i = 0; i < childCount; i++) {
            child = parent.getChildAt(i);
            params = (RecyclerView.LayoutParams) child.getLayoutParams();
            drawDecorationArea(canvas, left, right, child, params,
                    params.getViewAdapterPosition(), adapter.getItemCount());
        }
    }

    private void drawDecorationArea(Canvas canvas, int left, int right, View child,
                                    RecyclerView.LayoutParams params, int position, int childCount) {
        int rectBottom = child.getTop() - params.topMargin;
        if (showHeadLine || position > 0) {
            int top = rectBottom - height;
            canvas.drawRect(left,
                    top,
                    right,
                    rectBottom,
                    backgroundPaint);
        }
        // 最后一个item，画一个底部的分割线
        if (showBottomLine && childCount == position + 1) {
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + height;
            canvas.drawRect(left,
                    top,
                    right,
                    bottom,
                    backgroundPaint);
        }
    }

    /**
     * Builder Class
     */
    public static final class Builder {

        private int height;
        private boolean showHeadLine;
        private boolean showBottomLine;
        private int lineColor;

        /**
         * 构造函数
         * @param context context
         */
        public Builder(@NonNull Context context) {
            height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8F,
                    context.getResources().getDisplayMetrics());
            lineColor = Color.argb(0x00, 0x0, 0x2, 0x6);
            showHeadLine = true;
            showBottomLine = true;
        }

        /**
         * height
         * @param val height
         */
        public Builder height(int val) {
            height = val;
            return this;
        }

        /**
         * show head line
         * @param val show or not
         */
        public Builder showHeadLine(boolean val) {
            showHeadLine = val;
            return this;
        }

        /**
         * show bottom line
         * @param val show or not
         */
        public Builder showBottomLine(boolean val) {
            showBottomLine = val;
            return this;
        }

        /**
         * line color
         * @param val color, not resource id
         */
        public Builder lineColor(int val) {
            lineColor = val;
            return this;
        }

        /**
         * build
         */
        public HorLineItemDecoration build() {
            return new HorLineItemDecoration(this);
        }

    }
}
