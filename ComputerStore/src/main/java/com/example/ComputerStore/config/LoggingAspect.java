package com.example.ComputerStore.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // Pointcut care acoperă toate metodele din pachetul service
    @Pointcut("execution(* com.example.ComputerStore.service.*.*(..))")
    public void serviceMethods() {}

    @Around("serviceMethods()")
    public Object logExecutionTimeAndMethodInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("AOP [Start] Executing {} with args: {}", methodName, Arrays.toString(args));
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long timeTaken = System.currentTimeMillis() - startTime;
            log.info("AOP [Success] Executed {} in {} ms. Result: {}", methodName, timeTaken, result);
            return result;
        } catch (Throwable ex) {
            long timeTaken = System.currentTimeMillis() - startTime;
            log.error("AOP [Error] Exception in {} after {} ms. Error: {}", methodName, timeTaken, ex.getMessage());
            throw ex;
        }
    }
}
