package com.ticketsystem.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);

    @Around("execution(* com.ticketsystem.service.*.*(..))")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        
        Object result = null;
        boolean success = true;
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
            return result;
            
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            throw e;
            
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (success) {
                if (executionTime > 1000) {
                    logger.warn("Performance: SLOW method {} took {}ms", methodName, executionTime);
                } else {
                    logger.debug("Performance: Method {} took {}ms", methodName, executionTime);
                }
            } else {
                logger.error("Performance: Method {} FAILED after {}ms. Error: {}", 
                    methodName, executionTime, errorMessage);
            }
        }
    }
    
    @Around("execution(* com.ticketsystem.controller.*.*(..))")
    public Object monitorControllerPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String endpoint = joinPoint.getSignature().toShortString();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            logger.info("API Performance: {} completed in {}ms", endpoint, executionTime);
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("API Performance: {} FAILED after {}ms", endpoint, executionTime);
            throw e;
        }
    }
}