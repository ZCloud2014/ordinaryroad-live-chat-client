package com.kuaishou.live.listener;

import com.kuaishou.live.model.DanmuMessage;
import com.kuaishou.live.model.GiftMessage;
import com.kuaishou.live.model.LikeMessage;
import com.kuaishou.live.model.RoomStatsMessage;

/**
 * 快手直播消息监听器
 */
public interface KuaishouLiveListener {

    /**
     * 连接成功
     */
    void onConnected();

    /**
     * 连接断开
     * @param reason 断开原因
     */
    void onDisconnected(String reason);

    /**
     * 发生错误
     * @param error 错误信息
     */
    void onError(Exception error);

    /**
     * 收到弹幕消息
     * @param danmu 弹幕消息
     */
    void onDanmuMessage(DanmuMessage danmu);

    /**
     * 收到礼物消息
     * @param gift 礼物消息
     */
    void onGiftMessage(GiftMessage gift);

    /**
     * 收到点赞消息
     * @param like 点赞消息
     */
    void onLikeMessage(LikeMessage like);

    /**
     * 收到直播间统计信息
     * @param stats 统计信息
     */
    void onRoomStatsMessage(RoomStatsMessage stats);

    /**
     * 收到其他未解析的消息
     * @param payloadType 消息类型
     * @param rawData 原始数据
     */
    void onOtherMessage(int payloadType, byte[] rawData);
}
