# 图书馆管理系统 v2.0+ 技术规格文档

## 版本信息

| 项目 | 说明 |
|------|------|
| 文档版本 | v2.0-draft |
| 当前系统版本 | v1.0 |
| 目标版本 | v2.0+ |
| 编写日期 | 2026-05-12 |
| 状态 | 规划中 |

---

## 一、微信小程序/移动端 App 开发计划

### 1.1 技术选型

| 方案 | 技术栈 | 优势 | 劣势 | 推荐度 |
|------|--------|------|------|--------|
| ✅ uni-app | Vue3 + TypeScript | 一套代码多端运行、与现有前端技术栈一致、社区活跃 | 性能略逊原生 | ⭐⭐⭐⭐⭐ |
| 微信原生 | WXML/WXSS/JS | 性能最优、API 支持最全 | 仅限微信生态、开发成本高 | ⭐⭐⭐ |
| Taro | React/Vue | 跨端支持好、京东团队维护 | 与现有 Vue3 技术栈有差异 | ⭐⭐⭐⭐ |
| Flutter | Dart | 高性能跨平台 | 学习成本高、与现有技术栈无关 | ⭐⭐ |

**最终选择：uni-app (Vue3 + TypeScript + Vite)**

### 1.2 架构设计

```
┌─────────────────────────────────────────────┐
│              uni-app 小程序端                │
├─────────────┬───────────────┬───────────────┤
│   pages/    │  components/  │    store/     │
│  (页面路由)  │  (公共组件)    │  (Pinia状态)  │
├─────────────┼───────────────┼───────────────┤
│   api/      │    utils/     │    hooks/     │
│  (接口封装)  │  (工具函数)    │  (组合式函数)  │
└─────────────┴───────────────┴───────────────┘
                        │
                  HTTPS / WSS
                        │
┌─────────────────────────────────────────────┐
│          Spring Boot API Gateway            │
│     (复用现有后端，增加小程序专用接口)         │
└─────────────────────────────────────────────┘
```

### 1.3 核心功能模块

| 模块 | 功能点 | 优先级 |
|------|--------|--------|
| 用户认证 | 微信授权登录、手机号绑定、JWT 令牌管理 | P0 |
| 图书检索 | 扫码查书、ISBN 搜索、分类浏览 | P0 |
| 借阅管理 | 在线借阅、续借、归还提醒 | P0 |
| 个人中心 | 借阅历史、逾期提醒、个人信息 | P0 |
| 消息推送 | 微信模板消息、订阅消息 | P1 |
| 图书推荐 | 个性化推荐列表 | P2 |
| 扫码借还 | 微信扫一扫集成 | P1 |

### 1.4 开发里程碑

| 阶段 | 内容 | 交付物 |
|------|------|--------|
| M1 (2周) | 项目搭建、微信登录、基础框架 | 可运行的空壳应用 |
| M2 (3周) | 图书检索、借阅管理核心功能 | 核心功能可用 |
| M3 (2周) | 个人中心、消息推送 | 完整用户体验闭环 |
| M4 (1周) | 测试、优化、提审 | 上线版本 |

### 1.5 微信登录对接方案

```
用户点击登录 → wx.login() 获取 code
     → 后端 /auth/wx-login 接口用 code 换取 openid/session_key
     → 查找绑定用户 → 生成 JWT 返回
     → 若未绑定 → 引导绑定手机号 → 创建/关联账号
```

**后端新增接口：**

| 接口 | 方法 | 说明 |
|------|------|------|
| `/auth/wx-login` | POST | 微信 code 登录 |
| `/auth/wx-bind-phone` | POST | 绑定手机号 |
| `/auth/wx-check-bind` | GET | 检查 openid 绑定状态 |

---

## 二、图书智能推荐算法（协同过滤）

### 2.1 算法选型

| 算法 | 类型 | 适用场景 | 推荐度 |
|------|------|----------|--------|
| ✅ 基于用户的协同过滤 (UserCF) | 内存型 | 用户量适中、推荐实时性高 | ⭐⭐⭐⭐ |
| 基于物品的协同过滤 (ItemCF) | 内存型 | 物品稳定、用户兴趣变化快 | ⭐⭐⭐⭐ |
| 矩阵分解 (ALS) | 模型型 | 大规模数据、隐式反馈 | ⭐⭐⭐ |
| 深度学习 (Wide&Deep) | 模型型 | 复杂特征、海量数据 | ⭐⭐ |

