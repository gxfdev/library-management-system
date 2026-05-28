package com.library.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class BusinessHealthIndicator implements HealthIndicator {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, AtomicLong> errorCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> apiTimers = new ConcurrentHashMap<>();

    @Override
    public Health health() {
        Health.Builder healthBuilder = Health.up();

        try {
            long totalErrors = errorCounters.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum();

            if (totalErrors > 100) {
                healthBuilder = Health.down()
                        .withDetail("error_count", totalErrors)
                        .withDetail("reason", "错误数量超过阈值");
            } else if (totalErrors > 50) {
                healthBuilder = Health.status("WARNING")
                        .withDetail("error_count", totalErrors)
                        .withDetail("reason", "错误数量接近阈值");
            }

            healthBuilder
                    .withDetail("active_apis", apiTimers.size())
                    .withDetail("total_errors", totalErrors)
                    .withDetail("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            healthBuilder = Health.down()
                    .withDetail("error", e.getMessage());
        }

        return healthBuilder.build();
    }

    public void recordError(String apiName) {
        errorCounters.computeIfAbsent(apiName, k -> new AtomicLong(0))
                .incrementAndGet();
    }

    public Timer.Sample startTimer(String apiName) {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String apiName) {
        sample.stop(apiTimers.computeIfAbsent(apiName,
                k -> Timer.builder("api.response.time")
                        .description("API响应时间")
                        .tag("api", apiName)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry)));
    }

    public void resetCounters() {
        errorCounters.clear();
    }
}
