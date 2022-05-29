package com.sdcz.endpass.model;

import android.util.Log;

import com.comix.meeting.entities.BaseUser;
import com.sdcz.endpass.SdkUtil;
import com.inpor.base.sdk.audio.AudioManager;
import com.inpor.base.sdk.user.UserManager;

import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Description: 麦克风能量值监听:* 1.注册观察者* 2.添加要监听的参会人* 3.一旦不需要再监听，则移除参会人(观察者也一样)
 */
public class MicEnergyMonitor {
    public static final String ATTENDEE_VIEW = "AttendeeView";
    public static final String MEETING_MENU_CONTAINER = "MeetingBottomAndTopMenuContainer";
    public static final String ATTENDEE_CATEGORY_VIEW = "AttendeeCategoryView";
    public static final String VIDEO_SCREEN_VIEW = "VideoScreenView";


    private final UserManager userModel;
    private static final String TAG = MicEnergyMonitor.class.getSimpleName();
    /**
     * 查询声音能量值的间隔，在D1上效果：50ms(太快不自然)，100ms(自然，有点快)，200ms(很自然)
     * 250ms(略显卡顿)，500ms(卡顿非常明显)。
     */
    private final long internal = 200;
    private final AudioManager audioModel;

    public interface AudioEnergyListener {
        void onAudioEnergyChanged(List<BaseUser> sources);
    }

    private static volatile MicEnergyMonitor instance;

    /**
     * 获取实例
     *
     * @return AudioEnergyMonitor
     */
    public static MicEnergyMonitor getInstance() {
        if (instance == null) {
            synchronized (MicEnergyMonitor.class) {
                if (instance == null) {
                    instance = new MicEnergyMonitor();
                }
            }
        }
        return instance;
    }

    private static void releaseInstance() {
        synchronized (MicEnergyMonitor.class) {
            instance = null;
        }
    }

    private final CopyOnWriteArrayList<BaseUser> sources = new CopyOnWriteArrayList<>();//被监听的所有用户

    /**
     * 由于 注册的监听可能存在多层嵌套的临时界面 如activty 注册监听
     * 而存在临时创建弹出popwind也注册监听的场景导致关闭popwind时 sources中对象被删activty 注册监听无法收到情况(popwind view 等销毁在popwind dimiss 回调之后)
     * 添加 sourceMap对监听用户id分组 List  string(组名)，
     * 先入对象定义了组名，该对象以不同的组名再次进入不会替换组名，嵌套的界面 推荐使用组名添加删除
     */
    private ConcurrentHashMap<Long, CopyOnWriteArrayList<String>> sourceMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AudioEnergyListener> observersMap = new ConcurrentHashMap<>();//对应不同组的监听回调
    private volatile boolean enable = true;
    private Timer timer;

    private MicEnergyMonitor() {
        userModel =  SdkUtil.getUserManager();
        audioModel = SdkUtil.getAudioManager();
    }

    private final TimerTask captureTask = new TimerTask() {
        @Override
        public void run() {
            if (sources == null || sources.isEmpty()) {
                Log.i(TAG, "mic sources is empty");
                return;
            }
            if (!enable) {
                Log.i(TAG, "mic sources is disable");
                return;
            }
            boolean flag = false;//标记是否存在更新
            HashSet<String> updateGroup = new HashSet<>();//需要跟新的组
            for (BaseUser user : sources) {
                if (!user.isSpeechDone()) {
                    // 如果参会人没有广播音频则不用更新
                    continue;
                }
                flag = true;
                int energy;
                if (user.isLocalUser()) {
                    energy = audioModel.getMicrophoneEnergy(0);
                } else {
                    energy = audioModel.getMicrophoneEnergy(user.getAudioSourceId());
                }
                user.setSoundEnergy(energy);
                if (sourceMap.containsKey(user.getUserId())) {
                    CopyOnWriteArrayList<String> list = sourceMap.get(user.getUserId());
                    updateGroup.addAll(list);
                }
                //检测本地禁用状态
                if (user.getUserId() == userModel.getLocalUser().getUserId() &&
                        AppCache.getInstance().isMicDisable()) {
                    user.setSoundEnergy(0);
                }
                Log.i(TAG, String.format("user=%s, energy=%d", user.getNickName(), energy));
            }

            if (flag) {
                for (String str : updateGroup) {
                    if (observersMap.containsKey(str) && observersMap.get(str) != null) {
                        observersMap.get(str).onAudioEnergyChanged(sources);
                    }
                }
            }
        }
    };

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * @Description: 添加监听
     * @Author: xingwt
     * @return:
     * @Parame: listener AudioEnergyListener
     * @Parame: String group 对应监听组名
     * @CreateDate: 2021/3/19 14:30
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/19 14:30
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void addAudioEnergyListener(AudioEnergyListener listener, String group) {
        if (!observersMap.containsKey(group)) {
            observersMap.put(group, listener);
        }
    }

