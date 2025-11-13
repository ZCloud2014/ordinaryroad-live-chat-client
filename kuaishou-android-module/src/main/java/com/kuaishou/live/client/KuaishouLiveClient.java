package com.kuaishou.live.client;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.kuaishou.live.api.KuaishouApi;
import com.kuaishou.live.listener.KuaishouLiveListener;
import com.kuaishou.live.utils.ProtobufParser;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * 快手直播WebSocket客户端 - Android版
 * 用于连接快手直播间，接收弹幕、礼物等消息
 */
public class KuaishouLiveClient {

    private static final String TAG = "KuaishouLiveClient";

    private final KuaishouApi kuaishouApi;
    private final KuaishouLiveListener listener;
    private final Handler mainHandler;
    private final OkHttpClient okHttpClient;

    private String roomId;
    private String cookie;
    private String kww;

    private WebSocket webSocket;
    private KuaishouApi.RoomInitResult roomInitResult;
    private long lastHeartbeatTime = 0;
    private static final long HEARTBEAT_INTERVAL = 20000; // 20秒心跳

    private boolean isConnected = false;
    private boolean isStopped = false;

    public KuaishouLiveClient(String roomId, KuaishouLiveListener listener) {
        this(roomId, null, null, listener);
    }

    public KuaishouLiveClient(String roomId, String cookie, String kww, KuaishouLiveListener listener) {
        this.roomId = roomId;
        this.cookie = cookie;
        this.kww = kww;
        this.listener = listener;
        this.kuaishouApi = new KuaishouApi();
        this.mainHandler = new Handler(Looper.getMainLooper());

        this.okHttpClient = new OkHttpClient.Builder()
                .pingInterval(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 开始连接
     */
    public void start() {
        isStopped = false;

        new Thread(() -> {
            try {
                // 初始化房间信息
                Log.d(TAG, "开始初始化房间: " + roomId);
                if (cookie != null && !cookie.isEmpty()) {
                    roomInitResult = kuaishouApi.roomInitWithCookie(roomId, cookie, kww);
                } else {
                    roomInitResult = kuaishouApi.roomInit(roomId);
                }

                Log.d(TAG, "房间初始化成功");
                Log.d(TAG, "liveStreamId: " + roomInitResult.getLiveStreamId());
                Log.d(TAG, "token: " + roomInitResult.getToken());
                Log.d(TAG, "websocketUrls: " + roomInitResult.getWebsocketUrls());

                // 选择一个WebSocket地址
                List<String> wsUrls = roomInitResult.getWebsocketUrls();
                if (wsUrls == null || wsUrls.isEmpty()) {
                    throw new IOException("无法获取WebSocket地址，主播可能未开播");
                }

                String wsUrl = wsUrls.get(new Random().nextInt(wsUrls.size()));
                Log.d(TAG, "选择WebSocket地址: " + wsUrl);

                // 连接WebSocket
                connectWebSocket(wsUrl);

            } catch (Exception e) {
                Log.e(TAG, "初始化失败", e);
                notifyError(e);
            }
        }).start();
    }

    /**
     * 停止连接
     */
    public void stop() {
        isStopped = true;
        if (webSocket != null) {
            webSocket.close(1000, "客户端主动关闭");
            webSocket = null;
        }
        isConnected = false;
    }

    /**
     * 发送弹幕
     */
    public void sendDanmu(String content) {
        if (!isConnected || roomInitResult == null) {
            Log.w(TAG, "未连接，无法发送弹幕");
            return;
        }

        if (cookie == null || cookie.isEmpty()) {
            Log.w(TAG, "未设置Cookie，无法发送弹幕");
            return;
        }

        new Thread(() -> {
            try {
                kuaishouApi.sendComment(cookie, kww, roomId, roomInitResult.getLiveStreamId(), content);
                Log.d(TAG, "弹幕发送成功: " + content);
            } catch (IOException e) {
                Log.e(TAG, "弹幕发送失败", e);
            }
        }).start();
    }

    /**
     * 点赞
     */
    public void clickLike(int count) {
        if (!isConnected || roomInitResult == null) {
            Log.w(TAG, "未连接，无法点赞");
            return;
        }

        if (cookie == null || cookie.isEmpty()) {
            Log.w(TAG, "未设置Cookie，无法点赞");
            return;
        }

        new Thread(() -> {
            try {
                kuaishouApi.clickLike(cookie, kww, roomId, roomInitResult.getLiveStreamId(), count);
                Log.d(TAG, "点赞成功: " + count);
            } catch (IOException e) {
                Log.e(TAG, "点赞失败", e);
            }
        }).start();
    }

    /**
     * 连接WebSocket
     */
    private void connectWebSocket(String wsUrl) {
        Request request = new Request.Builder()
                .url(wsUrl)
                .build();

        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket连接成功");
                isConnected = true;

                // 发送进入房间消息
                sendEnterRoomMessage();

                // 启动心跳
                startHeartbeat();

                // 通知连接成功
                notifyConnected();
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                try {
                    // 解析protobuf消息
                    ProtobufParser.parseSocketMessage(bytes.toByteArray(), listener);
                } catch (Exception e) {
                    Log.e(TAG, "解析消息失败", e);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "收到文本消息: " + text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket正在关闭: " + code + " " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket已关闭: " + code + " " + reason);
                isConnected = false;
                notifyDisconnected(reason);

                // 如果不是主动停止，尝试重连
                if (!isStopped) {
                    Log.d(TAG, "3秒后尝试重连...");
                    mainHandler.postDelayed(() -> start(), 3000);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket连接失败", t);
                isConnected = false;
                notifyError(new Exception(t));

                // 如果不是主动停止，尝试重连
                if (!isStopped) {
                    Log.d(TAG, "5秒后尝试重连...");
                    mainHandler.postDelayed(() -> start(), 5000);
                }
            }
        });
    }

