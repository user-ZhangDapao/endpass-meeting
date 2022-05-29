package com.sdcz.endpass.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sdcz.endpass.R;
import com.sdcz.endpass.base.RecyclerViewAdapter;
import com.sdcz.endpass.base.RecyclerViewHolder;
import com.sdcz.endpass.bean.DeviceItem;
import com.sdcz.endpass.util.AttendeeUtils;

/**
 * @Description: 参会人音视频设备列表适配器
 */
public class DeviceAdapter extends RecyclerViewAdapter<DeviceItem> {

    public interface ItemListener {
        /**
         * 点击设备的图标
         * @param position position
         * @param item DeviceItem
         */
        void onDeviceItemClick(int position, DeviceItem item);
    }

    private final ItemListener listener;

    public DeviceAdapter(ItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerViewHolder<DeviceItem> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.recycler_item_attendee_device, parent, false);
        DeviceViewHolder holder = new DeviceViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                int pos = holder.getLayoutPosition();
                if (pos == RecyclerView.NO_POSITION) {
                    return;
                }
                onItemClickListener.onItemClick(DeviceAdapter.this, pos, holder.itemView);
            }
        });
        return holder;
    }

    private class DeviceViewHolder extends RecyclerViewHolder<DeviceItem> {

        private final TextView tvDeviceName;
        private final ImageView imgCamera;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            imgCamera = itemView.findViewById(R.id.imgCamera);
            imgCamera.setOnClickListener(view -> {
                int pos = getLayoutPosition();
                if (pos == RecyclerView.NO_POSITION) {
                    return;
                }
                listener.onDeviceItemClick(pos, item);
            });
        }

        @Override
        protected void onBindViewHolder(int position, DeviceItem item) {
            String deviceName;
            int logo;
            if (DeviceItem.CAMERA == item.getDeviceType()) {
                deviceName = item.getVideoChannel().strName;
                logo = AttendeeUtils.getCameraStateLogo(item.getVideoChannel());
            } else {
                deviceName = item.getAudioChannel().strName;
                logo = AttendeeUtils.getMicStateLogo(item.getAudioChannel());
            }
            tvDeviceName.setText(deviceName);
            imgCamera.setImageResource(logo);
        }

    }

}