**推荐策略：UserCF + ItemCF 混合推荐**

### 2.2 数据模型设计

```sql
-- 用户-图书交互记录表（隐式反馈）
CREATE TABLE user_book_interaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    action_type ENUM('BORROW', 'FAVORITE', 'RATE', 'BROWSE') NOT NULL,
    weight DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_book_action (user_id, book_id, action_type),
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id)
);

-- 推荐结果缓存表
CREATE TABLE recommendation_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    score DECIMAL(5,4) NOT NULL,
    reason VARCHAR(200),
    algorithm VARCHAR(50),
    expire_time TIMESTAMP NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_expire (expire_time)
);
```

**行为权重配置：**

| 行为 | 权重 | 说明 |
|------|------|------|
| BORROW | 1.0 | 借阅是最强信号 |
| FAVORITE | 0.8 | 收藏表示兴趣 |
| RATE | 0.6 | 评分（需 ≥ 4 星） |
| BROWSE | 0.3 | 浏览详情页 |

### 2.3 算法实现架构

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  数据采集层   │────▶│  算法计算层   │────▶│  结果服务层   │
│              │     │              │     │              │
│ 借阅记录     │     │ UserCF 计算   │     │ 推荐接口     │
│ 浏览记录     │     │ ItemCF 计算   │     │ 缓存管理     │
│ 收藏/评分    │     │ 混合排序      │     │ A/B 测试     │
│ 搜索记录     │     │ 冷启动处理    │     │ 效果追踪     │
└──────────────┘     └──────────────┘     └──────────────┘
```

### 2.4 核心算法伪代码

```java
// UserCF: 找到与目标用户最相似的 K 个用户，推荐他们借阅但目标用户未借阅的书
public List<BookRecommendation> userCFRecommend(Long userId, int topK, int topN) {
    // 1. 获取目标用户的借阅向量
    Map<Long, Double> targetVector = getUserVector(userId);

    // 2. 计算与其他用户的余弦相似度
    List<SimilarUser> similarUsers = findTopKSimilarUsers(userId, targetVector, topK);

    // 3. 从相似用户借阅的书中，筛选目标用户未借阅的
    // 4. 按相似度加权评分排序，取 TopN
    return aggregateAndRank(similarUsers, targetVector, topN);
}

// ItemCF: 基于用户已借阅的书，推荐与之相似的书
public List<BookRecommendation> itemCFRecommend(Long userId, int topN) {
    // 1. 获取用户已借阅的图书列表
    List<Long> borrowedBooks = getUserBorrowedBooks(userId);

    // 2. 计算图书间相似度（基于共同借阅用户）
    // 3. 对每本已借阅的书，找到最相似的 M 本书
    // 4. 去重、排序、取 TopN
    return recommendSimilarItems(borrowedBooks, topN);
}

// 混合推荐
public List<BookRecommendation> hybridRecommend(Long userId, int topN) {
    List<BookRecommendation> userCFResults = userCFRecommend(userId, 50, topN * 2);
    List<BookRecommendation> itemCFResults = itemCFRecommend(userId, topN * 2);

    // 加权融合: UserCF 0.4 + ItemCF 0.6
    return mergeAndRerank(userCFResults, 0.4, itemCFResults, 0.6, topN);
}
```

### 2.5 冷启动策略

| 场景 | 策略 |
|------|------|
| 新用户 | 基于注册时选择的兴趣分类推荐热门图书 |
| 新图书 | 基于分类和标签推荐给有相关兴趣的用户 |
| 无行为数据 | 全局热门排行 + 最新入库 |

### 2.6 离线计算调度

```yaml
# 定时任务配置
recommendation:
  schedule:
    user-similarity: "0 0 3 * * ?"    # 每天凌晨3点计算用户相似度
    item-similarity: "0 0 4 * * ?"    # 每天凌晨4点计算物品相似度
    generate-recommend: "0 0 5 * * ?" # 每天凌晨5点生成推荐结果
    cache-expire-hours: 24             # 推荐缓存24小时
