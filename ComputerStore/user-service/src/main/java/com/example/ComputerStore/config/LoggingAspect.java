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

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // Pointcut care acoperă toate metodele din pachetul service
    @Pointcut("execution(* com.example.ComputerStore.service.*.*(..))")
    public void serviceMethods() {}

    /**
     * Aspect for automatic logging of all service methods.
     * Logs method entry, arguments, execution time, and exit (result or exception).
     */
    @Around("serviceMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        Object[] args = joinPoint.getArgs();

        log.debug("AOP [Start] Enter: {}.{}() with argument[s] = {}", className, methodName, Arrays.toString(args));

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            log.debug("AOP [Success] Exit: {}.{}() with result = {}. Execution time: {} ms", 
                    className, methodName, result, executionTime);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("AOP [Error] Illegal argument: {} in {}.{}()", Arrays.toString(args), className, methodName);
            throw e;
        } catch (Throwable e) {
            log.error("AOP [Error] Exception in {}.{}() after {} ms with cause = {}", 
                    className, methodName, (System.currentTimeMillis() - start), e.getCause() != null ? e.getCause() : "NULL");
            throw e;
        }
    }
}
