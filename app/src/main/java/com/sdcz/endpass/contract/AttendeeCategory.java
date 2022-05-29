package com.sdcz.endpass.contract;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Description: UI上分类显示参会人
 */
@IntDef({AttendeeCategory.ALL,
        AttendeeCategory.SPEAKING,
        AttendeeCategory.REQUEST_SPEAKING,
        AttendeeCategory.OFFLINE})
@Retention(RetentionPolicy.SOURCE)
public @interface AttendeeCategory {
    int ALL = 10;              // 所有参会人
    int SPEAKING = 11;         // 正在发言
    int REQUEST_SPEAKING = 12; // 请求发言
    int OFFLINE = 13;          // 离线参会人
}
