package com.khu.acc.newsfeed.common.interceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    // 분당 100회 요청 제한
    private final Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientId = getClientId(request);
        Bucket bucket = buckets.computeIfAbsent(clientId, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            log.warn("Rate limit exceeded for client: {}", clientId);
            response.setStatus(429); // Too Many Requests
            response.setHeader("X-Rate-Limit-Retry-After-Seconds", "60");
            return false;
        }
    }

    private String getClientId(HttpServletRequest request) {
        // 실제 구현에서는 사용자 ID나 IP 주소를 사용
        String userId = request.getHeader("X-User-ID");
        if (userId != null) {
            return "user:" + userId;
        }
        return "ip:" + request.getRemoteAddr();
    }

    private Bucket createNewBucket(String clientId) {
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}