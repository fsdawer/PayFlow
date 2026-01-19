package com.payflow.domain.subscription.controller;

import com.payflow.domain.subscription.dto.SubscriptionCreateRequest;
import com.payflow.domain.subscription.dto.SubscriptionResponse;
import com.payflow.domain.subscription.dto.SubscriptionUpdateRequest;
import com.payflow.domain.subscription.service.SubscriptionService;
import com.payflow.domain.user.entity.User;
import com.payflow.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 구독 관리 API Controller
 * @AuthenticationPrincipal을 통해 인증된 사용자 정보를 주입받아 처리
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

  private final SubscriptionService subscriptionService;
  private final UserRepository userRepository;


  /**
   * 구독 정보 등록
   * @param email JWT 토큰에서 추출한 사용자 이메일
   * @param request 구독 생성 요청 정보
   * @return 생성된 구독 정보
   */
  @PostMapping
  public ResponseEntity<SubscriptionResponse> createSubscription(
      @AuthenticationPrincipal String email,
      @Valid @RequestBody SubscriptionCreateRequest request) {
    
    Long userId = getUserIdByEmail(email);
    SubscriptionResponse response = subscriptionService.createSubscription(userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }


  /**
   * 내 구독 정보 전체 조회
   * @param email JWT 토큰에서 추출한 사용자 이메일
   * @return 구독 목록
   */
  @GetMapping
  public ResponseEntity<List<SubscriptionResponse>> getSubscriptions(
      @AuthenticationPrincipal String email) {
    
    Long userId = getUserIdByEmail(email);
    List<SubscriptionResponse> responses = subscriptionService.getSubscriptions(userId);
    return ResponseEntity.ok(responses);
  }


  /**
   * 구독 정보 수정
   * @param email JWT 토큰에서 추출한 사용자 이메일
   * @param subscriptionId 수정할 구독 ID
   * @param request 수정 요청 정보
   * @return 수정된 구독 정보
   */
  @PatchMapping("/{subscriptionId}")
  public ResponseEntity<SubscriptionResponse> updateSubscription(
      @AuthenticationPrincipal String email,
      @PathVariable Long subscriptionId,
      @Valid @RequestBody SubscriptionUpdateRequest request) {
    
    Long userId = getUserIdByEmail(email);
    SubscriptionResponse response = subscriptionService.updateSubscription(userId, subscriptionId, request);
    return ResponseEntity.ok(response);
  }


  /**
   * 구독 정보 삭제
   * @param email JWT 토큰에서 추출한 사용자 이메일
   * @param subscriptionId 삭제할 구독 ID
   * @return 204 No Content
   */
  @DeleteMapping("/{subscriptionId}")
  public ResponseEntity<Void> deleteSubscription(
      @AuthenticationPrincipal String email,
      @PathVariable Long subscriptionId) {
    
    Long userId = getUserIdByEmail(email);
    subscriptionService.deleteSubscription(userId, subscriptionId);
    return ResponseEntity.noContent().build();
  }


  /**
   * 이메일로 사용자 ID 조회 (헬퍼 메소드)
   * @param email 사용자 이메일
   * @return 사용자 ID
   */
  private Long getUserIdByEmail(String email) {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("인증 정보가 유효하지 않습니다.");
    }
    
    User user = userRepository.findByEmail(email.trim())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. email: " + email));
    
    return user.getUserId();
  }
}