    /**
     * @Description: 移除监听
     * @Author: xingwt
     * @return:
     * @Parame: listener AudioEnergyListener
     * @Parame: String group 对应监听组名
     * @CreateDate: 2021/3/19 14:30
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/19 14:30
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void removeAudioEnergyListener(AudioEnergyListener listener, String group) {
        if (observersMap.containsKey(group)) {
            removeAudioSourcesGroup(group);
            observersMap.remove(group, listener);
            observersMap.remove(group);
        }
    }

    /**
     * @Description: 添加要监听的用户
     * @Author: xingwt
     * @return:
     * @Parame: BaseUser user 用户
     * @Parame: String group 对应组名
     * @CreateDate: 2021/3/19 14:24
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/19 14:24
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void addAudioSource(BaseUser user, String group) {
        if (user == null) {
            return;
        }
        BaseUser old = null;
        for (BaseUser temp : sources) {
            if (temp.getUserId() == user.getUserId()) {
                old = temp;
                break;
            }
        }
        // 如果已经存在相同ID的参会人则移除，所以当收到能量值更新时，建议不要直接使用BaseUser对象
        // (理论上直接使用也可以，因为本身确实也是同一个对象，因为最终都都来源于UserModel)
        if (old != null) {
            sources.remove(old);
        }
        sources.add(user);
        // 判断当前 监听user 以及对应组名 是否已经加入 无则加入

        CopyOnWriteArrayList<String> lists;
        if (!sourceMap.containsKey(user.getUserId())) {
            lists = new CopyOnWriteArrayList<>();
            sourceMap.put(user.getUserId(), lists);
        } else {
            lists = sourceMap.get(user.getUserId());
        }
        if (!lists.contains(group)) {
            lists.add(group);
        }
    }

    /**
     * @Description: 移除用户
     * @Author: xingwt
     * @return:
     * @Parame: BaseUser user 用户
     * @Parame: String group 对应组名
     * @CreateDate: 2021/3/19 14:24
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/19 14:24
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void removeAudioSource(BaseUser user, String group) {
        if (user == null) {
            return;
        }
        if (sourceMap.containsKey(user.getUserId())) {
            CopyOnWriteArrayList<String> lists;
            lists = sourceMap.get(user.getUserId());
            if (!lists.contains(group)) {
                lists.remove(group);
            }
            if (lists.size() <= 0) {
                sourceMap.remove(user.getUserId());
                sources.remove(user);
            }
        }
    }

    /*
      移除多个用户
     */
    /**
     * @Description: 批量移除用户
     * @Author: xingwt
     * @return:
     * @Parame: sources
     * @Parame: String group
     * @CreateDate: 2021/3/19 14:41
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/19 14:41
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void removeAudioSources(List<BaseUser> sources, String group) {
        if (sources == null || sources.isEmpty()) {
            return;
        }
        for (BaseUser user : sources) {
            removeAudioSource(user, group);
        }
    }

    /**
     * @Description: 移除一组用户
     * @Author: xingwt
     * @return:
     * @Parame: String group
     * @CreateDate: 2021/3/19 14:42
     * @UpdateUser: xingwt
     * @UpdateDate: 2021/3/19 14:42
     * @UpdateRemark: 更新说明
     * @Version: 1.0
     */
    public void removeAudioSourcesGroup(String group) {
        if (group == null || group.isEmpty()) {
            return;
        }
        ConcurrentHashMap<Long, CopyOnWriteArrayList<String>> map = new ConcurrentHashMap();
        for (ConcurrentHashMap.Entry<Long, CopyOnWriteArrayList<String>> m : sourceMap.entrySet()) {
            CopyOnWriteArrayList<String> lists = m.getValue();
            if (!lists.contains(group)) {
                lists.remove(group);
            }
            if (lists.size() <= 0) {
                sources.remove(m.getKey());
            } else {
                map.put(m.getKey(), m.getValue());
            }
        }
        sourceMap = map;
    }

    /**
     * 开始语音能量值跟踪
     */
    public void startAudioEnergyMonitor() {
        try {
            timer = new Timer("audio-energy-monitor");
            timer.schedule(captureTask, 2000, internal);
            Log.i(TAG, "startAudioEnergyMonitor");
        } catch (Exception ex) {
            Log.i(TAG, "start audio energy monitor exception:" + ex.getMessage());
        }
    }

    /**
     * 停止语音能量跟踪
     */
    public void stopAudioEnergyMonitor() {
        if (timer == null) {
            return;
        }
        try {
            timer.cancel();
            timer = null;
            Log.i(TAG, "stopAudioEnergyMonitor");
        } catch (Exception ex) {
            Log.i(TAG, "stop audio energy monitor exception:" + ex.getMessage());
        }
    }

    /**
     * 释放资源(销毁MeetingActivity时调用)
     */
    public void release() {
        observersMap.clear();
        sourceMap.clear();
        sources.clear();
        MicEnergyMonitor.releaseInstance();
    }
}
















