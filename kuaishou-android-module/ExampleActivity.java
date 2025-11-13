package com.kuaishou.live.example;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kuaishou.live.client.KuaishouLiveClient;
import com.kuaishou.live.listener.SimpleKuaishouLiveListener;
import com.kuaishou.live.model.DanmuMessage;
import com.kuaishou.live.model.GiftMessage;
import com.kuaishou.live.model.LikeMessage;
import com.kuaishou.live.model.RoomStatsMessage;

/**
 * 快手直播监听示例Activity
 *
 * 这个示例展示了如何：
 * 1. 连接快手直播间
 * 2. 接收弹幕、礼物、点赞消息
 * 3. 发送弹幕
 * 4. 为主播点赞
 */
public class ExampleActivity extends AppCompatActivity {

    private static final String TAG = "KuaishouExample";

    private EditText etRoomId;
    private EditText etCookie;
    private EditText etMessage;
    private Button btnConnect;
    private Button btnDisconnect;
    private Button btnSendDanmu;
    private Button btnLike;
    private TextView tvLog;

    private KuaishouLiveClient liveClient;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_example);

        initViews();
        setupListeners();
    }

    private void initViews() {
        // 假设你的布局文件中有这些控件
        // etRoomId = findViewById(R.id.et_room_id);
        // etCookie = findViewById(R.id.et_cookie);
        // etMessage = findViewById(R.id.et_message);
        // btnConnect = findViewById(R.id.btn_connect);
        // btnDisconnect = findViewById(R.id.btn_disconnect);
        // btnSendDanmu = findViewById(R.id.btn_send_danmu);
        // btnLike = findViewById(R.id.btn_like);
        // tvLog = findViewById(R.id.tv_log);
    }

    private void setupListeners() {
        // 连接按钮
        btnConnect.setOnClickListener(v -> connectToLiveRoom());

        // 断开按钮
        btnDisconnect.setOnClickListener(v -> disconnectFromLiveRoom());

        // 发送弹幕按钮
        btnSendDanmu.setOnClickListener(v -> sendDanmu());

        // 点赞按钮
        btnLike.setOnClickListener(v -> clickLike());
    }

    /**
     * 连接到直播间
     */
    private void connectToLiveRoom() {
        String roomId = etRoomId.getText().toString().trim();
        if (roomId.isEmpty()) {
            appendLog("请输入房间ID");
            return;
        }

        String cookie = etCookie.getText().toString().trim();

        appendLog("正在连接房间: " + roomId);

        // 创建监听器
        SimpleKuaishouLiveListener listener = new SimpleKuaishouLiveListener() {
            @Override
            public void onConnected() {
                isConnected = true;
                appendLog("✅ 连接成功！");
                updateButtonStates();
            }

            @Override
            public void onDisconnected(String reason) {
                isConnected = false;
                appendLog("❌ 连接断开: " + reason);
                updateButtonStates();
            }

            @Override
            public void onError(Exception error) {
                appendLog("❌ 错误: " + error.getMessage());
                Log.e(TAG, "发生错误", error);
            }

            @Override
            public void onDanmuMessage(DanmuMessage danmu) {
                String badge = danmu.getBadgeName() != null ?
                        "[" + danmu.getBadgeName() + " Lv." + danmu.getBadgeLevel() + "]" : "";
                appendLog("💬 " + badge + danmu.getUsername() + ": " + danmu.getContent());
            }

            @Override
            public void onGiftMessage(GiftMessage gift) {
                appendLog("🎁 " + gift.getUsername() + " 送出 " +
                        gift.getGiftName() + " x " + gift.getGiftCount());
            }

            @Override
            public void onLikeMessage(LikeMessage like) {
                appendLog("❤️ " + like.getUsername() + " 点赞了");
            }

            @Override
            public void onRoomStatsMessage(RoomStatsMessage stats) {
                appendLog("📊 观看: " + stats.getWatchingCount() +
                        ", 点赞: " + stats.getLikedCount());
            }

            @Override
            public void onOtherMessage(int payloadType, byte[] rawData) {
                // 可以在这里处理其他类型的消息
                Log.d(TAG, "收到其他消息类型: " + payloadType);
            }
        };

        // 创建客户端
        if (cookie.isEmpty()) {
            // 不使用Cookie，只能接收消息
            liveClient = new KuaishouLiveClient(roomId, listener);
        } else {
            // 使用Cookie，可以发送弹幕和点赞
            String kww = ""; // 可以从浏览器获取
            liveClient = new KuaishouLiveClient(roomId, cookie, kww, listener);
        }

        // 开始连接
        liveClient.start();
    }

    /**
     * 断开连接
     */
    private void disconnectFromLiveRoom() {
        if (liveClient != null) {
            liveClient.stop();
            liveClient = null;
            isConnected = false;
            appendLog("已断开连接");
            updateButtonStates();
        }
    }

    /**
     * 发送弹幕
     */
    private void sendDanmu() {
        if (!isConnected || liveClient == null) {
            appendLog("请先连接到直播间");
            return;
        }

        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            appendLog("请输入要发送的消息");
            return;
        }

        liveClient.sendDanmu(message);
        appendLog("📤 发送弹幕: " + message);
        etMessage.setText(""); // 清空输入框
    }

    /**
     * 点赞
     */
    private void clickLike() {
        if (!isConnected || liveClient == null) {
            appendLog("请先连接到直播间");
            return;
        }

        liveClient.clickLike(10); // 点赞10次
        appendLog("❤️ 已点赞 x10");
    }

    /**
     * 添加日志到TextView
     */
    private void appendLog(String message) {
        runOnUiThread(() -> {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss")
                    .format(new java.util.Date());
            tvLog.append("[" + timestamp + "] " + message + "\n");

            // 滚动到底部
            tvLog.post(() -> {
                int scrollAmount = tvLog.getLayout().getLineTop(tvLog.getLineCount())
                        - tvLog.getHeight();
                if (scrollAmount > 0) {
                    tvLog.scrollTo(0, scrollAmount);
                }
            });
        });
    }

    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        runOnUiThread(() -> {
            btnConnect.setEnabled(!isConnected);
            btnDisconnect.setEnabled(isConnected);
            btnSendDanmu.setEnabled(isConnected);
            btnLike.setEnabled(isConnected);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 确保在Activity销毁时断开连接
        disconnectFromLiveRoom();
    }
}
