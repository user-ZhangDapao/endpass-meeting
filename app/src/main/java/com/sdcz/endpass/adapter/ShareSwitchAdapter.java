package com.sdcz.endpass.adapter;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.comix.meeting.entities.BaseShareBean;
import com.comix.meeting.entities.VncShareBean;
import com.comix.meeting.entities.WhiteBoard;
import com.comix.meeting.interfaces.IWhiteBoardOperation;
import com.sdcz.endpass.R;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.base.RecyclerViewAdapter;
import com.sdcz.endpass.base.RecyclerViewHolder;
import com.sdcz.endpass.util.ImageCache;
import com.inpor.base.sdk.share.ScreenShareManager;
import com.inpor.base.sdk.whiteboard.WBShareManager;

/**
 * @description 分享时，底部切换共享的adapter
 */
public class ShareSwitchAdapter extends RecyclerViewAdapter<BaseShareBean> {

    private ShareTabClickListener shareTabClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private String uuid;


    public void setShareTabClickListener(ShareTabClickListener shareTabClickListener) {
        this.shareTabClickListener = shareTabClickListener;
    }

    /**
     * 添加
     */
    public void addItem(BaseShareBean item) {
        if (data == null || item == null) {
            return;
        }
        int positionStart = data.size();
        data.add(positionStart, item);
        notifyItemRangeInserted(positionStart, 1);
    }

    /**
     * 删除指定item
     */
    public void removeItem(BaseShareBean item) {
        if (data == null) {
            return;
        }
        int positionStart = data.indexOf(item);
        if (positionStart != -1) {
            data.remove(item);
            notifyItemRangeRemoved(positionStart, 1);
        }
    }

