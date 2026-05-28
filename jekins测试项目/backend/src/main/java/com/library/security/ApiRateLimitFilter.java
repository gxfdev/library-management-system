package com.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {

    @Value("${security.rate-limit.api-max-requests:100}")
    private int maxRequests;

    @Value("${security.rate-limit.api-window-seconds:60}")
    private int windowSeconds;

    private final ConcurrentHashMap<String, SlidingWindowRecord> requestRecords = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri == null || shouldSkip(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String key = clientIp + ":" + uri;

        long now = System.currentTimeMillis();
        SlidingWindowRecord record = requestRecords.compute(key, (k, v) -> {
            if (v == null || now - v.windowStart > windowSeconds * 1000L) {
                return new SlidingWindowRecord(now, 1);
            }
            return new SlidingWindowRecord(v.windowStart, v.count + 1);
        });

        if (record.count > maxRequests) {
            log.warn("API限流: IP {} 访问 {} 在 {}秒内请求 {} 次", clientIp, uri, windowSeconds, record.count);
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", String.valueOf(windowSeconds));
            Map<String, Object> result = Map.of(
                "code", 429,
                "message", "请求过于频繁，请" + windowSeconds + "秒后重试"
            );
            new ObjectMapper().writeValue(response.getWriter(), result);
            return;
        }

        if (requestRecords.size() > 50000) {
            cleanupExpiredRecords(now);
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkip(String uri) {
        return uri.contains("/actuator/health") ||
               uri.contains("/captcha/") ||
               uri.endsWith(".js") ||
               uri.endsWith(".css") ||
               uri.endsWith(".ico") ||
               uri.endsWith(".png") ||
               uri.endsWith(".jpg");
    }

    private void cleanupExpiredRecords(long now) {
        Iterator<Map.Entry<String, SlidingWindowRecord>> it = requestRecords.entrySet().iterator();
        int removed = 0;
        while (it.hasNext()) {
            Map.Entry<String, SlidingWindowRecord> entry = it.next();
            if (now - entry.getValue().windowStart > windowSeconds * 1000L * 2) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("清理过期API限流记录: {}条", removed);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty() && !"unknown".equalsIgnoreCase(forwarded)) {
            String ip = forwarded.split(",")[0].trim();
            if (!ip.isEmpty() && isValidIp(ip)) return ip;
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp) && isValidIp(realIp)) {
            return realIp.trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    private boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(ipPattern);
    }

    private record SlidingWindowRecord(long windowStart, int count) {}
}