```

---

## 三、多语言国际化 (i18n) 框架

### 3.1 技术选型

| 层级 | 方案 | 说明 |
|------|------|------|
| 前端 | ✅ vue-i18n@9 | Vue3 官方 i18n 方案，与 Vue3 Composition API 深度集成 |
| 后端 | ✅ Spring MessageSource | Spring 原生国际化支持 |
| 语言包管理 | JSON 文件 | 前后端共享翻译键名规范 |

### 3.2 前端实现方案

```
src/
├── locales/
│   ├── index.ts          # i18n 实例配置
│   ├── zh-CN.ts          # 中文简体
│   ├── en-US.ts          # 英文
│   └── modules/          # 按模块拆分
│       ├── common.ts     # 公共翻译
│       ├── book.ts       # 图书模块
│       ├── borrow.ts     # 借阅模块
│       └── user.ts       # 用户模块
```

**核心配置：**

```typescript
// locales/index.ts
import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages: { 'zh-CN': zhCN, 'en-US': enUS }
})

export default i18n
```

**组件中使用：**

```vue
<template>
  <el-button>{{ t('common.search') }}</el-button>
  <span>{{ t('book.total', { count: 100 }) }}</span>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
</script>
```

### 3.3 后端实现方案

```yaml
# application.yml
spring:
  messages:
    basename: i18n/messages
    encoding: UTF-8
    cache-duration: 3600s
```

```
resources/
├── i18n/
│   ├── messages_zh_CN.properties
│   └── messages_en_US.properties
```

**API 响应国际化：**

```java
@RestController
@RequestMapping("/api")
public class I18nController {

    @Autowired
    private MessageSource messageSource;

    private String getMessage(String code, Object[] args, Locale locale) {
        return messageSource.getMessage(code, args, locale);
    }

    @GetMapping("/books/{id}")
    public Result<Book> getBook(@PathVariable Long id,
                                 @RequestHeader(value = "Accept-Language", defaultValue = "zh-CN") String lang) {
        Locale locale = Locale.forLanguageTag(lang.replace("_", "-"));
        // 业务逻辑中使用 getMessage() 返回国际化消息
    }
}
```

### 3.4 语言切换流程

```
用户切换语言 → 前端更新 vue-i18n locale
             → localStorage 持久化
             → 请求头 Accept-Language 自动携带
             → 后端 MessageSource 解析对应语言
```

### 3.5 支持语言优先级

| 语言 | 代码 | 优先级 | 说明 |
|------|------|--------|------|
| 简体中文 | zh-CN | P0 | 默认语言 |
| English | en-US | P1 | 国际化首选 |
| 繁体中文 | zh-TW | P2 | 港澳台地区 |

---

## 四、消息通知系统架构设计

### 4.1 系统架构

```
┌──────────────────────────────────────────────────────┐
│                    消息通知中心                        │
├──────────┬──────────┬──────────┬─────────────────────┤
│  邮件通道  │  短信通道  │ 站内消息  │   微信模板消息      │
│ (SMTP)   │ (SMS API) │ (WebSocket)│ (订阅消息)         │
├──────────┴──────────┴──────────┴─────────────────────┤
│                  消息队列 (RabbitMQ)                    │
├──────────────────────────────────────────────────────┤
│                  通知模板引擎                           │
├──────────────────────────────────────────────────────┤
│                  用户通知偏好                           │
└──────────────────────────────────────────────────────┘
```

### 4.2 数据库设计

```sql
-- 通知模板表
CREATE TABLE notification_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    channel ENUM('EMAIL', 'SMS', 'IN_APP', 'WECHAT') NOT NULL,
    subject VARCHAR(200),
    content_template TEXT NOT NULL,
    enabled TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 通知记录表
CREATE TABLE notification_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    template_code VARCHAR(50),
    channel VARCHAR(20) NOT NULL,
    title VARCHAR(200),
    content TEXT,
    status ENUM('PENDING', 'SENT', 'FAILED', 'READ') DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    error_msg VARCHAR(500),
    send_time TIMESTAMP NULL,
    read_time TIMESTAMP NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
);