    /**
     * 发送进入房间消息
     */
    private void sendEnterRoomMessage() {
        try {
            byte[] enterRoomMsg = ProtobufParser.createEnterRoomMessage(
                    roomInitResult.getToken(),
                    roomInitResult.getLiveStreamId()
            );
            webSocket.send(ByteString.of(enterRoomMsg));
            Log.d(TAG, "发送进入房间消息成功");
        } catch (Exception e) {
            Log.e(TAG, "发送进入房间消息失败", e);
        }
    }

    /**
     * 启动心跳
     */
    private void startHeartbeat() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnected && !isStopped) {
                    long now = System.currentTimeMillis();
                    if (now - lastHeartbeatTime >= HEARTBEAT_INTERVAL) {
                        sendHeartbeat();
                        lastHeartbeatTime = now;
                    }
                    mainHandler.postDelayed(this, HEARTBEAT_INTERVAL);
                }
            }
        }, HEARTBEAT_INTERVAL);
    }

    /**
     * 发送心跳
     */
    private void sendHeartbeat() {
        try {
            byte[] heartbeatMsg = ProtobufParser.createHeartbeatMessage();
            webSocket.send(ByteString.of(heartbeatMsg));
            Log.d(TAG, "发送心跳");
        } catch (Exception e) {
            Log.e(TAG, "发送心跳失败", e);
        }
    }

    // 通知方法
    private void notifyConnected() {
        mainHandler.post(() -> listener.onConnected());
    }

    private void notifyDisconnected(String reason) {
        mainHandler.post(() -> listener.onDisconnected(reason));
    }

    private void notifyError(Exception error) {
        mainHandler.post(() -> listener.onError(error));
    }

    // Getter方法
    public boolean isConnected() {
        return isConnected;
    }

    public KuaishouApi.RoomInitResult getRoomInitResult() {
        return roomInitResult;
    }
}
