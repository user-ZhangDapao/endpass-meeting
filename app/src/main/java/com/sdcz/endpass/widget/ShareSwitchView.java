package com.sdcz.endpass.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.GridItemDecoration;
import com.sdcz.endpass.adapter.ShareSwitchAdapter;

/**
 * @author yinhui
 * @date create at 2021/1/6
 * @description
 */
public class ShareSwitchView extends FrameLayout {

    private final View containerView;
    private final ImageView backIv;
    private final ImageView closeIv;
    private final RecyclerView recyclerView;

    /**
     * 构造
     * @param context 上下文
     */
    public ShareSwitchView(Context context) {
        super(context);
        View.inflate(context, R.layout.meetingui_share_switch_view, this);
        containerView = findViewById(R.id.container);
        backIv = findViewById(R.id.back_iv);
        closeIv = findViewById(R.id.close_iv);
        recyclerView = findViewById(R.id.recycler_view);

        GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
        GridItemDecoration.Builder builder = new GridItemDecoration.Builder(context);
        builder.setColorResource(R.color.color_transparent)
                .setHorizontalSpan(R.dimen.dp50)
                .setVerticalSpan(R.dimen.dp20)
                .setShowLastLine(true);
        recyclerView.addItemDecoration(builder.build());
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutParams params = (LayoutParams) containerView.getLayoutParams();
        LinearLayout.LayoutParams rvLayoutParams = (LinearLayout.LayoutParams) recyclerView.getLayoutParams();
        int resId;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.gravity = Gravity.END;
            params.width = getContext().getResources().getDimensionPixelOffset(R.dimen.dp360);
            params.height = LayoutParams.MATCH_PARENT;
            resId = R.drawable.shape_select_shared_right;
            rvLayoutParams.height = LayoutParams.MATCH_PARENT;
        } else {
            params.gravity = Gravity.BOTTOM;
            params.width = LayoutParams.MATCH_PARENT;
            params.height = LayoutParams.WRAP_CONTENT;
            rvLayoutParams.height = SizeUtils.dp2px(360);
            resId = R.drawable.shape_select_shared;
        }
        containerView.setBackgroundResource(resId);
        recyclerView.setLayoutParams(rvLayoutParams);
        containerView.setLayoutParams(params);
    }

    public void setAdapter(ShareSwitchAdapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    public void setChildrenClickListener(OnClickListener listener) {
        backIv.setOnClickListener(listener);
        closeIv.setOnClickListener(listener);
    }
}
