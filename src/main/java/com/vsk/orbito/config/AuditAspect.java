package com.vsk.orbito.config;

import com.vsk.orbito.audit.entity.AuditLog;
import com.vsk.orbito.audit.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    // intercepts every method in every @RestController
    @Around("within(@org.springframework.web.bind.annotation" +
            ".RestController *)")
    public Object auditControllerCall(
            ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();
        MethodSignature signature =
                (MethodSignature) joinPoint.getSignature();

        String className  = joinPoint.getTarget()
                .getClass().getSimpleName();
        String methodName = signature.getMethod().getName();

        // get current user from security context
        String userEmail = "anonymous";
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !auth.getName().equals("anonymousUser")) {
            userEmail = auth.getName();
        }

        // get HTTP request details
        String httpMethod  = "UNKNOWN";
        String requestUrl  = "UNKNOWN";
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder
                            .getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                httpMethod = request.getMethod();
                requestUrl = request.getRequestURI();
            }
        } catch (Exception ignored) {}

        // proceed with the actual method
        Object result;
        String status = "SUCCESS";
        String errorMessage = null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            status = "FAILED";
            errorMessage = ex.getMessage();
            long executionTime = System.currentTimeMillis() - startTime;

            // save audit log asynchronously — never block the response
            saveAuditLogAsync(userEmail, className, methodName,
                    httpMethod, requestUrl, executionTime,
                    status, errorMessage);

            throw ex; // rethrow so GlobalExceptionHandler handles it
        }

        long executionTime = System.currentTimeMillis() - startTime;
        saveAuditLogAsync(userEmail, className, methodName,
                httpMethod, requestUrl, executionTime,
                status, null);

        log.info("[AUDIT] {}.{}() | user={} | {}ms | {}",
                className, methodName, userEmail,
                executionTime, status);

        return result;
    }

    @Async
    public void saveAuditLogAsync(
            String userEmail, String className,
            String methodName, String httpMethod,
            String requestUrl, Long executionTimeMs,
            String status, String errorMessage) {
        try {
            AuditLog log = AuditLog.builder()
                    .userEmail(userEmail)
                    .className(className)
                    .methodName(methodName)
                    .httpMethod(httpMethod)
                    .requestUrl(requestUrl)
                    .executionTimeMs(executionTimeMs)
                    .status(status)
                    .errorMessage(errorMessage)
                    .build();
            auditLogRepository.save(log);
        } catch (Exception e) {
            // never let audit logging crash the app
        }
    }
}