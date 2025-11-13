# 快手直播抓取模块 - Android版

这是一个从 [ordinaryroad-live-chat-client](https://github.com/OrdinaryRoad-Project/ordinaryroad-live-chat-client) 项目中提取的快手直播抓取模块，专门为Android平台优化。

## 📦 功能特性

- ✅ 连接快手直播间WebSocket
- ✅ 接收弹幕消息
- ✅ 接收礼物消息
- ✅ 接收点赞消息
- ✅ 接收直播间统计信息（观看人数、点赞数）
- ✅ 发送弹幕（需要Cookie）
- ✅ 为主播点赞（需要Cookie）
- ✅ 自动重连机制
- ✅ 心跳保持连接

## 🚀 快速开始

### 1. 添加依赖

在你的Android项目的 `build.gradle` 中添加：

```gradle
dependencies {
    // OkHttp (WebSocket支持)
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'

    // JSON解析
    implementation 'org.json:json:20230227'

    // Google Protobuf (可选，完整功能需要)
    implementation 'com.google.protobuf:protobuf-java:3.25.1'
}
```

### 2. 添加网络权限

在 `AndroidManifest.xml` 中添加：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 3. 基础使用

```java
import com.kuaishou.live.client.KuaishouLiveClient;
import com.kuaishou.live.listener.SimpleKuaishouLiveListener;
import com.kuaishou.live.model.*;

// 创建监听器
SimpleKuaishouLiveListener listener = new SimpleKuaishouLiveListener() {
    @Override
    public void onConnected() {
        Log.d("Kuaishou", "连接成功");
    }

    @Override
    public void onDanmuMessage(DanmuMessage danmu) {
        Log.d("Kuaishou", "收到弹幕: " + danmu.getUsername() + ": " + danmu.getContent());
    }

    @Override
    public void onGiftMessage(GiftMessage gift) {
        Log.d("Kuaishou", "收到礼物: " + gift.getUsername() + " 送出 " +
              gift.getGiftName() + " x " + gift.getGiftCount());
    }

    @Override
    public void onLikeMessage(LikeMessage like) {
        Log.d("Kuaishou", "收到点赞: " + like.getUsername());
    }

    @Override
    public void onRoomStatsMessage(RoomStatsMessage stats) {
        Log.d("Kuaishou", "观看人数: " + stats.getWatchingCount() +
              ", 点赞数: " + stats.getLikedCount());
    }

    @Override
    public void onError(Exception error) {
        Log.e("Kuaishou", "发生错误", error);
    }

    @Override
    public void onDisconnected(String reason) {
        Log.d("Kuaishou", "连接断开: " + reason);
    }
};

// 创建客户端（不需要Cookie）
String roomId = "3x4av52pnmw8mta"; // 房间ID或用户ID
KuaishouLiveClient client = new KuaishouLiveClient(roomId, listener);

// 开始连接
client.start();

// 停止连接
// client.stop();
```

### 4. 使用Cookie（发送弹幕、点赞）

```java
// 从浏览器获取Cookie和Kww
String cookie = "你的Cookie字符串";
String kww = "你的Kww值"; // 可选，但建议提供以避免滑块验证

// 创建客户端
KuaishouLiveClient client = new KuaishouLiveClient(roomId, cookie, kww, listener);
client.start();

// 发送弹幕
client.sendDanmu("你好，主播！");

// 点赞
client.clickLike(10); // 点赞10次
```

## 📖 API文档

### KuaishouLiveClient

#### 构造方法

```java
// 不使用Cookie（只接收消息）
KuaishouLiveClient(String roomId, KuaishouLiveListener listener)

// 使用Cookie（可发送弹幕、点赞）
KuaishouLiveClient(String roomId, String cookie, String kww, KuaishouLiveListener listener)
```

#### 主要方法

```java
void start()                    // 开始连接
void stop()                     // 停止连接
void sendDanmu(String content)  // 发送弹幕（需要Cookie）
void clickLike(int count)       // 点赞（需要Cookie）
boolean isConnected()           // 是否已连接
```

### KuaishouLiveListener

所有消息回调接口：

```java
void onConnected()                              // 连接成功
void onDisconnected(String reason)              // 连接断开
void onError(Exception error)                   // 发生错误
void onDanmuMessage(DanmuMessage danmu)         // 收到弹幕
void onGiftMessage(GiftMessage gift)            // 收到礼物
void onLikeMessage(LikeMessage like)            // 收到点赞
void onRoomStatsMessage(RoomStatsMessage stats) // 收到统计信息
void onOtherMessage(int payloadType, byte[] rawData) // 收到其他消息
```

### 消息模型

#### DanmuMessage（弹幕）

```java
String getUid()          // 用户ID
String getUsername()     // 用户名
String getUserAvatar()   // 用户头像URL
String getContent()      // 弹幕内容
String getBadgeName()    // 粉丝牌名称
int getBadgeLevel()      // 粉丝牌等级
long getTimestamp()      // 时间戳
```

#### GiftMessage（礼物）

```java
String getUid()          // 用户ID
String getUsername()     // 用户名
String getGiftId()       // 礼物ID
String getGiftName()     // 礼物名称
String getGiftImageUrl() // 礼物图片URL
int getGiftCount()       // 礼物数量
int getComboCount()      // 连击数
```

#### LikeMessage（点赞）

```java
String getUid()        // 用户ID
String getUsername()   // 用户名
int getLikeCount()     // 点赞数量
```

#### RoomStatsMessage（统计）

```java
String getLikedCount()      // 点赞总数
String getWatchingCount()   // 当前观看人数
```

## 🔧 完整集成指南

### 当前限制

这个简化版本暂时只提供了框架代码，**完整的消息解析需要集成Google Protobuf库和快手的proto定义文件**。

当前版本可以：
- ✅ 连接WebSocket
- ✅ 发送弹幕和点赞
- ✅ 接收原始消息

当前版本无法：
- ❌ 解析弹幕内容（需要protobuf）
- ❌ 解析礼物信息（需要protobuf）
- ❌ 解析点赞信息（需要protobuf）

### 完整集成步骤

要启用完整的消息解析功能：

#### 步骤1：复制Protobuf类

从原项目中复制以下protobuf类到 `com.kuaishou.live.protobuf` 包：

```
live-chat-client-codec-kuaishou/src/main/java/tech/ordinaryroad/live/chat/client/codec/kuaishou/protobuf/
├── SocketMessageOuterClass.java
├── PayloadTypeOuterClass.java
├── SCWebFeedPushOuterClass.java
├── WebCommentFeedOuterClass.java
├── WebGiftFeedOuterClass.java
├── WebLikeFeedOuterClass.java
├── UserInfoOuterClass.java
├── SimpleUserInfoOuterClass.java
├── LiveAudienceStateOuterClass.java
├── CSWebEnterRoomOuterClass.java
├── CSWebHeartbeatOuterClass.java
└── ... (其他相关类)
```

#### 步骤2：实现ProtobufParser

在 `ProtobufParser.java` 中取消注释示例代码，并使用实际的protobuf类：

```java
import com.kuaishou.live.protobuf.*;

public static void parseSocketMessage(byte[] data, KuaishouLiveListener listener) {
    try {
        SocketMessageOuterClass.SocketMessage socketMessage =
            SocketMessageOuterClass.SocketMessage.parseFrom(data);

        PayloadTypeOuterClass.PayloadType payloadType = socketMessage.getPayloadType();
        byte[] payload = socketMessage.getPayload().toByteArray();

        if (payloadType == PayloadTypeOuterClass.PayloadType.SC_FEED_PUSH) {
            SCWebFeedPushOuterClass.SCWebFeedPush feedPush =
                SCWebFeedPushOuterClass.SCWebFeedPush.parseFrom(payload);

            // 解析弹幕
            for (WebCommentFeedOuterClass.WebCommentFeed commentFeed :
                 feedPush.getCommentFeedsList()) {
                DanmuMessage danmu = new DanmuMessage();
                danmu.setUid(commentFeed.getUser().getPrincipalId());
                danmu.setUsername(commentFeed.getUser().getUserName());
                danmu.setUserAvatar(commentFeed.getUser().getHeadUrl());
                danmu.setContent(commentFeed.getContent());
                // ... 设置其他字段
                listener.onDanmuMessage(danmu);
            }
            // ... 解析礼物、点赞等
        }
    } catch (Exception e) {
        Log.e(TAG, "解析失败", e);
    }
}
```

#### 步骤3：完整的依赖

```gradle
dependencies {
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'org.json:json:20230227'
    implementation 'com.google.protobuf:protobuf-java:3.25.1'

    // 如果使用Kotlin
    implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.9.0'
}
```

## 🌟 使用示例

### 示例1：简单的弹幕监听

```java
KuaishouLiveClient client = new KuaishouLiveClient("3x4av52pnmw8mta",
    new SimpleKuaishouLiveListener() {
        @Override
        public void onDanmuMessage(DanmuMessage danmu) {
            // 显示弹幕到UI
            runOnUiThread(() -> {
                textView.append(danmu.getUsername() + ": " + danmu.getContent() + "\n");
            });
        }
    });
client.start();
```

### 示例2：关键词监听

```java
new SimpleKuaishouLiveListener() {
    @Override
    public void onDanmuMessage(DanmuMessage danmu) {
        String content = danmu.getContent();

        // 检测关键词
        if (content.contains("抽奖") || content.contains("送礼物")) {
            Log.d("Keyword", "检测到关键词: " + content);
            // 执行相应操作...
        }
    }
}
```

### 示例3：统计用户互动

```java
Map<String, Integer> userInteractionCount = new HashMap<>();

new SimpleKuaishouLiveListener() {
    @Override
    public void onDanmuMessage(DanmuMessage danmu) {
        String uid = danmu.getUid();
        userInteractionCount.put(uid,
            userInteractionCount.getOrDefault(uid, 0) + 1);
    }

    @Override
    public void onGiftMessage(GiftMessage gift) {
        String uid = gift.getUid();
        userInteractionCount.put(uid,
            userInteractionCount.getOrDefault(uid, 0) + 10);
    }
}
```

### 示例4：自动回复

```java
String cookie = "你的Cookie";
String kww = "你的Kww";
KuaishouLiveClient client = new KuaishouLiveClient("roomId", cookie, kww,
    new SimpleKuaishouLiveListener() {
        @Override
        public void onDanmuMessage(DanmuMessage danmu) {
            if (danmu.getContent().contains("@主播")) {
                // 延迟1秒后回复
                new Handler().postDelayed(() -> {
                    client.sendDanmu("@" + danmu.getUsername() + " 感谢关注！");
                }, 1000);
            }
        }
    });
client.start();
```

## 📁 项目结构

```
kuaishou-android-module/
├── src/main/java/com/kuaishou/live/
│   ├── api/
│   │   └── KuaishouApi.java          # API调用（房间初始化、发送弹幕等）
│   ├── client/
│   │   └── KuaishouLiveClient.java   # WebSocket客户端主类
│   ├── listener/
│   │   ├── KuaishouLiveListener.java # 消息监听接口
│   │   └── SimpleKuaishouLiveListener.java # 简化监听器
│   ├── model/
│   │   ├── DanmuMessage.java         # 弹幕消息模型
│   │   ├── GiftMessage.java          # 礼物消息模型
│   │   ├── LikeMessage.java          # 点赞消息模型
│   │   └── RoomStatsMessage.java     # 统计消息模型
│   ├── protobuf/                     # Protobuf类（需从原项目复制）
│   └── utils/
│       └── ProtobufParser.java       # Protobuf解析工具
├── build.gradle                       # Gradle配置
└── README.md                          # 本文档
```

## ❓ 常见问题

### 1. 如何获取Cookie和Kww？

1. 打开Chrome浏览器
2. 访问快手直播间：`https://live.kuaishou.com/u/你的房间ID`
3. 按F12打开开发者工具
4. 切换到Network标签
5. 刷新页面
6. 找到任意请求，查看Request Headers
7. 复制Cookie值
8. 复制Kww值（在请求头中）

### 2. 为什么收不到弹幕？

- 确保已完成protobuf集成（见"完整集成指南"）
- 检查网络连接
- 确认主播正在直播
- 查看Logcat是否有错误信息

### 3. 发送弹幕失败？

- 确保提供了有效的Cookie
- Cookie可能已过期，需要重新获取
- 发送频率过快会被限制
- 建议同时提供Kww参数

### 4. 如何实现断线重连？

客户端已内置自动重连机制。如果需要自定义：

```java
@Override
public void onDisconnected(String reason) {
    Log.d(TAG, "连接断开: " + reason);
    // 等待5秒后重连
    new Handler().postDelayed(() -> {
        if (!client.isConnected()) {
            client.start();
        }
    }, 5000);
}
```

## 📝 注意事项

1. **Cookie安全**：不要在代码中硬编码Cookie，建议使用加密存储
2. **请求频率**：发送弹幕不要过于频繁，建议间隔1-2秒
3. **内存管理**：在Activity/Fragment的onDestroy中调用`client.stop()`
4. **线程安全**：回调在主线程执行，可以直接更新UI
5. **Protobuf依赖**：完整功能需要集成protobuf库和proto定义文件

## 📄 开源协议

MIT License - 与原项目保持一致

## 🔗 相关链接

- 原项目：[ordinaryroad-live-chat-client](https://github.com/OrdinaryRoad-Project/ordinaryroad-live-chat-client)
- 快手开放平台：[https://open.kuaishou.com/](https://open.kuaishou.com/)

## 💬 支持

如有问题，请提交Issue或参考原项目文档。
