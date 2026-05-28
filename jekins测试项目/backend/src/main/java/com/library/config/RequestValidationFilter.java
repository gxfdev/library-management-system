package com.library.config;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RequestValidationFilter extends OncePerRequestFilter {

    @Value("${security.request.timestamp-tolerance:300000}")
    private long timestampTolerance;

    @Value("${security.request.nonce-cache-size:10000}")
    private int nonceCacheSize;

    private final ConcurrentHashMap<String, Long> nonceCache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (isPublicEndpoint(uri) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String timestamp = request.getHeader("X-Timestamp");
        String nonce = request.getHeader("X-Nonce");

        if (timestamp != null && nonce != null) {
            if (!validateTimestamp(timestamp)) {
                log.warn("请求时间戳无效: uri={}, timestamp={}", uri, timestamp);
                sendErrorResponse(response, 400, "请求已过期");
                return;
            }

            if (!validateNonce(nonce)) {
                log.warn("请求nonce重复: uri={}, nonce={}", uri, nonce);
                sendErrorResponse(response, 400, "重复请求");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean validateTimestamp(String timestamp) {
        try {
            long requestTime = Long.parseLong(timestamp);
            long now = System.currentTimeMillis();
            return Math.abs(now - requestTime) <= timestampTolerance;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateNonce(String nonce) {
        if (nonceCache.size() > nonceCacheSize) {
            long cutoff = System.currentTimeMillis() - timestampTolerance * 2;
            nonceCache.entrySet().removeIf(entry -> entry.getValue() < cutoff);
        }

        if (nonceCache.containsKey(nonce)) {
            return false;
        }

        nonceCache.put(nonce, System.currentTimeMillis());
        return true;
    }

    private boolean isPublicEndpoint(String uri) {
        if (uri == null) return true;
        return uri.contains("/auth/login") ||
                uri.contains("/auth/register") ||
                uri.contains("/captcha") ||
                uri.contains("/swagger-ui") ||
                uri.contains("/v3/api-docs") ||
                uri.contains("/webjars") ||
                uri.contains("/actuator/health");
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> result = Map.of("code", status, "message", message);
        new ObjectMapper().writeValue(response.getWriter(), result);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && (path.contains("/swagger-ui") || path.contains("/v3/api-docs") || path.contains("/webjars"));
    }
}
