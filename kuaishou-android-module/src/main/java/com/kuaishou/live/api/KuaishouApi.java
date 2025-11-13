package com.kuaishou.live.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 快手直播API - Android简化版
 * 用于获取房间信息和WebSocket连接地址
 */
public class KuaishouApi {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final Pattern PATTERN_LIVE_ROOM_DETAIL = Pattern.compile("\"playList\":\\s*\\[([\\s\\S]*?)\\](?=,\\s*\"loading\"|$)");

    private final OkHttpClient httpClient;

    public KuaishouApi() {
        this.httpClient = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }

    /**
     * 初始化房间信息（不使用Cookie）
     * @param roomId 房间ID或用户ID
     * @return 房间初始化结果
     */
    public RoomInitResult roomInit(String roomId) throws IOException {
        // 方法1: 使用API直接获取（推荐，无需Cookie）
        String url = "https://live.kuaishou.com/live_api/liveroom/livedetail?principalId=" + roomId;

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }

            String body = response.body().string();
            JSONObject json = new JSONObject(body);

            if (!json.has("data")) {
                throw new IOException("响应数据格式错误");
            }

            JSONObject data = json.getJSONObject("data");
            RoomInitResult result = new RoomInitResult();

            // 解析直播流信息
            if (data.has("liveStream")) {
                JSONObject liveStream = data.getJSONObject("liveStream");
                result.setLiveStreamId(liveStream.optString("id", ""));
            }

            // 解析WebSocket信息
            if (data.has("websocketInfo")) {
                JSONObject websocketInfo = data.getJSONObject("websocketInfo");
                result.setToken(websocketInfo.optString("token", ""));

                if (websocketInfo.has("webSocketAddresses")) {
                    JSONArray addresses = websocketInfo.getJSONArray("webSocketAddresses");
                    List<String> websocketUrls = new ArrayList<>();
                    for (int i = 0; i < addresses.length(); i++) {
                        websocketUrls.add(addresses.getString(i));
                    }
                    result.setWebsocketUrls(websocketUrls);
                }
            }

            // 解析房间信息
            result.setRoomTitle(data.optString("caption", ""));
            result.setRoomId(roomId);

            // 判断直播状态
            String liveStatus = "LIVING";
            if (data.has("liveStream")) {
                liveStatus = data.getJSONObject("liveStream").optString("playStatus", "LIVING");
            }
            result.setLiveStatus(liveStatus);

