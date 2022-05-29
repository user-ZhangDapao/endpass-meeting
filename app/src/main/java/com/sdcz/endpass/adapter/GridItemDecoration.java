package com.sdcz.endpass.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * @Description: RecyclerView 表格布局Item间的分割线
 */
public class GridItemDecoration extends RecyclerView.ItemDecoration {

    private final Drawable divider;
    private final boolean showLastLine;
    private final int horizonSpan;
    private final int verticalSpan;

    private GridItemDecoration(int horizonSpan, int verticalSpan, int color, boolean showLastLine) {
        this.horizonSpan = horizonSpan;
        this.showLastLine = showLastLine;
        this.verticalSpan = verticalSpan;
        this.divider = new ColorDrawable(color);
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        drawHorizontal(canvas, parent);
        drawVertical(canvas, parent);
    }

    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            if (isLastRaw(parent, i, getSpanCount(parent), childCount) && !showLastLine) {
                continue;
            }
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int left = child.getLeft() - params.leftMargin;
            int right = child.getRight() + params.rightMargin;
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + horizonSpan;

            divider.setBounds(left, top, right, bottom);
            divider.draw(canvas);
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            if ((parent.getChildViewHolder(child).getAdapterPosition() + 1) % getSpanCount(parent) == 0) {
                continue;
            }
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getTop() - params.topMargin;
            int bottom = child.getBottom() + params.bottomMargin + horizonSpan;
            int left = child.getRight() + params.rightMargin;
            int right = left + verticalSpan;
            if (i == childCount - 1) { //满足条件( 最后一行 && 不绘制 ) 将vertical多出的一部分去掉;
                right -= verticalSpan;
            }
            divider.setBounds(left, top, right, bottom);
            divider.draw(canvas);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int spanCount = getSpanCount(parent);
        int childCount = parent.getAdapter().getItemCount();
        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        if (itemPosition < 0) {
            return;
        }
        int column = itemPosition % spanCount;
        int bottom;
        int left = column * verticalSpan / spanCount;
        int right = verticalSpan - (column + 1) * verticalSpan / spanCount;
        if (isLastRaw(parent, itemPosition, spanCount, childCount)) {
            if (showLastLine) {
                bottom = horizonSpan;
            } else {
                bottom = 0;
            }
        } else {
            bottom = horizonSpan;
        }
        outRect.set(left, 0, right, bottom);
    }

    /**
     * 获取列数
     */
    private int getSpanCount(RecyclerView parent) {
        // 列数
        int spanCount = 1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return spanCount;
    }

    /**
     * 是否最后一行
     * @param parent     RecyclerView
     * @param pos        当前item的位置
     * @param spanCount  每行显示的item个数
     * @param childCount child个数
     */
    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            return isLastRaw(pos, spanCount, childCount);
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                return isLastRaw(pos, spanCount, childCount); // StaggeredGridLayoutManager 且纵向滚动
            } else {
                // StaggeredGridLayoutManager 且横向滚动
                return (pos + 1) % spanCount == 0;
            }
        }
        return false;
    }

    private boolean isLastRaw(int pos, int spanCount, int childCount) {
        int remainCount = childCount % spanCount;
        if (remainCount == 0) {
            return pos >= childCount - spanCount; //如果正好最后一行完整;
        } else {
            return pos >= childCount - childCount % spanCount;
        }
    }

    /**
     * 使用Builder构造
     */
    public static class Builder {
        private final Context context;
        private final Resources resources;
        private boolean showLastLine;
        private int horizonSpan;
        private int verticalSpan;
        private int color;

        /**
         * 构造函数
         */
        public Builder(Context context) {
            this.context = context;
            resources = context.getResources();
            showLastLine = true;
            horizonSpan = 0;
            verticalSpan = 0;
            color = Color.WHITE;
        }

        /**
         * 通过资源文件设置分隔线颜色
         */
        public Builder setColorResource(@ColorRes int resource) {
            setColor(ContextCompat.getColor(context, resource));
            return this;
        }

        /**
         * 设置颜色
         */
        public Builder setColor(@ColorInt int color) {
            this.color = color;
            return this;
        }

        /**
         * 通过dp设置垂直间距
         */
        public Builder setVerticalSpan(@DimenRes int vertical) {
            this.verticalSpan = resources.getDimensionPixelSize(vertical);
            return this;
        }

        /**
         * 通过px设置垂直间距
         */
        public Builder setVerticalSpan(float vertical) {
            this.verticalSpan = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_PX, vertical, resources.getDisplayMetrics());
            return this;
        }

        /**
         * 通过dp设置水平间距
         */
        public Builder setHorizontalSpan(@DimenRes int horizontal) {
            this.horizonSpan = resources.getDimensionPixelSize(horizontal);
            return this;
        }

        /**
         * 通过px设置水平间距
         */
        public Builder setHorizontalSpan(float horizontal) {
            this.horizonSpan = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_PX, horizontal, resources.getDisplayMetrics());
            return this;
        }

        /**
         * 是否最后一条显示分割线
         */
        public Builder setShowLastLine(boolean show) {
            showLastLine = show;
            return this;
        }

        public GridItemDecoration build() {
            return new GridItemDecoration(horizonSpan, verticalSpan, color, showLastLine);
        }
    }
}