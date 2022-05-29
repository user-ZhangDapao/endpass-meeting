package com.sdcz.endpass.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.sdcz.endpass.R;
import com.sdcz.endpass.util.EmotionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;


/**
 * @Description     聊天窗口表情选择的Adapter
 */
public class MeetingChatEmotionAdapter
        extends RecyclerView.Adapter<MeetingChatEmotionAdapter.MeetingChatEmotionViewHolder> {
    private final LinkedHashMap<String, Integer> emotionMap;
    private final List<Integer> resIdList;
    private final List<String> resKeyList;
    private EmotionOnClickListener emotionOnClickListener;
    public static final int ROW_EMOTION_NUMBER = 8;

    /**
     * 构造函数
     */
    public MeetingChatEmotionAdapter() {
        emotionMap = EmotionUtils.getEmotionMap();
        resIdList = new ArrayList<>();
        resKeyList = new ArrayList<>();
        Set<String> keySet = emotionMap.keySet();
        for (String key : keySet) {
            resKeyList.add(key);
            resIdList.add(emotionMap.get(key));
        }
        //如果一行不满8个，占位补满，使得删除按钮位于行的末尾
        int vacancyNum = ROW_EMOTION_NUMBER - emotionMap.size() % ROW_EMOTION_NUMBER;
        for (int i = 0; i < vacancyNum - 1; i++) {
            resKeyList.add("/:(null)");
            resIdList.add(0);
        }
        resKeyList.add("/:(delete)");
        resIdList.add(R.mipmap.emoji_backspace);

    }

    @NonNull
    @Override
    public MeetingChatEmotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emotion_layout, null, true);
        return new MeetingChatEmotionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingChatEmotionViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Integer resId = resIdList.get(position);
        if (resId > 0) {
            holder.imEmotion.setImageResource(resId);
        }

        holder.imEmotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View itemView) {
                if (emotionOnClickListener != null && resId > 0) {
                    emotionOnClickListener.onClickEmotionItemListener(itemView, resKeyList.get(position), resId);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (emotionMap == null) {
            return 0;
        }
        return resIdList.size();
    }

    public static class MeetingChatEmotionViewHolder extends RecyclerView.ViewHolder {

        public ImageView imEmotion;

        public MeetingChatEmotionViewHolder(@NonNull View itemView) {
            super(itemView);
            initView(itemView);
        }

        private void initView(View itemView) {
            imEmotion = itemView.findViewById(R.id.im_emotion);

        }
    }


    public void setEmotionOnClickListener(EmotionOnClickListener emotionOnClickListener) {
        this.emotionOnClickListener = emotionOnClickListener;
    }

    public interface EmotionOnClickListener {
        void onClickEmotionItemListener(View view, String resStr, int resId);
    }
}