-- 用户通知偏好表
CREATE TABLE notification_preference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    email_enabled TINYINT DEFAULT 1,
    sms_enabled TINYINT DEFAULT 0,
    in_app_enabled TINYINT DEFAULT 1,
    wechat_enabled TINYINT DEFAULT 0,
    borrow_reminder TINYINT DEFAULT 1,
    overdue_notice TINYINT DEFAULT 1,
    system_notice TINYINT DEFAULT 1,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 4.3 通知场景配置

| 场景 | 模板代码 | 通道 | 触发时机 |
|------|----------|------|----------|
| 借阅成功 | BORROW_SUCCESS | 站内+微信 | 借阅操作完成 |
| 还书提醒 | RETURN_REMINDER | 站内+微信 | 到期前3天 |
| 逾期通知 | OVERDUE_NOTICE | 站内+短信+微信 | 逾期当天 |
| 预约到书 | RESERVATION_READY | 站内+微信 | 图书归还入库 |
| 系统公告 | SYSTEM_NOTICE | 站内+邮件 | 管理员发布 |
| 密码修改 | PASSWORD_CHANGED | 邮件+短信 | 密码变更成功 |

### 4.4 技术实现要点

**消息队列异步发送：**

```java
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final NotificationTemplateMapper templateMapper;
    private final NotificationRecordMapper recordMapper;

    @Override
    public void send(String templateCode, Long userId, Map<String, String> params) {
        NotificationTemplate template = templateMapper.selectByCode(templateCode);
        if (template == null || !template.getEnabled()) return;

        String content = renderTemplate(template.getContentTemplate(), params);

        NotificationRecord record = new NotificationRecord();
        record.setUserId(userId);
        record.setTemplateCode(templateCode);
        record.setChannel(template.getChannel().name());
        record.setTitle(template.getSubject());
        record.setContent(content);
        record.setStatus("PENDING");
        recordMapper.insert(record);

        // 异步投递到消息队列
        rabbitTemplate.convertAndSend("notification.exchange",
                "notification." + template.getChannel().name().toLowerCase(),
                record.getId());
    }
}
```

**WebSocket 站内消息推送：**

```java
@Component
@RequiredArgsConstructor
public class InAppNotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRecordMapper recordMapper;

    @RabbitListener(queues = "notification.in_app")
    public void handleInAppNotification(Long recordId) {
        NotificationRecord record = recordMapper.selectById(recordId);
        if (record != null) {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(record.getUserId()),
                    "/queue/notifications",
                    Map.of("id", record.getId(), "title", record.getTitle(),
                           "content", record.getContent(), "time", record.getCreateTime())
            );
            record.setStatus("SENT");
            record.setSendTime(LocalDateTime.now());
            recordMapper.updateById(record);
        }
    }
}
```

---

## 五、RFID/条形码硬件集成规范

### 5.1 硬件选型建议

| 设备类型 | 推荐型号 | 通信方式 | 适用场景 |
|----------|----------|----------|----------|
| RFID 读写器 | Impinj R420 | TCP/IP (LLRP) | 图书入库、盘点 |
| RFID 标签 | Alien H3 超高频标签 | 无源 | 图书贴标 |
| 手持 RFID | Zebra MC3390xR | Wi-Fi/蓝牙 | 移动盘点 |
| 条码扫描枪 | Honeywell 1900 | USB HID | 借还台扫码 |
| 二维码扫描 | 内置摄像头 | SDK | 自助借还 |

### 5.2 集成架构

```
┌─────────────┐    LLRP/TCP    ┌──────────────┐
│  RFID 读写器  │──────────────▶│  RFID 网关服务  │
└─────────────┘                │  (Spring Boot) │
┌─────────────┐    USB HID     │                │
│  条码扫描枪   │──────────────▶│  扫码监听服务   │
└─────────────┘                └───────┬────────┘
                                       │ REST API
                                       ▼
                              ┌──────────────────┐
                              │  图书馆管理后端    │
                              │  (现有系统)        │
                              └──────────────────┘
```

### 5.3 RFID 网关服务 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/rfid/read` | GET | 读取当前天线范围内的标签 |
| `/rfid/write` | POST | 向标签写入数据（图书ID等） |
| `/rfid/inventory/start` | POST | 启动盘点任务 |
| `/rfid/inventory/stop` | POST | 停止盘点任务 |
| `/rfid/inventory/status` | GET | 获取盘点进度 |
| `/barcode/parse` | POST | 解析条码/二维码内容 |

