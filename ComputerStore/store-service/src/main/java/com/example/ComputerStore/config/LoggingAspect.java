package com.example.ComputerStore.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

// defineste un aspect pentru logarea automata a metodelor din stratul de servicii
@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // pointcut care selecteaza toate metodele din pachetul service
    @Pointcut("execution(* com.example.ComputerStore.service.*.*(..))")
    public void serviceMethods() {}

    // sfat de tip around care intercepteaza rularea metodelor inregistreaza parametrii si masoara timpul de executie
    @Around("serviceMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // extrage numele metodei clasa si argumentele trimise
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        Object[] args = joinPoint.getArgs();

        // logheaza intrarea in metoda
        log.debug("AOP [Start] Enter: {}.{}() with argument[s] = {}", className, methodName, Arrays.toString(args));

        // salveaza timpul de pornire pentru a calcula durata
        long start = System.currentTimeMillis();
        try {
            // porneste executia metodei reale
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            // logheaza succesul metodei si durata de executie
            log.debug("AOP [Success] Exit: {}.{}() with result = {}. Execution time: {} ms", 
                    className, methodName, result, executionTime);
            return result;
        } catch (IllegalArgumentException e) {
            // intercepteaza argumentele invalide si le logheaza
            log.error("AOP [Error] Illegal argument: {} in {}.{}()", Arrays.toString(args), className, methodName);
            throw e;
        } catch (Throwable e) {
            // intercepteaza erorile generale le logheaza si le trimite mai departe
            log.error("AOP [Error] Exception in {}.{}() after {} ms with cause = {}", 
                    className, methodName, (System.currentTimeMillis() - start), e.getCause() != null ? e.getCause() : "NULL");
            throw e;
        }
    }
}
