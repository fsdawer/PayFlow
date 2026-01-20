package com.payflow.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    // AI 인사이트: 1분당 5회
    private static final int AI_MAX_REQUESTS = 5;
    private static final int AI_TIME_WINDOW = 60; // 초

    // 일반 API: 1분당 100회
    private static final int API_MAX_REQUESTS = 100;
    private static final int API_TIME_WINDOW = 60; // 초

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIP(request);
        String requestUri = request.getRequestURI();
        
        // AI API는 더 엄격한 제한
        if (requestUri.startsWith("/api/ai/")) {
            return checkRateLimit(clientIp, "ai", AI_MAX_REQUESTS, AI_TIME_WINDOW, response);
        }
        
        // 일반 API
        if (requestUri.startsWith("/api/")) {
            return checkRateLimit(clientIp, "api", API_MAX_REQUESTS, API_TIME_WINDOW, response);
        }
        
        return true;
    }

    private boolean checkRateLimit(String clientIp, String type, int maxRequests, int timeWindow, HttpServletResponse response) throws Exception {
        String key = String.format("rate_limit:%s:%s", type, clientIp);
        
        Long currentCount = redisTemplate.opsForValue().increment(key);
        
        if (currentCount == null) {
            currentCount = 1L;
        }
        
        if (currentCount == 1) {
            // 첫 요청이면 TTL 설정
            redisTemplate.expire(key, Duration.ofSeconds(timeWindow));
        }
        
        // 제한 초과 확인
        if (currentCount > maxRequests) {
            log.warn("⚠️ Rate limit exceeded: type={}, ip={}, count={}", type, clientIp, currentCount);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\":\"Too many requests\",\"message\":\"1분당 최대 %d회 요청 가능합니다. 잠시 후 다시 시도해주세요.\"}",
                maxRequests
            ));
            return false;
        }
        
        // 남은 요청 횟수를 헤더에 추가
        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(maxRequests - currentCount));
        
        log.debug("✅ Rate limit OK: type={}, ip={}, count={}/{}", type, clientIp, currentCount, maxRequests);
        return true;
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
