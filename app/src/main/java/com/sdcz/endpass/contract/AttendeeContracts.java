package com.sdcz.endpass.contract;

import com.comix.meeting.entities.BaseUser;

import java.util.List;

/**
 * @Description: 参会人MVP接口
 */
public interface AttendeeContracts {

    interface IModel {
        /**
         * 按条件查询参会人
         * @param category 类别{@link AttendeeCategory}
         * @param sort 是否需要排序(列表显示需要排序，统计类别数目则不需要)
         */
        List<BaseUser> queryUsers(@AttendeeCategory int category, boolean sort);

        /**
         * 从列表中查找指定关键词的用户
         * @param all 搜索的列表
         * @param keyword 关键词
         * @return 符合关键词的用户
         */
        List<BaseUser> queryUsers(List<BaseUser> all, String keyword);

        /**
         * 对传入的参会人列表排序
         * @param list 要排序的参会人列表
         */
        void sort(List<BaseUser> list);
    }

    interface IView {
        /**
         *  查询用户结果回调
         * @param category 类别{@link AttendeeCategory}
         * @param users 结果
         */
        default void onUsersResult(@AttendeeCategory int category, List<BaseUser> users) {}

        /**
         * 按关键字查找用户结果返回
         * @param key 关键字
         * @param users 符合条件的用户
         */
        default void onUsersResult(String key, List<BaseUser> users) {}

        /**
         * 类别计数结果
         * @param category 类别
         * @param count 总数
         */
        default void onCountUser(@AttendeeCategory int category, int count) {}
    }

    interface IPresenter {
        /**
         * 按条件查询参会人
         * @param category 类别{@link AttendeeCategory}
         */
        void queryUsers(@AttendeeCategory int category);

        /**
         * 按关键字查找用户
         * @param category 类别
         * @param key 关键字
         */
        void queryUsers(@AttendeeCategory int category, String key);

        /**
         * 取消关键字查找(根据输入框改变可能会快速多次查找，把前面的覆盖掉)
         */
        void cancelKeywordSearch();

        /**
         * 计算类别的总数
         * @return 总数
         */
        void countUser();
    }
}
