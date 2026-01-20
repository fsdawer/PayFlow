package com.payflow.domain.subscription.controller;

import com.payflow.domain.subscription.dto.SubscriptionCreateRequest;
import com.payflow.domain.subscription.dto.SubscriptionResponse;
import com.payflow.domain.subscription.dto.SubscriptionUpdateRequest;
import com.payflow.domain.subscription.service.SubscriptionService;
import com.payflow.global.redis.RedisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final RedisService redisService;

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions(
            @AuthenticationPrincipal Long userId) {
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptions(userId);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * 최근 조회한 구독 목록 (Redis)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<SubscriptionResponse>> getRecentSubscriptions(
            @AuthenticationPrincipal Long userId) {
        
        List<Object> recentIds = redisService.getRecentSubscriptions(userId);
        
        if (recentIds == null || recentIds.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        // Redis에서 가져온 ID로 구독 조회
        List<SubscriptionResponse> recent = recentIds.stream()
                .map(id -> {
                    try {
                        Long subscriptionId = Long.valueOf(id.toString());
                        return subscriptionService.findById(subscriptionId, userId);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(sub -> sub != null)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recent);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        SubscriptionResponse subscription = subscriptionService.findById(id, userId);
        return ResponseEntity.ok(subscription);
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody SubscriptionCreateRequest request,
            @AuthenticationPrincipal Long userId) {
        SubscriptionResponse created = subscriptionService.createSubscription(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionUpdateRequest request,
            @AuthenticationPrincipal Long userId) {
        SubscriptionResponse updated = subscriptionService.updateSubscription(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        subscriptionService.deleteSubscription(userId, id);
        return ResponseEntity.noContent().build();
    }
}