### 5.4 数据格式规范

**RFID 标签数据格式（EPC 编码）：**

```
|  版本(2bit)  |  机构代码(8bit)  |  图书ID(22bit)  |  校验(4bit)  |
|     01       |   00000001       |  xxxxxxxxxxxxxx  |    xxxx      |
```

**条码格式：**

| 类型 | 格式 | 示例 |
|------|------|------|
| ISBN 条码 | EAN-13 | 9787111999999 |
| 馆内条码 | CODE-128 | LIB-2024-000001 |
| 借阅码二维码 | QR-JSON | {"type":"borrow","code":"BR20240001"} |

### 5.5 盘点流程

```
管理员启动盘点 → 选择书架/区域
     → RFID 网关启动扫描
     → 实时上报扫描到的标签
     → 后端比对馆藏数据
     → 生成盘点报告（在架/错架/遗失）
     → 管理员确认处理
```

---

## 六、大屏数据可视化看板需求

### 6.1 技术选型

| 方案 | 技术 | 优势 | 推荐度 |
|------|------|------|--------|
| ✅ DataV + ECharts | Vue3 + ECharts | 阿里开源、大屏专用组件、视觉效果好 | ⭐⭐⭐⭐⭐ |
| 积木报表 | JimuReport | 低代码、快速搭建 | ⭐⭐⭐⭐ |
| Grafana | Grafana | 运维监控强、数据源丰富 | ⭐⭐⭐ |

### 6.2 看板布局设计

```
┌──────────────────────────────────────────────────────────────┐
│                    图书馆数据可视化大屏                         │
├──────────┬──────────────────────────────┬────────────────────┤
│          │                              │                    │
│  馆藏总量  │      实时借还动态地图          │   今日借还统计      │
│  数字翻牌  │      (中国地图热力图)          │   (环形图)         │
│          │                              │                    │
├──────────┼──────────────────────────────┼────────────────────┤
│          │                              │                    │
│ 分类占比   │     月度借阅趋势              │   热门图书排行      │
│ (玫瑰图)  │     (折线图)                  │   (横向柱状图)      │
│          │                              │                    │
├──────────┼──────────────────────────────┼────────────────────┤
│          │                              │                    │
│ 读者画像   │     各书架利用率              │   实时滚动消息      │
│ (雷达图)  │     (3D柱状图)               │   (跑马灯)         │
│          │                              │                    │
└──────────┴──────────────────────────────┴────────────────────┘
```

### 6.3 数据指标

| 指标 | 数据源 | 刷新频率 | 可视化类型 |
|------|--------|----------|-----------|
| 馆藏总量 | book 表 COUNT | 5分钟 | 数字翻牌器 |
| 在借数量 | borrow_record WHERE status='BORROWING' | 实时 | 数字翻牌器 |
| 今日借出 | borrow_record WHERE DATE(borrow_date)=TODAY | 实时 | 数字翻牌器 |
| 今日归还 | borrow_record WHERE DATE(return_date)=TODAY | 实时 | 数字翻牌器 |
| 逾期数量 | borrow_record WHERE status='OVERDUE' | 5分钟 | 数字翻牌器 |
| 分类占比 | book GROUP BY category_id | 1小时 | 玫瑰图 |
| 月度趋势 | borrow_record GROUP BY MONTH | 1天 | 折线图 |
| 热门图书 | borrow_record GROUP BY book_id ORDER BY COUNT DESC LIMIT 10 | 1小时 | 横向柱状图 |
| 书架利用率 | book_location GROUP BY shelf_id | 5分钟 | 3D柱状图 |
| 读者画像 | user GROUP BY role, borrow_count | 1天 | 雷达图 |

### 6.4 技术实现

```typescript
// 大屏独立项目结构
datav-screen/
├── src/
│   ├── views/
│   │   └── Dashboard.vue       # 主看板页面
│   ├── components/
│   │   ├── DigitalFlop.vue     # 数字翻牌器
│   │   ├── ChinaMap.vue        # 地图组件
│   │   ├── RingChart.vue       # 环形图
│   │   └── ScrollTable.vue     # 滚动表格
│   ├── api/
│   │   └── dashboard.ts        # 大屏专用API
│   └── hooks/
│       └── useWebSocket.ts     # 实时数据推送
```

