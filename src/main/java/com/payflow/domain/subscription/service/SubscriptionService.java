package com.payflow.domain.subscription.service;

import com.payflow.domain.subscription.dto.SubscriptionCreateRequest;
import com.payflow.domain.subscription.dto.SubscriptionResponse;
import com.payflow.domain.subscription.dto.SubscriptionUpdateRequest;
import java.util.List;

public interface SubscriptionService {

  SubscriptionResponse createSubscription(Long userId, SubscriptionCreateRequest request);

  List<SubscriptionResponse> getSubscriptions(Long userId);

  SubscriptionResponse updateSubscription(
      Long userId, Long subscriptionId, SubscriptionUpdateRequest request);

  void deleteSubscription(Long userId, Long subscriptionId);
}
