package com.kuaishou.live.model;

/**
 * 直播间统计信息
 */
public class RoomStatsMessage {
    private String likedCount;       // 点赞总数
    private String watchingCount;    // 当前观看人数
    private long timestamp;          // 时间戳

    public RoomStatsMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public String getLikedCount() { return likedCount; }
    public void setLikedCount(String likedCount) { this.likedCount = likedCount; }

    public String getWatchingCount() { return watchingCount; }
    public void setWatchingCount(String watchingCount) { this.watchingCount = watchingCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "RoomStatsMessage{" +
                "likedCount='" + likedCount + '\'' +
                ", watchingCount='" + watchingCount + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