**WebSocket 实时数据推送：**

```java
@ServerEndpoint("/ws/dashboard")
@Component
public class DashboardWebSocket {

    @OnOpen
    public void onOpen(Session session) {
        DashboardSessionManager.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        DashboardSessionManager.remove(session);
    }

    // 定时推送实时数据
    @Scheduled(fixedRate = 5000)
    public void pushRealtimeData() {
        DashboardData data = dashboardService.getRealtimeData();
        DashboardSessionManager.broadcast(JSON.toJSONString(data));
    }
}
```

---

## 七、微服务架构改造路线图

### 7.1 当前架构 vs 目标架构

**当前（单体架构）：**
```
┌─────────────────────────────────┐
│       Spring Boot 单体应用       │
│  ┌─────┬─────┬─────┬─────────┐ │
│  │用户  │图书  │借阅  │统计报表  │ │
│  │模块  │模块  │模块  │模块      │ │
│  └─────┴─────┴─────┴─────────┘ │
│         共享 MySQL 数据库         │
└─────────────────────────────────┘
```

**目标（微服务架构）：**
```
┌──────────────────────────────────────────────────────┐
│                   API Gateway (Spring Cloud Gateway)  │
│                   认证/限流/路由/日志                    │
├──────────┬──────────┬──────────┬──────────┬──────────┤
│ 用户服务  │ 图书服务  │ 借阅服务  │ 通知服务  │ 推荐服务  │
│ user-svc │ book-svc │borrow-svc│notify-svc│rec-svc   │
│  8001    │  8002    │  8003    │  8004    │  8005    │
├──────────┴──────────┴──────────┴──────────┴──────────┤
│              基础设施层                                │
│  Nacos(注册配置) | Sentinel(限流) | Seata(分布式事务)   │
│  RabbitMQ(消息)  | Redis(缓存)    | MinIO(文件存储)    │
├──────────────────────────────────────────────────────┤
│  MySQL(分库)  |  Elasticsearch(搜索)  |  Prometheus   │
└──────────────────────────────────────────────────────┘
```

### 7.2 服务拆分策略

| 服务 | 职责 | 数据库 | 技术栈 |
|------|------|--------|--------|
| gateway-svc | API 网关、认证、限流 | - | Spring Cloud Gateway |
| user-svc | 用户管理、认证授权 | user_db | Spring Boot + MyBatis-Plus |
| book-svc | 图书管理、分类、书架 | book_db | Spring Boot + MyBatis-Plus |
| borrow-svc | 借阅、归还、续借、罚款 | borrow_db | Spring Boot + MyBatis-Plus |
| notify-svc | 消息通知 | notify_db | Spring Boot + RabbitMQ |
| recommend-svc | 智能推荐 | recommend_db | Spring Boot + Python(算法) |
| report-svc | 统计报表、数据导出 | 只读副本 | Spring Boot + MyBatis-Plus |
| file-svc | 文件上传下载 | MinIO | Spring Boot + MinIO SDK |

### 7.3 改造阶段

| 阶段 | 内容 | 风险 | 周期 |
|------|------|------|------|
| **Phase 1: 基础设施** | 搭建 Nacos、Gateway、统一认证 | 低 | 2周 |
| **Phase 2: 服务拆分** | 按模块拆分微服务，数据库分离 | 中 | 4周 |
| **Phase 3: 消息总线** | 引入 RabbitMQ，服务间异步通信 | 中 | 2周 |
| **Phase 4: 分布式事务** | Seata 集成，保证数据一致性 | 高 | 2周 |
| **Phase 5: 可观测性** | 链路追踪、日志聚合、监控告警 | 低 | 1周 |
| **Phase 6: 灰度发布** | 金丝雀发布、流量切换 | 中 | 1周 |

