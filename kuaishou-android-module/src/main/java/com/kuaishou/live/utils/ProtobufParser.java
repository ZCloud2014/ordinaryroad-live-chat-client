package com.kuaishou.live.utils;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.kuaishou.live.listener.KuaishouLiveListener;
import com.kuaishou.live.model.DanmuMessage;
import com.kuaishou.live.model.GiftMessage;
import com.kuaishou.live.model.LikeMessage;
import com.kuaishou.live.model.RoomStatsMessage;
import com.kuaishou.live.protobuf.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protobuf消息解析器 - 完整实现版
 */
public class ProtobufParser {

    private static final String TAG = "ProtobufParser";

    // 礼物连击缓存
    private static final ConcurrentHashMap<String, WebGiftFeedOuterClass.WebGiftFeed> giftCache =
            new ConcurrentHashMap<>();

    /**
     * 解析SocketMessage
     */
    public static void parseSocketMessage(byte[] data, KuaishouLiveListener listener) {
        try {
            SocketMessageOuterClass.SocketMessage socketMessage =
                    SocketMessageOuterClass.SocketMessage.parseFrom(data);

            PayloadTypeOuterClass.PayloadType payloadType = socketMessage.getPayloadType();
            ByteString payloadByteString = socketMessage.getPayload();

            if (payloadByteString == null || payloadByteString.isEmpty()) {
                return;
            }

            byte[] payload = payloadByteString.toByteArray();
            handleMessage(payloadType, payload, listener);

        } catch (Exception e) {
            Log.e(TAG, "解析SocketMessage失败", e);
        }
    }

    /**
     * 处理不同类型的消息
     */
    private static void handleMessage(PayloadTypeOuterClass.PayloadType payloadType,
                                      byte[] payload,
                                      KuaishouLiveListener listener) {
        try {
            switch (payloadType) {
                case SC_ENTER_ROOM_ACK:
                    Log.d(TAG, "收到进入房间确认");
                    SCWebEnterRoomAckOuterClass.SCWebEnterRoomAck ack =
                            SCWebEnterRoomAckOuterClass.SCWebEnterRoomAck.parseFrom(payload);
                    Log.d(TAG, "进入房间成功");
                    break;

                case SC_HEARTBEAT_ACK:
                    Log.d(TAG, "收到心跳响应");
                    break;

                case SC_FEED_PUSH:
                    // 最重要的消息类型，包含弹幕、礼物、点赞等
                    parseSCFeedPush(payload, listener);
                    break;

                default:
                    // 其他消息
                    listener.onOtherMessage(payloadType.getNumber(), payload);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "处理消息失败: payloadType=" + payloadType, e);
        }
    }

    /**
     * 解析SC_FEED_PUSH消息
     */
    private static void parseSCFeedPush(byte[] payload, KuaishouLiveListener listener) {
        try {
            SCWebFeedPushOuterClass.SCWebFeedPush scWebFeedPush =
                    SCWebFeedPushOuterClass.SCWebFeedPush.parseFrom(payload);

            // 解析弹幕
            List<WebCommentFeedOuterClass.WebCommentFeed> commentFeeds =
                    scWebFeedPush.getCommentFeedsList();
            for (WebCommentFeedOuterClass.WebCommentFeed commentFeed : commentFeeds) {
                DanmuMessage danmu = parseDanmu(commentFeed);
                listener.onDanmuMessage(danmu);
            }

            // 解析礼物
            List<WebGiftFeedOuterClass.WebGiftFeed> giftFeeds =
                    scWebFeedPush.getGiftFeedsList();
            for (WebGiftFeedOuterClass.WebGiftFeed giftFeed : giftFeeds) {
                GiftMessage gift = parseGift(giftFeed);
                listener.onGiftMessage(gift);
            }

            // 解析点赞
            List<WebLikeFeedOuterClass.WebLikeFeed> likeFeeds =
                    scWebFeedPush.getLikeFeedsList();
            for (WebLikeFeedOuterClass.WebLikeFeed likeFeed : likeFeeds) {
                LikeMessage like = parseLike(likeFeed);
                listener.onLikeMessage(like);
            }

            // 解析统计信息
            String displayLikeCount = scWebFeedPush.getDisplayLikeCount();
            String displayWatchingCount = scWebFeedPush.getDisplayWatchingCount();

            if (displayLikeCount != null || displayWatchingCount != null) {
                RoomStatsMessage stats = new RoomStatsMessage();
                stats.setLikedCount(displayLikeCount);
                stats.setWatchingCount(displayWatchingCount);
                listener.onRoomStatsMessage(stats);
            }

        } catch (Exception e) {
            Log.e(TAG, "解析SC_FEED_PUSH失败", e);
        }
    }

    /**
     * 解析弹幕消息
     */
    private static DanmuMessage parseDanmu(WebCommentFeedOuterClass.WebCommentFeed commentFeed) {
        DanmuMessage danmu = new DanmuMessage();

        // 用户信息
        if (commentFeed.hasUser()) {
            UserInfoOuterClass.UserInfo user = commentFeed.getUser();
            danmu.setUid(user.getPrincipalId());
            danmu.setUsername(user.getUserName());
            danmu.setUserAvatar(user.getHeadUrl());
        }

        // 弹幕内容
        danmu.setContent(commentFeed.getContent());

        // 粉丝牌信息
        if (commentFeed.hasSenderState()) {
            LiveAudienceStateOuterClass.LiveAudienceState senderState = commentFeed.getSenderState();
            String badgeName = getBadgeName(senderState);
            int badgeLevel = getBadgeLevel(senderState);
            danmu.setBadgeName(badgeName);
            danmu.setBadgeLevel(badgeLevel);
        }

        return danmu;
    }

