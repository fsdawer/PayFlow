package com.payflow.domain.ai.repository;

import com.payflow.domain.ai.entity.AIReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AIReportRepository extends JpaRepository<AIReport, Long> {
    Optional<AIReport> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
}
