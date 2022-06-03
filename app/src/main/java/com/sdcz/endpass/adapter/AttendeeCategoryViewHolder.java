package com.sdcz.endpass.adapter;

import android.view.View;

import com.sdcz.endpass.R;
import com.sdcz.endpass.base.RecyclerViewHolder;
import com.sdcz.endpass.widget.AttendeeCategoryView;


/**
 * @Description: 参会人列表所在的ViewPager2的Adapter的ViewHolder
 */
class AttendeeCategoryViewHolder extends RecyclerViewHolder<Integer> {

    private final AttendeeCategoryView categoryView;
    private String channelCode = "";

    public AttendeeCategoryViewHolder(View itemView, String channelCode) {
        super(itemView);
        this.channelCode = channelCode;
        categoryView = itemView.findViewById(R.id.categoryView);
    }

    @Override
    protected void onBindViewHolder(int position, Integer item) {
        categoryView.setCategory(item);
        categoryView.setChannelCode(channelCode);
    }

}