### 7.4 关键技术决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 注册中心 | ✅ Nacos | 同时支持注册发现和配置中心，国内生态好 |
| 配置中心 | ✅ Nacos Config | 与注册中心统一，减少运维复杂度 |
| 网关 | ✅ Spring Cloud Gateway | 响应式、性能好、Spring 生态 |
| 限流熔断 | ✅ Sentinel | 阿里开源、控制台可视化、规则动态推送 |
| 分布式事务 | ✅ Seata AT 模式 | 对业务侵入小、性能可接受 |
| 消息队列 | ✅ RabbitMQ | 可靠消息、延迟队列支持好 |
| 链路追踪 | ✅ SkyWalking | 国产、无侵入、UI 友好 |

---

## 八、Kubernetes 集群部署方案

### 8.1 集群架构

```
┌─────────────────────────────────────────────────────┐
│                   Kubernetes Cluster                  │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │  Ingress     │  │  Ingress     │  │  Ingress     │ │
│  │  Controller  │  │  Controller  │  │  Controller  │ │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘ │
│         │                │                │         │
│  ┌──────▼──────────────▼────────────────▼──────┐   │
│  │              Ingress (Nginx)                 │   │
│  │         SSL 终止 / 域名路由 / 限流            │   │
│  └──────────────────┬──────────────────────────┘   │
│                     │                               │
│  ┌──────────────────▼──────────────────────────┐   │
│  │           API Gateway Pod (x2)               │   │
│  └──────────────────┬──────────────────────────┘   │
│                     │                               │
│  ┌──────┬──────┬───┴──┬──────┬──────┬──────────┐  │
│  │user  │book  │borrow│notify│rec   │report    │  │
│  │Pod(x2)│Pod(x2)│Pod(x2)│Pod(x2)│Pod(x1)│Pod(x1) │  │
│  └──┬───┴──┬──┴──┬───┴──┬───┴──┬───┴────┬─────┘  │
│     │      │     │      │      │        │         │
│  ┌──▼──────▼─────▼──────▼──────▼────────▼──────┐  │
│  │              Service Mesh (可选 Istio)        │  │
│  └──────────────────┬──────────────────────────┘   │
│                     │                               │
│  ┌──────────────────▼──────────────────────────┐   │
│  │              中间件层                         │   │
│  │  MySQL主从 | Redis集群 | RabbitMQ | MinIO    │   │
│  │  Nacos集群 | Elasticsearch                  │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

### 8.2 资源需求

**开发/测试环境：**

| 资源 | 规格 | 数量 | 用途 |
|------|------|------|------|
| Master 节点 | 4C8G | 1 | 集群管理 |
| Worker 节点 | 8C16G | 2 | 工作负载 |
| 存储 | 100GB SSD | - | 持久化存储 |

**生产环境：**

| 资源 | 规格 | 数量 | 用途 |
|------|------|------|------|
| Master 节点 | 4C8G | 3 | 高可用集群管理 |
| Worker 节点 | 8C32G | 4+ | 工作负载 |
| 存储节点 | 8C16G + 500GB SSD | 3 | 分布式存储 (Ceph/Longhorn) |
| 负载均衡 | 4C8G | 2 | HAProxy + Keepalived |

### 8.3 各服务资源配额

| 服务 | CPU Request | CPU Limit | Memory Request | Memory Limit | 副本数 |
|------|-------------|-----------|----------------|--------------|--------|
| gateway | 500m | 1000m | 512Mi | 1Gi | 2 |
| user-svc | 250m | 500m | 512Mi | 1Gi | 2 |
| book-svc | 250m | 500m | 512Mi | 1Gi | 2 |
| borrow-svc | 250m | 500m | 512Mi | 1Gi | 2 |
| notify-svc | 250m | 500m | 256Mi | 512Mi | 2 |
| recommend-svc | 500m | 1000m | 1Gi | 2Gi | 1 |
| report-svc | 250m | 1000m | 512Mi | 2Gi | 1 |
| frontend | 100m | 200m | 128Mi | 256Mi | 2 |
| MySQL | 500m | 2000m | 1Gi | 4Gi | 1主1从 |
| Redis | 250m | 500m | 512Mi | 2Gi | 3哨兵 |
| RabbitMQ | 250m | 500m | 512Mi | 1Gi | 1 |
| Nacos | 250m | 500m | 512Mi | 1Gi | 3 |

### 8.4 部署清单

```yaml
# k8s/deployment/gateway-deployment.yaml 示例
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway-svc
  namespace: library
  labels:
    app: gateway-svc
