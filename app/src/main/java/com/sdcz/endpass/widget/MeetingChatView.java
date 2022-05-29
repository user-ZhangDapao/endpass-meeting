package com.sdcz.endpass.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Constraints;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.comix.meeting.Opcode;
import com.sdcz.endpass.LiveDataBus;
import com.sdcz.endpass.R;
import com.sdcz.endpass.adapter.MeetingChatAdapter;
import com.sdcz.endpass.adapter.MeetingChatEmotionAdapter;
import com.sdcz.endpass.base.BasePopupWindowContentView;
import com.sdcz.endpass.constant.Constant;
import com.sdcz.endpass.model.ChatManager;
import com.sdcz.endpass.util.MeetingTempDataUtils;
import com.sdcz.endpass.util.NetUtil;
import com.sdcz.endpass.util.SharedPrefsUtil;
import com.sdcz.endpass.util.SoftKeyboardHelper;
import com.inpor.nativeapi.adaptor.ChatMsgInfo;

import java.util.List;


/**
 * @Author wuyr
 * @Date 2020/11/27 20:09
 * @Description 聊天功能视图
 */
public class MeetingChatView extends BasePopupWindowContentView
        implements SoftKeyboardHelper.SoftKeyboardListener,
        View.OnClickListener, MeetingChatEmotionAdapter.EmotionOnClickListener,
        ChatManager.ChatMessageListener,
        ChatRecyclerView.OnClickChatRecyclerListener {
    public static final String TAG = "MeetingChatWindowView";
    private final Context context;
    private MeetingChatAdapter meetingChatAdapter;
    private ChatRecyclerView chatContentView;
    private RecyclerView chatExpressionView;
    private ImageView expressionSwitch;
    private ImageView imClose;
    private ImageView sendMsgView;
    private TextView backTitleView;
    private View backgroundView;
    private ChatEmotionEditText textInputEditText;
    private ConstraintLayout rootLayout;
    private ConstraintLayout contentView;
    private SoftKeyboardHelper softKeyboardHelper;
    private RelativeLayout inPutLayout;
    private final ConstraintSet applyConstraintSet = new ConstraintSet();
    private boolean isShowExpression;
    private boolean isShowSoftKeyboard;
    private int verticalParentHeight;
    private ChatManager chatManager;
    private boolean isShowSelectAttenderLayout;
    private long lastOnclickTime;


    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public MeetingChatView(Context context) {
        super(context);
        this.context = context;
        initView();
        initData();
        initEvent();
    }

    private void orientationChange(Configuration configuration) {
        Log.i(TAG, "orientationChange: integer is " + configuration.orientation);
        if (Configuration.ORIENTATION_PORTRAIT == configuration.orientation) {
            onPortraitListener();
        } else if (Configuration.ORIENTATION_LANDSCAPE == configuration.orientation) {
            onLandscapeListener();
        }
    }

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param attrs   属性
     */
    public MeetingChatView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
        initData();
        initEvent();
    }


    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.meeting_chat_layout, this, true);
        this.rootLayout = findViewById(R.id.ml_root_layout);
        contentView = findViewById(R.id.root_content_view);
        inPutLayout = findViewById(R.id.cl_input_layout);
        chatContentView = findViewById(R.id.rv_chat_window);
        imClose = findViewById(R.id.im_close);
        backgroundView = findViewById(R.id.close_background_view);
        expressionSwitch = findViewById(R.id.im_expression_switch);
        chatExpressionView = findViewById(R.id.rl_chat_expression);
        textInputEditText = findViewById(R.id.et_input);
        backTitleView = findViewById(R.id.tv_title);
        sendMsgView = findViewById(R.id.tv_send);
        sendMsgView.setActivated(true);
        textInputEditText.setTextIsSelectable(true);
    }


    private void setEditTextFocusable(EditText searchEditTextView, boolean focusable) {
        searchEditTextView.setFocusable(focusable);
        searchEditTextView.setClickable(focusable);
        searchEditTextView.setSelected(focusable);
        searchEditTextView.setCursorVisible(focusable);
        searchEditTextView.setFocusableInTouchMode(focusable);
        Editable editable = searchEditTextView.getText();
        String contentText = editable.toString();
        if (TextUtils.isEmpty(contentText)) {
            searchEditTextView.setSelection(0);
        } else {
            searchEditTextView.setSelection(contentText.length());
        }
        if (focusable) {
            searchEditTextView.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            searchEditTextView.setInputType(InputType.TYPE_NULL);
        }
        searchEditTextView.requestFocus();
    }


    private void initData() {
        chatManager = ChatManager.getInstance();
        chatManager.setChatMessageListener(this);
        //初始化横竖屏状态
        initScreenOrientationState();
        meetingChatAdapter = new MeetingChatAdapter(this.context);
        List<ChatMsgInfo> allMsg = chatManager.getAllMsg();
        meetingChatAdapter.setChatMsgInfoDataArray(allMsg);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatContentView.setLayoutManager(linearLayoutManager);
        chatContentView.setAdapter(meetingChatAdapter);
        chatContentView.setOnClickChatRecyclerListener(this);
        chatExpressionView.setLayoutManager(new GridLayoutManager(this.context, 8));
        MeetingChatEmotionAdapter meetingChatEmotionAdapter = new MeetingChatEmotionAdapter();
        meetingChatEmotionAdapter.setEmotionOnClickListener(this);
        chatExpressionView.setAdapter(meetingChatEmotionAdapter);
        softKeyboardHelper = new SoftKeyboardHelper(this.context);
        applyConstraintSet.clone(rootLayout);
        initSelectAttenderChatData();
        if (allMsg.size() > 0) {
            linearLayoutManager.scrollToPosition(allMsg.size() - 1);
        }
        //恢复输入框中未发送的信息
        String tempMsg = MeetingTempDataUtils.getInstance().getString(MeetingTempDataUtils.TEMP_MSG_KEY);
        Log.i(TAG, "tempMsg:" + tempMsg);
        if (!TextUtils.isEmpty(tempMsg)) {
            textInputEditText.setEmotionText(tempMsg);
            sendMsgView.setActivated(false);
            MeetingTempDataUtils.getInstance().cleanString(MeetingTempDataUtils.TEMP_MSG_KEY);
        }

    }

    private void initScreenOrientationState() {
        //默认为竖屏布局参数，如果是横屏,则设置为横屏布局参数
        float editTextY = 0;
        if (ScreenUtils.isPortrait()) {
            initPortLayoutParams();
            editTextY =
                    SharedPrefsUtil.getFloat(Constant.SoftKeyboardParams.VERTICAL_SOFT_KEYBOARD_Y_KEY);
        } else {
            initLandLayoutParams();
            editTextY = SharedPrefsUtil
                    .getFloat(Constant.SoftKeyboardParams.LANDSCAPE_SOFT_KEYBOARD_Y_KEY);
        }

        //editTextY在软键盘弹出的时候获取，如果为0，则说明出来没打开过软键盘
        if (editTextY <= 0) {
            //显示软键盘图标，第一次先打开软键盘，不允许先开表情软键盘，因为不知道软键盘高度
            expressionSwitch.setActivated(true);
        }
    }

    private void initSelectAttenderChatData() {
        LinearLayoutManager selectAttenderLayoutManager = new LinearLayoutManager(context);
        selectAttenderLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    }

    /**
     * 初始化横屏布局参数
     */
    private void initLandLayoutParams() {
        setSelectAttenderLayoutParams(Configuration.ORIENTATION_LANDSCAPE);
        setParentLayoutParams(Configuration.ORIENTATION_LANDSCAPE);
        setInputLayoutParams(Configuration.ORIENTATION_LANDSCAPE);
    }


    private void initPortLayoutParams() {
        setSelectAttenderLayoutParams(Configuration.ORIENTATION_PORTRAIT);
        setParentLayoutParams(Configuration.ORIENTATION_PORTRAIT);
        setInputLayoutParams(Configuration.ORIENTATION_PORTRAIT);
    }


    /**
     * 初始化事件监听
     */
    private void initEvent() {
        softKeyboardHelper.setKeyboardListener(rootLayout, this);
        contentView.setOnClickListener(this);
        expressionSwitch.setOnClickListener(this);
        imClose.setOnClickListener(this);
        backTitleView.setOnClickListener(this);
        backgroundView.setOnClickListener(this);
        sendMsgView.setOnClickListener(this);
        rootLayout.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        verticalParentHeight = rootLayout.getMeasuredHeight();
                        if (verticalParentHeight > 0) {
                            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });

        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sendMsgView.setActivated(TextUtils.isEmpty(s.toString().trim()));
            }
        });
        MutableLiveData<Configuration> liveData = LiveDataBus.getInstance().getLiveData(LiveDataBus.KEY_MEETING_ACTIVITY_CONFIG);
        liveData.observeForever(this::orientationChange);
        setInputEditTextState();
    }


    /**
     * 显示表情键盘
     */
    private void showEmotionKeyboard() {
        //设置输入框布局参数，固定输入框的位置
        int orientation = ScreenUtils.isPortrait()
                ? Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
        setEmotionKeyboardLayoutParams(orientation);
        //设置聊天信息的显示位置
        if (!isShowSoftKeyboard) {
            setMsgItemLocation();
        }
    }

    /**
     * 隐藏表情键盘
     */
    private void hideEmotionKeyboard() {
        LayoutParams layoutParams =
                new Constraints.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        layoutParams.topToTop = LayoutParams.PARENT_ID;
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        layoutParams.verticalBias = 1;
        if (ScreenUtils.isPortrait()) {
            layoutParams.leftToLeft = LayoutParams.PARENT_ID;

        } else {
            layoutParams.leftToLeft = R.id.guideline_vertical_left;
        }
        inPutLayout.setLayoutParams(layoutParams);
        inPutLayout.setBackgroundResource(R.drawable.shape_land_input);
        inPutLayout.requestLayout();
        expressionSwitch.setActivated(false);
        isShowExpression = false;
    }


    private void setEmotionKeyboardLayoutParams(int orientation) {
        //处理输入框父容器
        LayoutParams layoutParams = new Constraints.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        layoutParams.topToTop = LayoutParams.PARENT_ID;
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        float editTextViewY = 0;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            editTextViewY = getInputViewY(true);
            layoutParams.leftToLeft = LayoutParams.PARENT_ID;
        } else {
            editTextViewY = getInputViewY(false);
            layoutParams.leftToLeft = R.id.guideline_vertical_left;
        }
        if (editTextViewY <= 0) {
            layoutParams.verticalBias = 0.5f;
        } else {
            layoutParams.verticalBias = 0.0f;
        }
        inPutLayout.setBackgroundResource(R.color.color_333747);
        layoutParams.topMargin = (int) editTextViewY;
        inPutLayout.setLayoutParams(layoutParams);
        inPutLayout.requestLayout();
    }


    /**
     * 点击事件监听
     *
     * @param itemView 当前被点击的View
     */
    @Override
    public void onClick(View itemView) {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastOnclickTime < 500) {
            return;
        }
        lastOnclickTime = currentTimeMillis;
        int id = itemView.getId();
        if (id == R.id.root_content_view) {
            closeInput();
        } else if (id == R.id.im_expression_switch) {
            inputWindowControl();
        } else if (id == R.id.im_close || id == R.id.close_background_view) {
            dismissPopupWindow();
            recycle();
        } else if (id == R.id.tv_title) {
            chatManager.updateUnReadMsgNum();
            closeInput();
            popupWindowBack();
            recycle();
        } else if (id == R.id.tv_send) {
            onClickSend();
        }
    }

    private void onClickSend() {
        //网络判断
        boolean networkConnected = NetUtil.isNetworkConnected();
        if (!networkConnected) {
            ToastUtils.showShort(R.string.meeting_network_disable);
            return;
        }

        Editable msgEditable = textInputEditText.getText();
        String msgContent = msgEditable.toString();
        chatManager.setDesUserId(0L);
        int state = chatManager.sendMessage(chatManager.getDesUserId(), msgContent);
        if (state == Opcode.SUCCESS) {
            textInputEditText.setText("");
            chatManager.setDesUserId(0L);
        } else if (state == Opcode.NO_PERMISSION_PUBLIC_CHAT) {
            ToastUtils.showShort(
                    R.string.meetingui_permission_pub_chat_not_allow);
        }
    }


    /**
     * 是否显示选择参会人视图
     *
     * @param isShow 是否显示 选择参会人 视图
     */
    private void isShowSelectAttenderLayout(boolean isShow) {
        int orientation = ScreenUtils.isPortrait()
                ? Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
        setSelectAttenderLayoutParams(orientation);

        if (isShow) {
            isShowSelectAttenderLayout = true;
            contentView.setVisibility(INVISIBLE);
            closeInput();
        } else {
            isShowSelectAttenderLayout = false;
            contentView.setVisibility(VISIBLE);
        }
    }

    /**
     * 设置选择私聊对象布局的参数
     */
    private void setSelectAttenderLayoutParams(int orientation) {
        LayoutParams layoutParams = new Constraints.LayoutParams(0, 0);
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutParams.width = 0;
            layoutParams.height = 0;
            layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
            layoutParams.leftToLeft = LayoutParams.PARENT_ID;
            layoutParams.rightToRight = LayoutParams.PARENT_ID;
            layoutParams.topToTop = R.id.root_content_view;
        } else {
            layoutParams.width = 0;
            layoutParams.height = 0;
            layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
            layoutParams.leftToLeft = R.id.guideline_vertical_left;
            layoutParams.rightToRight = LayoutParams.PARENT_ID;
            layoutParams.topToTop = LayoutParams.PARENT_ID;
        }
    }


    /**
     * 点击表情列表item回调
     *
     * @param view   当前被点击的View
     * @param resStr 表情编码
     * @param resId  表情图片Id
     */
    @Override
    public void onClickEmotionItemListener(View view, String resStr, int resId) {
        textInputEditText.setEmotionText(resStr);
    }

    /**
     * 接收到聊天信息后，回调该方法
     *
     * @param message 聊天信息参数对象
     * @see ChatManager.ChatMessageListener
     */
    @Override
    public void onChatMessage(ChatMsgInfo message) {
        this.post(() -> {
            LinearLayoutManager layoutManager = (LinearLayoutManager) chatContentView.getLayoutManager();
            if (layoutManager != null) {
                int lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();
                meetingChatAdapter.appendChatMsgInfo(message);
                Log.i(TAG, "lastVisibleItem:" + lastVisibleItem + "totalItemCount:" + totalItemCount);
                if (lastVisibleItem == totalItemCount - 1) {
                    layoutManager.scrollToPosition(meetingChatAdapter.getChatMessageArray().size() - 1);
                }
            }
        });
    }


    /**
     * 关闭输入框，
     */
    private void closeInput() {
        if (isShowSoftKeyboard) {
            isShowSoftKeyboard = false;
            softKeyboardHelper.hideSoftKeyboard(textInputEditText);
            expressionSwitch.setActivated(false);
        }

        if (isShowExpression) {
            isShowExpression = false;
            hideEmotionKeyboard();
        }


    }


    /**
     * 控制输入表情键盘和系统键盘的显示和隐藏
     */
    private void inputWindowControl() {
        boolean activated = expressionSwitch.isActivated();
        if (activated) {
            hideEmotionKeyboard();
            softKeyboardHelper.showSoftKeyboard(textInputEditText);
            expressionSwitch.setActivated(false);
            //isShowExpression = false;
        } else {
            isShowExpression = true;
            showEmotionKeyboard();
            softKeyboardHelper.hideSoftKeyboard(textInputEditText);
            expressionSwitch.setActivated(true);
        }
    }


    /**
     * 切换至横屏时回调该方法
     */
    @Override
    public void onLandscapeListener() {
        Log.i(TAG, "分割線--------------------------横屏-------------------------------分割線+\n");
        closeInput();
        float editTextViewY = SharedPrefsUtil
                .getFloat(Constant.SoftKeyboardParams.LANDSCAPE_SOFT_KEYBOARD_Y_KEY);
        //如果从来没打开过软键盘
        if (editTextViewY <= 0) {
            expressionSwitch.setActivated(true);
        }
        setParentLayoutParams(Configuration.ORIENTATION_LANDSCAPE);
        setInputLayoutParams(Configuration.ORIENTATION_LANDSCAPE);
        setSelectAttenderLayoutParams(Configuration.ORIENTATION_LANDSCAPE);
        setMsgItemLocation();
    }

    /**
     * 切换至竖屏时回调该方法
     */
    @Override
    public void onPortraitListener() {
        Log.i(TAG, "分割線---------------------------竖屏------------------------------分割線");
        closeInput();
        float editTextViewY = SharedPrefsUtil
                .getFloat(Constant.SoftKeyboardParams.VERTICAL_SOFT_KEYBOARD_Y_KEY);
        //如果从来没打开过软键盘
        if (editTextViewY <= 0) {
            expressionSwitch.setActivated(true);
        }
        setParentLayoutParams(Configuration.ORIENTATION_PORTRAIT);
        setInputLayoutParams(Configuration.ORIENTATION_PORTRAIT);
        setSelectAttenderLayoutParams(Configuration.ORIENTATION_PORTRAIT);
        setMsgItemLocation();
    }


    /**
     * 设置父容器的横竖屏布局参数
     *
     * @param orientation Configuration.ORIENTATION_LANDSCAPE 横屏
     *                    Configuration.ORIENTATION_PORTRAIT 竖屏
     */
    public void setParentLayoutParams(int orientation) {
        LayoutParams layoutParams = new Constraints.LayoutParams(0, 0);
        layoutParams.width = 0;
        layoutParams.height = 0;
        layoutParams.bottomToTop = R.id.cl_input_layout;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.topToTop = LayoutParams.PARENT_ID;
            layoutParams.leftToLeft = R.id.guideline_vertical_left;
            layoutParams.rightToRight = R.id.guideline_vertical_right;
            contentView.setBackgroundResource(R.drawable.shape_select_shared_right_2);
        } else {
            layoutParams.leftToLeft = LayoutParams.PARENT_ID;
            layoutParams.topToTop = R.id.guideline_horizontal_top;
            layoutParams.rightToRight = LayoutParams.PARENT_ID;
            contentView.setBackgroundResource(R.drawable.shape_select_shared);
        }
        contentView.setLayoutParams(layoutParams);
        contentView.requestLayout();
    }


    /**
     * 设置输入框布局参数
     *
     * @param orientation Configuration.ORIENTATION_LANDSCAPE 横屏
     *                    Configuration.ORIENTATION_PORTRAIT 竖屏
     */
    private void setInputLayoutParams(int orientation) {

        LayoutParams layoutParams =
                new Constraints.LayoutParams(0, Constraints.LayoutParams.WRAP_CONTENT);
        layoutParams.width = 0;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        layoutParams.topToTop = LayoutParams.PARENT_ID;
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        layoutParams.verticalBias = 1;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //如果软键盘或者表情符号键盘是显示的，那么在横屏状态下输入框是横向填充父窗口的
            layoutParams.leftToLeft = R.id.guideline_vertical_left;
            layoutParams.rightToRight = R.id.guideline_vertical_right;
            inPutLayout.setBackgroundResource(R.drawable.shape_land_input);
        } else {
            layoutParams.leftToLeft = LayoutParams.PARENT_ID;
            layoutParams.rightToRight = LayoutParams.PARENT_ID;
            inPutLayout.setBackgroundResource(R.color.color_333747);
        }
        inPutLayout.setLayoutParams(layoutParams);
        inPutLayout.requestLayout();
    }


    /**
     * 软键盘弹出事件监听回调
     *
     * @param softKeyboardHeight 软键盘高度
     */

    @Override
    public void onSoftKeyboardShow(final int softKeyboardHeight) {
        if (!isShowExpression) {
            setMsgItemLocation();
        }
        Log.i(TAG, "软键盘显示:" + inPutLayout.getY());
        isShowSoftKeyboard = true;
        isShowExpression = false;
        saveInputViewY();
        expressionSwitch.setActivated(false);
        //保存软键盘弹出后输入框的Y位置
        if (ScreenUtils.isPortrait()) {
            setInputLayoutParams(Configuration.ORIENTATION_PORTRAIT);
        } else {
            setInputLayoutParams(Configuration.ORIENTATION_LANDSCAPE);
        }


    }

    /**
     * 设置聊天信息item滑动到一行
     */
    private void setMsgItemLocation() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) chatContentView.getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        int index = meetingChatAdapter.getChatMessageArray().size() - 1;
        layoutManager.scrollToPosition(index);
        Log.i(TAG, "设置聊天信息行：" + index);

    }

    /**
     * 软键盘隐藏事件监听
     *
     * @param softKeyboardHeight 软键盘高度
     */
    @Override
    public void onSoftKeyboardHide(int softKeyboardHeight) {
        isShowSoftKeyboard = false;
        
        if (ScreenUtils.isPortrait()) {
            if (!isShowExpression) {
                setInputLayoutParams(Configuration.ORIENTATION_PORTRAIT);
            }
        } else {
            if (!isShowExpression) {
                setInputLayoutParams(Configuration.ORIENTATION_LANDSCAPE);
            }
        }

        if (isShowSelectAttenderLayout) {
            int orientation = ScreenUtils.isPortrait()
                    ? Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
            setSelectAttenderLayoutParams(orientation);
        }

    }


    @Override
    public void recycle() {
        super.recycle();
        Log.i(TAG, "ChatView资源清理");
    }

    /**
     * 设置聊天输入框的视图状态
     */
    public void setInputEditTextState() {
        boolean hasPublicChat = ChatManager.getInstance().hasPubChatPermission();
        if (!hasPublicChat) {
            setEditTextFocusable(textInputEditText, true);
            textInputEditText.setHint(context.getResources().getString(R.string.meetingui_pub_chat_not_hint));
            expressionSwitch.setClickable(true);
            expressionSwitch.setFocusable(true);
        }else {
            setEditTextFocusable(textInputEditText, true);
            textInputEditText.setHint("");
            expressionSwitch.setClickable(true);
            expressionSwitch.setFocusable(true);
        }
    }


    /**
     * 获取弹窗键盘输入框后，聊天内容输入框的Y轴位置
     */
    private float getInputViewY(boolean isPortrait) {
        float inputViewY = 0;
        if (isPortrait) {
            inputViewY = SharedPrefsUtil
                    .getFloat(Constant.SoftKeyboardParams.VERTICAL_SOFT_KEYBOARD_Y_KEY);
        } else {
            inputViewY = SharedPrefsUtil
                    .getFloat(Constant.SoftKeyboardParams.LANDSCAPE_SOFT_KEYBOARD_Y_KEY);
        }
        return inputViewY;
    }


    /**
     * 保存输入窗口的Y位置
     */
    private void saveInputViewY() {
        float inputViewY = 0;
        if (ScreenUtils.isPortrait()) {
            inputViewY = SharedPrefsUtil.getFloat(Constant.SoftKeyboardParams.VERTICAL_SOFT_KEYBOARD_Y_KEY);
            if (inputViewY <= 0) {
                SharedPrefsUtil
                        .putFloat(Constant.SoftKeyboardParams.VERTICAL_SOFT_KEYBOARD_Y_KEY, inPutLayout.getY());
            }
        } else {
            inputViewY = SharedPrefsUtil.getFloat(Constant.SoftKeyboardParams.LANDSCAPE_SOFT_KEYBOARD_Y_KEY);
            if (inputViewY <= 0) {
                SharedPrefsUtil
                        .putFloat(Constant.SoftKeyboardParams.LANDSCAPE_SOFT_KEYBOARD_Y_KEY, inPutLayout.getY());
            }
        }
    }

    /**
     * 点击聊天展示信息的RecyclerView时回调
     */
    @Override
    public void onClickChatWindowListener() {

        closeInput();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        chatManager.setChatMessageListener(null);
        MutableLiveData<Configuration> liveData = LiveDataBus.getInstance().getLiveData(LiveDataBus.KEY_MEETING_ACTIVITY_CONFIG);
        liveData.removeObserver(this::orientationChange);
        Editable msgEditable = textInputEditText.getText();
        if (msgEditable != null && !TextUtils.isEmpty(msgEditable.toString())) {
            String tempMsg = msgEditable.toString();
            MeetingTempDataUtils.getInstance().saveString(MeetingTempDataUtils.TEMP_MSG_KEY, tempMsg);
        }
    }
}
