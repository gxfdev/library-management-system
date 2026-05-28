package com.library.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class AlertService {

    @Value("${alert.enabled:true}")
    private boolean alertEnabled;

    @Value("${alert.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${alert.webhook.url:}")
    private String webhookUrl;

    @Value("${alert.error-threshold:10}")
    private int errorThreshold;

    private final ConcurrentHashMap<String, AtomicInteger> errorCounters = new ConcurrentHashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void recordError(String type, String message, Exception e) {
        if (!alertEnabled) return;

        AtomicInteger counter = errorCounters.computeIfAbsent(type, k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();

        log.warn("[ALERT] 错误记录 - 类型: {}, 次数: {}, 消息: {}", type, count, message);

        if (count >= errorThreshold) {
            sendAlert(type, message, e, count);
            counter.set(0);
        }
    }

    @Async
    protected void sendAlert(String type, String message, Exception e, int count) {
        try {
            String alertMessage = buildAlertMessage(type, message, e, count);
            
            log.error("🚨 生产环境报警触发:\n{}", alertMessage);

            if (webhookUrl != null && !webhookUrl.isEmpty()) {
                sendWebhookAlert(alertMessage);
            }

            if (emailEnabled) {
                sendEmailAlert(alertMessage);
            }

        } catch (Exception ex) {
            log.error("发送报警失败", ex);
        }
    }

    private String buildAlertMessage(String type, String message, Exception e, int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚨 **图书馆管理系统 - 生产环境报警**\n\n");
        sb.append("- **时间**: ").append(LocalDateTime.now().format(formatter)).append("\n");
        sb.append("- **类型**: ").append(type).append("\n");
        sb.append("- **错误次数**: ").append(count).append(" (阈值: ").append(errorThreshold).append(")\n");
        sb.append("- **消息**: ").append(message).append("\n");
        
        if (e != null) {
            sb.append("- **异常类**: ").append(e.getClass().getSimpleName()).append("\n");
            sb.append("- **堆栈信息**:\n");
            for (StackTraceElement element : e.getStackTrace()) {
                sb.append("  at ").append(element.toString()).append("\n");
                if (sb.length() > 2000) {
                    sb.append("  ... (堆栈信息过长，已截断)");
                    break;
                }
            }
        }
        
        return sb.toString();
    }

    private void sendWebhookAlert(String message) {
        log.info("发送Webhook报警到: {}", webhookUrl);
    }

    private void sendEmailAlert(String message) {
        log.info("发送邮件报警: {}", message.substring(0, Math.min(100, message.length())));
    }

    public void clearErrorCounter(String type) {
        errorCounters.remove(type);
    }

    public int getErrorCount(String type) {
        AtomicInteger counter = errorCounters.get(type);
        return counter != null ? counter.get() : 0;
    }
}
