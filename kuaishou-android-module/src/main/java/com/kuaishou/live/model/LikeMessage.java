package com.kuaishou.live.model;

/**
 * 点赞消息
 */
public class LikeMessage {
    private String uid;              // 用户ID
    private String username;         // 用户名
    private String userAvatar;       // 用户头像URL
    private int likeCount;           // 点赞数量
    private long timestamp;          // 时间戳

    public LikeMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "LikeMessage{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", likeCount=" + likeCount +
                ", timestamp=" + timestamp +
                '}';
    }
}
