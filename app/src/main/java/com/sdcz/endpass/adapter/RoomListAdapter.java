package com.sdcz.endpass.adapter;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.Utils;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sdcz.endpass.R;
import com.sdcz.endpass.bean.BaseRoomData;
import com.sdcz.endpass.presenter.RoomListViewModel;

import java.util.ArrayList;

public class RoomListAdapter extends BaseMultiItemQuickAdapter<BaseRoomData, BaseViewHolder> {
    public RoomListAdapter() {
        super(new ArrayList<>());
        addItemType(RoomListViewModel.TYPE_CLOUD_ROOM, R.layout.itme_room_list_info);
        addItemType(RoomListViewModel.TYPE_SCHEDULE_ROOM, R.layout.itme_room_list_info);
        addItemType(RoomListViewModel.TYPE_INSTANT_ROOM, R.layout.itme_room_list_info);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, BaseRoomData item) {
        helper.setText(R.id.tv_room_name, item.getDisplayRoomName());
        String format;
        switch (item.getItemType()) {
            case RoomListViewModel.TYPE_CLOUD_ROOM:
                helper.setGone(R.id.tv_room_user, true);
                helper.setGone(R.id.tv_room_time, false);
                helper.setText(R.id.tv_room_user, item.getDisplayCurrentUserCount() + "/" + item.getDisplayMaxUserCount());
                format = Utils.getApp().getString(R.string.format_room_id_room_code, item.getDisplayInviteCode());
                helper.setText(R.id.tv_room_code, format);
                break;
            case RoomListViewModel.TYPE_SCHEDULE_ROOM:
                helper.setGone(R.id.tv_room_user, false);
                helper.setGone(R.id.tv_room_time, true);
                helper.setText(R.id.tv_room_time, item.getDisplayStartTime() + "  " + item.getDisplayEndTime());
                format = Utils.getApp().getString(R.string.format_room_id_room_code, item.getDisplayInviteCode());
                helper.setText(R.id.tv_room_code, format);
                break;
            case RoomListViewModel.TYPE_INSTANT_ROOM:
                helper.setGone(R.id.tv_room_user, false);
                helper.setGone(R.id.tv_room_time, false);
                format = Utils.getApp().getString(R.string.format_invite_room_code, item.getDisplayInviteCode());
                helper.setText(R.id.tv_room_code, format);
                break;
        }
    }
}
