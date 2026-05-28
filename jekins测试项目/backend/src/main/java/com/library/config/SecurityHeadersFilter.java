package com.library.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "X-Forwarded-For",
            "X-Real-IP",
            "X-Original-URL",
            "X-Rewrite-URL"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        String csp = buildCspPolicy(request);
        response.setHeader("Content-Security-Policy", csp);

        response.setHeader("Permissions-Policy",
                "camera=(), microphone=(), geolocation=(), payment=(), usb=(), magnetometer=(), gyroscope=()");

        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
        response.setHeader("Cross-Origin-Embedder-Policy", "require-corp");

        if (sslEnabled) {
            response.setHeader("Strict-Transport-Security",
                    "max-age=31536000; includeSubDomains; preload");
        }

        filterChain.doFilter(request, response);
    }

    private String buildCspPolicy(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean isApi = uri != null && uri.startsWith("/api/");
        boolean isSwagger = uri != null && (uri.contains("/swagger-ui") || uri.contains("/v3/api-docs"));

        if (isSwagger) {
            return "default-src 'self'; " +
                   "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                   "style-src 'self' 'unsafe-inline'; " +
                   "img-src 'self' data:; " +
                   "font-src 'self'; " +
                   "connect-src 'self'; " +
                   "frame-ancestors 'none'; " +
                   "base-uri 'self'; " +
                   "form-action 'self'";
        }

        if (isApi) {
            return "default-src 'none'; " +
                   "connect-src 'self'; " +
                   "frame-ancestors 'none'";
        }

        return "default-src 'self'; " +
               "script-src 'self' 'nonce-{random}'; " +
               "style-src 'self' 'unsafe-inline'; " +
               "img-src 'self' data: blob: https:; " +
               "font-src 'self' data: https:; " +
               "connect-src 'self'; " +
               "media-src 'self'; " +
               "object-src 'none'; " +
               "frame-ancestors 'none'; " +
               "base-uri 'self'; " +
               "form-action 'self'; " +
               "upgrade-insecure-requests";
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && (
                path.endsWith(".js") ||
                path.endsWith(".css") ||
                path.endsWith(".ico") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".svg") ||
                path.endsWith(".woff") ||
                path.endsWith(".woff2")
        );
    }
}
