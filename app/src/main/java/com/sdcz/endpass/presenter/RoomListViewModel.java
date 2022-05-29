package com.sdcz.endpass.presenter;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.annimon.stream.Stream;
import com.sdcz.endpass.SdkUtil;
import com.sdcz.endpass.bean.CloudRoom;
import com.sdcz.endpass.bean.InstantMeeting;
import com.sdcz.endpass.bean.Result;
import com.sdcz.endpass.bean.ScheduleMeeting;
import com.inpor.base.sdk.roomlist.IRoomListResultInterface;
import com.inpor.base.sdk.roomlist.RoomListManager;
import com.inpor.sdk.repository.BaseResponse;
import com.inpor.sdk.repository.PageResponse;
import com.inpor.sdk.repository.bean.CloudRoomInfo;
import com.inpor.sdk.repository.bean.InstantMeetingDto;
import com.inpor.sdk.repository.bean.ScheduleMeetingInfo;

import java.util.List;

public class RoomListViewModel extends ViewModel {

    public static final int TYPE_CLOUD_ROOM = 0;
    public static final int TYPE_SCHEDULE_ROOM = 1;
    public static final int TYPE_INSTANT_ROOM = 2;
    public static final int DEFAULT_PAGE_SIZE = 10;
    /* 查询 */
    public static final int DEFAULT_RECENT_DAY = 7;
    private final RoomListManager roomListManager;

    private MutableLiveData<Result<List<CloudRoom>>> cloudRoomResult = new MutableLiveData<>();
    private MutableLiveData<Result<List<ScheduleMeeting>>> scheduleRoomResult = new MutableLiveData<>();
    private MutableLiveData<Result<List<InstantMeeting>>> instantRoomResult = new MutableLiveData<>();
    private IRoomListResultInterface<BaseResponse<PageResponse<ScheduleMeetingInfo>>> scheduleMeetingInterface = new IRoomListResultInterface<BaseResponse<PageResponse<ScheduleMeetingInfo>>>() {
        @Override
        public void succeed(BaseResponse<PageResponse<ScheduleMeetingInfo>> result) {
            PageResponse<ScheduleMeetingInfo> pageResult = result.getResult();
            if (pageResult == null) {
                scheduleRoomResult.postValue(Result.createEmpty());
            } else {
                List<ScheduleMeetingInfo> pageResultItems = pageResult.getItems();
                if (pageResultItems == null || pageResultItems.isEmpty()) {
                    scheduleRoomResult.postValue(Result.createEmpty());
                } else {
                    List<ScheduleMeeting> scheduleMeetings = Stream.of(pageResultItems)
                            .map(ScheduleMeeting::convert)
                            .toList();
                    scheduleRoomResult.postValue(Result.createSuccess(scheduleMeetings));
                }
            }
        }

        @Override
        public void failed(int code, String errorMsg) {
            scheduleRoomResult.postValue(Result.createError(code, errorMsg));
        }
    };
    private IRoomListResultInterface<BaseResponse<InstantMeetingDto>> instantMeetingInterface = new IRoomListResultInterface<BaseResponse<InstantMeetingDto>>() {
        @Override
        public void failed(int code, String errorMsg) {
            instantRoomResult.postValue(Result.createError(code, errorMsg));
        }

        @Override
        public void succeed(BaseResponse<InstantMeetingDto> result) {

            if (result == null || result.getResult() == null ||
                    result.getResult().getItems() == null || result.getResult().getItems().isEmpty()) {
                instantRoomResult.postValue(Result.createEmpty());
            } else {
                List<InstantMeeting> instantMeetings = Stream.of(result.getResult().getItems())
                        .map(InstantMeeting::convert)
                        .toList();
                instantRoomResult.postValue(Result.createSuccess(instantMeetings));
            }
        }
    };
    private IRoomListResultInterface<BaseResponse<List<CloudRoomInfo>>> cloudMeetingInterface = new IRoomListResultInterface<BaseResponse<List<CloudRoomInfo>>>() {
        @Override
        public void failed(int code, String errorMsg) {
            cloudRoomResult.postValue(Result.createError(code, errorMsg));
        }

        @Override
        public void succeed(BaseResponse<List<CloudRoomInfo>> result) {
            if (result == null || result.getRoomList() == null || result.getRoomList().isEmpty()) {
                cloudRoomResult.postValue(Result.createEmpty());
            } else {
                List<CloudRoom> cloudRooms = Stream.of(result.getRoomList())
                        .map(CloudRoom::convert)
                        .toList();
                cloudRoomResult.postValue(Result.createSuccess(cloudRooms));
            }
        }
    };

    public RoomListViewModel() {
        roomListManager = SdkUtil.getRoomListManager();
    }

    public void queryRoomList(int type, int page) {
        queryRoomListByKeyword(type, page, DEFAULT_PAGE_SIZE,"");
    }

    public void queryRoomListByKeyword(int type, int page, int pageSize, String keyword) {
        switch (type) {
            case TYPE_CLOUD_ROOM:
                roomListManager.getOrQueryCloudMeetingRoom(keyword, Integer.MAX_VALUE, 1, cloudMeetingInterface);
                break;
            case TYPE_SCHEDULE_ROOM:
                roomListManager.getMeetingScheduleList(keyword, DEFAULT_RECENT_DAY, page, pageSize, scheduleMeetingInterface);
                break;
            case TYPE_INSTANT_ROOM:
                roomListManager.getInstantMeetings(2, instantMeetingInterface);
                break;
        }
    }

    public MutableLiveData<Result<List<CloudRoom>>> getCloudRoomResult() {
        return cloudRoomResult;
    }

    public MutableLiveData<Result<List<ScheduleMeeting>>> getScheduleRoomResult() {
        return scheduleRoomResult;
    }

    public MutableLiveData<Result<List<InstantMeeting>>> getInstantRoomResult() {
        return instantRoomResult;
    }
}
