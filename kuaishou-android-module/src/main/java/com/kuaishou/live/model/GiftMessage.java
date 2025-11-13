package com.kuaishou.live.model;

/**
 * 礼物消息
 */
public class GiftMessage {
    private String uid;              // 用户ID
    private String username;         // 用户名
    private String userAvatar;       // 用户头像URL
    private String giftId;           // 礼物ID
    private String giftName;         // 礼物名称
    private String giftImageUrl;     // 礼物图片URL
    private int giftCount;           // 礼物数量
    private int comboCount;          // 连击数
    private long timestamp;          // 时间戳

    public GiftMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public String getGiftId() { return giftId; }
    public void setGiftId(String giftId) { this.giftId = giftId; }

    public String getGiftName() { return giftName; }
    public void setGiftName(String giftName) { this.giftName = giftName; }

    public String getGiftImageUrl() { return giftImageUrl; }
    public void setGiftImageUrl(String giftImageUrl) { this.giftImageUrl = giftImageUrl; }

    public int getGiftCount() { return giftCount; }
    public void setGiftCount(int giftCount) { this.giftCount = giftCount; }

    public int getComboCount() { return comboCount; }
    public void setComboCount(int comboCount) { this.comboCount = comboCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "GiftMessage{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", giftName='" + giftName + '\'' +
                ", giftCount=" + giftCount +
                ", comboCount=" + comboCount +
                ", timestamp=" + timestamp +
                '}';
    }
}
