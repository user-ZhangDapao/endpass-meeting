package com.sdcz.endpass.util;

import com.comix.meeting.entities.BaseUser;
import com.inpor.nativeapi.adaptor.RoomUserInfo;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * @Date: 2020/12/14
 * @Author: hugo
 * @Description: 参会人列表排序，规则如下：
 * @Description: * 参会人排序规则：
 * @Description: * 1.按照参会人角色排序：自己>主讲>管理员=初始管理员=主席=参会人=出席=旁听。
 * @Description: * 2.同为管理员、初始管理员、主席、参会人、出席、旁听，按照昵称排序 ：字母（包括汉字）>数字>特殊符号。
 * @Description: * 3.字母按升序排序，不区分大小写（如果当前的字母相同，则依次从下一个字母开始比较排序）。
 * @Description: * 4.汉字按拼音字母的升序排序（如果当前的字母相同，则依次从下一个字母开始比较排序）。
 * @Description: * 5.数字按照升序排列（如果当前的数字相同，则依次从下一个数字开始比较排序）。
 * @Description: * 6.特殊字符按照程序自带的unicode码表排序。
 */
public class AttendeeComparator implements Comparator<BaseUser> {

    public static final int MAX = 200; // 超过200个参会人则不排序
    private final Collator collator = Collator.getInstance(Locale.CHINA);
    private int count = 0;

    /**
     * 构造函数
     *
     * @param count 列表的size
     */
    public AttendeeComparator(int count) {
        this.count = count;
    }

    @Override
    public int compare(BaseUser leftUser, BaseUser rightUser) {
        int priorityDiff = getSzGdUserPriority(rightUser) - getSzGdUserPriority(leftUser);
        if (count < MAX) {
            // 如果用户角色相同则判断首字符是否是特殊字符
            if (priorityDiff == 0) {
                priorityDiff = compareValidator(leftUser, rightUser);
                // 若首位都是特殊字符，则直接按特殊字符 > 数字 > 字母 > 汉字进行排序
                if (priorityDiff == 0) {
                    priorityDiff = compareCollationKey(leftUser, rightUser);
                }
            }
        }

        return priorityDiff;
    }

    private static final int LOCAL_USER_PRIORITY = 100000;
    private static final int MAIN_SPEAKER_PRIORITY = 99999;
    private static final int MAIN_ATTEND_PRIORITY = Integer.MAX_VALUE;
    private static final int TELEPHONE_USER_PRIORITY = -100;

    private int getSzGdUserPriority(BaseUser user) {
        if (user.getRoomUserInfo().terminalType == RoomUserInfo.TERMINAL_TELEPHONE) {
            return TELEPHONE_USER_PRIORITY;
        } else if (user.isLocalUser()) {
            return LOCAL_USER_PRIORITY;
        } else if (user.isMainSpeakerDone()) {
            return MAIN_SPEAKER_PRIORITY;
        } else {
            if (user.getRoomUserInfo().seatList < 0) {
                return MAIN_ATTEND_PRIORITY - user.getRoomUserInfo().seatList;
            }
            return user.getRoomUserInfo().userRight;
        }
    }

    private int compareCollationKey(BaseUser leftUser, BaseUser rightUser) {
        CollationKey leftKey = collator.getCollationKey(leftUser.getNickName().toLowerCase());
        CollationKey rightKey = collator.getCollationKey(rightUser.getNickName().toLowerCase());
        return leftKey.compareTo(rightKey);
    }

    private int compareValidator(BaseUser leftUser, BaseUser rightUser) {
        boolean lhsIsSpecial = ValidatorUtil.isSpecialChar(leftUser.getNickName().substring(0, 1));
        boolean rhsIsSpecial = ValidatorUtil.isSpecialChar(rightUser.getNickName().substring(0, 1));
        if (lhsIsSpecial && !rhsIsSpecial) {
            return 1;
        } else if (!lhsIsSpecial && rhsIsSpecial) {
            return -1;
        }
        return 0;
    }
}
