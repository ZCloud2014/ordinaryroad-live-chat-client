# 快手直播抓取Android模块 - 快速开始

## ✨ 已完成的工作

我已经将快手的抓取逻辑提取为一个**独立的Android模块**，可以直接集成到你的Android项目中。

### 📁 模块结构

```
kuaishou-android-module/
├── src/main/java/com/kuaishou/live/
│   ├── api/
│   │   └── KuaishouApi.java              # HTTP API调用（房间初始化、发弹幕等）
│   ├── client/
│   │   └── KuaishouLiveClient.java       # WebSocket客户端主类
│   ├── listener/
│   │   ├── KuaishouLiveListener.java     # 完整的消息监听接口
│   │   └── SimpleKuaishouLiveListener.java # 简化的监听器（推荐使用）
│   ├── model/
│   │   ├── DanmuMessage.java             # 弹幕消息模型
│   │   ├── GiftMessage.java              # 礼物消息模型
│   │   ├── LikeMessage.java              # 点赞消息模型
│   │   └── RoomStatsMessage.java         # 统计消息模型
│   └── utils/
│       └── ProtobufParser.java           # Protobuf解析工具
├── README.md                              # 完整文档
├── build.gradle                           # Gradle配置
├── ExampleActivity.java                   # 示例Activity
└── layout_example.xml                     # 示例布局文件
```

## 🚀 30秒快速集成

### 1. 将模块复制到你的项目

```bash
cp -r kuaishou-android-module your-android-project/
```

### 2. 在项目的 settings.gradle 中添加

```gradle
include ':kuaishou-android-module'
```

### 3. 在app的 build.gradle 中添加依赖

```gradle
dependencies {
    implementation project(':kuaishou-android-module')
}
```

### 4. 开始使用

```java
// 创建监听器
SimpleKuaishouLiveListener listener = new SimpleKuaishouLiveListener() {
    @Override
    public void onDanmuMessage(DanmuMessage danmu) {
        Log.d("Kuaishou", danmu.getUsername() + ": " + danmu.getContent());
    }

    @Override
    public void onGiftMessage(GiftMessage gift) {
        Log.d("Kuaishou", gift.getUsername() + " 送出 " + gift.getGiftName());
    }
};

// 创建客户端并连接
KuaishouLiveClient client = new KuaishouLiveClient("房间ID", listener);
client.start();
```

## 📝 核心功能

### ✅ 已实现（可直接使用）

1. **连接直播间** - 自动获取WebSocket地址并连接
2. **接收原始消息** - 可以接收到所有类型的消息
3. **发送弹幕** - 需要提供Cookie
4. **为主播点赞** - 需要提供Cookie
5. **自动重连** - 连接断开后自动尝试重连
6. **心跳保持** - 自动发送心跳保持连接

### ⚠️ 需要完善（见README）

消息解析部分使用了**简化的框架代码**，完整功能需要：

1. 从原项目复制Protobuf类到 `protobuf/` 目录
2. 实现 `ProtobufParser` 中的解析逻辑
3. 详细步骤见 `README.md` 的"完整集成指南"部分

## 💡 使用场景示例

### 场景1：弹幕监控

```java
new SimpleKuaishouLiveListener() {
    @Override
    public void onDanmuMessage(DanmuMessage danmu) {
        // 检测关键词
        if (danmu.getContent().contains("抽奖")) {
            // 执行抽奖逻辑
        }
    }
}
```

### 场景2：进入直播间提醒

```java
new SimpleKuaishouLiveListener() {
    @Override
    public void onConnected() {
        // 连接成功后自动发送弹幕
        client.sendDanmu("大家好！");
    }
}
```

### 场景3：统计互动数据

```java
private int danmuCount = 0;
private int giftCount = 0;

new SimpleKuaishouLiveListener() {
    @Override
    public void onDanmuMessage(DanmuMessage danmu) {
        danmuCount++;
    }

    @Override
    public void onGiftMessage(GiftMessage gift) {
        giftCount++;
    }

    @Override
    public void onRoomStatsMessage(RoomStatsMessage stats) {
        Log.d("Stats", "弹幕数: " + danmuCount + ", 礼物数: " + giftCount);
    }
}
```

## 🔧 技术特点

1. **轻量级** - 只依赖OkHttp和Protobuf，无需Netty
2. **Android优化** - 回调自动切换到主线程，可直接更新UI
3. **易于集成** - 独立模块，不会与项目冲突
4. **完整文档** - 提供详细的API文档和示例代码

## 📚 详细文档

请查看 `README.md` 获取：
- 完整的API文档
- 更多使用示例
- 完整集成Protobuf的步骤
- 常见问题解答
- 最佳实践建议

## 🔗 原理说明

### 抓取流程

1. **初始化房间**
   - 调用快手API获取直播流ID
   - 获取WebSocket服务器地址和Token

2. **建立WebSocket连接**
   - 发送进入房间消息
   - 开始接收服务器推送的消息

3. **消息解析**
   - 解析Protobuf格式的二进制消息
   - 提取弹幕、礼物、点赞等信息
   - 通过回调通知上层应用

4. **心跳保持**
   - 每20秒发送一次心跳
   - 保持WebSocket连接不断开

### 关键API

| 功能 | API地址 | 说明 |
|------|---------|------|
| 房间初始化 | `/live_api/liveroom/livedetail` | 获取直播信息 |
| WebSocket信息 | `/live_api/liveroom/websocketinfo` | 获取WS地址和Token |
| 发送弹幕 | `/live_api/liveroom/sendComment` | 需要Cookie |
| 点赞 | `/live_api/liveroom/like` | 需要Cookie |

## ⚡ 性能优化建议

1. **内存管理** - 在Activity/Fragment的onDestroy中调用`client.stop()`
2. **线程安全** - 所有回调已在主线程执行，无需手动切换
3. **消息过滤** - 只处理需要的消息类型，其他消息忽略
4. **连接池** - 如果需要监听多个直播间，建议使用单个OkHttpClient实例

## 📞 支持

如有问题，请查阅：
1. `README.md` - 完整文档
2. `ExampleActivity.java` - 完整的示例代码
3. 原项目Issue - [ordinaryroad-live-chat-client](https://github.com/OrdinaryRoad-Project/ordinaryroad-live-chat-client/issues)

---

**注意**: 此模块是从原项目提取并为Android平台优化的版本，保持MIT开源协议。
