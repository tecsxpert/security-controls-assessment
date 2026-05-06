package com.internship.tool.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.entity.AuditLog;
import com.internship.tool.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    // Pointcut for service methods that perform CRUD operations
    @Around("execution(* com.internship.tool.service.*.save*(..)) || " +
            "execution(* com.internship.tool.service.*.create*(..)) || " +
            "execution(* com.internship.tool.service.*.update*(..)) || " +
            "execution(* com.internship.tool.service.*.delete*(..)) || " +
            "execution(* com.internship.tool.service.*.remove*(..))")
    public Object auditCrudOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Determine operation type
        String operation = determineOperation(methodName);

        // Get current user
        String username = getCurrentUsername();

        Object result = null;
        String oldData = null;
        String newData = null;

        try {
            // For updates, try to capture old data if first parameter is an entity with ID
            if ("UPDATE".equals(operation) && joinPoint.getArgs().length > 0) {
                Object firstArg = joinPoint.getArgs()[0];
                oldData = serializeObject(firstArg);
            }

            // Execute the method
            result = joinPoint.proceed();

            // Serialize result for new data
            if (result != null) {
                newData = serializeObject(result);
            }

            // Create audit log
            createAuditLog(className, operation, username, oldData, newData, methodName);

        } catch (Exception e) {
            // Log the error but don't let it break the business logic
            log.error("Error in audited method {}: {}", methodName, e.getMessage());

            // Still create audit log for failed operations
            createAuditLog(className, operation + "_FAILED", username, oldData, "ERROR: " + e.getMessage(), methodName);

            throw e; // Re-throw the exception
        }

        return result;
    }

    private String determineOperation(String methodName) {
        String lowerMethod = methodName.toLowerCase();
        if (lowerMethod.startsWith("save") || lowerMethod.startsWith("create")) {
            return "CREATE";
        } else if (lowerMethod.startsWith("update")) {
            return "UPDATE";
        } else if (lowerMethod.startsWith("delete") || lowerMethod.startsWith("remove")) {
            return "DELETE";
        }
        return "UNKNOWN";
    }

    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "SYSTEM";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String serializeObject(Object obj) {
        try {
            if (obj == null) return null;
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to serialize object for audit: {}", e.getMessage());
            return "SERIALIZATION_ERROR";
        }
    }

    private void createAuditLog(String entityType, String action, String username,
                               String oldData, String newData, String methodName) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEntityType(entityType.replace("Service", "")); // Remove "Service" suffix
            auditLog.setAction(action);
            auditLog.setUsername(username);
            auditLog.setOldData(oldData);
            auditLog.setNewData(newData);
            auditLog.setMethodName(methodName);

            auditRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }
}