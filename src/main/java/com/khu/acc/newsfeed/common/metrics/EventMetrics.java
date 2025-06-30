package com.khu.acc.newsfeed.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 이벤트 발행 관련 메트릭 수집
 */
@Component
@RequiredArgsConstructor
public class EventMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void incrementEventPublished(String eventType) {
        Counter.builder("event.published")
                .tag("type", eventType)
                .tag("status", "success")
                .register(meterRegistry)
                .increment();
    }
    
    public void incrementEventFailed(String eventType, String errorType) {
        Counter.builder("event.published")
                .tag("type", eventType)
                .tag("status", "failed")
                .tag("error", errorType)
                .register(meterRegistry)
                .increment();
    }
    
    public Timer.Sample startEventTimer(String eventType) {
        return Timer.start(meterRegistry);
    }
    
    public void stopEventTimer(Timer.Sample sample, String eventType) {
        sample.stop(Timer.builder("event.processing.duration")
                .tag("type", eventType)
                .register(meterRegistry));
    }
    
    public void incrementCircuitBreakerOpened(String circuitBreakerName) {
        Counter.builder("circuit.breaker.opened")
                .tag("name", circuitBreakerName)
                .register(meterRegistry)
                .increment();
    }
    
    public void incrementRetryAttempt(String operationName) {
        Counter.builder("retry.attempts")
                .tag("operation", operationName)
                .register(meterRegistry)
                .increment();
    }
}