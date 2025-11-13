package com.kuaishou.live.model;

/**
 * 弹幕消息
 */
public class DanmuMessage {
    private String uid;              // 用户ID
    private String username;         // 用户名
    private String userAvatar;       // 用户头像URL
    private String content;          // 弹幕内容
    private String badgeName;        // 粉丝牌名称
    private int badgeLevel;          // 粉丝牌等级
    private long timestamp;          // 时间戳

    public DanmuMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }

    public int getBadgeLevel() { return badgeLevel; }
    public void setBadgeLevel(int badgeLevel) { this.badgeLevel = badgeLevel; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "DanmuMessage{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", content='" + content + '\'' +
                ", badgeName='" + badgeName + '\'' +
                ", badgeLevel=" + badgeLevel +
                ", timestamp=" + timestamp +
                '}';
    }
}
