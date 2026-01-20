package com.payflow.global.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 최근 조회한 구독 ID 저장 (최대 10개)
     */
    public void addRecentSubscription(Long userId, Long subscriptionId) {
        String key = "recent_subscriptions:" + userId;
        
        // List 앞에 추가 (최신순)
        redisTemplate.opsForList().leftPush(key, subscriptionId);
        
        // 최대 10개만 유지
        redisTemplate.opsForList().trim(key, 0, 9);
        
        // 7일 후 만료
        redisTemplate.expire(key, Duration.ofDays(7));
        
        log.debug("📌 최근 조회 구독 저장: userId={}, subscriptionId={}", userId, subscriptionId);
    }

    /**
     * 최근 조회한 구독 ID 목록 조회
     */
    public List<Object> getRecentSubscriptions(Long userId) {
        String key = "recent_subscriptions:" + userId;
        List<Object> recent = redisTemplate.opsForList().range(key, 0, 9);
        
        log.debug("📋 최근 조회 구독 조회: userId={}, count={}", userId, recent != null ? recent.size() : 0);
        return recent;
    }

    /**
     * 값 저장 (TTL 포함)
     */
    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 값 조회
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 키 삭제
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 키 존재 여부 확인
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 패턴으로 키 검색
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }
}
