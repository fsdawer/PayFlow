package com.payflow.domain.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_insights")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIInsight {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long insightId;

  @Column(nullable = false)
  private Long userId;

  @Column(name = "insight_type", nullable = false, length = 50)
  private String insightType;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(name = "reason_text", columnDefinition = "TEXT")
  private String reasonText;

  @Column(name = "suggestion_text", columnDefinition = "TEXT")
  private String suggestionText;

  @Column(columnDefinition = "TEXT")
  private String summary;

  @Column(name = "duplicates_json", columnDefinition = "text")
  private String duplicatesJson;

  @Column(name = "recommendations_json", columnDefinition = "text")
  private String recommendationsJson;

  @Column(name = "spending_trend", columnDefinition = "TEXT")
  private String spendingTrend;

  @Column
  private Integer totalMonthlySpending;

  @Column
  private Integer totalSubscriptions;

  @Column(length = 20)
  private String severity;

  @Column
  private Double confidence;

  @Column(name = "input_snapshot", columnDefinition = "TEXT")
  private String inputSnapshot;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    
    // Ensure summary is never null due to DB constraints
    if (this.summary == null) this.summary = "";
    if (this.totalMonthlySpending == null) this.totalMonthlySpending = 0;
    if (this.totalSubscriptions == null) this.totalSubscriptions = 0;
  }

  @PreUpdate
  void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
