package com.payflow.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DuplicateSubscription {
    private String category;
    private String[] subscriptions;
    private String suggestion;
}