    public void updateUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * selected变化
     * @param item 当前选择
     */
    public void notifySelectedItemChanged(BaseShareBean item) {
        if (data == null) {
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            BaseShareBean shareBean = data.get(i);
            shareBean.setActive(item.getId() == shareBean.getId());
            notifyItemChanged(i);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @NonNull
    @Override
    public RecyclerViewHolder<BaseShareBean> onCreateViewHolder(@NonNull ViewGroup parent,
                                                                int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 1) {
            View view = inflater.inflate(R.layout.meetingui_item_share_switch_video, parent, false);
            return new VideoViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.meetingui_item_share_switch_nor, parent, false);
            return new NorViewHolder(view);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerViewHolder<BaseShareBean> holder) {
        if (holder instanceof VideoViewHolder) {
            VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
            videoViewHolder.release();
        }
    }

    private void changeTitle(BaseShareBean item, TextView tv) {
        if (item == null || tv == null) {
            return;
        }
        switch (item.getType()) {
            case DATA_TYPE_APPSHARE:
                tv.setText(R.string.meetingui_screen_shared);
                break;
            case DATA_TYPE_WB:
                tv.setText(item.getTitle());
                break;
        }
    }

    private void loadImage(ImageView contentIv, WBShareManager shareModel, WhiteBoard whiteBoard) {
        final String key = uuid + "_wb_" + whiteBoard.getId();
        // 先从内存中拿
        Bitmap bitmap = ImageCache.getBitmapFromMem(key);
        if (bitmap != null) {
            Log.i("leslie", "image exists in memory");
            contentIv.setImageBitmap(bitmap);
            return;
        }
        contentIv.setImageResource(R.color.color_white);
        contentIv.setTag(key);
        new ImageDownloadTask(contentIv, shareModel, whiteBoard).execute(key);
    }

    private class VideoViewHolder extends RecyclerViewHolder<BaseShareBean>
            implements SurfaceHolder.Callback, View.OnClickListener {

        View labelLayout;
        TextView titleTv;
        ImageView closeIv;
        FrameLayout frameLayout;
        //SurfaceView surfaceView;
        ScreenShareManager shareModel;

        public VideoViewHolder(View itemView) {
            super(itemView);
            labelLayout = itemView.findViewById(R.id.item_label_layout);
            titleTv = itemView.findViewById(R.id.tv_title);
            closeIv = itemView.findViewById(R.id.close_iv);
            frameLayout = itemView.findViewById(R.id.item_surface_layout);

            closeIv.setOnClickListener(this);
            itemView.setOnClickListener(this);
            shareModel = SdkUtil.getShareManager();
        }

        @Override
        protected void onBindViewHolder(int position, BaseShareBean item) {
            if (item.isActive()) {
                selectedPosition = position;
                itemView.setSelected(true);
                labelLayout.setSelected(true);
            } else {
                itemView.setSelected(false);
                labelLayout.setSelected(false);
            }
            changeTitle(item, titleTv);
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        }

        @Override
        public void onClick(View view) {
            if (view == closeIv) {
                if (shareTabClickListener != null) {
                    int pos = getLayoutPosition();
                    if (pos == RecyclerView.NO_POSITION) {
                        return;
                    }
                    shareTabClickListener.onShareTabCloseClick(ShareSwitchAdapter.this, pos, item);
                }
            } else if (view == itemView) {
                if (shareTabClickListener != null) {
                    int pos = getLayoutPosition();
                    if (pos == RecyclerView.NO_POSITION) {
                        return;
                    }
                    shareTabClickListener.onShareTabClick(ShareSwitchAdapter.this, pos, item);
                }
            }
        }

        void release() {
        }
    }

    private class NorViewHolder extends RecyclerViewHolder<BaseShareBean>
            implements View.OnClickListener {

        View labelLayout;
        TextView titleTv;
        ImageView closeIv;
        ImageView contentIv;
        WBShareManager shareModel;

        public NorViewHolder(View itemView) {
            super(itemView);
            labelLayout = itemView.findViewById(R.id.item_label_layout);
            titleTv = itemView.findViewById(R.id.tv_title);
            closeIv = itemView.findViewById(R.id.close_iv);
            contentIv = itemView.findViewById(R.id.item_iv);
            closeIv.setOnClickListener(this);
            itemView.setOnClickListener(this);
            shareModel = SdkUtil.getWbShareManager();
        }

        @Override
        protected void onBindViewHolder(int position, BaseShareBean item) {
            if (item.isActive()) {
                selectedPosition = position;
                itemView.setSelected(true);
                labelLayout.setSelected(true);
            } else {
                itemView.setSelected(false);
                labelLayout.setSelected(false);
            }
            changeTitle(item, titleTv);
            if (item instanceof VncShareBean) {
                contentIv.setImageResource(R.mipmap.meetingui_vnc_share);
            } else if (item instanceof WhiteBoard) {
                WhiteBoard whiteBoard = (WhiteBoard) item;
                loadImage(contentIv, shareModel, whiteBoard);
            }
        }

        @Override
        public void onClick(View view) {
            if (view == closeIv) {
                if (shareTabClickListener != null) {
                    int pos = getLayoutPosition();
                    if (pos == RecyclerView.NO_POSITION) {
                        return;
                    }
                    shareTabClickListener.onShareTabCloseClick(ShareSwitchAdapter.this, pos, item);
                }
            } else if (view == itemView) {
                if (shareTabClickListener != null) {
                    int pos = getLayoutPosition();
                    if (pos == RecyclerView.NO_POSITION) {
                        return;
                    }
                    shareTabClickListener.onShareTabClick(ShareSwitchAdapter.this, pos, item);
                }
            }
        }
    }

    static class ImageDownloadTask extends AsyncTask<String, Integer, Bitmap> {

        private String key;
        private final ImageView imageView;
        private final WBShareManager shareModel;
        private final WhiteBoard whiteBoard;

        public ImageDownloadTask(ImageView imageView, WBShareManager shareModel, WhiteBoard whiteBoard) {
            this.imageView = imageView;
            this.shareModel = shareModel;
            this.whiteBoard = whiteBoard;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                key = params[0];
                IWhiteBoardOperation operation = shareModel.getCurrentWhiteBoardOperation();
                Bitmap bitmap = operation.createBitmapByWb(whiteBoard);
                if (bitmap != null) {
                    // 将图片加入到内存缓存中
                    ImageCache.putBitmapToMem(key, bitmap);
                }
                return bitmap;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                // 通过 tag 来防止图片错位
                if (imageView.getTag() != null && imageView.getTag().equals(key) && result != null) {
                    imageView.setImageBitmap(result);
                }
            }
        }
    }

    public interface ShareTabClickListener {

        /**
         *
         */
        void onShareTabClick(RecyclerViewAdapter<BaseShareBean> viewAdapter,
                                         int position,BaseShareBean item);

        /**
         *
         */
        void onShareTabCloseClick(RecyclerViewAdapter<BaseShareBean> viewAdapter,
                                         int position,BaseShareBean item);
    }
}
