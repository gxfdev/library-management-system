package com.library.exception;

import com.library.common.Result;
import com.library.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AlertService alertService;

    private static final String SANITIZED_ERROR = "操作失败，请检查输入后重试";

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        
        String msg = e.getMessage();
        if (msg != null && (msg.contains("SQL") || msg.contains("sql") || msg.contains("Exception")
                || msg.contains("stack") || msg.contains("javax.") || msg.contains("java.")
                || msg.contains("org.springframework") || msg.contains("com.mysql")
                || msg.contains("jdbc") || msg.contains("hibernate"))) {
            log.error("疑似信息泄露异常: {}", msg);
            alertService.recordError("SECURITY_LEAK", "疑似敏感信息泄露: " + msg, e);
            return Result.error(400, SANITIZED_ERROR);
        }
        
        if (isCriticalError(msg)) {
            alertService.recordError("BUSINESS_ERROR", msg, e);
        }
        
        return Result.error(400, msg != null ? msg : SANITIZED_ERROR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("登录凭证错误 - IP: {}, 用户: {}", getClientIp(), e.getMessage());
        alertService.recordError("AUTH_FAILED", "登录失败", e);
        return Result.error(401, "用户名或密码错误");
    }

    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleDisabledException(DisabledException e) {
        log.warn("账号已禁用: {}", e.getMessage());
        return Result.error(403, "账号已被禁用，请联系管理员");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证异常: {}", e.getMessage());
        alertService.recordError("AUTH_EXCEPTION", "认证异常", e);
        return Result.error(401, "认证失败，请重新登录");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("访问被拒绝 - IP: {}, 路径: {}", getClientIp(), getRequestPath());
        alertService.recordError("ACCESS_DENIED", "权限不足", e);
        return Result.error(403, "权限不足，无法执行该操作");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", message);
        return Result.error(400, message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数绑定失败");
        log.warn("参数绑定失败: {}", message);
        return Result.error(400, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParamException(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getMessage());
        return Result.error(400, "缺少必要参数: " + e.getParameterName());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return Result.error(400, "参数格式错误");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        return Result.error(404, "请求的资源不存在");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        alertService.recordError("SYSTEM_ERROR", "系统内部错误", e);
        return Result.error(500, "系统内部错误，请稍后重试");
    }

    private boolean isCriticalError(String message) {
        if (message == null) return false;
        String lowerMsg = message.toLowerCase();
        return lowerMsg.contains("database") ||
               lowerMsg.contains("connection") ||
               lowerMsg.contains("timeout") ||
               lowerMsg.contains("out of memory") ||
               lowerMsg.contains("nullpointer") ||
               lowerMsg.contains("库存不足") ||
               lowerMsg.contains("借阅数量已达上限");
    }

    private String getClientIp() {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attrs =
                (org.springframework.web.context.request.ServletRequestAttributes)
                    org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes();
            jakarta.servlet.http.HttpServletRequest httpRequest = attrs.getRequest();
            
            String ip = httpRequest.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = httpRequest.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = httpRequest.getRemoteAddr();
            }
            return ip != null ? ip : "unknown";
        } catch (Exception ignored) {}
        return "unknown";
    }

    private String getRequestPath() {
        try {
            Object requestAttr = org.springframework.web.context.request.RequestContextHolder
                .currentRequestAttributes();
            if (requestAttr instanceof org.springframework.web.context.request.ServletRequestAttributes servletAttrs) {
                return servletAttrs.getRequest().getRequestURI();
            }
        } catch (Exception ignored) {}
        return "unknown";
    }
}
