package com.payflow.domain.ai.repository;

import com.payflow.domain.ai.entity.AIInsight;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIInsightRepository extends JpaRepository<AIInsight, Long> {
  Optional<AIInsight> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
