package com.example.devflow.aspect;

import com.example.devflow.dto.response.TaskResponse;
import com.example.devflow.service.ActivityLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * AOP aspect for automatic activity logging.
 * <p>
 * Uses @AfterReturning instead of @Around because:
 * 1. We only need to act after successful execution — no need to control the method flow.
 * 2. @Around would require manually calling proceed() and handling exceptions,
 *    which adds complexity without benefit since we don't modify the return value.
 * 3. @AfterReturning is simpler and safer: it only fires when the method succeeds,
 *    so we don't log failed operations as activities.
 * <p>
 * This aspect is the ONLY place where ActivityLogService is called in the context
 * of task operations — TaskServiceImpl does NOT call it manually.
 */
@Aspect
@Component
public class LoggingAspect {

    private final ActivityLogService activityLogService;

    public LoggingAspect(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    /**
     * Pointcut targeting TaskService.updateTaskStatus().
     */
    @Pointcut("execution(* com.example.devflow.service.TaskService.updateTaskStatus(..))")
    public void taskStatusUpdatePointcut() {
    }

    /**
     * Pointcut targeting TaskService.createTask().
     */
    @Pointcut("execution(* com.example.devflow.service.TaskService.createTask(..))")
    public void taskCreatePointcut() {
    }

    /**
     * After returning advice that logs task status changes.
     */
    @AfterReturning(pointcut = "taskStatusUpdatePointcut()", returning = "result")
    public void logTaskStatusChange(JoinPoint joinPoint, Object result) {
        if (result instanceof TaskResponse taskResponse) {
            String content = String.format(
                    "Task '%s' status updated to %s",
                    taskResponse.getTitle(),
                    taskResponse.getStatus()
            );
            activityLogService.createLog(content, taskResponse.getProjectId());
        }
    }

    /**
     * After returning advice that logs task creation.
     */
    @AfterReturning(pointcut = "taskCreatePointcut()", returning = "result")
    public void logTaskCreation(JoinPoint joinPoint, Object result) {
        if (result instanceof TaskResponse taskResponse) {
            String content = String.format(
                    "Task '%s' created with status %s",
                    taskResponse.getTitle(),
                    taskResponse.getStatus()
            );
            activityLogService.createLog(content, taskResponse.getProjectId());
        }
    }
}