    /**
     * 解析礼物消息
     */
    private static GiftMessage parseGift(WebGiftFeedOuterClass.WebGiftFeed giftFeed) {
        GiftMessage gift = new GiftMessage();

        // 用户信息
        if (giftFeed.hasUser()) {
            UserInfoOuterClass.UserInfo user = giftFeed.getUser();
            gift.setUid(user.getPrincipalId());
            gift.setUsername(user.getUserName());
            gift.setUserAvatar(user.getHeadUrl());
        }

        // 礼物信息
        gift.setGiftId(String.valueOf(giftFeed.getGiftId()));

        // 计算礼物数量（处理连击）
        int giftCount = calculateGiftCount(giftFeed);
        gift.setGiftCount(giftCount);
        gift.setComboCount(giftFeed.getComboCount());

        // 礼物图片和名称（需要从API获取，这里先用ID）
        gift.setGiftName("礼物-" + giftFeed.getGiftId());
        gift.setGiftImageUrl("");

        return gift;
    }

    /**
     * 解析点赞消息
     */
    private static LikeMessage parseLike(WebLikeFeedOuterClass.WebLikeFeed likeFeed) {
        LikeMessage like = new LikeMessage();

        // 用户信息
        if (likeFeed.hasUser()) {
            UserInfoOuterClass.UserInfo user = likeFeed.getUser();
            like.setUid(user.getPrincipalId());
            like.setUsername(user.getUserName());
            like.setUserAvatar(user.getHeadUrl());
        }

        // 点赞数量
        like.setLikeCount(1); // 快手每次点赞固定为1

        return like;
    }

    /**
     * 计算礼物数量（处理连击）
     */
    private static int calculateGiftCount(WebGiftFeedOuterClass.WebGiftFeed webGiftFeed) {
        int giftCount;
        String mergeKey = webGiftFeed.getMergeKey();

        if (giftCache.containsKey(mergeKey)) {
            WebGiftFeedOuterClass.WebGiftFeed cachedGift = giftCache.get(mergeKey);
            int cachedComboCount = cachedGift.getComboCount();
            giftCount = webGiftFeed.getComboCount() - cachedComboCount;
        } else {
            int batchSize = webGiftFeed.getBatchSize();
            int comboCount = webGiftFeed.getComboCount();
            if (comboCount == 1) {
                giftCount = batchSize;
            } else {
                giftCount = comboCount;
            }
        }

        giftCache.put(mergeKey, webGiftFeed);
        return giftCount;
    }

    /**
     * 获取粉丝牌名称
     */
    private static String getBadgeName(LiveAudienceStateOuterClass.LiveAudienceState liveAudienceState) {
        String badgeName = null;
        try {
            for (LiveAudienceStateOuterClass.LiveAudienceState.LiveAudienceState_11 state11 :
                 liveAudienceState.getLiveAudienceState11List()) {
                if (state11.hasLiveAudienceState111()) {
                    LiveAudienceStateOuterClass.LiveAudienceState.LiveAudienceState_111 state111 =
                            state11.getLiveAudienceState111();
                    String badgeIcon = state111.getBadgeIcon();
                    if (badgeIcon != null && badgeIcon.toLowerCase().startsWith("fans")) {
                        badgeName = state111.getBadgeName();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return badgeName;
    }

    /**
     * 获取粉丝牌等级
     */
    private static int getBadgeLevel(LiveAudienceStateOuterClass.LiveAudienceState liveAudienceState) {
        int badgeLevel = 0;
        try {
            if (liveAudienceState.hasLiveFansGroupState()) {
                badgeLevel = liveAudienceState.getLiveFansGroupState().getIntimacyLevel();
            }
        } catch (Exception e) {
            // ignore
        }
        return badgeLevel;
    }

    /**
     * 创建进入房间消息
     */
    public static byte[] createEnterRoomMessage(String token, String liveStreamId) throws IOException {
        CSWebEnterRoomOuterClass.CSWebEnterRoom enterRoom =
                CSWebEnterRoomOuterClass.CSWebEnterRoom.newBuilder()
                .setToken(token)
                .setLiveStreamId(liveStreamId)
                .setPageId("web_live")
                .build();

        SocketMessageOuterClass.SocketMessage socketMessage =
                SocketMessageOuterClass.SocketMessage.newBuilder()
                .setPayloadType(PayloadTypeOuterClass.PayloadType.CS_ENTER_ROOM)
                .setPayload(enterRoom.toByteString())
                .build();

        return socketMessage.toByteArray();
    }

    /**
     * 创建心跳消息
     */
    public static byte[] createHeartbeatMessage() throws IOException {
        CSWebHeartbeatOuterClass.CSWebHeartbeat heartbeat =
                CSWebHeartbeatOuterClass.CSWebHeartbeat.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .build();

        SocketMessageOuterClass.SocketMessage socketMessage =
                SocketMessageOuterClass.SocketMessage.newBuilder()
                .setPayloadType(PayloadTypeOuterClass.PayloadType.CS_HEARTBEAT)
                .setPayload(heartbeat.toByteString())
                .build();

        return socketMessage.toByteArray();
    }
}
