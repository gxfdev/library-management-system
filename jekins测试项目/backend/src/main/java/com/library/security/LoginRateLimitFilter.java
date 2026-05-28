package com.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    @Value("${login.rate-limit.max-attempts:5}")
    private int maxAttempts;

    @Value("${login.rate-limit.window-seconds:300}")
    private int windowSeconds;

    @Value("${register.rate-limit.max-attempts:3}")
    private int registerMaxAttempts;

    private final ConcurrentHashMap<String, AttemptRecord> loginAttemptRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptRecord> registerAttemptRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptRecord> forgotPasswordAttemptRecords = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredRecords() {
        long now = System.currentTimeMillis();
        cleanupRecords(loginAttemptRecords, now);
        cleanupRecords(registerAttemptRecords, now);
        cleanupRecords(forgotPasswordAttemptRecords, now);
    }

    private void cleanupRecords(ConcurrentHashMap<String, AttemptRecord> records, long now) {
        Iterator<Map.Entry<String, AttemptRecord>> it = records.entrySet().iterator();
        int removed = 0;
        while (it.hasNext()) {
            Map.Entry<String, AttemptRecord> entry = it.next();
            if (now - entry.getValue().windowStart > windowSeconds * 1000L * 2) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("清理过期限流记录: {}条", removed);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isAuthEndpoint(request, "/auth/login")) {
            checkRateLimit(request, response, filterChain, loginAttemptRecords, maxAttempts, "登录");
        } else if (isAuthEndpoint(request, "/auth/register")) {
            checkRateLimit(request, response, filterChain, registerAttemptRecords, registerMaxAttempts, "注册");
        } else if (isAuthEndpoint(request, "/auth/forgot-password/send-code")) {
            checkRateLimit(request, response, filterChain, forgotPasswordAttemptRecords, 3, "忘记密码");
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private void checkRateLimit(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain,
                                ConcurrentHashMap<String, AttemptRecord> records,
                                int maxAttempts,
                                String action) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        long now = System.currentTimeMillis();

        if (records.size() > 10000) {
            records.clear();
            log.warn("限流记录过多，执行清理");
        }

        AttemptRecord record = records.compute(clientIp, (k, v) -> {
            if (v == null || now - v.windowStart > windowSeconds * 1000L) {
                return new AttemptRecord(now, 1);
            }
            return new AttemptRecord(v.windowStart, v.count + 1);
        });

        if (record.count > maxAttempts) {
            log.warn("{}限流: IP {} 在 {} 秒内尝试 {} 次", action, clientIp, windowSeconds, record.count);
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            Map<String, Object> result = Map.of(
                "code", 429,
                "message", action + "尝试过于频繁，请" + windowSeconds + "秒后重试"
            );
            new ObjectMapper().writeValue(response.getWriter(), result);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthEndpoint(HttpServletRequest request, String endpoint) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI() != null
                && request.getRequestURI().endsWith(endpoint);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty() && !"unknown".equalsIgnoreCase(forwarded)) {
            String ip = forwarded.split(",")[0].trim();
            if (!ip.isEmpty()) return ip;
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
            return realIp.trim();
        }
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr == null || remoteAddr.isEmpty()) {
            return "unknown";
        }
        return remoteAddr;
    }

    private record AttemptRecord(long windowStart, int count) {}
}