spec:
  replicas: 2
  selector:
    matchLabels:
      app: gateway-svc
  template:
    metadata:
      labels:
        app: gateway-svc
    spec:
      containers:
        - name: gateway-svc
          image: registry.cn-hangzhou.aliyuncs.com/library/gateway-svc:latest
          ports:
            - containerPort: 8000
          resources:
            requests:
              cpu: 500m
              memory: 512Mi
            limits:
              cpu: 1000m
              memory: 1Gi
          env:
            - name: NACOS_ADDR
              valueFrom:
                configMapKeyRef:
                  name: library-config
                  key: nacos-addr
            - name: JWT_SECRET
              valueFrom:
                secretKeyRef:
                  name: library-secrets
                  key: jwt-secret
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8000
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8000
            initialDelaySeconds: 20
            periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: gateway-svc
  namespace: library
spec:
  selector:
    app: gateway-svc
  ports:
    - port: 8000
      targetPort: 8000
  type: ClusterIP
```

### 8.5 CI/CD 流水线

```
代码提交 → GitLab/GitHub
    → 触发 CI Pipeline
    → 代码检查 (SonarQube)
    → 单元测试 + 集成测试
    → Docker 镜像构建
    → 推送到镜像仓库 (Harbor/ACR)
    → 触发 CD Pipeline
    → 更新 K8s Deployment
    → 健康检查
    → 灰度发布 (可选)
    → 全量发布
```

**GitLab CI 配置示例：**

```yaml
# .gitlab-ci.yml
stages:
  - test
  - build
  - deploy

test:
  stage: test
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn test -B
  only:
    - merge_requests
    - main

build:
  stage: build
  image: docker:24
  services:
    - docker:24-dind
  script:
    - docker build -t $REGISTRY/$IMAGE:$CI_COMMIT_SHORT_SHA .
    - docker push $REGISTRY/$IMAGE:$CI_COMMIT_SHORT_SHA
  only:
    - main

deploy-dev:
  stage: deploy
  image: bitnami/kubectl
  script:
    - kubectl set image deployment/$SERVICE $SERVICE=$REGISTRY/$IMAGE:$CI_COMMIT_SHORT_SHA -n library-dev
    - kubectl rollout status deployment/$SERVICE -n library-dev
  environment:
    name: dev
  only:
    - main

deploy-prod:
  stage: deploy
  image: bitnami/kubectl
  script:
    - kubectl set image deployment/$SERVICE $SERVICE=$REGISTRY/$IMAGE:$CI_COMMIT_SHORT_SHA -n library-prod
    - kubectl rollout status deployment/$SERVICE -n library-prod
  environment:
    name: prod
  when: manual
  only:
    - tags
```

### 8.6 监控告警体系

| 组件 | 工具 | 监控内容 |
|------|------|----------|
| 指标监控 | Prometheus + Grafana | CPU/内存/磁盘/网络、JVM、业务指标 |
| 日志聚合 | EFK (Elasticsearch + Fluentd + Kibana) | 应用日志、访问日志、错误日志 |
| 链路追踪 | SkyWalking | 服务调用链、性能瓶颈 |
| 告警通知 | AlertManager → 钉钉/邮件 | 服务宕机、资源超限、业务异常 |

---

## 附录：技术栈版本汇总

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 后端运行时 |
| Spring Boot | 3.2.x | 后端框架 |
| Spring Cloud | 2023.0.x | 微服务框架 |
| Vue3 | 3.4.x | 前端框架 |
| TypeScript | 5.x | 前端类型系统 |
| MySQL | 8.0 | 关系数据库 |
| Redis | 7.x | 缓存/会话 |
| RabbitMQ | 3.12+ | 消息队列 |
| Nacos | 2.3.x | 注册/配置中心 |
| Kubernetes | 1.28+ | 容器编排 |
| Docker | 24+ | 容器化 |
| MinIO | latest | 对象存储 |
| Elasticsearch | 8.x | 搜索引擎 |
| Prometheus | 2.x | 指标监控 |
| Grafana | 10.x | 可视化面板 |
