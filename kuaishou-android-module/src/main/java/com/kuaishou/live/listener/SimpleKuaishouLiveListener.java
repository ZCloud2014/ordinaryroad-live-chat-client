package com.kuaishou.live.listener;

import com.kuaishou.live.model.DanmuMessage;
import com.kuaishou.live.model.GiftMessage;
import com.kuaishou.live.model.LikeMessage;
import com.kuaishou.live.model.RoomStatsMessage;

/**
 * 快手直播监听器简化实现
 * 继承此类可以只实现需要的方法
 */
public abstract class SimpleKuaishouLiveListener implements KuaishouLiveListener {

    @Override
    public void onConnected() {
        // 默认空实现
    }

    @Override
    public void onDisconnected(String reason) {
        // 默认空实现
    }

    @Override
    public void onError(Exception error) {
        // 默认空实现
    }

    @Override
    public void onDanmuMessage(DanmuMessage danmu) {
        // 默认空实现
    }

    @Override
    public void onGiftMessage(GiftMessage gift) {
        // 默认空实现
    }

    @Override
    public void onLikeMessage(LikeMessage like) {
        // 默认空实现
    }

    @Override
    public void onRoomStatsMessage(RoomStatsMessage stats) {
        // 默认空实现
    }

    @Override
    public void onOtherMessage(int payloadType, byte[] rawData) {
        // 默认空实现
    }
}
