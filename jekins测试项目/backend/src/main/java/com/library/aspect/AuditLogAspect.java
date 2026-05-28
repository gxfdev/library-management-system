package com.library.aspect;

import com.library.annotation.AuditLog;
import com.library.entity.OperationLog;
import com.library.service.OperationLogService;
import com.library.util.DataMaskUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final OperationLogService operationLogService;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        OperationLog operationLog = new OperationLog();
        operationLog.setModule(auditLog.module());
        operationLog.setAction(auditLog.action());
        operationLog.setDescription(auditLog.description());
        operationLog.setCreateTime(LocalDateTime.now());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setRequestMethod(request.getMethod());
            operationLog.setIp(getClientIp(request));
            operationLog.setUserAgent(truncate(request.getHeader("User-Agent"), 500));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long userId) {
                operationLog.setUserId(userId);
            }
            String name = authentication.getName();
            if (name != null && !"anonymousUser".equals(name)) {
                operationLog.setUsername(name);
            }
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        if (paramNames != null && args != null) {
            StringBuilder params = new StringBuilder();
            for (int i = 0; i < paramNames.length; i++) {
                if (i > 0) params.append(", ");
                String paramValue = args[i] != null ? args[i].toString() : "null";
                params.append(paramNames[i]).append("=").append(DataMaskUtil.maskName(paramValue));
            }
            operationLog.setRequestParams(truncate(params.toString(), 2000));
        }

        try {
            Object result = joinPoint.proceed();
            operationLog.setStatus(1);
            return result;
        } catch (Throwable e) {
            operationLog.setStatus(0);
            operationLog.setErrorMsg(truncate(e.getMessage(), 1000));
            throw e;
        } finally {
            try {
                operationLogService.save(operationLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty() && !"unknown".equalsIgnoreCase(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
