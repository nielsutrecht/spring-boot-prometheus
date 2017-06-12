package com.nibado.example.springprometheus.aspects;

import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class RequestLogAspect {
    private Map<String, Counter> errorCounters = new HashMap<>();

    static final Summary summary = Summary.build()
            .name("rest_requests_seconds")
            .labelNames("type", "method")
            .help("Requests")
            .register();

    static final Counter errors = Counter.build().name("rest_requests_errors")
            .labelNames("type", "method")
            .help("Request errors")
            .register();

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(public * *(..))")
    public Object log(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Class<?> type = proceedingJoinPoint.getSignature().getDeclaringType();
        String method = proceedingJoinPoint.getSignature().getName();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();

        Summary.Timer requestTimer = summary(type, method).startTimer();

        Object value;

        try {
            value = proceedingJoinPoint.proceed();
        } catch (Throwable t) {
            errorCounter(type, method).inc();

            throw t;
        } finally {
            long ms = (long) (requestTimer.observeDuration() * 1000.0);
            log.info(
                    "{} {} in {} ms: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    ms,
                    request.getHeader("session-id"));
        }

        return value;
    }

    private Summary.Child summary(final Class<?> type, final String method) {
        String typeString = snakeCase(type.getSimpleName());
        String methodString = snakeCase(method);

        return summary.labels(typeString, methodString);
    }

    private Counter.Child errorCounter(final Class<?> type, final String method) {
        String typeString = snakeCase(type.getSimpleName());
        String methodString = snakeCase(method);

        return errors.labels(typeString, methodString);
    }

    private static String snakeCase(final String identifier) {
        StringBuilder builder = new StringBuilder(identifier.length() + 10);
        for (int i = 0; i < identifier.length(); i++) {
            if (Character.isUpperCase(identifier.charAt(i))) {
                if (i > 0) {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(identifier.charAt(i)));
            } else {
                builder.append(identifier.charAt(i));
            }
        }

        return builder.toString();
    }
}