            return result;
        }
    }

    /**
     * 初始化房间信息（使用Cookie，可避免滑块验证）
     * @param roomId 房间ID
     * @param cookie Cookie字符串
     * @param kww Kww请求头（可选）
     * @return 房间初始化结果
     */
    public RoomInitResult roomInitWithCookie(String roomId, String cookie, String kww) throws IOException {
        String url = "https://live.kuaishou.com/u/" + roomId;

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Cookie", cookie);

        if (kww != null && !kww.isEmpty()) {
            requestBuilder.header("Kww", kww);
        }

        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }

            String body = response.body().string();

            // 正则提取playList
            Matcher matcher = PATTERN_LIVE_ROOM_DETAIL.matcher(body);
            if (!matcher.find()) {
                throw new IOException("无法从页面提取直播信息，可能主播未开播");
            }

            String playListJson = matcher.group(1).replace("undefined", "null");
            JSONObject liveDetail = new JSONObject("{\"playList\":[" + playListJson + "]}");
            JSONArray playList = liveDetail.getJSONArray("playList");

            if (playList.length() == 0) {
                throw new IOException("主播未开播");
            }

            JSONObject firstStream = playList.getJSONObject(0);
            RoomInitResult result = new RoomInitResult();

            String liveStreamId = "";
            if (firstStream.has("liveStream") && !firstStream.isNull("liveStream")) {
                JSONObject liveStream = firstStream.getJSONObject("liveStream");
                liveStreamId = liveStream.optString("id", "");
                result.setLiveStreamId(liveStreamId);
            }

            // 获取WebSocket信息
            if (!liveStreamId.isEmpty()) {
                String wsInfoUrl = "https://live.kuaishou.com/live_api/liveroom/websocketinfo?liveStreamId=" + liveStreamId;
                Request wsRequest = new Request.Builder()
                        .url(wsInfoUrl)
                        .header("User-Agent", USER_AGENT)
                        .header("Cookie", cookie)
                        .header("Referer", "https://live.kuaishou.com/u/" + roomId)
                        .build();

                if (kww != null && !kww.isEmpty()) {
                    wsRequest = wsRequest.newBuilder().header("Kww", kww).build();
                }

                try (Response wsResponse = httpClient.newCall(wsRequest).execute()) {
                    if (wsResponse.isSuccessful()) {
                        String wsBody = wsResponse.body().string();
                        JSONObject wsJson = new JSONObject(wsBody);

                        if (wsJson.has("data")) {
                            JSONObject wsData = wsJson.getJSONObject("data");
                            result.setToken(wsData.optString("token", ""));

                            if (wsData.has("websocketUrls")) {
                                JSONArray urls = wsData.getJSONArray("websocketUrls");
                                List<String> websocketUrls = new ArrayList<>();
                                for (int i = 0; i < urls.length(); i++) {
                                    websocketUrls.add(urls.getString(i));
                                }
                                result.setWebsocketUrls(websocketUrls);
                            }
                        }
                    }
                }
            }

            result.setRoomId(roomId);
            result.setRoomTitle(firstStream.optString("caption", ""));
            result.setLiveStatus("LIVING");

            return result;
        }
    }

    /**
     * 发送弹幕
     * @param cookie Cookie
     * @param kww Kww头
     * @param roomId 房间ID
     * @param liveStreamId 直播流ID
     * @param content 弹幕内容
     */
    public void sendComment(String cookie, String kww, String roomId, String liveStreamId, String content) throws IOException {
        String url = "https://live.kuaishou.com/live_api/liveroom/sendComment";

        JSONObject requestBody = new JSONObject();
        requestBody.put("liveStreamId", liveStreamId);
        requestBody.put("content", content);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(okhttp3.RequestBody.create(
                        requestBody.toString(),
                        okhttp3.MediaType.parse("application/json; charset=utf-8")
                ))
                .header("User-Agent", USER_AGENT)
                .header("Cookie", cookie)
                .header("Origin", "https://live.kuaishou.com")
                .header("Referer", "https://live.kuaishou.com/u/" + roomId);

        if (kww != null && !kww.isEmpty()) {
            requestBuilder.header("Kww", kww);
        }

        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("发送弹幕失败: " + response.code());
            }

            String body = response.body().string();
            JSONObject json = new JSONObject(body);

            if (json.has("data")) {
                JSONObject data = json.getJSONObject("data");
                int result = data.optInt("result", -1);
                if (result != 1) {
                    throw new IOException("发送弹幕失败: " + (result == 2 ? "请求过快" : "未知错误"));
                }
            }
        }
    }

    /**
     * 点赞
     * @param cookie Cookie
     * @param kww Kww头
     * @param roomId 房间ID
     * @param liveStreamId 直播流ID
     * @param count 点赞数量
     */
    public void clickLike(String cookie, String kww, String roomId, String liveStreamId, int count) throws IOException {
        String url = "https://live.kuaishou.com/live_api/liveroom/like";

        JSONObject requestBody = new JSONObject();
        requestBody.put("liveStreamId", liveStreamId);
        requestBody.put("count", count);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(okhttp3.RequestBody.create(
                        requestBody.toString(),
                        okhttp3.MediaType.parse("application/json; charset=utf-8")
                ))
                .header("User-Agent", USER_AGENT)
                .header("Cookie", cookie)
                .header("Origin", "https://live.kuaishou.com")
                .header("Referer", "https://live.kuaishou.com/u/" + roomId);

        if (kww != null && !kww.isEmpty()) {
            requestBuilder.header("Kww", kww);
        }

        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("点赞失败: " + response.code());
            }
        }
    }

    /**
     * 房间初始化结果
     */
    public static class RoomInitResult {
        private String roomId;
        private String liveStreamId;
        private String token;
        private List<String> websocketUrls;
        private String roomTitle;
        private String liveStatus;

        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }

        public String getLiveStreamId() { return liveStreamId; }
        public void setLiveStreamId(String liveStreamId) { this.liveStreamId = liveStreamId; }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public List<String> getWebsocketUrls() { return websocketUrls; }
        public void setWebsocketUrls(List<String> websocketUrls) { this.websocketUrls = websocketUrls; }

        public String getRoomTitle() { return roomTitle; }
        public void setRoomTitle(String roomTitle) { this.roomTitle = roomTitle; }

        public String getLiveStatus() { return liveStatus; }
        public void setLiveStatus(String liveStatus) { this.liveStatus = liveStatus; }
    }
}
